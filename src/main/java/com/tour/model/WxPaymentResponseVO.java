package com.tour.model;

import lombok.Data;

/**
 * @Author kuril
 * @Description 微信支付响应数据模型
 */
@Data
public class WxPaymentResponseVO {
    
    /**
     * 时间戳
     */
    private String timeStamp;
    
    /**
     * 随机字符串
     */
    private String nonceStr;
    
    /**
     * 订单详情扩展字符串
     * 注意：由于package是Java关键字，这里使用packageStr
     */
    private String packageStr;
    
    /**
     * 签名方式
     */
    private String signType;
    
    /**
     * 签名
     */
    private String paySign;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 预支付交易会话标识
     */
    private String prepayId;
} 