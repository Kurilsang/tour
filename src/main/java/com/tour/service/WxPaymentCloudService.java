package com.tour.service;

import com.tour.dto.WxCloudResponseDTO;
import com.tour.dto.WxPaymentNotifyDTO;
import com.tour.dto.WxRefundNotifyDTO;

/**
 * 微信支付云托管回调服务接口
 */
public interface WxPaymentCloudService {
    /**
     * 处理微信支付结果通知（云托管环境）
     * 
     * @param notifyDTO 支付通知对象
     * @return 处理结果
     */
    WxCloudResponseDTO handlePayNotify(WxPaymentNotifyDTO notifyDTO);
    
    /**
     * 处理微信支付产品订单结果通知（云托管环境）
     * 
     * @param notifyDTO 支付通知对象
     * @return 处理结果
     */
    WxCloudResponseDTO handleProductPayNotify(WxPaymentNotifyDTO notifyDTO);
    
    /**
     * 处理微信支付活动订单结果通知（云托管环境）
     * 
     * @param notifyDTO 支付通知对象
     * @return 处理结果
     */
    WxCloudResponseDTO handleActivityPayNotify(WxPaymentNotifyDTO notifyDTO);
    
    /**
     * 处理微信退款结果通知（云托管环境）
     * 
     * @param notifyDTO 退款通知对象
     * @return 处理结果
     */
    WxCloudResponseDTO handleRefundNotify(WxRefundNotifyDTO notifyDTO);
} 