package com.tour.dto;

import lombok.Data;

/**
 * 微信退款结果通知DTO（云托管环境）
 */
@Data
public class WxRefundNotifyDTO {
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
     * 微信支付订单号
     */
    private String transactionId;
    
    /**
     * 商户订单号
     */
    private String outTradeNo;
    
    /**
     * 微信退款单号
     */
    private String refundId;
    
    /**
     * 商户退款单号
     */
    private String outRefundNo;
    
    /**
     * 订单金额（分）
     */
    private Integer totalFee;
    
    /**
     * 应结订单金额
     */
    private Integer settlementTotalFee;
    
    /**
     * 申请退款金额
     */
    private Integer refundFee;
    
    /**
     * 退款金额
     */
    private Integer settlementRefundFee;
    
    /**
     * 退款状态
     */
    private String refundStatus;
    
    /**
     * 退款成功时间
     */
    private String successTime;
    
    /**
     * 退款入账账户
     */
    private String refundRecvAccout;
    
    /**
     * 退款资金来源
     */
    private String refundAccount;
    
    /**
     * 退款发起来源
     */
    private String refundRequestSource;
} 