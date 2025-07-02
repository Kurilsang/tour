package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.UserContext;
import com.tour.common.util.WxRefundUtil;
import com.tour.dao.ActivityOrderMapper;
import com.tour.dao.ProductOrderMapper;
import com.tour.dao.WxRefundRecordMapper;
import com.tour.enums.RoleEnum;
import com.tour.model.ActivityOrder;
import com.tour.model.ProductOrder;
import com.tour.model.WxRefundRecord;
import com.tour.service.WxRefundService;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author kuril
 * @Description 微信支付退款服务实现类
 */
@Service
@Slf4j
public class WxRefundServiceImpl implements WxRefundService {

    private final WxRefundUtil wxRefundUtil;
    private final ProductOrderMapper productOrderMapper;
    private final ActivityOrderMapper activityOrderMapper;
    private final WxRefundRecordMapper wxRefundRecordMapper;
    
    @Value("${spring.profiles.active}")
    private String activeProfile;
    
    @Value("${wechat.pay.cloud.env-id:}")
    private String cloudEnvId;
    
    @Value("${wechat.pay.cloud.container.service:}")
    private String cloudContainerService;
    
    @Value("${wechat.pay.cloud.container.path:}")
    private String cloudContainerPath;
    
    @Value("${wechat.pay.cloud.container.refund-path:}")
    private String cloudRefundPath;

    public WxRefundServiceImpl(WxRefundUtil wxRefundUtil, 
                             ProductOrderMapper productOrderMapper,
                             ActivityOrderMapper activityOrderMapper,
                             WxRefundRecordMapper wxRefundRecordMapper) {
        this.wxRefundUtil = wxRefundUtil;
        this.productOrderMapper = productOrderMapper;
        this.activityOrderMapper = activityOrderMapper;
        this.wxRefundRecordMapper = wxRefundRecordMapper;
    }

    /**
     * 申请退款
     *
     * @param outTradeNo 商户订单号
     * @param reason 退款原因
     * @return 退款结果
     */
    @Override
    @Transactional
    public Refund refund(String outTradeNo, String reason) {
        // 调用带realReason参数的方法，默认realReason与reason相同
        return refund(outTradeNo, reason, reason);
    }
    
    /**
     * 申请退款
     *
     * @param outTradeNo 商户订单号
     * @param reason 退款原因（微信支付平台展示）
     * @param realReason 真实退款原因（存储到数据库）
     * @return 退款结果
     */
    @Override
    @Transactional
    public Refund refund(String outTradeNo, String reason, String realReason) {
        return refund(outTradeNo, reason, realReason, null);
    }
    
    /**
     * 申请退款（带指定金额）
     *
     * @param outTradeNo 商户订单号
     * @param reason 退款原因（微信支付平台展示）
     * @param realReason 真实退款原因（存储到数据库）
     * @param specifiedAmount 指定退款金额（可选，仅适用于活动订单）
     * @return 退款结果
     */
    @Override
    @Transactional
    public Refund refund(String outTradeNo, String reason, String realReason, BigDecimal specifiedAmount) {
        String role = UserContext.getRole();
        if (outTradeNo == null || outTradeNo.isEmpty()) {
            throw new ServiceException("商户订单号不能为空");
        }
        
        try {
            // 查询订单金额信息
            BigDecimal totalAmount = findOrderAmount(outTradeNo);
            log.info("微信退款，金额{}",totalAmount);
            if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("未找到订单或订单金额无效");
            }
            
            // 查询当前最新的订单金额作为退款金额
            BigDecimal refundAmount = findCurrentOrderAmount(outTradeNo, specifiedAmount);
            log.info("当前订单最新金额（退款金额）：{}", refundAmount);
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("未找到订单最新金额或金额无效");
            }
            
            log.info("订单金额查询成功，订单号: {}, 原始总金额: {}, 退款金额: {}", 
                    outTradeNo, totalAmount, refundAmount);
            
            // 生成商户退款单号
            String outRefundNo = wxRefundUtil.generateOutRefundNo();
            
            // 金额单位转换：元 -> 分
            int totalAmountFen = totalAmount.multiply(new BigDecimal(100)).intValue();
            int refundAmountFen = refundAmount.multiply(new BigDecimal(100)).intValue();
            
            // 调用退款工具执行退款
            String wxRefundReason = reason; // 传递给微信支付平台的退款原因
            
            // 若为用户角色，使用固定退款理由给微信支付平台
            if(role.equals(RoleEnum.USER.getCode())) {
                wxRefundReason = "用户取消订单";
            }
            
            // 根据环境决定使用何种方式申请退款
            Refund refund;
            if ("prod".equals(activeProfile)) {
                // 生产环境：使用云托管退款API
                log.info("使用微信云托管环境申请退款，环境ID: {}", cloudEnvId);
                refund = wxRefundUtil.createRefundWithCloudEnv(
                        outTradeNo, 
                        outRefundNo, 
                        totalAmountFen, 
                        refundAmountFen, 
                        wxRefundReason,
                        cloudContainerService,
                        cloudRefundPath
                );
            } else {
                // 开发环境：使用原有的退款方式
                log.info("使用原生SDK申请退款");
                refund = wxRefundUtil.createRefund(
                        outTradeNo, 
                        outRefundNo, 
                        totalAmountFen, 
                        refundAmountFen, 
                        wxRefundReason
                );
            }
            
            // 保存退款记录
            WxRefundRecord refundRecord = new WxRefundRecord();
            refundRecord.setOutTradeNo(outTradeNo);
            refundRecord.setOutRefundNo(outRefundNo);
            refundRecord.setRefundId(refund.getRefundId());
            refundRecord.setTotalAmount(totalAmount);
            refundRecord.setRefundAmount(refundAmount);
            refundRecord.setRefundStatus(refund.getStatus().toString());
            
            // 保存真实退款原因到数据库
            refundRecord.setReason(realReason);
            
            refundRecord.setUpdateTime(LocalDateTime.now());
            refundRecord.setCreateTime(LocalDateTime.now());
            
            // 微信支付退款成功时间可能为null
            if (refund.getSuccessTime() != null) {
                // 将String类型转换为LocalDateTime
                refundRecord.setSuccessTime(LocalDateTime.now());
            }
            
            wxRefundRecordMapper.insert(refundRecord);
            log.info("退款记录保存成功, 商户退款单号: {}", outRefundNo);
            
            return refund;
        } catch (ServiceException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            log.error("申请退款失败: {}", e.getMessage());
            throw new ServiceException("申请退款失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询订单金额
     * 先查询商品订单表，如果没有再查询活动订单表
     * 如果有历史退款记录，使用最早记录中的总金额
     *
     * @param orderNo 订单号
     * @return 订单金额
     */
    private BigDecimal findOrderAmount(String orderNo) {
        // 查询是否存在退款记录
        try {
            // 查询该订单的所有退款记录并按创建时间升序排序
            List<WxRefundRecord> refundRecords = wxRefundRecordMapper.selectList(
                    new LambdaQueryWrapper<WxRefundRecord>()
                            .eq(WxRefundRecord::getOutTradeNo, orderNo)
                            .orderByAsc(WxRefundRecord::getCreateTime)
            );
            
            // 如果有退款记录，使用最早记录的总金额作为原始总金额
            if (refundRecords != null && !refundRecords.isEmpty()) {
                WxRefundRecord firstRecord = refundRecords.get(0);
                BigDecimal originalTotalAmount = firstRecord.getTotalAmount();
                log.info("从退款记录中获取到原始订单总金额: {}, 订单号: {}", originalTotalAmount, orderNo);
                return originalTotalAmount;
            }
        } catch (Exception e) {
            log.warn("查询原始订单总金额异常: {}", e.getMessage());
            // 如果查询退款记录失败，继续从订单表查询
        }
        
        // 查询商品订单
        QueryWrapper<ProductOrder> productOrderQueryWrapper = new QueryWrapper<>();
        productOrderQueryWrapper.eq("order_no", orderNo);
        ProductOrder productOrder = productOrderMapper.selectOne(productOrderQueryWrapper);
        
        if (productOrder != null) {
            log.info("找到商品订单, 订单号: {}, 金额: {}", orderNo, productOrder.getTotalAmount());
            return productOrder.getTotalAmount();
        }
        
        // 查询活动订单
        QueryWrapper<ActivityOrder> activityOrderQueryWrapper = new QueryWrapper<>();
        activityOrderQueryWrapper.eq("order_no", orderNo);
        ActivityOrder activityOrder = activityOrderMapper.selectOne(activityOrderQueryWrapper);
        
        if (activityOrder != null) {
            log.info("找到活动订单, 订单号: {}, 金额: {}", orderNo, activityOrder.getTotalAmount());
            return activityOrder.getTotalAmount();
        }
        
        log.error("未找到订单, 订单号: {}", orderNo);
        throw new ServiceException("未找到订单: " + orderNo);
    }

    /**
     * 查询订单当前最新金额
     * 直接从订单表获取最新的金额，不考虑历史退款记录
     *
     * @param orderNo 订单号
     * @return 当前订单金额
     */
    private BigDecimal findCurrentOrderAmount(String orderNo) {
        return findCurrentOrderAmount(orderNo, null);
    }
    
    /**
     * 查询订单当前最新金额
     * 直接从订单表获取最新的金额，不考虑历史退款记录
     * 如果指定了退款金额，则直接返回指定金额（仅适用于活动订单）
     *
     * @param orderNo 订单号
     * @param specifiedAmount 指定退款金额（可选，仅适用于活动订单）
     * @return 当前订单金额
     */
    private BigDecimal findCurrentOrderAmount(String orderNo, BigDecimal specifiedAmount) {
        // 先查询订单实际金额
        BigDecimal actualOrderAmount = null;
        
        // 查询商品订单
        QueryWrapper<ProductOrder> productOrderQueryWrapper = new QueryWrapper<>();
        productOrderQueryWrapper.eq("order_no", orderNo);
        ProductOrder productOrder = productOrderMapper.selectOne(productOrderQueryWrapper);
        
        if (productOrder != null) {
            log.info("找到商品订单当前金额, 订单号: {}, 金额: {}", orderNo, productOrder.getTotalAmount());
            actualOrderAmount = productOrder.getTotalAmount();
        } else {
            // 商品订单不存在，查询活动订单
            QueryWrapper<ActivityOrder> activityOrderQueryWrapper = new QueryWrapper<>();
            activityOrderQueryWrapper.eq("order_no", orderNo);
            ActivityOrder activityOrder = activityOrderMapper.selectOne(activityOrderQueryWrapper);
            
            if (activityOrder != null) {
                log.info("找到活动订单当前金额, 订单号: {}, 金额: {}", orderNo, activityOrder.getTotalAmount());
                actualOrderAmount = activityOrder.getTotalAmount();
            } else {
                log.error("未找到订单当前金额, 订单号: {}", orderNo);
                throw new ServiceException("未找到订单当前金额: " + orderNo);
            }
        }
        
        // 如果指定了退款金额，检查是否超过订单金额
        if (specifiedAmount != null) {
            log.info("使用指定退款金额, 订单号: {}, 指定金额: {}, 订单实际金额: {}", orderNo, specifiedAmount, actualOrderAmount);
            
            // 检查指定金额是否超过订单实际金额
            if (specifiedAmount.compareTo(actualOrderAmount) > 0) {
                log.error("指定退款金额({})超过订单实际金额({}), 订单号: {}", specifiedAmount, actualOrderAmount, orderNo);
                throw new ServiceException("指定退款金额不能超过订单实际金额");
            }
            
            return specifiedAmount;
        }
        
        // 没有指定退款金额，返回订单实际金额
        return actualOrderAmount;
    }

    /**
     * 查询退款状态
     *
     * @param outTradeNo 商户订单号
     * @return 退款信息
     */
    @Override
    @Transactional
    public Refund queryRefund(String outTradeNo) {
        if (outTradeNo == null || outTradeNo.isEmpty()) {
            throw new ServiceException("商户订单号不能为空");
        }
        
        try {
            // 首先根据商户订单号查询退款记录
            QueryWrapper<WxRefundRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("out_trade_no", outTradeNo);
            // 按创建时间降序排序，获取最新的退款记录
            queryWrapper.orderByDesc("create_time");
            List<WxRefundRecord> refundRecords = wxRefundRecordMapper.selectList(queryWrapper);
            
            // 如果没有退款记录，返回null
            if (refundRecords == null || refundRecords.isEmpty()) {
                return null;
            }
            
            // 获取最新的退款记录
            WxRefundRecord refundRecord = refundRecords.get(0);
            String outRefundNo = refundRecord.getOutRefundNo();
            log.info("找到最新退款记录，商户订单号: {}, 商户退款单号: {}", outTradeNo, outRefundNo);
            
            // 根据环境决定使用何种方式查询退款状态
            Refund refund;
            if ("prod".equals(activeProfile)) {
                // 生产环境：从数据库获取微信退款记录
                log.info("使用微信云托管环境，从数据库获取退款状态");
                refund = queryRefundFromDb(outTradeNo);
            } else {
                // 开发环境：使用原有的退款查询方式
                log.info("使用原生SDK查询退款状态");
                refund = wxRefundUtil.queryRefundByOutRefundNo(outRefundNo);
            
            // 更新退款记录
            refundRecord.setRefundStatus(refund.getStatus().toString());
            refundRecord.setRefundId(refund.getRefundId());
            refundRecord.setUpdateTime(LocalDateTime.now());
            
            // 微信支付退款成功时间可能为null
            if (refund.getSuccessTime() != null) {
                // 将String类型转换为LocalDateTime
                refundRecord.setSuccessTime(LocalDateTime.now());
            }
            
            wxRefundRecordMapper.updateById(refundRecord);
            log.info("退款记录更新成功, 商户订单号: {}, 商户退款单号: {}, 退款状态: {}", 
                    outTradeNo, outRefundNo, refund.getStatus());
            }
            
            return refund;
        } catch (ServiceException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            log.error("查询退款失败: {}", e.getMessage());
            throw new ServiceException("查询退款失败: " + e.getMessage());
        }
    }
    
    /**
     * 从数据库查询退款记录并转换为Refund对象
     *
     * @param outTradeNo 商户订单号
     * @return 退款信息
     */
    @Override
    public Refund queryRefundFromDb(String outTradeNo) {
        if (outTradeNo == null || outTradeNo.isEmpty()) {
            throw new ServiceException("商户订单号不能为空");
        }
        
        try {
            // 查询退款记录
            QueryWrapper<WxRefundRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("out_trade_no", outTradeNo);
            // 按创建时间降序排序，获取最新的退款记录
            queryWrapper.orderByDesc("create_time");
            List<WxRefundRecord> refundRecords = wxRefundRecordMapper.selectList(queryWrapper);
            
            if (refundRecords == null || refundRecords.isEmpty()) {
                log.info("未找到退款记录，商户订单号: {}", outTradeNo);
                return null;
            }
            
            // 获取最新的退款记录
            WxRefundRecord latestRefundRecord = refundRecords.get(0);
            
            // 计算所有退款记录的refundAmount之和
            BigDecimal totalRefundAmount = BigDecimal.ZERO;
            for (WxRefundRecord record : refundRecords) {
                if (record.getRefundAmount() != null) {
                    totalRefundAmount = totalRefundAmount.add(record.getRefundAmount());
                }
            }
            
            log.info("从数据库查询到退款记录，商户订单号: {}, 商户退款单号: {}, 累计退款金额: {}", 
                    outTradeNo, latestRefundRecord.getOutRefundNo(), totalRefundAmount);
            
            // 创建一个新的WxRefundRecord对象，保留最新退款记录的所有属性，但使用计算出的总退款金额
            WxRefundRecord mergedRecord = new WxRefundRecord();
            mergedRecord.setId(latestRefundRecord.getId());
            mergedRecord.setOutTradeNo(latestRefundRecord.getOutTradeNo());
            mergedRecord.setOutRefundNo(latestRefundRecord.getOutRefundNo());
            mergedRecord.setRefundId(latestRefundRecord.getRefundId());
            mergedRecord.setTotalAmount(latestRefundRecord.getTotalAmount());
            mergedRecord.setRefundAmount(totalRefundAmount); // 使用累计的退款金额
            mergedRecord.setRefundStatus(latestRefundRecord.getRefundStatus());
            mergedRecord.setReason(latestRefundRecord.getReason());
            mergedRecord.setCreateTime(latestRefundRecord.getCreateTime());
            mergedRecord.setUpdateTime(latestRefundRecord.getUpdateTime());
            mergedRecord.setSuccessTime(latestRefundRecord.getSuccessTime());
            
            // 将合并后的WxRefundRecord转换为Refund
            Refund refund = wxRefundUtil.convertDbRecordToRefund(mergedRecord);
            
            return refund;
        } catch (Exception e) {
            log.error("从数据库查询退款记录失败: {}", e.getMessage());
            throw new ServiceException("查询退款记录失败: " + e.getMessage());
        }
    }
} 