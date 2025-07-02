package com.tour.enums;

/**
 * 心愿路线状态枚举
 * 
 * @Author Abin
 */
public enum WishStatus {
    
    /**
     * 待成团
     */
    PENDING(0, "待成团"),
    
    /**
     * 已成团
     */
    FULFILLED(1, "已成团"),
    
    /**
     * 已关闭
     */
    CLOSED(2, "已关闭");
    
    private final Integer code;
    private final String desc;
    
    WishStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    /**
     * 根据code获取枚举
     */
    public static WishStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (WishStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
} 