package com.tour.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author kuril
 * @Description 微信支付回调服务接口
 */
public interface WxPaymentNotifyService {
    
    /**
     * 处理商品订单支付结果通知
     *
     * @param requestBody 通知请求体
     * @param request HTTP请求对象，用于获取微信支付请求头
     * @return 处理结果，返回给微信支付平台
     */
    Map<String, String> handleProductOrderNotify(String requestBody, HttpServletRequest request);
    
    /**
     * 处理活动订单支付结果通知
     *
     * @param requestBody 通知请求体
     * @param request HTTP请求对象，用于获取微信支付请求头
     * @return 处理结果，返回给微信支付平台
     */
    Map<String, String> handleActivityOrderNotify(String requestBody, HttpServletRequest request);
} 