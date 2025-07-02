package com.tour.common.interceptor;

import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.JwtUtil;
import com.tour.common.util.UserContext;
import com.tour.config.WhiteListConfig;
import com.tour.enums.RoleEnum;
import com.tour.model.User;
import com.tour.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 *
 * @Author Abin
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WhiteListConfig whiteListConfig;
    
    @Autowired
    private IUserService userService;

    private final PathMatcher pathMatcher = new AntPathMatcher();
    
    /**
     * 路径类型枚举
     */
    private enum PathType {
        /** 白名单路径 */
        WHITE,
        /** 管理员路径 */
        ADMIN,
        /** 超级管理员路径 */
        SUPER_ADMIN,
        /** 普通路径 */
        NORMAL
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求路径并进行一次性匹配分类
        String requestPath = request.getRequestURI();
        PathType pathType = getPathType(requestPath);
        
        // 如果是白名单路径，直接放行
        if (pathType == PathType.WHITE) {
            return true;
        }

        // 检查是否来自微信云托管环境（请求头中包含openid）
        // 注意：此功能仅在微信云托管环境下且关闭了外网访问时安全有效
        // 在开放外网访问的情况下，攻击者可能伪造请求头，存在安全风险
        String wxOpenId = request.getHeader("x-wx-openid");
        if (wxOpenId == null || wxOpenId.isEmpty()) {
            wxOpenId = request.getHeader("x-wx-from-openid");
        }
        
        // 如果是普通接口（非管理员接口）且存在微信云托管环境的OpenID，直接使用该OpenID
        if (pathType == PathType.NORMAL && wxOpenId != null && !wxOpenId.isEmpty()) {
            log.info("检测到来自微信云托管环境的请求，使用请求头中的OpenID: {}", wxOpenId);
            // 将OpenID存入上下文
            UserContext.setOpenId(wxOpenId);
            // 默认设置为普通用户角色
            UserContext.setRole(RoleEnum.USER.getCode());
            return true;
        }

        // 获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证token
        if (token == null || token.isEmpty()) {
            log.info("请先登录，请求url{}", requestPath);
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "请先登录");
        }

        // 验证token是否有效
        if (!jwtUtil.validateToken(token)) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }

        // 获取openId并存入上下文
        String openId = jwtUtil.getOpenIdFromToken(token);
        if (openId == null) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED, "无效的登录信息");
        }
        UserContext.setOpenId(openId);
        
        // 获取token中的角色
        String tokenRole = jwtUtil.getRoleFromToken(token);
        
        // 如果是管理员或超级管理员接口，进行角色一致性检查
        boolean isAdminAccess = (pathType == PathType.ADMIN || pathType == PathType.SUPER_ADMIN);
        if (isAdminAccess) {
            // 从数据库获取用户最新信息，验证角色是否一致
            User user = userService.getUserByOpenid(openId);
            if (user == null) {
                throw new ServiceException(ErrorCode.UNAUTHORIZED, "用户不存在");
            }
            
            // 验证token中的角色是否与用户当前角色一致
            if (!tokenRole.equals(user.getRole())) {
                log.warn("管理权限接口访问 - 用户 {} 的令牌角色 {} 与数据库角色 {} 不匹配", openId, tokenRole, user.getRole());
                throw new ServiceException(ErrorCode.UNAUTHORIZED, "用户权限已变更，请重新登录");
            }
        }
        
        // 将验证通过的角色存入上下文
        UserContext.setRole(tokenRole);
        
        // 基于路径类型和角色进行权限检查
        if (pathType == PathType.SUPER_ADMIN && !RoleEnum.SUPER_ADMIN.getCode().equals(tokenRole)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权访问该接口，需要超级管理员权限");
        }
        
        if (pathType == PathType.ADMIN && 
            !RoleEnum.ADMIN.getCode().equals(tokenRole) && 
            !RoleEnum.SUPER_ADMIN.getCode().equals(tokenRole)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权访问该接口，需要管理员权限");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理用户上下文
        UserContext.clear();
    }
    
    /**
     * 一次性判断路径类型，避免多次匹配
     * @param requestPath 请求路径
     * @return 路径类型
     */
    private PathType getPathType(String requestPath) {
        // 优先检查白名单路径
        if (whiteListConfig.getUrls().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath))) {
            return PathType.WHITE;
        }
        
        // 检查超级管理员路径
        if (whiteListConfig.getSuperAdminUrls().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath))) {
            return PathType.SUPER_ADMIN;
        }
        
        // 检查管理员路径
        if (whiteListConfig.getAdminUrls().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath))) {
            return PathType.ADMIN;
        }
        
        // 默认为普通路径
        return PathType.NORMAL;
    }
} 