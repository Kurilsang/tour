package com.tour.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.common.Result;
import com.tour.common.util.UserContext;
import com.tour.dto.WishCreateDTO;
import com.tour.dto.WishUpdateDTO;
import com.tour.query.WishQuery;
import com.tour.service.IWishService;
import com.tour.service.IWishCommentService;
import com.tour.vo.WishVO;
import com.tour.vo.WishCommentVO;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * 心愿路线控制器
 *
 * @Author Abin
 */
@Api(tags = "心愿路线接口", description = "心愿路线的创建、查询与投票")
@RestController
@RequestMapping("/api/wish")
public class WishController {

    @Autowired
    private IWishService wishService;

    @Autowired
    private IWishCommentService wishCommentService;

    /**
     * 创建心愿路线
     */
    @Operation(summary = "创建心愿路线", description = "创建新的心愿路线，传入位置信息会自动创建新的位置记录")
    @ApiResponses({
        @ApiResponse(code = 200, message = "创建成功", response = Result.class),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping
    public Result<WishVO> createWish(
            @Valid @RequestBody @Parameter(description = "心愿路线创建信息（包含完整的位置信息）", required = true, schema = @Schema(implementation = WishCreateDTO.class)) WishCreateDTO wishCreateDTO) {
        // 从上下文获取当前用户的openid
        String openid = UserContext.getOpenId();
        
        // 调用服务创建心愿路线
        WishVO wishVO = wishService.createWish(wishCreateDTO, openid);
        
        return Result.success(wishVO);
    }
    
    /**
     * 更新心愿路线
     */
    @Operation(summary = "更新心愿路线", description = "更新已有的心愿路线，仅心愿创建者可操作，已成团或已关闭的心愿不可修改")
    @ApiResponses({
        @ApiResponse(code = 200, message = "更新成功", response = Result.class),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 403, message = "无权限操作"),
        @ApiResponse(code = 404, message = "心愿不存在"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PutMapping
    public Result<WishVO> updateWish(
            @Valid @RequestBody @Parameter(description = "心愿路线更新信息", required = true, schema = @Schema(implementation = WishUpdateDTO.class)) WishUpdateDTO wishUpdateDTO) {
        // 从上下文获取当前用户的openid
        String openid = UserContext.getOpenId();
        
        // 调用服务更新心愿路线
        WishVO wishVO = wishService.updateWish(wishUpdateDTO, openid);
        
        return Result.success(wishVO);
    }
    
    /**
     * 查询心愿路线详情
     */
    @Operation(summary = "查询心愿路线详情", description = "根据ID查询心愿路线详情")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功", response = Result.class),
        @ApiResponse(code = 404, message = "心愿路线不存在"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/{id}")
    public Result<WishVO> getWishById(@PathVariable @Parameter(description = "心愿路线ID") Long id) {
        // 从上下文获取当前用户的openid（可能为空，表示未登录）
        String userOpenid = null;
        try {
            userOpenid = UserContext.getOpenId();
        } catch (Exception e) {
            // 未登录用户，忽略异常
        }
        
        // 查询详情，传入当前用户openid以判断是否是所有者
        WishVO wishVO = wishService.getWishById(id, userOpenid);
        return Result.success(wishVO);
    }
    
    /**
     * 高级查询心愿路线列表
     */
    @Operation(summary = "查询心愿路线列表", description = "高级查询心愿路线列表，支持关键词搜索和排序")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功", response = Result.class),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/list")
    public Result<IPage<WishVO>> queryWishList(
            @RequestParam(required = false) @Parameter(description = "搜索关键词，可匹配标题或描述") String keyword,
            @RequestParam(required = false) @Parameter(description = "状态：0-待成团，1-已成团") Integer status,
            @RequestParam(required = false) @Parameter(description = "排序方式：0-最新，1-最热（投票数最多），默认0") Integer sortType,
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
     * 查询当前用户创建的心愿路线列表
     */
    @Operation(summary = "查询我的心愿路线列表", description = "分页查询当前用户创建的心愿路线列表")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/my")
    public Result<IPage<WishVO>> queryMyWishList(
            @RequestParam(required = false) @Parameter(description = "页码，默认1") Integer pageNo,
            @RequestParam(required = false) @Parameter(description = "每页大小，默认10") Integer pageSize) {
        // 从上下文获取当前用户的openid
        String openid = UserContext.getOpenId();
        
        IPage<WishVO> page = wishService.queryUserWishList(pageNo, pageSize, openid);
        return Result.success(page);
    }
    
    /**
     * 查询当前用户投票过的心愿路线列表
     */
    @Operation(summary = "查询我投票的心愿路线列表", description = "分页查询当前用户投票过的心愿路线列表")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/voted")
    public Result<IPage<WishVO>> queryMyVotedWishList(
            @RequestParam(required = false) @Parameter(description = "页码，默认1") Integer pageNo,
            @RequestParam(required = false) @Parameter(description = "每页大小，默认10") Integer pageSize) {
        // 从上下文获取当前用户的openid
        String openid = UserContext.getOpenId();
        
        IPage<WishVO> page = wishService.queryUserVotedWishList(pageNo, pageSize, openid);
        return Result.success(page);
    }
    
    /**
     * 投票
     */
    @Operation(summary = "投票", description = "为心愿路线投票")
    @ApiResponses({
        @ApiResponse(code = 200, message = "投票成功", response = Result.class),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 404, message = "心愿路线不存在"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/{id}/vote")
    public Result<Boolean> voteWish(
            @PathVariable @Parameter(description = "心愿路线ID", required = true) Long id) {
        // 从上下文获取当前用户的openid
        String openid = UserContext.getOpenId();
        
        boolean result = wishService.voteWish(id, openid);
        return Result.success(result);
    }
    
    /**
     * 取消投票
     */
    @Operation(summary = "取消投票", description = "取消对心愿路线的投票")
    @ApiResponses({
        @ApiResponse(code = 200, message = "取消成功", response = Result.class),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 404, message = "心愿路线不存在"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @DeleteMapping("/{id}/vote")
    public Result<Boolean> cancelVote(
            @PathVariable @Parameter(description = "心愿路线ID", required = true) Long id) {
        // 从上下文获取当前用户的openid
        String openid = UserContext.getOpenId();
        
        boolean result = wishService.cancelVote(id, openid);
        return Result.success(result);
    }
    
    /**
     * 关闭心愿路线
     */
    @Operation(summary = "关闭心愿路线", description = "关闭自己创建的心愿路线，已关闭的心愿不会在首页展示")
    @ApiResponses({
        @ApiResponse(code = 200, message = "关闭成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 403, message = "无权限操作"),
        @ApiResponse(code = 404, message = "心愿路线不存在"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PutMapping("/{id}/close")
    public Result<Boolean> closeWish(
            @PathVariable @Parameter(description = "心愿路线ID", required = true) Long id) {
        // 从上下文获取当前用户的openid
        String openid = UserContext.getOpenId();
        
        // 调用服务关闭心愿路线
        boolean success = wishService.closeWish(id, openid);
        
        return Result.success(success);
    }
} 