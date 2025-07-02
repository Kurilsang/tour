package com.tour.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tour.common.constant.Constants;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.dao.LocationMapper;
import com.tour.dao.UserMapper;
import com.tour.dao.WishCommentMapper;
import com.tour.dao.WishMapper;
import com.tour.dao.WishVoteMapper;
import com.tour.dto.WishCreateDTO;
import com.tour.dto.WishUpdateDTO;
import com.tour.enums.MediaCheckEnum;
import com.tour.enums.PageSize;
import com.tour.model.Location;
import com.tour.model.User;
import com.tour.model.Wish;
import com.tour.model.WishComment;
import com.tour.model.WishVote;
import com.tour.query.WishQuery;
import com.tour.service.IWishService;
import com.tour.service.IMediaSubmitService;
import com.tour.service.IMediaCheckExecutorService;
import com.tour.service.ITextCheckService;
import com.tour.vo.WishCommentVO;
import com.tour.vo.WishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 心愿路线服务实现类
 *
 * @Author Abin
 */
@Slf4j
@Service
public class WishServiceImpl extends ServiceImpl<WishMapper, Wish> implements IWishService {

    @Autowired
    private LocationMapper locationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private WishVoteMapper wishVoteMapper;
    
    @Autowired
    private WishCommentMapper wishCommentMapper;

    @Autowired
    private IMediaSubmitService mediaSubmitService;

    @Autowired
    private IMediaCheckExecutorService mediaCheckExecutorService;
    
    @Autowired
    private ITextCheckService textCheckService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WishVO createWish(WishCreateDTO wishCreateDTO, String userOpenid) {
        // 参数校验
        if (wishCreateDTO == null || userOpenid == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }
        
        // 检测心愿标题和描述内容
        boolean isContentValid = textCheckService.checkWishContentOptimized(
                wishCreateDTO.getTitle(), 
                wishCreateDTO.getDescription(), 
                userOpenid
        );
        
        if (!isContentValid) {
            log.warn("创建心愿内容不合规，title: {}, description: {}, userOpenid: {}", 
                    wishCreateDTO.getTitle(), wishCreateDTO.getDescription(), userOpenid);
            throw new ServiceException(ErrorCode.CONTENT_RISKY, "心愿内容包含敏感信息，请修改后重试");
        }
        
        // 校验location是否有效
        Location locationFromRequest = wishCreateDTO.getLocation();
        if (locationFromRequest == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "目的地不能为空");
        }
        
        // 验证位置信息的必要字段
        if (locationFromRequest.getName() == null || locationFromRequest.getAddress() == null || 
            locationFromRequest.getLatitude() == null || locationFromRequest.getLongitude() == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "目的地信息不完整");
        }
        
        // 保存位置信息
        Location location = new Location();
        location.setName(locationFromRequest.getName());
        location.setAddress(locationFromRequest.getAddress());
        location.setLatitude(locationFromRequest.getLatitude());
        location.setLongitude(locationFromRequest.getLongitude());
        
        try {
            // 使用 MyBatis Plus 提供的方法保存位置信息
            locationMapper.insert(location);
            log.info("成功保存位置信息，ID：{}", location.getId());
        } catch (Exception e) {
            log.error("保存位置信息失败", e);
            throw new ServiceException(ErrorCode.SYSTEM_ERROR, "保存位置信息失败");
        }
        
        // 构建Wish对象
        Wish wish = new Wish();
        wish.setUserOpenid(userOpenid);
        wish.setTitle(wishCreateDTO.getTitle());
        wish.setDescription(wishCreateDTO.getDescription());
        wish.setLocationId(location.getId()); // 使用刚保存的位置ID
        
        // 保存图片列表，用于后续的异步检测
        List<String> imageUrls = wishCreateDTO.getImageUrls();
        
        // 将图片列表转为JSON字符串
        if (!CollectionUtils.isEmpty(imageUrls)) {
            wish.setImageUrls(JSON.toJSONString(imageUrls));
        }
        
        // 设置默认值
        wish.setStatus(0); // 0-待成团
        wish.setVoteCount(0L); // 初始投票数为0
        wish.setCommentCount(0L); // 初始评论数为0
        
        // 使用 MyBatis Plus 的 save 方法保存心愿路线
        save(wish);
        
        // 在数据库保存成功后，再进行异步图像检测
        if (!CollectionUtils.isEmpty(imageUrls)) {
            log.info("心愿创建成功，开始异步检测图片，wishId: {}, userOpenid: {}, imageCount: {}", 
                    wish.getId(), userOpenid, imageUrls.size());
            mediaCheckExecutorService.asyncCheckWishImages(imageUrls, wish.getId(), userOpenid);
        }
        
        // 转换为VO并返回（创建接口返回详细信息，包括位置但不包括用户信息）
        WishVO wishVO = convertToVOWithLocation(wish, location);
        
        return wishVO;
    }
    
    @Override
    public WishVO getWishById(Long id, String currentUserOpenid) {
        if (id == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "ID不能为空");
        }
        
        // 使用 MyBatis Plus 的 getById 方法获取心愿路线
        Wish wish = getById(id);
        if (wish == null) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "心愿路线不存在");
        }
        
        // 查询位置详情
        Location location = locationMapper.selectById(wish.getLocationId());
        if (location == null) {
            log.warn("心愿路线{}关联的位置信息{}不存在", id, wish.getLocationId());
        }
        
        // 查询发起人信息（必须查询发起人的头像和昵称）
        User user = getUserByOpenid(wish.getUserOpenid());
        if (user == null) {
            log.warn("心愿路线{}的发起人{}不存在", id, wish.getUserOpenid());
        }
        
        // 查询最新的3条评论
        LambdaQueryWrapper<WishComment> commentWrapper = Wrappers.lambdaQuery();
        commentWrapper.eq(WishComment::getWishId, id)
                     .orderByDesc(WishComment::getCreateTime)
                     .last("LIMIT 3");
        List<WishComment> latestComments = wishCommentMapper.selectList(commentWrapper);
        
        // 转换为详情VO（包含位置、发起人信息和评论信息，但不包含发起人openid）
        WishVO wishVO = convertToVOWithDetail(wish, location, user, latestComments);
        
        // 判断当前用户是否是心愿所有者
        if (currentUserOpenid != null && currentUserOpenid.equals(wish.getUserOpenid())) {
            wishVO.setIsOwner(true);
        } else {
            wishVO.setIsOwner(false);
        }
        
        // 判断当前用户是否已投票
        if (currentUserOpenid != null) {
            LambdaQueryWrapper<WishVote> voteWrapper = Wrappers.lambdaQuery();
            voteWrapper.eq(WishVote::getWishVoteId, id)
                      .eq(WishVote::getUserOpenid, currentUserOpenid);
            WishVote vote = wishVoteMapper.selectOne(voteWrapper);
            wishVO.setHasVoted(vote != null);
        } else {
            wishVO.setHasVoted(false);
        }
        
        // 查询最近的5个投票用户
        LambdaQueryWrapper<WishVote> voteUsersWrapper = Wrappers.lambdaQuery();
        voteUsersWrapper.eq(WishVote::getWishVoteId, id)
                       .orderByDesc(WishVote::getVoteTime)
                       .last("LIMIT 5");
        List<WishVote> recentVotes = wishVoteMapper.selectList(voteUsersWrapper);
        
        if (!recentVotes.isEmpty()) {
            // 收集所有投票用户的openid
            List<String> voteUserOpenids = recentVotes.stream()
                    .map(WishVote::getUserOpenid)
                    .collect(Collectors.toList());
            
            // 批量查询用户信息
            LambdaQueryWrapper<User> userWrapper = Wrappers.lambdaQuery();
            userWrapper.in(User::getOpenid, voteUserOpenids);
            List<User> voteUsers = userMapper.selectList(userWrapper);
            
            // 创建用户映射
            Map<String, User> userMap = voteUsers.stream()
                    .collect(Collectors.toMap(User::getOpenid, user1 -> user1, (u1, u2) -> u1));
            
            // 转换为投票用户列表
            List<WishVO.UserVoteInfo> voteUserInfoList = new ArrayList<>();
            for (WishVote voteItem : recentVotes) {
                User voteUser = userMap.get(voteItem.getUserOpenid());
                if (voteUser != null) {
                    WishVO.UserVoteInfo userVoteInfo = new WishVO.UserVoteInfo();
                    userVoteInfo.setNickname(voteUser.getNickname());
                    userVoteInfo.setAvatar(voteUser.getAvatar());
                    voteUserInfoList.add(userVoteInfo);
                }
            }
            
            wishVO.setVoteUsers(voteUserInfoList);
        } else {
            wishVO.setVoteUsers(new ArrayList<>());
        }
        
        // 如果有评论，为每条评论设置isOwner属性
        if (wishVO.getComments() != null && !wishVO.getComments().isEmpty()) {
            for (WishCommentVO comment : wishVO.getComments()) {
                // 从WishComment表中查询该评论的userOpenid
                WishComment originalComment = wishCommentMapper.selectById(comment.getId());
                if (originalComment != null && currentUserOpenid != null) {
                    comment.setIsOwner(currentUserOpenid.equals(originalComment.getUserOpenid()));
                } else {
                    comment.setIsOwner(false);
                }
            }
        }
        
        return wishVO;
    }
    
    @Override
    public WishVO getWishById(Long id) {
        // 兼容旧接口，不传入当前用户信息
        return getWishById(id, null);
    }
    
    @Override
    public IPage<WishVO> queryWishList(WishQuery query) {
        if (query == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "查询参数不能为空");
        }
        
        // 设置默认分页参数
        if (query.getPageNo() == null) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null) {
            query.setPageSize(PageSize.SIZE10.getSize());
        }
        
        // 普通视图默认不显示已关闭的心愿（状态值为2）
        // 如果没有指定状态，则只显示状态为0或1的心愿
        if (query.getStatus() == null) {
            // 使用自定义条件，只查询未关闭的心愿
            LambdaQueryWrapper<Wish> wrapper = Wrappers.lambdaQuery();
            wrapper.ne(Wish::getStatus, 2); // 状态不等于2（已关闭）
            
            if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
                wrapper.and(w -> w.like(Wish::getTitle, query.getKeyword())
                              .or()
                              .like(Wish::getDescription, query.getKeyword()));
            }
            
            if (query.getStartTime() != null) {
                wrapper.ge(Wish::getCreateTime, query.getStartTime());
            }
            
            if (query.getEndTime() != null) {
                wrapper.le(Wish::getCreateTime, query.getEndTime());
            }
            
            // 设置排序
            if (query.getSortType() != null && query.getSortType() == 1) {
                // 按热度排序
                wrapper.orderByDesc(Wish::getVoteCount, Wish::getCreateTime);
            } else {
                // 默认按时间排序
                wrapper.orderByDesc(Wish::getCreateTime);
            }
            
            // 创建分页对象
            Page<Wish> page = new Page<>(query.getPageNo(), query.getPageSize());
            
            // 执行查询
            IPage<Wish> wishPage = page(page, wrapper);
            
            // 转换为包含用户信息的VO列表
            return convertToSimpleVOPage(wishPage);
        }
        
        // 如果指定了具体状态，则使用Mapper的XML查询
        // 创建分页对象
        Page<Wish> page = new Page<>(query.getPageNo(), query.getPageSize());
        
        // 调用Mapper执行查询
        IPage<Wish> wishPage = baseMapper.queryWishList(page, query);
        
        // 转换为包含用户信息的VO列表
        return convertToSimpleVOPage(wishPage);
    }
    
    @Override
    public IPage<WishVO> queryUserWishList(Integer pageNo, Integer pageSize, String userOpenid) {
        if (userOpenid == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "用户openid不能为空");
        }
        
        // 设置默认分页参数
        if (pageNo == null) {
            pageNo = 1;
        }
        if (pageSize == null) {
            pageSize = PageSize.SIZE10.getSize();
        }
        
        // 创建分页对象
        Page<Wish> page = new Page<>(pageNo, pageSize);
        
        // 使用 MyBatis Plus 的 LambdaQueryWrapper 构建查询条件
        LambdaQueryWrapper<Wish> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Wish::getUserOpenid, userOpenid)
               .ne(Wish::getStatus, 2) // 不显示已关闭的心愿
               .orderByDesc(Wish::getCreateTime);
        
        // 使用 MyBatis Plus 的 page 方法进行分页查询
        IPage<Wish> wishPage = page(page, wrapper);
        
        // 转换为包含用户信息的VO列表
        return convertToSimpleVOPage(wishPage);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean voteWish(Long wishId, String userOpenid) {
        // 参数校验
        if (wishId == null || userOpenid == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }
        
        // 查询心愿路线
        Wish wish = getById(wishId);
        if (wish == null) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "心愿路线不存在");
        }
        
        // 检查用户是否已投票
        LambdaQueryWrapper<WishVote> voteWrapper = Wrappers.lambdaQuery();
        voteWrapper.eq(WishVote::getWishVoteId, wishId)
                  .eq(WishVote::getUserOpenid, userOpenid);
        
        WishVote existVote = wishVoteMapper.selectOne(voteWrapper);
        if (existVote != null) {
            return true; // 已投票，直接返回成功
        }
        
        // 创建投票记录
        WishVote wishVote = new WishVote();
        wishVote.setWishVoteId(wishId);
        wishVote.setUserOpenid(userOpenid);
        wishVote.setVoteTime(LocalDateTime.now());
        
        // 保存投票记录
        wishVoteMapper.insert(wishVote);
        
        // 更新心愿路线投票数
        wish.setVoteCount(wish.getVoteCount() + 1L);
        updateById(wish);
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelVote(Long wishId, String userOpenid) {
        // 参数校验
        if (wishId == null || userOpenid == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }
        
        // 查询心愿路线
        Wish wish = getById(wishId);
        if (wish == null) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "心愿路线不存在");
        }
        
        // 检查用户是否已投票
        LambdaQueryWrapper<WishVote> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WishVote::getWishVoteId, wishId)
               .eq(WishVote::getUserOpenid, userOpenid);
        
        WishVote existVote = wishVoteMapper.selectOne(wrapper);
        if (existVote == null) {
            return true; // 未投票，直接返回成功
        }
        
        // 删除投票记录
        wishVoteMapper.delete(wrapper);
        
        // 更新心愿路线投票数
        wish.setVoteCount(Math.max(0L, wish.getVoteCount() - 1L));
        updateById(wish);
        
        return true;
    }
    
    /**
     * 将Wish对象转换为简化版WishVO对象（适用于列表查询，不包含用户和位置详情）
     */
    private WishVO convertToSimpleVO(Wish wish) {
        if (wish == null) {
            return null;
        }
        
        WishVO wishVO = new WishVO();
        BeanUtils.copyProperties(wish, wishVO);
        
        // 解析图片JSON为列表
        if (wish.getImageUrls() != null) {
            try {
                wishVO.setImageUrls(JSON.parseArray(wish.getImageUrls(), String.class));
            } catch (Exception e) {
                log.error("解析图片JSON失败", e);
                wishVO.setImageUrls(new ArrayList<>());
            }
        } else {
            wishVO.setImageUrls(new ArrayList<>());
        }
        
        return wishVO;
    }
    
    /**
     * 将Wish对象转换为带位置信息的WishVO对象（不包含用户详情）
     */
    private WishVO convertToVOWithLocation(Wish wish, Location location) {
        WishVO wishVO = convertToSimpleVO(wish);
        if (wishVO == null) {
            return null;
        }
        
        // 设置目的地信息
        if (location != null) {
            wishVO.setLocation(location);
        }
        
        return wishVO;
    }

    
    /**
     * 将Wish对象转换为详情WishVO对象（包含发起人、位置信息和评论信息，但不包含发起人openid）
     */
    private WishVO convertToVOWithDetail(Wish wish, Location location, User user, List<WishComment> comments) {
        WishVO wishVO = convertToVOWithLocation(wish, location);
        if (wishVO == null) {
            return null;
        }
        
        // 设置发起人信息（但不包含openid）
        if (user != null) {
            wishVO.setNickname(user.getNickname());
            wishVO.setAvatar(user.getAvatar());
        } else if (wish.getUserOpenid() != null) {
            // 如果未传入user对象，则尝试查询（确保始终能获取发起人信息）
            User creator = getUserByOpenid(wish.getUserOpenid());
            if (creator != null) {
                wishVO.setNickname(creator.getNickname());
                wishVO.setAvatar(creator.getAvatar());
            }
        }
        
        // 设置评论列表
        if (!CollectionUtils.isEmpty(comments)) {
            // 获取所有评论用户的openid
            List<String> commentUserOpenids = comments.stream()
                    .map(WishComment::getUserOpenid)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 批量查询评论用户信息
            List<User> commentUsers = new ArrayList<>();
            if (!commentUserOpenids.isEmpty()) {
                commentUsers = userMapper.selectList(
                        Wrappers.lambdaQuery(User.class)
                                .in(User::getOpenid, commentUserOpenids)
                );
            }
            
            // 创建用户映射
            Map<String, User> userMap = commentUsers.stream()
                    .collect(Collectors.toMap(User::getOpenid, u -> u, (u1, u2) -> u1));
            
            // 转换评论为VO
            List<WishCommentVO> commentVOs = new ArrayList<>();
            for (WishComment comment : comments) {
                WishCommentVO commentVO = new WishCommentVO();
                BeanUtils.copyProperties(comment, commentVO);
                
                // 设置用户信息
                User commentUser = userMap.get(comment.getUserOpenid());
                if (commentUser != null) {
                    commentVO.setNickname(commentUser.getNickname());
                    commentVO.setAvatar(commentUser.getAvatar());
                }
                
                // 处理图片链接
                if (comment.getImageUrls() != null && !comment.getImageUrls().isEmpty()) {
                    commentVO.setImageUrls(JSON.parseArray(comment.getImageUrls(), String.class));
                }
                
                commentVOs.add(commentVO);
            }
            
            wishVO.setComments(commentVOs);
        }
        
        return wishVO;
    }
    
    /**
     * 将Wish分页对象转换为简化WishVO分页对象（适用于列表查询，包含用户信息）
     */
    private IPage<WishVO> convertToSimpleVOPage(IPage<Wish> wishPage) {
        if (wishPage == null || CollectionUtils.isEmpty(wishPage.getRecords())) {
            return new Page<>(wishPage.getCurrent(), wishPage.getSize(), 0);
        }
        
        // 获取所有发起人的openid列表
        List<String> userOpenids = wishPage.getRecords().stream()
                .map(Wish::getUserOpenid)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        List<User> users = userMapper.selectList(
                Wrappers.lambdaQuery(User.class)
                        .in(User::getOpenid, userOpenids)
        );
        
        // 创建用户映射，方便快速查找
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getOpenid, user -> user, (u1, u2) -> u1));
        
        // 创建新的分页对象
        Page<WishVO> voPage = new Page<>(wishPage.getCurrent(), wishPage.getSize(), wishPage.getTotal());
        List<WishVO> voList = new ArrayList<>();
        
        for (Wish wish : wishPage.getRecords()) {
            // 转换为简化VO并添加到列表
            WishVO wishVO = convertToSimpleVO(wish);
            
            // 设置用户信息
            User user = userMap.get(wish.getUserOpenid());
            if (user != null) {
                wishVO.setNickname(user.getNickname());
                wishVO.setAvatar(user.getAvatar());
            }
            
            voList.add(wishVO);
        }
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    /**
     * 根据openid查询用户
     */
    private User getUserByOpenid(String openid) {
        if (openid == null) {
            return null;
        }
        
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getOpenid, openid);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public IPage<WishVO> queryUserVotedWishList(Integer pageNo, Integer pageSize, String userOpenid) {
        if (userOpenid == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "用户openid不能为空");
        }
        
        // 设置默认分页参数
        if (pageNo == null) {
            pageNo = 1;
        }
        if (pageSize == null) {
            pageSize = PageSize.SIZE10.getSize();
        }
        
        // 创建分页对象
        Page<Wish> page = new Page<>(pageNo, pageSize);
        
        // 先查询用户投票记录
        LambdaQueryWrapper<WishVote> voteWrapper = Wrappers.lambdaQuery();
        voteWrapper.eq(WishVote::getUserOpenid, userOpenid)
                  .orderByDesc(WishVote::getVoteTime);
        
        // 使用分页查询投票记录
        Page<WishVote> votePage = new Page<>(pageNo, pageSize);
        IPage<WishVote> voteResult = wishVoteMapper.selectPage(votePage, voteWrapper);
        
        if (voteResult.getRecords().isEmpty()) {
            return new Page<>(pageNo, pageSize, 0);
        }
        
        // 提取投票记录中的心愿ID列表
        List<Long> wishIds = voteResult.getRecords().stream()
                .map(WishVote::getWishVoteId)
                .collect(Collectors.toList());
        
        // 查询这些心愿的详细信息
        LambdaQueryWrapper<Wish> wishWrapper = Wrappers.lambdaQuery();
        wishWrapper.in(Wish::getId, wishIds)
                  .ne(Wish::getStatus, 2); // 不显示已关闭的心愿
        
        // 使用自定义SQL查询，保持与投票时间一致的排序
        List<Wish> wishes = baseMapper.selectList(wishWrapper);
        
        // 按照投票记录的顺序重新排序心愿列表
        Map<Long, Wish> wishMap = wishes.stream()
                .collect(Collectors.toMap(Wish::getId, wish -> wish));
        
        List<Wish> orderedWishes = wishIds.stream()
                .map(wishMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        // 创建包含分页信息的结果对象
        Page<Wish> resultPage = new Page<>(pageNo, pageSize, voteResult.getTotal());
        resultPage.setRecords(orderedWishes);
        
        // 转换为包含用户信息的VO列表
        return convertToSimpleVOPage(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WishVO updateWish(WishUpdateDTO wishUpdateDTO, String userOpenid) {
        // 参数校验
        if (wishUpdateDTO == null || wishUpdateDTO.getId() == null || userOpenid == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }
        
        // 查询原心愿是否存在
        Wish wish = getById(wishUpdateDTO.getId());
        if (wish == null) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "心愿路线不存在");
        }
        
        // 校验操作权限（只有心愿创建者可以修改）
        if (!wish.getUserOpenid().equals(userOpenid)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "您没有权限修改此心愿路线");
        }
        
        // 校验心愿状态（已成团或已关闭的心愿不能修改）
        if (wish.getStatus() > 0) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "已成团或已关闭的心愿路线不能修改");
        }
        
        // 检测更新后的心愿标题和描述内容
        boolean isContentValid = textCheckService.checkWishContentOptimized(
                wishUpdateDTO.getTitle(), 
                wishUpdateDTO.getDescription(), 
                userOpenid
        );
        
        if (!isContentValid) {
            log.warn("更新心愿内容不合规，title: {}, description: {}, userOpenid: {}", 
                    wishUpdateDTO.getTitle(), wishUpdateDTO.getDescription(), userOpenid);
            throw new ServiceException(ErrorCode.CONTENT_RISKY, "心愿内容包含敏感信息，请修改后重试");
        }
        
        // 查询原位置信息
        Location oldLocation = locationMapper.selectById(wish.getLocationId());
        
        // 检查位置信息是否需要更新
        Location locationFromRequest = wishUpdateDTO.getLocation();
        boolean needUpdateLocation = false;
        
        if (locationFromRequest != null) {
            // 验证位置信息的必要字段
            if (locationFromRequest.getName() == null || locationFromRequest.getAddress() == null || 
                locationFromRequest.getLatitude() == null || locationFromRequest.getLongitude() == null) {
                throw new ServiceException(ErrorCode.PARAM_ERROR, "目的地信息不完整");
            }
            
            // 判断位置信息是否变化
            needUpdateLocation = oldLocation == null || 
                                !locationFromRequest.getName().equals(oldLocation.getName()) ||
                                !locationFromRequest.getAddress().equals(oldLocation.getAddress()) ||
                                !locationFromRequest.getLatitude().equals(oldLocation.getLatitude()) ||
                                !locationFromRequest.getLongitude().equals(oldLocation.getLongitude());
        }
        
        // 更新位置信息或创建新位置
        Location location;
        if (needUpdateLocation) {
            if (oldLocation != null) {
                // 更新已有位置信息
                oldLocation.setName(locationFromRequest.getName());
                oldLocation.setAddress(locationFromRequest.getAddress());
                oldLocation.setLatitude(locationFromRequest.getLatitude());
                oldLocation.setLongitude(locationFromRequest.getLongitude());
                locationMapper.updateById(oldLocation);
                location = oldLocation;
            } else {
                // 创建新位置记录
                location = new Location();
                location.setName(locationFromRequest.getName());
                location.setAddress(locationFromRequest.getAddress());
                location.setLatitude(locationFromRequest.getLatitude());
                location.setLongitude(locationFromRequest.getLongitude());
                locationMapper.insert(location);
                // 更新心愿的位置ID
                wish.setLocationId(location.getId());
            }
        } else {
            location = oldLocation;
        }
        
        // 更新心愿基本信息
        wish.setTitle(wishUpdateDTO.getTitle());
        wish.setDescription(wishUpdateDTO.getDescription());
        
        // 获取新的图片列表
        List<String> newImageUrls = wishUpdateDTO.getImageUrls();
        List<String> oldImageUrls = null;
        List<String> imagesToCheck = new ArrayList<>();
        
        // 解析原有图片列表
        if (wish.getImageUrls() != null) {
            oldImageUrls = JSON.parseArray(wish.getImageUrls(), String.class);
        }
        
        // 更新图片列表
        if (newImageUrls != null) {
            wish.setImageUrls(JSON.toJSONString(newImageUrls));
            
            // 找出需要检测的新图片
            if (!CollectionUtils.isEmpty(newImageUrls)) {
                if (CollectionUtils.isEmpty(oldImageUrls)) {
                    // 如果原来没有图片，所有新图片都需要检测
                    imagesToCheck.addAll(newImageUrls);
                } else {
                    // 只检测新增的图片
                    for (String newUrl : newImageUrls) {
                        if (!oldImageUrls.contains(newUrl)) {
                            imagesToCheck.add(newUrl);
                        }
                    }
                }
            }
        }
        
        // 保存心愿更新
        updateById(wish);
        
        // 数据库更新成功后，再进行异步图像检测
        if (!CollectionUtils.isEmpty(imagesToCheck)) {
            log.info("心愿更新成功，开始异步检测新增图片，wishId: {}, userOpenid: {}, imageCount: {}", 
                    wish.getId(), userOpenid, imagesToCheck.size());
            mediaCheckExecutorService.asyncCheckWishImages(imagesToCheck, wish.getId(), userOpenid);
        }
        
        // 查询更新后的完整信息
        User user = getUserByOpenid(userOpenid);
        
        // 转换为VO并返回
        WishVO wishVO = convertToVOWithDetail(wish, location, user, null);
        wishVO.setIsOwner(true); // 设置为所有者，因为是当前用户修改的
        
        return wishVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeWish(Long wishId, String userOpenid) {
        // 参数校验
        if (wishId == null || userOpenid == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }
        
        // 查询心愿是否存在
        Wish wish = getById(wishId);
        if (wish == null) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "心愿路线不存在");
        }
        
        // 校验操作权限（只有心愿创建者可以关闭）
        if (!wish.getUserOpenid().equals(userOpenid)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "您没有权限关闭此心愿路线");
        }
        
        // 校验心愿状态（已关闭的心愿不能再次关闭）
        if (wish.getStatus() == 2) {
            return true; // 已经是关闭状态，直接返回成功
        }
        
        // 更新心愿状态为已关闭（状态值为2）
        wish.setStatus(2);
        
        // 保存更新
        boolean success = updateById(wish);
        
        if (success) {
            log.info("用户[{}]关闭了心愿[{}]", userOpenid, wishId);
        }
        
        return success;
    }
} 