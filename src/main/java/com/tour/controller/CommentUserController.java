package com.tour.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.common.Result;
import com.tour.dto.CommentDTO;
import com.tour.query.CommentQuery;
import com.tour.service.CommentService;
import com.tour.vo.CommentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.*;

/**
 * 评论用户接口
 *
 * @Author Kuril
 * @DateTime 2025/5/16 16:45
 */
@Api(tags = "活动评论用户接口", description = "评论用户接口")
@RestController
@RequestMapping("/api/comment")
public class CommentUserController {

    private final CommentService commentService;

    public CommentUserController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 添加评论
     *
     * @param commentDTO 评论数据
     * @return 添加结果
     */
    @Operation(summary = "添加评论", description = "用户添加活动评论")
    @ApiResponses({
            @ApiResponse(code = 200, message = "添加成功", response = Result.class),
            @ApiResponse(code = 400, message = "参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/add")
    public Result addComment(@RequestBody @Parameter(description = "评论信息", required = true, schema = @Schema(implementation = CommentDTO.class)) CommentDTO commentDTO) {
        boolean result = commentService.addComment(commentDTO);
        return Result.success(result);
    }

    /**
     * 加载评论列表
     *
     * @param commentQuery 查询条件
     * @return 评论列表
     */
    @Operation(summary = "加载评论列表", description = "根据条件加载评论列表")
    @ApiResponses({
            @ApiResponse(code = 200, message = "加载成功", response = Result.class),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/loadComments")
    public Result loadComments(@RequestBody @Parameter(description = "查询条件", required = true) CommentQuery commentQuery) {
        IPage<CommentVO> commentVOPage = commentService.loadComments(commentQuery);
        return Result.success(commentVOPage);
    }
} 