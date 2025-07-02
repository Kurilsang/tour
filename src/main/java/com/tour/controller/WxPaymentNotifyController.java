package com.tour.controller;

import com.tour.common.util.WxPaymentUtil;
import com.tour.service.WxPaymentNotifyService;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author kuril
 * @Description 微信支付回调控制器（非云托管环境）
 * 注意：非云托管环境下使用此控制器，云托管环境下请使用WxPaymentCloudController
 */
@RestController
@RequestMapping("/api/wx/payment/notify")
@Slf4j
public class WxPaymentNotifyController {

    private final WxPaymentNotifyService wxPaymentNotifyService;
    private final WxPaymentUtil wxPaymentUtil;

    @Autowired
    public WxPaymentNotifyController(WxPaymentNotifyService wxPaymentNotifyService, WxPaymentUtil wxPaymentUtil) {
        this.wxPaymentNotifyService = wxPaymentNotifyService;
        this.wxPaymentUtil = wxPaymentUtil;
    }

    /**
     * 处理商品订单支付结果通知
     * @param requestBody 通知请求体
     * @param request HTTP请求对象
     * @return 处理结果
     */
    @PostMapping("/product")
    public ResponseEntity<Map<String, String>> handleProductOrderNotify(@RequestBody String requestBody, HttpServletRequest request) {
        log.info("接收到微信支付商品订单通知");
        try {
            // 5秒内完成验签
            long startTime = System.currentTimeMillis();
            
            // 解析和验证通知
            Transaction transaction = wxPaymentUtil.parsePayNotify(requestBody, request);
            
            // 验证商户号
            if (!wxPaymentUtil.verifyNotifyMerchantId(transaction)) {
                log.error("商户号不匹配: {}", transaction.getMchid());
                // 验签失败返回4XX状态码
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(wxPaymentUtil.createFailResponse("商户号不匹配"));
            }
            
            // 处理业务逻辑
            wxPaymentNotifyService.handleProductOrderNotify(requestBody, request);
            
            // 记录处理时间
            long endTime = System.currentTimeMillis();
            log.info("微信支付通知处理耗时: {}ms", (endTime - startTime));
            
            // 验签通过返回200或204状态码，无需返回内容
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (Exception e) {
            log.error("处理微信支付通知异常", e);
            // 验签失败返回500状态码
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(wxPaymentUtil.createFailResponse(e.getMessage()));
        }
    }
    
    /**
     * 处理活动订单支付结果通知
     * @param requestBody 通知请求体
     * @param request HTTP请求对象
     * @return 处理结果
     */
    @PostMapping("/activity")
    public ResponseEntity<Map<String, String>> handleActivityOrderNotify(@RequestBody String requestBody, HttpServletRequest request) {
        log.info("接收到微信支付活动订单通知");
        try {
            // 5秒内完成验签
            long startTime = System.currentTimeMillis();
            
            // 解析和验证通知
            Transaction transaction = wxPaymentUtil.parsePayNotify(requestBody, request);
            
            // 验证商户号
            if (!wxPaymentUtil.verifyNotifyMerchantId(transaction)) {
                log.error("商户号不匹配: {}", transaction.getMchid());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(wxPaymentUtil.createFailResponse("商户号不匹配"));
            }
            
            // 处理业务逻辑
            wxPaymentNotifyService.handleActivityOrderNotify(requestBody, request);
            
            // 记录处理时间
            long endTime = System.currentTimeMillis();
            log.info("微信支付通知处理耗时: {}ms", (endTime - startTime));
            
            // 验签通过返回200或204状态码，无需返回内容
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (Exception e) {
            log.error("处理微信支付通知异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(wxPaymentUtil.createFailResponse(e.getMessage()));
        }
    }
} 