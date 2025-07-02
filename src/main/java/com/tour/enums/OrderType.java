package com.tour.enums;

import lombok.Getter;

/**
 * 订单类型枚举
 *
 * @Author Kuril
 */
@Getter
public enum OrderType {

    ACTIVITY(1, "活动订单"),
    PRODUCT(2, "商品订单");

    private final int code;
    private final String desc;

    OrderType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(int code) {
        for (OrderType type : OrderType.values()) {
            if (type.getCode() == code) {
                return type.getDesc();
            }
        }
        return "";
    }
} 