package com.tour.enums;

import lombok.Getter;

/**
 * 订单退款状态枚举
 *
 * @Author Kuril
 */
@Getter
public enum OrderRefundStatusEnum {

    /**
     * 未申请退款
     */
    NOT_APPLIED(0, "未申请退款"),

    /**
     * 已申请退款
     */
    APPLIED(1, "已申请退款"),
    
    /**
     * 不可退款
     */
    NOT_REFUNDABLE(2, "不可退款");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String desc;

    OrderRefundStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态码获取对应的枚举
     *
     * @param code 状态码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static OrderRefundStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (OrderRefundStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }

        return null;
    }
} 