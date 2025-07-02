package com.tour.controller;

import com.tour.common.Result;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.service.IWxAuthService;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 *
 * @Author Abin
 */
@Api(tags = "认证接口", description = "用户认证相关接口")
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IWxAuthService wxAuthService;

    /**
     * 微信登录
     */
    @Operation(summary = "微信登录", description = "使用微信小程序登录凭证进行登录")
    @ApiResponses({
        @ApiResponse(code = 200, message = "登录成功", response = Result.class),
        @ApiResponse(code = 400, message = "登录凭证不能为空"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/wxLogin")
    public Result<Map<String, Object>> wxLogin(
            @RequestBody @Parameter(description = "微信登录凭证", required = true, schema = @Schema(implementation = Map.class)) Map<String, String> params) {
        // 参数校验
        String code = params.get("code");
        if (code == null || code.isEmpty()) {
            throw new ServiceException("登录凭证不能为空");
        }
        
        // 调用服务处理登录
        return Result.success(wxAuthService.wxLogin(code));
    }
    
    /**
     * 云托管环境自动检查登录状态，为避免请求头伪造，开放此登录功能时务必关闭外网访问功能
     * 直接从请求头中获取用户信息，无需code换取
     */
    @Operation(summary = "云托管环境检查登录", description = "从云托管环境请求头中获取用户信息并返回token")
    @ApiResponses({
        @ApiResponse(code = 200, message = "登录成功", response = Result.class),
        @ApiResponse(code = 401, message = "未在云托管环境中或缺少OpenID信息"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/check")
    public Result<Map<String, Object>> checkCloudAuth(@RequestHeader Map<String, String> headers) {
        log.info("收到云托管环境登录检查请求，请求头信息: {}", headers);
        
        // 从请求头中获取OpenID和AppID
        String openId = headers.get("x-wx-openid");
        // 资源复用情况下可能用的是from-openid
        if (openId == null || openId.isEmpty()) {
            openId = headers.get("x-wx-from-openid");
        }
        
        String appId = headers.get("x-wx-appid");
        if (appId == null || appId.isEmpty()) {
            appId = headers.get("x-wx-from-appid");
        }
        
        // 获取UnionID（如果有）
        String unionId = headers.get("x-wx-unionid");
        if (unionId == null || unionId.isEmpty()) {
            unionId = headers.get("x-wx-from-unionid");
        }
        
        // 检查是否有必要的用户信息
        if (openId == null || openId.isEmpty() || appId == null || appId.isEmpty()) {
            log.warn("未能从云托管环境获取到用户标识: openId={}, appId={}", openId, appId);
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "未能从云托管环境获取到用户标识");
        }
        
        log.info("从云托管环境获取到用户信息: openId={}, appId={}, unionId={}", openId, appId, unionId);
        
        // 调用服务处理云托管环境下的登录
        return Result.success(wxAuthService.cloudHostingLogin(openId, appId, unionId));
    }
} 