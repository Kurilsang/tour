package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tour.common.exception.ServiceException;
import com.tour.dao.WxRefundRecordMapper;
import com.tour.dto.EnrollmentDTO;
import com.tour.dto.WxCloudResponseDTO;
import com.tour.dto.WxPaymentNotifyDTO;
import com.tour.dto.WxRefundNotifyDTO;
import com.tour.model.Traveler;
import com.tour.model.WxRefundRecord;
import com.tour.service.ProductOrderService;
import com.tour.service.SignUpService;
import com.tour.service.WxPaymentCloudService;
import com.tour.vo.ActivityOrderDetail;
import com.tour.vo.TravelerVO;
import com.tour.vo.TravelerOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 微信支付云托管回调服务实现类
 */
@Service
@Slf4j
public class WxPaymentCloudServiceImpl implements WxPaymentCloudService {

    @Value("${wechat.pay.merchant-id}")
    private String merchantId;

    private final ProductOrderService productOrderService;
    private final ActivityOrderServiceImpl activityOrderService;
    private final SignUpService signUpService;
    private final WxRefundRecordMapper wxRefundRecordMapper;

    @Autowired
    public WxPaymentCloudServiceImpl(ProductOrderService productOrderService,
                                     ActivityOrderServiceImpl activityOrderService,
                                     SignUpService signUpService,
                                     WxRefundRecordMapper wxRefundRecordMapper) {
        this.productOrderService = productOrderService;
        this.activityOrderService = activityOrderService;
        this.signUpService = signUpService;
        this.wxRefundRecordMapper = wxRefundRecordMapper;
    }

    /**
     * 处理微信支付结果通知（云托管环境）
     * 通用处理方法，根据订单号前缀判断是商品订单还是活动订单
     *
     * @param notifyDTO 支付通知对象
     * @return 处理结果
     */
    @Override
    @Transactional
    public WxCloudResponseDTO handlePayNotify(WxPaymentNotifyDTO notifyDTO) {
        try {
            log.info("收到微信支付云托管通知: {}", notifyDTO);
            
            // 验证必要参数
            if (notifyDTO == null || notifyDTO.getOutTradeNo() == null) {
                return WxCloudResponseDTO.error("缺少必要参数");
            }
            
            // 校验商户ID
            if (!merchantId.equals(notifyDTO.getMchId()) && !merchantId.equals(notifyDTO.getSubMchId())) {
                log.error("商户号不匹配: 期望 {} 但收到 {} 或 {}", 
                        merchantId, notifyDTO.getMchId(), notifyDTO.getSubMchId());
                return WxCloudResponseDTO.error("商户号不匹配");
            }
            
            // 检查支付状态
            if (!"SUCCESS".equals(notifyDTO.getReturnCode()) || !"SUCCESS".equals(notifyDTO.getResultCode())) {
                log.warn("支付未成功: returnCode={}, resultCode={}", 
                        notifyDTO.getReturnCode(), notifyDTO.getResultCode());
                return WxCloudResponseDTO.success(); // 仍返回成功，避免微信重试
            }
            
            // 根据订单号前缀分发到不同的处理方法
            String orderNo = notifyDTO.getOutTradeNo();
            if (orderNo.startsWith("PO")) { // 商品订单前缀
                return handleProductPayNotify(notifyDTO);
            } else { // 活动订单
                return handleActivityPayNotify(notifyDTO);
            }
        } catch (Exception e) {
            log.error("处理微信支付通知异常", e);
            return WxCloudResponseDTO.error("处理异常: " + e.getMessage());
        }
    }

    /**
     * 处理微信支付产品订单结果通知（云托管环境）
     *
     * @param notifyDTO 支付通知对象
     * @return 处理结果
     */
    @Override
    @Transactional
    public WxCloudResponseDTO handleProductPayNotify(WxPaymentNotifyDTO notifyDTO) {
        try {
            log.info("处理商品订单支付通知：{}", notifyDTO.getOutTradeNo());
            
            // 查询订单
            String outTradeNo = notifyDTO.getOutTradeNo();
            String openid = notifyDTO.getOpenid();
            String subOpenid = notifyDTO.getSubOpenid();
            log.info("outTradeNo:{},openid:{},subOpenid:{}",outTradeNo,openid,subOpenid);
            // 处理支付结果
            if ("SUCCESS".equals(notifyDTO.getResultCode())) {
                try {
                    // 更新订单状态为已支付
                    productOrderService.payOrder(outTradeNo, subOpenid);
                    log.info("商品订单支付成功处理完成: {}", outTradeNo);
                } catch (Exception e) {
                    log.error("处理商品订单支付成功通知异常: {}", e.getMessage(), e);
                }
            } else {
                log.warn("商品订单支付未成功，状态: {}", notifyDTO.getResultCode());
            }
            
            // 无论处理是否成功，都返回成功，防止微信重试
            return WxCloudResponseDTO.success();
        } catch (Exception e) {
            log.error("处理商品订单支付通知异常", e);
            // 即使发生异常也返回成功，避免微信重复通知
            return WxCloudResponseDTO.success();
        }
    }

    /**
     * 处理微信支付活动订单结果通知（云托管环境）
     *
     * @param notifyDTO 支付通知对象
     * @return 处理结果
     */
    @Override
    @Transactional
    public WxCloudResponseDTO handleActivityPayNotify(WxPaymentNotifyDTO notifyDTO) {
        try {
            log.info("处理活动订单支付通知：{}", notifyDTO.getOutTradeNo());
            
            String outTradeNo = notifyDTO.getOutTradeNo();
            String openid = notifyDTO.getSubOpenid();
            
            // 处理支付结果
            if ("SUCCESS".equals(notifyDTO.getResultCode())) {
                try {
                    // 获取订单详情
                    ActivityOrderDetail activityOrderDetail = activityOrderService.findActivityOrderAndBusLocationByOrderNo(outTradeNo);
                    
                    // 构建报名DTO
                    EnrollmentDTO enrollmentDTO = new EnrollmentDTO();
                    
                    // 设置活动ID
                    enrollmentDTO.setActivityId(activityOrderDetail.getActivityOrder().getActivityId());
                    
                    // 转化出行人列表
                    List<TravelerOrderVO> travelerList = activityOrderDetail.getTravelerNameList();
                    List<TravelerVO> travelerVOS = new ArrayList<>();
                    for (TravelerOrderVO traveler : travelerList) {
                        TravelerVO travelerVO = new TravelerVO();
                        travelerVO.setId(String.valueOf(traveler.getId()));
                        travelerVOS.add(travelerVO);
                    }
                    enrollmentDTO.setTravelers(travelerVOS);
                    
                    // 设置用户ID和价格
                    enrollmentDTO.setUserId(openid);
                    enrollmentDTO.setPrice(activityOrderDetail.getActivityOrder().getTotalAmount());
                    
                    // 调用支付订单服务
                    signUpService.payOrder(outTradeNo, openid, enrollmentDTO);
                    log.info("活动订单支付成功处理完成: {}", outTradeNo);
                } catch (Exception e) {
                    log.error("处理活动订单支付成功通知异常: {}", e.getMessage(), e);
                }
            } else {
                log.warn("活动订单支付未成功，状态: {}", notifyDTO.getResultCode());
            }
            
            // 无论处理是否成功，都返回成功，防止微信重试
            return WxCloudResponseDTO.success();
        } catch (Exception e) {
            log.error("处理活动订单支付通知异常", e);
            // 即使发生异常也返回成功，避免微信重复通知
            return WxCloudResponseDTO.success();
        }
    }

    /**
     * 处理微信退款结果通知（云托管环境）
     *
     * @param notifyDTO 退款通知对象
     * @return 处理结果
     */
    @Override
    @Transactional
    public WxCloudResponseDTO handleRefundNotify(WxRefundNotifyDTO notifyDTO) {
        try {
            log.info("处理退款结果通知：{}", notifyDTO);
            
            // 校验商户ID
            if (!merchantId.equals(notifyDTO.getMchId()) && !merchantId.equals(notifyDTO.getSubMchId())) {
                log.error("商户号不匹配: 期望 {} 但收到 {} 或 {}", 
                        merchantId, notifyDTO.getMchId(), notifyDTO.getSubMchId());
                return WxCloudResponseDTO.error("商户号不匹配");
            }
            
            // 检查退款状态
            if (!"SUCCESS".equals(notifyDTO.getReturnCode())) {
                log.warn("退款通知状态不成功: {}", notifyDTO.getReturnCode());
                return WxCloudResponseDTO.success(); // 仍返回成功，避免微信重试
            }
            
            // 更新退款记录状态
            String outRefundNo = notifyDTO.getOutRefundNo();
            String refundStatus = notifyDTO.getRefundStatus();
            
            // 查询退款记录
            QueryWrapper<WxRefundRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("out_refund_no", outRefundNo);
            WxRefundRecord refundRecord = wxRefundRecordMapper.selectOne(queryWrapper);
            
            if (refundRecord != null) {
                // 更新退款状态
                refundRecord.setRefundStatus(refundStatus);
                refundRecord.setRefundId(notifyDTO.getRefundId());
                
                // 如果退款成功，记录退款成功时间
                if ("SUCCESS".equals(refundStatus) && notifyDTO.getSuccessTime() != null) {
                    try {
                        // 解析退款成功时间
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime successTime = LocalDateTime.parse(notifyDTO.getSuccessTime(), formatter);
                        refundRecord.setSuccessTime(successTime);
                    } catch (Exception e) {
                        log.warn("解析退款成功时间失败: {}", notifyDTO.getSuccessTime());
                        refundRecord.setSuccessTime(LocalDateTime.now());
                    }
                }
                
                refundRecord.setUpdateTime(LocalDateTime.now());
                wxRefundRecordMapper.updateById(refundRecord);
                
                log.info("已更新退款记录状态: {}, 状态: {}", outRefundNo, refundStatus);
            } else {
                log.warn("未找到对应的退款记录: {}", outRefundNo);
                
                // 尝试创建新的退款记录
                WxRefundRecord newRecord = new WxRefundRecord();
                newRecord.setOutRefundNo(outRefundNo);
                newRecord.setOutTradeNo(notifyDTO.getOutTradeNo());
                newRecord.setRefundId(notifyDTO.getRefundId());
                newRecord.setRefundStatus(refundStatus);
                
                // 金额单位转换：分 -> 元
                BigDecimal totalAmount = new BigDecimal(notifyDTO.getTotalFee()).divide(new BigDecimal(100));
                BigDecimal refundAmount = new BigDecimal(notifyDTO.getRefundFee()).divide(new BigDecimal(100));
                
                newRecord.setTotalAmount(totalAmount);
                newRecord.setRefundAmount(refundAmount);
                
                // 如果退款成功，记录退款成功时间
                if ("SUCCESS".equals(refundStatus) && notifyDTO.getSuccessTime() != null) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime successTime = LocalDateTime.parse(notifyDTO.getSuccessTime(), formatter);
                        newRecord.setSuccessTime(successTime);
                    } catch (Exception e) {
                        log.warn("解析退款成功时间失败: {}", notifyDTO.getSuccessTime());
                        newRecord.setSuccessTime(LocalDateTime.now());
                    }
                }
                
                newRecord.setCreateTime(LocalDateTime.now());
                newRecord.setUpdateTime(LocalDateTime.now());
                
                wxRefundRecordMapper.insert(newRecord);
                log.info("已创建新的退款记录: {}", outRefundNo);
            }
            
            return WxCloudResponseDTO.success();
        } catch (Exception e) {
            log.error("处理退款通知异常", e);
            // 即使发生异常也返回成功，避免微信重复通知
            return WxCloudResponseDTO.success();
        }
    }
} 