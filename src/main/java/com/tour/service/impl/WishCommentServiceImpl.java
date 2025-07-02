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
import com.tour.dao.UserMapper;
import com.tour.dao.WishCommentMapper;
import com.tour.dao.WishMapper;
import com.tour.dto.WishCommentDTO;
import com.tour.enums.MediaCheckEnum;
import com.tour.enums.PageSize;
import com.tour.enums.RoleEnum;
import com.tour.model.User;
import com.tour.model.Wish;
import com.tour.model.WishComment;
import com.tour.query.WishCommentQuery;
import com.tour.service.IWishCommentService;
import com.tour.service.IMediaSubmitService;
import com.tour.service.IMediaCheckExecutorService;
import com.tour.service.ITextCheckService;
import com.tour.vo.WishCommentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 心愿路线评论服务实现类
 *
 * @Author Abin
 */
@Slf4j
@Service
public class WishCommentServiceImpl extends ServiceImpl<WishCommentMapper, WishComment> implements IWishCommentService {

    @Autowired
    private WishCommentMapper wishCommentMapper;
    
    @Autowired
    private WishMapper wishMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private IMediaSubmitService mediaSubmitService;
    
    @Autowired
    private IMediaCheckExecutorService mediaCheckExecutorService;
    
    @Autowired
    private ITextCheckService textCheckService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WishCommentVO addComment(WishCommentDTO wishCommentDTO, String userOpenid) {
        // 校验参数
        if (wishCommentDTO == null || wishCommentDTO.getWishId() == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "心愿ID不能为空");
        }
        if (userOpenid == null || userOpenid.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "用户信息不能为空");
        }
        
        // 检查心愿是否存在
        Wish wish = wishMapper.selectById(wishCommentDTO.getWishId());
        if (wish == null) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "心愿路线不存在");
        }
        
        // 检测评论内容是否合规（使用带上下文的检测方法）
        boolean isContentValid = textCheckService.checkCommentWithContext(
                wishCommentDTO.getContent(), 
                wish.getTitle(), 
                userOpenid
        );
        
        if (!isContentValid) {
            log.warn("评论内容不合规，comment: {}, wishId: {}, userOpenid: {}", 
                    wishCommentDTO.getContent(), wishCommentDTO.getWishId(), userOpenid);
            throw new ServiceException(ErrorCode.CONTENT_RISKY, "评论内容包含敏感信息，请修改后重试");
        }
        
        // 创建评论对象
        WishComment wishComment = new WishComment();
        wishComment.setWishId(wishCommentDTO.getWishId());
        wishComment.setUserOpenid(userOpenid);
        wishComment.setContent(wishCommentDTO.getContent());
        
        // 保存图片列表，用于后续的异步检测
        List<String> imageUrls = wishCommentDTO.getImageUrls();
        
        // 处理图片列表
        if (!CollectionUtils.isEmpty(imageUrls)) {
            wishComment.setImageUrls(JSON.toJSONString(imageUrls));
        }
        
        // 保存评论
        save(wishComment);
        
        // 更新心愿表的评论计数
        wish.setCommentCount(wish.getCommentCount() + 1L);
        wishMapper.updateById(wish);
        
        // 数据库更新成功后，再进行异步图像检测
        if (!CollectionUtils.isEmpty(imageUrls)) {
            log.info("评论创建成功，开始异步检测图片，commentId: {}, userOpenid: {}, imageCount: {}", 
                    wishComment.getId(), userOpenid, imageUrls.size());
            mediaCheckExecutorService.asyncCheckCommentImages(imageUrls, wishComment.getId(), userOpenid);
        }
        
        // 查询用户信息
        User user = userMapper.selectOne(
                Wrappers.lambdaQuery(User.class)
                        .eq(User::getOpenid, userOpenid)
        );
        
        // 转换为VO并返回
        WishCommentVO wishCommentVO = convertToVO(wishComment, user);
        
        return wishCommentVO;
    }
    
    @Override
    public IPage<WishCommentVO> queryCommentList(Integer pageNo, Integer pageSize, Long wishId, String currentUserOpenid) {
        if (wishId == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "心愿ID不能为空");
        }
        
        // 设置分页参数
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = PageSize.SIZE10.getSize();
        }
        
        // 查询评论列表
        Page<WishComment> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<WishComment> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WishComment::getWishId, wishId)
               .orderByDesc(WishComment::getCreateTime);
        IPage<WishComment> commentPage = wishCommentMapper.selectPage(page, wrapper);
        
        // 转换为VO
        if (commentPage.getRecords().isEmpty()) {
            return Page.of(pageNo, pageSize, commentPage.getTotal());
        }
        
        // 获取所有评论的用户openid列表
        List<String> userOpenids = commentPage.getRecords().stream()
                .map(WishComment::getUserOpenid)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        List<User> users = userMapper.selectList(
                Wrappers.lambdaQuery(User.class)
                        .in(User::getOpenid, userOpenids)
        );
        
        // 创建用户映射，方便快速查找
        java.util.Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getOpenid, user -> user));
        
        // 转换评论列表为VO列表
        List<WishCommentVO> voList = new ArrayList<>();
        for (WishComment comment : commentPage.getRecords()) {
            User user = userMap.get(comment.getUserOpenid());
            WishCommentVO commentVO = convertToVO(comment, user);
            
            // 判断当前用户是否是评论所有者
            if (currentUserOpenid != null && currentUserOpenid.equals(comment.getUserOpenid())) {
                commentVO.setIsOwner(true);
            } else {
                commentVO.setIsOwner(false);
            }
            
            voList.add(commentVO);
        }
        
        // 创建并返回VO分页对象
        IPage<WishCommentVO> voPage = new Page<>(pageNo, pageSize, commentPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    @Override
    public IPage<WishCommentVO> queryCommentList(Integer pageNo, Integer pageSize, Long wishId) {
        // 兼容旧接口，不传入当前用户信息
        return queryCommentList(pageNo, pageSize, wishId, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteComment(Long commentId, String userOpenid) {
        if (commentId == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "评论ID不能为空");
        }
        if (userOpenid == null || userOpenid.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "用户信息不能为空");
        }
        
        // 查询评论
        WishComment comment = getById(commentId);
        if (comment == null) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "评论不存在");
        }
        
        // 检查权限（仅评论发布者或管理员可以删除）
        User user = userMapper.selectOne(
                Wrappers.lambdaQuery(User.class)
                        .eq(User::getOpenid, userOpenid)
        );
        
        boolean isAdmin = RoleEnum.ADMIN.getCode().equals(user.getRole())||RoleEnum.SUPER_ADMIN.getCode().equals(user.getRole());
        boolean isCommentOwner = comment.getUserOpenid().equals(userOpenid);
        
        if (!isAdmin && !isCommentOwner) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "您没有权限删除此评论");
        }
        
        // 删除评论
        boolean result = removeById(commentId);
        
        // 更新心愿表的评论计数
        if (result) {
            Wish wish = wishMapper.selectById(comment.getWishId());
            if (wish != null && wish.getCommentCount() > 0L) {
                wish.setCommentCount(wish.getCommentCount() - 1L);
                wishMapper.updateById(wish);
            }
        }
        
        return result;
    }
    
    @Override
    public IPage<WishCommentVO> queryUserCommentList(Integer pageNo, Integer pageSize, String userOpenid) {
        if (userOpenid == null || userOpenid.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "用户信息不能为空");
        }
        
        // 设置分页参数
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = PageSize.SIZE10.getSize();
        }
        
        // 查询评论列表
        Page<WishComment> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<WishComment> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(WishComment::getUserOpenid, userOpenid)
               .orderByDesc(WishComment::getCreateTime);
        IPage<WishComment> commentPage = wishCommentMapper.selectPage(page, wrapper);
        
        // 转换为VO
        if (commentPage.getRecords().isEmpty()) {
            return Page.of(pageNo, pageSize, commentPage.getTotal());
        }
        
        // 获取所有评论涉及的心愿ID列表
        List<Long> wishIds = commentPage.getRecords().stream()
                .map(WishComment::getWishId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询心愿信息
        Map<Long, Wish> wishMap = new HashMap<>();
        if (!wishIds.isEmpty()) {
            List<Wish> wishes = wishMapper.selectList(
                    Wrappers.lambdaQuery(Wish.class)
                            .in(Wish::getId, wishIds)
            );
            wishMap = wishes.stream()
                    .collect(Collectors.toMap(Wish::getId, wish -> wish));
        }
        
        // 查询用户信息
        User user = userMapper.selectOne(
                Wrappers.lambdaQuery(User.class)
                        .eq(User::getOpenid, userOpenid)
        );
        
        // 转换评论列表为VO列表，每个评论VO都带有心愿标题信息
        List<WishCommentVO> voList = new ArrayList<>();
        for (WishComment comment : commentPage.getRecords()) {
            WishCommentVO commentVO = convertToVO(comment, user);
            
            // 获取心愿信息
            Wish wish = wishMap.get(comment.getWishId());
            if (wish != null) {
                commentVO.setWishTitle(wish.getTitle());
            }
            
            // 设置为所有者（因为是查询的当前用户的评论）
            commentVO.setIsOwner(true);
            
            voList.add(commentVO);
        }
        
        // 创建并返回VO分页对象
        IPage<WishCommentVO> voPage = new Page<>(pageNo, pageSize, commentPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    @Override
    public IPage<WishCommentVO> queryCommentListForAdmin(WishCommentQuery query) {
        if (query == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "查询参数不能为空");
        }
        
        // 设置分页参数
        if (query.getPageNo() == null || query.getPageNo() < 1) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(PageSize.SIZE10.getSize());
        }
        
        // 构建查询条件
        LambdaQueryWrapper<WishComment> wrapper = Wrappers.lambdaQuery();
        
        // 按心愿ID查询
        if (query.getWishId() != null) {
            wrapper.eq(WishComment::getWishId, query.getWishId());
        }
        
        // 按评论内容关键字搜索
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(WishComment::getContent, query.getKeyword());
        }
        
        // 按时间范围筛选
        if (query.getStartTime() != null) {
            wrapper.ge(WishComment::getCreateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(WishComment::getCreateTime, query.getEndTime());
        }
        
        // 按用户昵称搜索（需要先查询用户ID）
        List<String> targetUserOpenids = null;
        if (StringUtils.hasText(query.getNickname())) {
            List<User> users = userMapper.selectList(
                    Wrappers.lambdaQuery(User.class)
                            .like(User::getNickname, query.getNickname())
            );
            
            if (!users.isEmpty()) {
                targetUserOpenids = users.stream()
                        .map(User::getOpenid)
                        .collect(Collectors.toList());
                wrapper.in(WishComment::getUserOpenid, targetUserOpenids);
            } else {
                // 如果没有找到匹配的用户，返回空结果
                return new Page<>(query.getPageNo(), query.getPageSize(), 0);
            }
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(WishComment::getCreateTime);
        
        // 执行查询
        Page<WishComment> page = new Page<>(query.getPageNo(), query.getPageSize());
        IPage<WishComment> commentPage = wishCommentMapper.selectPage(page, wrapper);
        
        // 如果没有记录，返回空页
        if (commentPage.getRecords().isEmpty()) {
            return new Page<>(query.getPageNo(), query.getPageSize(), 0);
        }
        
        // 获取所有评论的用户openid
        List<String> userOpenids = commentPage.getRecords().stream()
                .map(WishComment::getUserOpenid)
                .distinct()
                .collect(Collectors.toList());
        
        // 获取所有相关的心愿ID
        List<Long> wishIds = commentPage.getRecords().stream()
                .map(WishComment::getWishId)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        List<User> users = userMapper.selectList(
                Wrappers.lambdaQuery(User.class)
                        .in(User::getOpenid, userOpenids)
        );
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getOpenid, user -> user));
        
        // 批量查询心愿信息
        List<Wish> wishes = wishMapper.selectList(
                Wrappers.lambdaQuery(Wish.class)
                        .in(Wish::getId, wishIds)
        );
        Map<Long, Wish> wishMap = wishes.stream()
                .collect(Collectors.toMap(Wish::getId, wish -> wish));
        
        // 转换评论列表为VO列表
        List<WishCommentVO> voList = new ArrayList<>();
        for (WishComment comment : commentPage.getRecords()) {
            User user = userMap.get(comment.getUserOpenid());
            WishCommentVO commentVO = convertToVO(comment, user);
            
            // 设置心愿标题信息
            Wish wish = wishMap.get(comment.getWishId());
            if (wish != null) {
                commentVO.setWishTitle(wish.getTitle());
            }
            
            voList.add(commentVO);
        }
        
        // 创建并返回VO分页对象
        IPage<WishCommentVO> voPage = new Page<>(query.getPageNo(), query.getPageSize(), commentPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCommentByAdmin(Long commentId, String adminOpenid) {
        if (commentId == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "评论ID不能为空");
        }
        if (adminOpenid == null || adminOpenid.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "管理员信息不能为空");
        }
        
        // 查询评论
        WishComment comment = getById(commentId);
        if (comment == null) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "评论不存在");
        }
        
        // 检查管理员权限
        User admin = userMapper.selectOne(
                Wrappers.lambdaQuery(User.class)
                        .eq(User::getOpenid, adminOpenid)
        );
        
        boolean isAdmin = RoleEnum.ADMIN.getCode().equals(admin.getRole()) || 
                         RoleEnum.SUPER_ADMIN.getCode().equals(admin.getRole());
        
        if (!isAdmin) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "您没有管理员权限");
        }
        
        // 删除评论
        boolean success = removeById(commentId);
        
        if (success) {
            // 更新心愿的评论计数
            Wish wish = wishMapper.selectById(comment.getWishId());
            if (wish != null && wish.getCommentCount() > 0) {
                wish.setCommentCount(wish.getCommentCount() - 1);
                wishMapper.updateById(wish);
            }
            
            log.info("管理员[{}]删除了评论[{}]", admin.getNickname(), commentId);
        }
        
        return success;
    }
    
    /**
     * 将评论实体转换为视图对象
     *
     * @param comment 评论实体
     * @param user 用户实体
     * @return 评论视图对象
     */
    private WishCommentVO convertToVO(WishComment comment, User user) {
        WishCommentVO vo = new WishCommentVO();
        BeanUtils.copyProperties(comment, vo);
        
        // 设置用户信息
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        
        // 处理图片链接
        if (comment.getImageUrls() != null && !comment.getImageUrls().isEmpty()) {
            vo.setImageUrls(JSON.parseArray(comment.getImageUrls(), String.class));
        }
        
        return vo;
    }
} 