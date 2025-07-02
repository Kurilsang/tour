package com.tour.model;

import lombok.Data;

/**
 * @Author kuril
 * @Description 微信支付请求参数模型
 */
@Data
public class WxPaymentRequest {
    
    /**
     * 支付金额（单位：分）
     */
    private Integer total;
    
    /**
     * 用户openid
     */
    private String openid;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 支付回调通知URL
     */
    private String notifyUrl;
    
    /**
     * 商户订单号
     * 如果为空，系统将自动生成
     */
    private String outTradeNo;
    
    /**
     * 云托管环境ID
     * 用于微信支付云托管回调
     */
    private String envId;
    
    /**
     * 回调类型
     * 1-云函数 2-云托管
     */
    private Integer callbackType;
    
    /**
     * 云托管容器信息
     * 包含service和path属性
     */
    private ContainerInfo container;
    
    /**
     * 子商户号
     */
    private String subMchId;
    
    /**
     * 云托管容器信息
     */
    @Data
    public static class ContainerInfo {
        /**
         * 服务名称
         */
        private String service;
        
        /**
         * 业务路径
         */
        private String path;
    }
} 