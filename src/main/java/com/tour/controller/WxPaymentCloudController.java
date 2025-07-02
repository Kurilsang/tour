package com.tour.controller;

import com.tour.dto.WxCloudResponseDTO;
import com.tour.dto.WxPaymentNotifyDTO;
import com.tour.dto.WxRefundNotifyDTO;
import com.tour.service.WxPaymentCloudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信支付云托管回调控制器
 * 处理来自微信云托管的支付和退款回调
 */
@RestController
@RequestMapping("/api/wx/payment/cloud")
@Slf4j
public class WxPaymentCloudController {

    private final WxPaymentCloudService wxPaymentCloudService;

    @Autowired
    public WxPaymentCloudController(WxPaymentCloudService wxPaymentCloudService) {
        this.wxPaymentCloudService = wxPaymentCloudService;
    }

    /**
     * 处理微信支付回调（云托管环境通用）
     * 
     * @param notifyDTO 支付通知DTO
     * @return 处理结果
     */
    @PostMapping("/notify")
    public WxCloudResponseDTO handlePayNotify(@RequestBody WxPaymentNotifyDTO notifyDTO) {
        log.info("接收到微信支付云托管通知");
        return wxPaymentCloudService.handlePayNotify(notifyDTO);
    }
    
    /**
     * 处理微信支付商品订单回调（云托管环境）
     * 
     * @param notifyDTO 支付通知DTO
     * @return 处理结果
     */
    @PostMapping("/notify/product")
    public WxCloudResponseDTO handleProductPayNotify(@RequestBody WxPaymentNotifyDTO notifyDTO) {
        log.info("接收到微信支付商品订单云托管通知");
        return wxPaymentCloudService.handleProductPayNotify(notifyDTO);
    }
    
    /**
     * 处理微信支付活动订单回调（云托管环境）
     * 
     * @param notifyDTO 支付通知DTO
     * @return 处理结果
     */
    @PostMapping("/notify/activity")
    public WxCloudResponseDTO handleActivityPayNotify(@RequestBody WxPaymentNotifyDTO notifyDTO) {
        log.info("接收到微信支付活动订单云托管通知");
        return wxPaymentCloudService.handleActivityPayNotify(notifyDTO);
    }
    
    /**
     * 处理微信退款回调（云托管环境）
     * 
     * @param notifyDTO 退款通知DTO
     * @return 处理结果
     */
    @PostMapping("/refund/notify")
    public WxCloudResponseDTO handleRefundNotify(@RequestBody WxRefundNotifyDTO notifyDTO) {
        log.info("接收到微信退款云托管通知");
        return wxPaymentCloudService.handleRefundNotify(notifyDTO);
    }
} 