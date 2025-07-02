package com.tour.service.impl;

import com.tour.common.util.WxPaymentUtil;
import com.tour.dto.EnrollmentDTO;
import com.tour.model.Traveler;
import com.tour.service.ProductOrderService;
import com.tour.service.WxPaymentNotifyService;
import com.tour.vo.ActivityOrderDetail;
import com.tour.vo.TravelerOrderVO;
import com.tour.vo.TravelerVO;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author kuril
 * @Description 微信支付回调服务实现类
 */
@Service("wxPaymentNotifyService")
@Slf4j
public class WxPaymentNotifyServiceImpl implements WxPaymentNotifyService {

    private final WxPaymentUtil wxPaymentUtil;
    private final ProductOrderService productOrderService;
    private final ActivityOrderServiceImpl activityOrderService;
    private final SignUpServiceImpl signUpService;

    @Value("${wechat.pay.merchant-id}")
    private String merchantId;

    @Autowired
    public WxPaymentNotifyServiceImpl(WxPaymentUtil wxPaymentUtil,
                                      ProductOrderService productOrderService, ActivityOrderServiceImpl activityOrderService, SignUpServiceImpl signUpService) {
        this.wxPaymentUtil = wxPaymentUtil;
        this.productOrderService = productOrderService;
        this.activityOrderService = activityOrderService;
        this.signUpService = signUpService;
    }

    /**
     * 处理商品订单支付结果通知
     *
     * @param requestBody 通知请求体
     * @param request HTTP请求对象，用于获取微信支付请求头
     * @return 处理结果，返回给微信支付平台
     */
    @Override
    public Map<String, String> handleProductOrderNotify(String requestBody, HttpServletRequest request) {
        log.info("开始处理微信支付商品订单通知，包体{}",requestBody);
        
        try {
            // 解析和验证通知
            Transaction transaction = wxPaymentUtil.parsePayNotify(requestBody, request);
            String outTradeNo = transaction.getOutTradeNo();
            
            log.info("微信支付商品订单通知，商户订单号: {}, 支付状态: {}", outTradeNo, transaction.getTradeState());
            
            // 验证商户号
            if (!wxPaymentUtil.verifyNotifyMerchantId(transaction)) {
                log.error("商户号不匹配: {} vs {}", merchantId, transaction.getMchid());
                return wxPaymentUtil.createFailResponse("商户号不匹配");
            }
            
            // 处理支付结果
            if ("SUCCESS".equals(transaction.getTradeState().name())) {
                try {
                    // 更新订单状态为已支付

                    productOrderService.payOrder(outTradeNo, transaction.getPayer().getOpenid());
                    log.info("商品订单支付成功处理完成: {}", outTradeNo);
                } catch (Exception e) {
                    log.error("处理商品订单支付成功通知异常: {}", e.getMessage(), e);
                    // 即使处理失败，也要返回成功，避免微信重复通知
                }
            } else {
                log.warn("商品订单支付未成功，状态: {}", transaction.getTradeState());
            }
            
            // 返回成功
            return wxPaymentUtil.createSuccessResponse();
        } catch (Exception e) {
            log.error("处理微信支付商品订单通知异常: {}", e.getMessage(), e);
            return wxPaymentUtil.createFailResponse(e.getMessage());
        }
    }

    /**
     * 处理活动订单支付结果通知
     *
     * @param requestBody 通知请求体
     * @param request HTTP请求对象，用于获取微信支付请求头
     * @return 处理结果，返回给微信支付平台
     */
    @Override
    public Map<String, String> handleActivityOrderNotify(String requestBody, HttpServletRequest request) {
        log.info("开始处理微信支付活动订单通知");
        
        try {
            // 解析和验证通知
            Transaction transaction = wxPaymentUtil.parsePayNotify(requestBody, request);
            String outTradeNo = transaction.getOutTradeNo();
            String openid = transaction.getPayer().getOpenid();
            
            log.info("微信支付活动订单通知，商户订单号: {}, 支付状态: {}", outTradeNo, transaction.getTradeState());
            
            // 验证商户号
            if (!wxPaymentUtil.verifyNotifyMerchantId(transaction)) {
                log.error("商户号不匹配: {} vs {}", merchantId, transaction.getMchid());
                return wxPaymentUtil.createFailResponse("商户号不匹配");
            }

            // 处理支付结果
            if ("SUCCESS".equals(transaction.getTradeState().name())) {
                try {
                    // 这里应当调用活动订单相关的服务
//                    获取DTO对应的内容
                    EnrollmentDTO enrollmentDTO = new EnrollmentDTO();
                    ActivityOrderDetail activityOrderDetail = activityOrderService.findActivityOrderAndBusLocationByOrderNo(outTradeNo);
//                    获取活动id
                    enrollmentDTO.setActivityId(activityOrderDetail.getActivityOrder().getActivityId());
//                    转化出行人列表
                    List<TravelerOrderVO> travelerList = activityOrderDetail.getTravelerNameList();
                    List<TravelerVO> travelerVOS = new ArrayList<>();
                    for(TravelerOrderVO traveler:travelerList)
                    {
                        TravelerVO travelerVO = new TravelerVO();
                        travelerVO.setId(String.valueOf(traveler.getId()));

                        travelerVOS.add(travelerVO);
                    }
                    enrollmentDTO.setTravelers(travelerVOS);

                    enrollmentDTO.setUserId(openid);
                    enrollmentDTO.setPrice(activityOrderDetail.getActivityOrder().getTotalAmount());
//                    参数设置完成调用支付方法
                    signUpService.payOrder(outTradeNo, openid,enrollmentDTO);
                    log.info("用户{}的活动订单支付成功: {}，等待活动订单服务实现",openid ,outTradeNo);
                } catch (Exception e) {
                    log.error("处理活动订单支付成功通知异常: {}", e.getMessage(), e);
                    // 即使处理失败，也要返回成功，避免微信重复通知
                }
            } else {
                log.warn("活动订单支付未成功，状态: {}", transaction.getTradeState());
            }
            
            // 返回成功
            return wxPaymentUtil.createSuccessResponse();
        } catch (Exception e) {
            log.error("处理微信支付活动订单通知异常: {}", e.getMessage(), e);
            return wxPaymentUtil.createFailResponse(e.getMessage());
        }
    }
} 