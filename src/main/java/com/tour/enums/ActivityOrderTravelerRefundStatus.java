package com.tour.enums;

import lombok.Getter;

/**
 * 活动订单出行人退款状态枚举
 * 
 * @author Kuril
 */
@Getter
public enum ActivityOrderTravelerRefundStatus {
    
    /**
     * 可退款
     */
    REFUNDABLE(0, "可免单"),
    
    /**
     * 不可退款
     */
    NON_REFUNDABLE(1, "不可退款");
    
    /**
     * 状态码
     */
    private final Integer code;
    
    /**
     * 状态描述
     */
    private final String desc;
    
    ActivityOrderTravelerRefundStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据状态码获取对应的枚举
     * 
     * @param code 状态码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static ActivityOrderTravelerRefundStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (ActivityOrderTravelerRefundStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        
        return null;
    }
} 