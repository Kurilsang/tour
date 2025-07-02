package com.tour.enums;

import lombok.Getter;

/**
 * @Author kuril
 * @Description 微信支付状态枚举
 */
@Getter
public enum WxPaymentStatusEnum {

    /**
     * 支付成功
     */
    SUCCESS("SUCCESS", "支付成功"),

    /**
     * 转入退款
     */
    REFUND("REFUND", "转入退款"),

    /**
     * 未支付
     */
    NOTPAY("NOTPAY", "未支付"),

    /**
     * 已关闭
     */
    CLOSED("CLOSED", "已关闭"),

    /**
     * 已撤销（仅付款码支付会返回）
     */
    REVOKED("REVOKED", "已撤销"),

    /**
     * 用户支付中（仅付款码支付会返回）
     */
    USERPAYIN("USERPAYIN", "用户支付中"),

    /**
     * 支付失败（仅付款码支付会返回）
     */
    PAYERROR("PAYERROR", "支付失败");

    /**
     * 状态码
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String desc;

    WxPaymentStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态码获取对应的枚举
     *
     * @param code 状态码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static WxPaymentStatusEnum getByCode(String code) {
        if (code == null) {
            return null;
        }

        for (WxPaymentStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }

        return null;
    }
} 