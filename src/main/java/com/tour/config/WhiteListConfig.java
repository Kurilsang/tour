package com.tour.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 白名单配置
 *
 * @Author Abin
 */
@Component
@ConfigurationProperties(prefix = "security.white-list")
public class WhiteListConfig {
    
    /**
     * 白名单路径
     */
    private List<String> urls = new ArrayList<>();
    
    /**
     * 仅管理员可访问的接口
     */
    private List<String> adminUrls = new ArrayList<>();
    
    /**
     * 仅超级管理员可访问的接口
     */
    private List<String> superAdminUrls = new ArrayList<>();

    /**
     * 默认白名单
     */
    private static final List<String> DEFAULT_WHITE_LIST = Arrays.asList(
            "/api/auth/wxLogin",    // 微信登录
            "/api/common/**",       // 公共接口
            "/error" ,               // 错误页面
            "/api/",
            "/api/count",
            "/api/wx/message/receive",  // 微信消息推送接收接口
            "/api/wx/payment/notify/**",
            "/api/wx/payment/cloud/refund/notify"
    );
    
    /**
     * 默认管理员接口
     */
    private static final List<String> DEFAULT_ADMIN_URLS = Arrays.asList(
            "/api/admin/**"        // 管理员接口
    );
    
    /**
     * 默认超级管理员接口
     */
    private static final List<String> DEFAULT_SUPER_ADMIN_URLS = Arrays.asList(
            "/api/super/**",        // 超级管理员接口
            "/api/admin/user/role"  // 用户角色调整接口
    );

    public WhiteListConfig() {
        // 添加默认白名单
        urls.addAll(DEFAULT_WHITE_LIST);
        // 添加默认管理员接口
        adminUrls.addAll(DEFAULT_ADMIN_URLS);
        // 添加默认超级管理员接口
        superAdminUrls.addAll(DEFAULT_SUPER_ADMIN_URLS);
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
    
    public List<String> getAdminUrls() {
        return adminUrls;
    }
    
    public void setAdminUrls(List<String> adminUrls) {
        this.adminUrls = adminUrls;
    }
    
    public List<String> getSuperAdminUrls() {
        return superAdminUrls;
    }
    
    public void setSuperAdminUrls(List<String> superAdminUrls) {
        this.superAdminUrls = superAdminUrls;
    }
} 