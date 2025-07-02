package com.tour.enums;

import lombok.Getter;

/**
 * 票类型枚举
 * 
 * @author Kuril
 */
@Getter
public enum TickTypeEnum {
    
    /**
     * 早鸟票
     */
    EARLY_BIRD(1, "早鸟票"),
    
    /**
     * 普通票
     */
    NORMAL(2, "普通票");
    
    /**
     * 类型码
     */
    private final Integer code;
    
    /**
     * 类型描述
     */
    private final String desc;
    
    TickTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据类型码获取对应的枚举
     * 
     * @param code 类型码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static TickTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (TickTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        
        return null;
    }
} 