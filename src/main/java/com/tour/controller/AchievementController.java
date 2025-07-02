package com.tour.controller;

import com.tour.common.Result;
import com.tour.common.util.UserContext;
import com.tour.service.IAchievementService;
import com.tour.vo.AchievementVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * @Author Abin
 * @Description 成就控制器
 * @DateTime 2025/5/8 15:45
 */
@Slf4j
@Api(tags = "成就管理接口", description = "成就相关的查询接口")
@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    @Autowired
    private IAchievementService achievementService;

    /**
     * 获取所有成就列表
     */
    @ApiOperation(value = "获取所有成就列表", notes = "获取所有成就，并标记当前用户是否已获得")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/list")
    public Result<List<AchievementVO>> getAchievementList() {
        // 从上下文获取用户的openid
        String openid = UserContext.getOpenId();
        // 查询成就列表
        List<AchievementVO> achievements = achievementService.getAchievementList(openid);
        return Result.success(achievements);
    }

    /**
     * 获取用户已获得的成就列表
     */
    @ApiOperation(value = "获取用户已获得的成就", notes = "获取当前用户已获得的所有成就")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/user")
    public Result<List<AchievementVO>> getUserAchievements() {
        // 从上下文获取用户的openid
        String openid = UserContext.getOpenId();
        // 查询用户已获得的成就
        List<AchievementVO> achievements = achievementService.getUserAchievements(openid);
        return Result.success(achievements);
    }

    /**
     * 获取成就详情
     */
    @ApiOperation(value = "获取成就详情", notes = "获取指定成就的详细信息，用于生成海报")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 404, message = "成就不存在"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/{id}/detail")
    public Result<AchievementVO> getAchievementDetail(@PathVariable("id") Long id) {
        // 查询成就详情
        AchievementVO achievement = achievementService.getAchievementById(id, null);
        return Result.success(achievement);
    }
    
    /**
     * 根据活动ID获取成就详情
     */
    @ApiOperation(value = "根据活动ID获取成就详情", notes = "通过活动ID获取关联的成就详情")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 404, message = "成就不存在"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/activity/{activityId}")
    public Result<AchievementVO> getAchievementByActivityId(@PathVariable("activityId") Long activityId) {
        // 从上下文获取用户的openid
        String openid = UserContext.getOpenId();
        // 查询成就详情
        AchievementVO achievement = achievementService.getAchievementByActivityId(activityId, openid);
        return Result.success(achievement);
    }

    /**
     * 扫码签到
     */
    @ApiOperation(value = "扫码签到", notes = "扫描二维码完成签到获取成就")
    @ApiResponses({
            @ApiResponse(code = 200, message = "签到成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 400, message = "无效的签到码"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/sign")
    public Result<AchievementVO> signIn(
            @ApiParam(value = "签到码", required = true) @RequestBody @Parameter(description = "签到码", required = true, schema = @Schema(implementation = Map.class)) Map<String, String> params) {
        String signInCode = params.get("signInCode");
        // 从上下文获取用户的openid
        String openid = UserContext.getOpenId();
        // 执行签到
        AchievementVO achievement = achievementService.signInAchievement(signInCode, openid);
        return Result.success(achievement);
    }


} 