package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tour.common.constant.Constants;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.UserContext;
import com.tour.dao.ActivityOrderMapper;
import com.tour.dao.CommentMapper;
import com.tour.dao.UserMapper;
import com.tour.dto.CommentDTO;
import com.tour.enums.CommentStatusEnum;
import com.tour.enums.PageSize;
import com.tour.model.ActivityOrder;
import com.tour.model.Comment;
import com.tour.model.User;
import com.tour.query.CommentQuery;
import com.tour.service.CommentService;
import com.tour.vo.CommentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Kuril
 * @Description 评论服务实现类
 * @DateTime 2025/5/16 16:35
 */
@Service("commentService")
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ActivityOrderMapper activityOrderMapper;
    private final UserMapper userMapper;

    public CommentServiceImpl(CommentMapper commentMapper, ActivityOrderMapper activityOrderMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.activityOrderMapper = activityOrderMapper;
        this.userMapper = userMapper;
    }

    /**
     * 添加评论
     *
     * @param commentDTO 评论数据传输对象
     * @return 是否添加成功
     */
    @Override
    public boolean addComment(CommentDTO commentDTO) {
        // 从上下文获取当前用户openid
        String openid = UserContext.getOpenId();
        if (openid == null || openid.isEmpty()) {
            throw new ServiceException("用户未登录");
        }

        // 根据orderNo查询活动订单，获取activityId
        QueryWrapper<ActivityOrder> orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.eq("order_no", commentDTO.getOrderNo());
        ActivityOrder activityOrder = activityOrderMapper.selectOne(orderQueryWrapper);
        if (activityOrder == null) {
            throw new ServiceException("订单不存在");
        }

        // 验证订单所有者
        if (!openid.equals(activityOrder.getOpenid())) {
            throw new ServiceException("只能评论自己的订单");
        }

        // 创建评论实体
        Comment comment = new Comment();
        comment.setOpenid(openid);
        comment.setOrderNo(commentDTO.getOrderNo());
        comment.setContent(commentDTO.getContent());
        comment.setActivityId(activityOrder.getActivityId());
        comment.setStatus(CommentStatusEnum.PENDING.getCode()); // 设置为待审核状态
        comment.setCreateTime(LocalDateTime.now());

        // 保存评论
        int rows = commentMapper.insert(comment);
        return rows > 0;
    }

    /**
     * 根据查询条件分页加载评论
     *
     * @param commentQuery 评论查询条件
     * @return 评论分页结果
     */
    @Override
    public IPage<CommentVO> loadComments(CommentQuery commentQuery) {
        // 构建分页参数
        Integer pageNo = commentQuery.getPageNo();
        Integer pageSize = commentQuery.getPageSize();
        if (pageNo == null) {
            pageNo = Constants.defaultPageNo;
        }
        if (pageSize == null) {
            pageSize = PageSize.SIZE10.getSize();
        }
        Page<Comment> page = new Page<>(pageNo, pageSize);

        // 构建查询条件
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        
        // 根据查询条件动态添加查询条件
        if (commentQuery.getId() != null) {
            queryWrapper.eq("id", commentQuery.getId());
        }
        if (commentQuery.getOpenid() != null && !commentQuery.getOpenid().isEmpty()) {
            queryWrapper.eq("openid", commentQuery.getOpenid());
        }
        if (commentQuery.getActivityId() != null) {
            queryWrapper.eq("activity_id", commentQuery.getActivityId());
        }
        if (commentQuery.getOrderNo() != null && !commentQuery.getOrderNo().isEmpty()) {
            queryWrapper.eq("order_no", commentQuery.getOrderNo());
        }
        if (commentQuery.getStatus() != null) {
            queryWrapper.eq("status", commentQuery.getStatus());
        }
        if (commentQuery.getContent() != null && !commentQuery.getContent().isEmpty()) {
            queryWrapper.like("content", commentQuery.getContent());
        }
        if (commentQuery.getStartTime() != null) {
            queryWrapper.ge("create_time", commentQuery.getStartTime());
        }
        if (commentQuery.getEndTime() != null) {
            queryWrapper.le("create_time", commentQuery.getEndTime());
        }

        // 处理排序信息
        String orderBy = commentQuery.getOrderBy();
        if (orderBy != null && !orderBy.isEmpty()) {
            queryWrapper.last("ORDER BY " + orderBy);
        } else {
            queryWrapper.orderByDesc("create_time"); // 默认按创建时间降序
        }

        // 执行分页查询
        IPage<Comment> commentPage = commentMapper.selectPage(page, queryWrapper);
        
        // 转换为VO对象
        IPage<CommentVO> commentVOPage = commentPage.convert(comment -> {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comment, commentVO);
            
            // 获取用户信息（头像和昵称）
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("openid", comment.getOpenid());
            User user = userMapper.selectOne(userQueryWrapper);
            if (user != null) {
                commentVO.setAvatar(user.getAvatar());
                commentVO.setNickName(user.getNickname());
            }
            return commentVO;
        });
        
        return commentVOPage;
    }
    
    /**
     * 批量删除评论
     *
     * @param ids 评论ID数组
     * @return 是否全部删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteComments(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return false;
        }
        
        // 将数组转换为List
        List<Long> idList = Arrays.asList(ids);
        
        try {
            // 批量删除评论
            int deletedCount = commentMapper.deleteBatchIds(idList);
            log.info("批量删除评论，请求删除{}条，实际删除{}条", ids.length, deletedCount);
            
            // 判断删除结果
            return deletedCount == ids.length;
        } catch (Exception e) {
            log.error("批量删除评论失败", e);
            throw new ServiceException("批量删除评论失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新评论状态
     *
     * @param id 评论ID
     * @param status 新状态
     * @return 是否更新成功
     */
    @Override
    public boolean updateCommentStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }
        
        // 确认评论存在
        Comment existingComment = commentMapper.selectById(id);
        if (existingComment == null) {
            throw new ServiceException("评论不存在");
        }
        
        // 创建更新对象
        Comment comment = new Comment();
        comment.setId(id);
        comment.setStatus(status);
        
        // 执行更新
        int updated = commentMapper.updateById(comment);
        return updated > 0;
    }
} 