package com.tour.enums;

import lombok.Getter;

/**
 * @Author kuril
 * @Description 商品订单状态枚举
 */
@Getter
public enum ProductOrderStatusEnum {
    
    /**
     * 待提货状态
     */
    PENDING_PICKUP(1, "待提货"),
    
    /**
     * 已完成状态
     */
    COMPLETED(2, "已完成"),
    
    /**
     * 已取消状态
     */
    CANCELED(3, "已取消"),
    
    /**
     * 待支付状态
     */
    NONPAYMENT(4, "待支付");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 状态描述
     */
    private final String desc;
    
    ProductOrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据状态码获取对应的枚举
     * 
     * @param code 状态码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static ProductOrderStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (ProductOrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        
        return null;
    }
    
    /**
     * 判断状态码是否有效
     * 
     * @param code 状态码
     * @return 是否有效
     */
    public static boolean isValidCode(Integer code) {
        return getByCode(code) != null;
    }
} 