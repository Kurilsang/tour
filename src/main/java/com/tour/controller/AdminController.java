package com.tour.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tour.common.Result;
import com.tour.common.util.UserContext;
import com.tour.dao.CommentMapper;
import com.tour.dao.RefundApplyMapper;
import com.tour.enums.CommentStatusEnum;
import com.tour.enums.RefundStatus;
import com.tour.model.Comment;
import com.tour.model.RefundApply;
import com.tour.vo.AdminToDoVO;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员控制器
 *
 * @Author Abin
 */
@Api(tags = "管理员接口", description = "仅管理员可访问的接口")
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private RefundApplyMapper refundApplyMapper;

    /**
     * 获取管理员信息
     */
    @Operation(summary = "获取管理员信息", description = "获取当前登录的管理员信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getAdminInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("openid", UserContext.getOpenId());
        data.put("role", UserContext.getRole());
        data.put("isAdmin", UserContext.isAdmin());
        return Result.success(data);
    }
    
    /**
     * @Author Kuril
     * 获取管理员待办事项数量
     */
    @Operation(summary = "获取管理员待办事项数量", description = "获取待审核评论总数和待处理退款申请总数")
    @GetMapping("/getToDo")
    public Result<AdminToDoVO> getToDo() {
        AdminToDoVO todoVO = new AdminToDoVO();
        
        // 查询待审核评论数量
        LambdaQueryWrapper<Comment> commentQueryWrapper = new LambdaQueryWrapper<>();
        commentQueryWrapper.eq(Comment::getStatus, CommentStatusEnum.PENDING.getCode());
        long pendingCommentCount = commentMapper.selectCount(commentQueryWrapper);
        todoVO.setPendingCommentCount(pendingCommentCount);
        
        // 查询待处理退款申请数量
        LambdaQueryWrapper<RefundApply> refundQueryWrapper = new LambdaQueryWrapper<>();
        refundQueryWrapper.eq(RefundApply::getStatus, RefundStatus.PENDING.getCode());
        long pendingRefundCount = refundApplyMapper.selectCount(refundQueryWrapper);
        todoVO.setPendingRefundCount(pendingRefundCount);
        
        return Result.success(todoVO);
    }
} 