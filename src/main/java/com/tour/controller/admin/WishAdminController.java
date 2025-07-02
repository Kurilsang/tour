/**
 * @Author Abin
 * @Description 心愿管理控制器
 */
package com.tour.controller.admin;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tour.common.Result;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.UserContext;
import com.tour.dto.WishStatusUpdateDTO;
import com.tour.model.User;
import com.tour.model.Wish;
import com.tour.query.WishCommentQuery;
import com.tour.query.WishQuery;
import com.tour.service.IUserService;
import com.tour.service.IWishCommentService;
import com.tour.service.IWishService;
import com.tour.vo.WishCommentVO;
import com.tour.vo.WishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 心愿管理控制器
 * 专门处理管理员对心愿的操作
 */
@Api(tags = "心愿管理接口", description = "管理员操作心愿的接口")
@Slf4j
@RestController
@RequestMapping("/api/admin/wish")
public class WishAdminController {

    @Autowired
    private IWishService wishService;
    
    @Autowired
    private IUserService userService;
    
    @Autowired
    private IWishCommentService wishCommentService;

    /**
     * 管理员查询心愿列表
     */
    @Operation(summary = "查询心愿列表", description = "管理员查询心愿列表，支持按标题和状态搜索，包含已关闭的心愿")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 403, message = "没有管理员权限"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/list")
    public Result<IPage<WishVO>> queryWishList(
            @RequestParam(required = false) @Parameter(description = "搜索关键词，可匹配标题或描述") String keyword,
            @RequestParam(required = false) @Parameter(description = "心愿状态：0-待成团，1-已成团，2-已关闭") Integer status,
            @RequestParam(required = false) @Parameter(description = "排序方式：0-最新，1-最热（投票数最多）") Integer sortType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "创建开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "创建结束时间") LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "1") @Parameter(description = "页码") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") @Parameter(description = "每页记录数") Integer pageSize) {
        
        // 构建查询参数
        WishQuery query = new WishQuery();
        query.setKeyword(keyword);
        query.setStatus(status);
        query.setSortType(sortType);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        
        // 调用服务执行查询
        IPage<WishVO> page = wishService.queryWishList(query);
        return Result.success(page);
    }
    
    /**
     * 管理员修改心愿状态
     */
    @Operation(summary = "修改心愿状态", description = "管理员修改心愿状态：0-待成团，1-已成团，2-已关闭")
    @ApiResponses({
        @ApiResponse(code = 200, message = "修改成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 403, message = "没有管理员权限"),
        @ApiResponse(code = 404, message = "心愿不存在"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PutMapping("/status")
    public Result<Boolean> updateWishStatus(
            @RequestBody @Parameter(description = "心愿状态更新信息", required = true, 
                schema = @Schema(implementation = WishStatusUpdateDTO.class)) WishStatusUpdateDTO updateDTO) {
                
        if (updateDTO == null || updateDTO.getWishId() == null || updateDTO.getStatus() == null) {
            return Result.error(ErrorCode.PARAM_ERROR, "参数不完整");
        }
        
        // 检查状态值有效性
        if (updateDTO.getStatus() < 0 || updateDTO.getStatus() > 2) {
            return Result.error(ErrorCode.PARAM_ERROR, "状态值无效，应为0-2之间的整数");
        }
        
        String adminOpenid = UserContext.getOpenId();
        User admin = userService.getUserByOpenid(adminOpenid);
        
        // 更新状态
        LambdaUpdateWrapper<Wish> updateWrapper = Wrappers.lambdaUpdate(Wish.class)
                .eq(Wish::getId, updateDTO.getWishId())
                .set(Wish::getStatus, updateDTO.getStatus());
        
        boolean success = wishService.update(updateWrapper);
        
        if (!success) {
            Wish wish = wishService.getById(updateDTO.getWishId());
            if (wish == null) {
                return Result.error(ErrorCode.RESOURCE_NOT_FOUND, "心愿不存在");
            }
        }
        
        log.info("管理员[{}]将心愿[{}]状态修改为[{}]", admin.getNickname(), updateDTO.getWishId(), updateDTO.getStatus());
        
        return Result.success(success);
    }
    
    /**
     * 管理员查询评论列表
     */
    @Operation(summary = "查询评论列表", description = "管理员高级查询评论列表，支持按内容、用户、心愿等条件搜索")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 403, message = "没有管理员权限"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/comment/list")
    public Result<IPage<WishCommentVO>> queryCommentList(
            @RequestParam(required = false) @Parameter(description = "搜索关键词，可匹配评论内容") String keyword,
            @RequestParam(required = false) @Parameter(description = "用户昵称") String nickname,
            @RequestParam(required = false) @Parameter(description = "心愿ID") Long wishId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "创建开始时间") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "创建结束时间") LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "1") @Parameter(description = "页码") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") @Parameter(description = "每页记录数") Integer pageSize) {
        
        // 构建查询参数
        WishCommentQuery query = new WishCommentQuery();
        query.setKeyword(keyword);
        query.setNickname(nickname);
        query.setWishId(wishId);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        
        // 调用服务执行查询
        IPage<WishCommentVO> page = wishCommentService.queryCommentListForAdmin(query);
        return Result.success(page);
    }
    
    /**
     * 管理员删除评论
     */
    @Operation(summary = "删除评论", description = "管理员删除任意评论")
    @ApiResponses({
        @ApiResponse(code = 200, message = "删除成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 403, message = "没有管理员权限"),
        @ApiResponse(code = 404, message = "评论不存在"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @DeleteMapping("/comment/{commentId}")
    public Result<Boolean> deleteComment(
            @PathVariable @Parameter(description = "评论ID", required = true) Long commentId) {
        
        // 获取当前管理员信息
        String adminOpenid = UserContext.getOpenId();
        
        // 调用服务执行删除
        boolean success = wishCommentService.deleteCommentByAdmin(commentId, adminOpenid);
        
        log.info("管理员[{}]删除了评论[{}]", adminOpenid, commentId);
        
        return Result.success(success);
    }
} 