package com.tour.common.enums;

import lombok.Getter;

/**
 * @Author Claude
 * @Description 退款状态枚举
 */
@Getter
public enum RefundStatusEnum {
    
    NOT_REFUNDED(0, "未退款"),
    REFUNDED(1, "已退款");
    
    private final Integer code;
    private final String desc;
    
    RefundStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 对应枚举
     */
    public static RefundStatusEnum getByCode(Integer code) {
        for (RefundStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
} 