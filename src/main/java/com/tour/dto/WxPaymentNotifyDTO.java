package com.tour.dto;

import lombok.Data;

/**
 * 微信支付结果通知回调DTO（云托管环境）
 */
@Data
public class WxPaymentNotifyDTO {
    /**
     * 返回状态码
     */
    private String returnCode;
    
    /**
     * 微信小程序/公众号appid
     */
    private String appid;
    
    /**
     * 微信支付商户号
     */
    private String mchId;
    
    /**
     * 子商户公众号ID
     */
    private String subAppid;
    
    /**
     * 子商户号
     */
    private String subMchId;
    
    /**
     * 随机字符串
     */
    private String nonceStr;
    
    /**
     * 业务结果
     */
    private String resultCode;
    
    /**
     * 用户标识
     */
    private String openid;
    
    /**
     * 是否关注公众账号
     */
    private String isSubscribe;
    
    /**
     * 子商户appid下用户标识
     */
    private String subOpenid;
    
    /**
     * 是否关注子公众号
     */
    private String subIsSubscribe;
    
    /**
     * 交易类型
     */
    private String tradeType;
    
    /**
     * 付款银行
     */
    private String bankType;
    
    /**
     * 订单金额（分）
     */
    private Integer totalFee;
    
    /**
     * 货币种类
     */
    private String feeType;
    
    /**
     * 现金支付金额
     */
    private Integer cashFee;
    
    /**
     * 微信支付订单号
     */
    private String transactionId;
    
    /**
     * 商户订单号
     */
    private String outTradeNo;
    
    /**
     * 支付完成时间
     */
    private String timeEnd;
} 