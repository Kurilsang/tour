package com.tour.service;

import com.tour.vo.WxPaymentOrderVO;

/**
 * @Author kuril
 * @Description 微信支付服务接口
 */
public interface WxPaymentService {
    
    /**
     * 通过商户订单号查询订单
     * 
     * @param outTradeNo 商户订单号
     * @return 订单查询结果
     */
    WxPaymentOrderVO queryOrderByOutTradeNo(String outTradeNo);
    
    /**
     * 通过预支付交易会话标识查询订单
     * 
     * @param prepayId 预支付交易会话标识
     * @return 订单查询结果
     */
    WxPaymentOrderVO queryOrderByPrepayId(String prepayId);
    
    /**
     * 关闭订单
     * 
     * @param outTradeNo 商户订单号
     * @return 是否关闭成功
     */
    boolean closeOrder(String outTradeNo);
} 