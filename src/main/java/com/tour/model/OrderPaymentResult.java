package com.tour.model;

import lombok.Data;

/**
 * @Author kuril
 * @Description 订单支付结果，包含订单信息和微信支付参数
 */
@Data
public class OrderPaymentResult<T> {
    
    /**
     * 订单信息
     */
    private T order;
    
    /**
     * 微信支付参数
     */
    private WxPaymentResponseVO payment;
    
    /**
     * 创建包含订单和支付信息的结果
     * 
     * @param order 订单对象
     * @param payment 支付参数
     * @return 组合结果
     */
    public static <T> OrderPaymentResult<T> of(T order, WxPaymentResponseVO payment) {
        OrderPaymentResult<T> result = new OrderPaymentResult<>();
        result.setOrder(order);
        result.setPayment(payment);
        return result;
    }
} 