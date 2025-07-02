package com.tour.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.common.Result;
import com.tour.common.util.UserContext;
import com.tour.dto.WishCommentDTO;
import com.tour.service.IWishCommentService;
import com.tour.vo.WishCommentVO;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 心愿路线评论控制器
 *
 * @Author Abin
 */
@Api(tags = "心愿路线评论接口", description = "评论的添加、查询与删除")
@RestController
@RequestMapping("/api/wish/comment")
public class WishCommentController {

    @Autowired
    private IWishCommentService wishCommentService;

    /**
     * 添加评论
     *
     * @param wishCommentDTO 评论DTO
     * @return 评论信息
     */
    @ApiOperation(value = "添加评论", notes = "用户对心愿路线发表评论")
    @PostMapping("/add")
    public Result<WishCommentVO> addComment(@RequestBody @Valid WishCommentDTO wishCommentDTO) {
        // 从上下文中获取用户openid
        String userOpenid = UserContext.getOpenId();
        WishCommentVO commentVO = wishCommentService.addComment(wishCommentDTO, userOpenid);
        return Result.success(commentVO);
    }

    /**
     * 分页查询心愿路线评论列表
     */
    @Operation(summary = "查询评论列表", description = "分页查询心愿路线的评论列表")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功", response = Result.class),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/list")
    public Result<IPage<WishCommentVO>> queryCommentList(
            @RequestParam @Parameter(description = "心愿路线ID", required = true) Long wishId,
            @RequestParam(required = false, defaultValue = "1") @Parameter(description = "页码") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") @Parameter(description = "每页记录数") Integer pageSize) {
        
        // 从上下文获取当前用户的openid（可能为空，表示未登录）
        String userOpenid = null;
        try {
            userOpenid = UserContext.getOpenId();
        } catch (Exception e) {
            // 未登录用户，忽略异常
        }
        
        // 查询评论列表，传入当前用户openid以判断是否是评论所有者
        IPage<WishCommentVO> page = wishCommentService.queryCommentList(pageNo, pageSize, wishId, userOpenid);
        return Result.success(page);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    @ApiOperation(value = "删除评论", notes = "用户删除自己发布的评论，管理员可删除任意评论")
    @DeleteMapping("/delete/{commentId}")
    public Result<Boolean> deleteComment(
            @PathVariable @ApiParam(value = "评论ID", required = true) Long commentId) {
        // 从上下文中获取用户openid
        String userOpenid = UserContext.getOpenId();
        boolean success = wishCommentService.deleteComment(commentId, userOpenid);
        return Result.success(success);
    }

    /**
     * 查询当前用户的所有评论
     */
    @Operation(summary = "查询我的所有评论", description = "分页查询当前用户发表的所有评论")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/my")
    public Result<IPage<WishCommentVO>> queryMyComments(
            @RequestParam(required = false, defaultValue = "1") @Parameter(description = "页码") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") @Parameter(description = "每页记录数") Integer pageSize) {
        
        // 从上下文中获取用户openid
        String userOpenid = UserContext.getOpenId();
        
        // 查询当前用户的所有评论
        IPage<WishCommentVO> page = wishCommentService.queryUserCommentList(pageNo, pageSize, userOpenid);
        return Result.success(page);
    }
} 