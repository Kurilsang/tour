package com.tour.service;

import com.wechat.pay.java.service.refund.model.Refund;

import java.math.BigDecimal;

/**
 * @Author kuril
 * @Description 微信支付退款服务接口
 */
public interface WxRefundService {
    
    /**
     * 申请退款
     *
     * @param outTradeNo 商户订单号
     * @param reason 退款原因
     * @return 退款结果
     */
    Refund refund(String outTradeNo, String reason);
    
    /**
     * 申请退款（带真实退款原因参数）
     *
     * @param outTradeNo 商户订单号
     * @param reason 退款原因（微信支付平台展示）
     * @param realReason 真实退款原因（存储到数据库）
     * @return 退款结果
     */
    Refund refund(String outTradeNo, String reason, String realReason);
    
    /**
     * 申请退款（带指定金额）
     *
     * @param outTradeNo 商户订单号
     * @param reason 退款原因（微信支付平台展示）
     * @param realReason 真实退款原因（存储到数据库）
     * @param specifiedAmount 指定退款金额（可选，仅适用于活动订单）
     * @return 退款结果
     */
    Refund refund(String outTradeNo, String reason, String realReason, BigDecimal specifiedAmount);
    
    /**
     * 查询退款状态
     *
     * @param outTradeNo 商户订单号
     * @return 退款信息
     */
    Refund queryRefund(String outTradeNo);
    
    /**
     * 从数据库查询退款记录
     * 用于云托管环境下从wx_refund_record表获取退款信息
     *
     * @param outTradeNo 商户订单号
     * @return 退款信息
     */
    Refund queryRefundFromDb(String outTradeNo);
} 