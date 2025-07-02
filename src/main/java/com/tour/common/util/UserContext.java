package com.tour.common.util;

/**
 * 用户上下文
 *
 * @Author Abin
 */
public class UserContext {
    
    private static final ThreadLocal<String> openidHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();
    
    /**
     * 设置用户openid
     */
    public static void setOpenId(String openid) {
        openidHolder.set(openid);
    }
    
    /**
     * 获取用户openid
     */
    public static String getOpenId() {
        return openidHolder.get();
    }
    
    /**
     * 设置用户角色
     */
    public static void setRole(String role) {
        roleHolder.set(role);
    }
    
    /**
     * 获取用户角色
     */
    public static String getRole() {
        return roleHolder.get();
    }
    
    /**
     * 判断当前用户是否是管理员
     */
    public static boolean isAdmin() {
        String role = getRole();
        return "admin".equals(role);
    }
    
    /**
     * 清理上下文
     */
    public static void clear() {
        openidHolder.remove();
        roleHolder.remove();
    }
} 