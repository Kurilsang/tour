package com.tour.controller;

import com.tour.common.Result;
import com.tour.enums.CommentStatusEnum;
import com.tour.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 评论管理员接口
 *
 * @Author Kuril
 * @DateTime 2025/5/16 17:00
 */
@Api(tags = "活动评论管理员接口", description = "活动评论管理员接口")
@RestController
@RequestMapping("/api/admin/comment")
public class CommentAdminController {

    private final CommentService commentService;

    public CommentAdminController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 批量删除评论
     *
     * @param ids 评论ID列表，格式如：1,2,3
     * @return 删除结果
     */
    @Operation(summary = "批量删除评论", description = "根据ID列表批量删除评论")
    @ApiResponses({
            @ApiResponse(code = 200, message = "删除成功", response = Result.class),
            @ApiResponse(code = 400, message = "参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/delete/{ids}")
    public Result batchDeleteComments(@PathVariable @Parameter(description = "评论ID列表，格式如：1,2,3", required = true) String ids) {
        // 解析ID字符串
        try {
            Long[] idArray = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toArray(Long[]::new);
            
            boolean result = commentService.batchDeleteComments(idArray);
            return Result.success(result);
        } catch (NumberFormatException e) {
            return Result.error("评论ID格式错误");
        } catch (Exception e) {
            return Result.error("删除评论失败: " + e.getMessage());
        }
    }

    /**
     * 审核通过评论（设置为可见）
     *
     * @param id 评论ID
     * @return 更新结果
     */
    @Operation(summary = "审核通过评论", description = "将评论状态设置为可见")
    @ApiResponses({
            @ApiResponse(code = 200, message = "更新成功", response = Result.class),
            @ApiResponse(code = 400, message = "参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PutMapping("/approve/{id}")
    public Result approveComment(@PathVariable @Parameter(description = "评论ID", required = true) Long id) {
        boolean result = commentService.updateCommentStatus(id, CommentStatusEnum.VISIBLE.getCode());
        return Result.success(result);
    }

    /**
     * 拒绝评论（设置为不可见）
     *
     * @param id 评论ID
     * @return 更新结果
     */
    @Operation(summary = "拒绝评论", description = "将评论状态设置为不可见")
    @ApiResponses({
            @ApiResponse(code = 200, message = "更新成功", response = Result.class),
            @ApiResponse(code = 400, message = "参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PutMapping("/reject/{id}")
    public Result rejectComment(@PathVariable @Parameter(description = "评论ID", required = true) Long id) {
        boolean result = commentService.updateCommentStatus(id, CommentStatusEnum.INVISIBLE.getCode());
        return Result.success(result);
    }

    /**
     * 更新评论状态
     *
     * @param id 评论ID
     * @param status 状态值（1-待审核，2-可见，3-不可见）
     * @return 更新结果
     */
    @Operation(summary = "更新评论状态", description = "更新指定评论的状态")
    @ApiResponses({
            @ApiResponse(code = 200, message = "更新成功", response = Result.class),
            @ApiResponse(code = 400, message = "参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PutMapping("/updateStatus/{id}/{status}")
    public Result updateCommentStatus(
            @PathVariable @Parameter(description = "评论ID", required = true) Long id,
            @PathVariable @Parameter(description = "状态值（1-待审核，2-可见，3-不可见）", required = true) Integer status) {
        // 验证状态值是否合法
        if (status != CommentStatusEnum.PENDING.getCode() && 
            status != CommentStatusEnum.VISIBLE.getCode() && 
            status != CommentStatusEnum.INVISIBLE.getCode()) {
            return Result.error("状态值无效");
        }
        
        boolean result = commentService.updateCommentStatus(id, status);
        return Result.success(result);
    }
} 