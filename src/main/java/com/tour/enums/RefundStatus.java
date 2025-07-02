package com.tour.enums;

import lombok.Getter;

/**
 * 退款申请状态枚举
 *
 * @Author Kuril
 */
@Getter
public enum RefundStatus {

    PENDING(1, "待审核"),
    REFUNDING(2, "退款中"),
    COMPLETED(3, "退款完成"),
    REJECTED(4, "拒绝退款"),
    CANCELED(5, "已取消");

    private final int code;
    private final String desc;

    RefundStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(int code) {
        for (RefundStatus status : RefundStatus.values()) {
            if (status.getCode() == code) {
                return status.getDesc();
            }
        }
        return "";
    }
} 