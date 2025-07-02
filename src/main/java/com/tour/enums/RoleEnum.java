/**
 * @Author Abin
 * @Description 系统角色枚举
 */
package com.tour.enums;

public enum RoleEnum {
    /**
     * 超级管理员
     */
    SUPER_ADMIN("super_admin", "超级管理员"),
    
    /**
     * 管理员
     */
    ADMIN("admin", "管理员"),
    
    /**
     * 普通用户
     */
    USER("user", "普通用户");

    /**
     * 角色代码
     */
    private final String code;
    
    /**
     * 角色名称
     */
    private final String name;

    RoleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }
} 