package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tour.common.constant.Constants;
import com.tour.common.enums.RefundStatusEnum;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.CopyTools;
import com.tour.common.util.EncryptUtil;
import com.tour.common.util.UserContext;
import com.tour.common.util.WxPaymentUtil;
import com.tour.dao.*;
import com.tour.dto.ActivityOrderDTO;
import com.tour.dto.EnrollmentDTO;
import com.tour.enums.ActivityOrderStatusEnum;
import com.tour.enums.ActivityStatusEnum;
import com.tour.enums.TickTypeEnum;
import com.tour.model.*;
import com.tour.service.SignUpService;
import com.tour.service.WxRefundService;
import com.tour.vo.SignUpListVO;
import com.tour.vo.TravelerVO;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service("signUpService")
@Slf4j

public class SignUpServiceImpl implements SignUpService {

    private final EnrollmentMapper enrollmentMapper;
    private final EnrollmentTravelerMapper enrollmentTravelerMapper;
    private final EncryptUtil encryptUtil;
    private final ActivityMapper activityMapper;
    private final ActivityOrderMapper activityOrderMapper;
    private final ActivityOrderTravelerMapper activityOrderTravelerMapper;
    private final WxPaymentUtil wxPaymentUtil;
    private final WxPaymentActivityOrderMapper wxPaymentActivityOrderMapper;
    private final WxRefundService wxRefundService;
    private final WxRefundRecordMapper wxRefundRecordMapper;
    private final com.tour.common.util.WxRefundUtil wxRefundUtil;
    
    @Value("${wechat.pay.activity-notify-url}")
    private String activityNotifyUrl;
    
    @Value("${spring.profiles.active}")
    private String activeProfile;
    
    @Value("${wechat.pay.cloud.env-id:}")
    private String cloudEnvId;
    
    @Value("${wechat.pay.cloud.container.service:}")
    private String cloudContainerService;
    
    @Value("${wechat.pay.cloud.container.path:}")
    private String cloudContainerPath;
    
    @Value("${wechat.pay.cloud.container.activity-path:}")
    private String cloudActivityPath;
    
    @Value("${wechat.pay.cloud.container.refund-path:}")
    private String cloudRefundPath;


    public SignUpServiceImpl(EnrollmentMapper enrollmentMapper, EnrollmentTravelerMapper enrollmentTravelerMapper, EncryptUtil encryptUtil, ActivityMapper activityMapper, ActivityOrderMapper activityOrderMapper, ActivityOrderTravelerMapper activityOrderTravelerMapper, WxPaymentUtil wxPaymentUtil, WxPaymentActivityOrderMapper wxPaymentActivityOrderMapper, WxRefundService wxRefundService, WxRefundRecordMapper wxRefundRecordMapper, com.tour.common.util.WxRefundUtil wxRefundUtil) {
        this.enrollmentMapper = enrollmentMapper;
        this.enrollmentTravelerMapper = enrollmentTravelerMapper;
        this.encryptUtil = encryptUtil;
        this.activityMapper = activityMapper;
        this.activityOrderMapper = activityOrderMapper;
        this.activityOrderTravelerMapper = activityOrderTravelerMapper;
        this.wxPaymentUtil = wxPaymentUtil;
        this.wxPaymentActivityOrderMapper = wxPaymentActivityOrderMapper;
        this.wxRefundService = wxRefundService;
        this.wxRefundRecordMapper = wxRefundRecordMapper;
        this.wxRefundUtil = wxRefundUtil;
    }

    @Transactional
    @Override
    public void signup(EnrollmentDTO enrollmentDTO,String orderNo) {
        Enrollment enrollment = CopyTools.copy(enrollmentDTO, Enrollment.class);
        enrollment.setOrderNo(orderNo);
        enrollmentMapper.insert(enrollment);
        List<TravelerVO> travelers = enrollmentDTO.getTravelers();

        for(TravelerVO traveler : travelers) {
//          防止一个出行人报名多次同一个活动
            QueryWrapper<EnrollmentTraveler> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("traveler_id", Long.valueOf(traveler.getId()));
            List<EnrollmentTraveler> enrollmentTravelers = enrollmentTravelerMapper.selectList(queryWrapper);
            if(enrollmentTravelers != null) {
                for(EnrollmentTraveler enrollmentTraveler : enrollmentTravelers) {
                    Long RepetitionEnrollmentId = enrollmentTraveler.getEnrollmentId();
                    QueryWrapper<Enrollment> enrollmentQueryWrapper = new QueryWrapper<>();
                    enrollmentQueryWrapper.eq("id", RepetitionEnrollmentId);
                    Enrollment RepetitionEnrollment = enrollmentMapper.selectOne(enrollmentQueryWrapper);
                    if(RepetitionEnrollment.getActivityId() == enrollmentDTO.getActivityId())
                    {
//                重复报名同一个活动了
                        throw new ServiceException("同一个出行人不能报名同一个活动！");
                    }
                }
            }


//            绑定一次报名下，有哪些出行人
            EnrollmentTraveler newEnrollmentTraveler = new EnrollmentTraveler();
            newEnrollmentTraveler.setEnrollmentId(enrollment.getId());
            newEnrollmentTraveler.setTravelerId(Long.valueOf(traveler.getId()));
            log.info("这是转化后的出行人ID{}", newEnrollmentTraveler.getTravelerId());
            newEnrollmentTraveler.setCreateTime(LocalDateTime.now());
            newEnrollmentTraveler.setUpdateTime(LocalDateTime.now());
            enrollmentTravelerMapper.insert(newEnrollmentTraveler);

        }
    }

    @Override
    public List<SignUpListVO> loadSignupListByActivityId(Long id) {
        List<SignUpListVO> signUpListVOList= enrollmentMapper.loadSignupListByActivityId(id);
        for(SignUpListVO signUpListVO : signUpListVOList) {
            // 解密并脱敏身份证号

            String idCard = encryptUtil.decrypt(signUpListVO.getIdCard());
            signUpListVO.setIdCard(idCard);
        }

        return signUpListVOList;
    }


    // 锁定订单并预留库存
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPaymentResult<ActivityOrder> lockOrder(ActivityOrderDTO activityOrderDTO) {
        log.info("接收activityOrderDTO{}",activityOrderDTO);
//      从上下文获取openid
        String openid = UserContext.getOpenId();

//        校验两个销量之和是否与参与人数相等
        if( (activityOrderDTO.getNormalNum()+activityOrderDTO.getEarlyBirdNum()) !=activityOrderDTO.getTravelers().size())
        {
            throw new ServiceException("参与人数与发送票数不符");
        }

        if(activityOrderDTO.getBusLocationId()==null)
        {
            throw new ServiceException("没有选择上车点");
        }
        //  校验活动是否存在且可报名
        Activity activity = activityMapper.selectById(activityOrderDTO.getActivityId());
        if (activity == null || activity.getStatus() != ActivityStatusEnum.PUBLISHED.getCode()) { // 1-已发布
            throw new ServiceException("活动不存在或不可报名");
        }

        //  校验库存
        if (activity.getAvailableEarlyBird() < activityOrderDTO.getEarlyBirdNum() ||
                activity.getAvailableNormal() < activityOrderDTO.getNormalNum()) {
            throw new ServiceException("库存不足");
        }

//        校验是否存在未支付订单
        QueryWrapper<ActivityOrder> checkIsNotPaied = new QueryWrapper<>();
        checkIsNotPaied.eq("openid", openid);
        checkIsNotPaied.eq("status",ActivityOrderStatusEnum.NONPAYMENT.getStatus());
        ActivityOrder existingOrder = activityOrderMapper.selectOne(checkIsNotPaied);
        if(existingOrder != null) {
            throw new ServiceException("当前存在未支付订单，请先取消订单或支付订单");
        }



        long newReservedEarly = activity.getReservedEarlyBird() + activityOrderDTO.getEarlyBirdNum();
        long newReservedNormal = activity.getReservedNormal() + activityOrderDTO.getNormalNum();
        //  预留库存
        int rows = activityMapper.update(null,
                new LambdaUpdateWrapper<Activity>()
                        .setSql("reserved_early_bird = reserved_early_bird + " + activityOrderDTO.getEarlyBirdNum())
                        .setSql("reserved_normal = reserved_normal + " + activityOrderDTO.getNormalNum())
                        .eq(Activity::getId, activityOrderDTO.getActivityId())
                        .ge(Activity::getEarlyBirdQuota,
                                newReservedEarly)
                        .ge(Activity::getNormalQuota,
                                newReservedNormal)
        );




        if (rows == 0) {
            throw new ServiceException("库存不足，预留失败");
        }

        //  创建订单
        ActivityOrder order = new ActivityOrder();
        BeanUtils.copyProperties(activityOrderDTO, order);
        order.setActivityId(activityOrderDTO.getActivityId());
        order.setOrderNo(generateOrderNo());
        order.setStatus(ActivityOrderStatusEnum.NONPAYMENT.getStatus()); // 1-待支付
        order.setExpireTime(LocalDateTime.now().plusMinutes(Constants.defaultOrderExpireTime)); // 15分钟超时
        order.setCreateTime(LocalDateTime.now());
        order.setOpenid(openid);
        order.setBusLocationId(activityOrderDTO.getBusLocationId());
//        后端处理总价格
        BigDecimal totalAmount =
                // 早鸟票总价 = 早鸟单价 × 早鸟数量
                activity.getEarlyBirdPrice().multiply(BigDecimal.valueOf(order.getEarlyBirdNum()))
                        // 普通票总价 = 普通单价 × 普通数量
                        .add(activity.getNormalPrice().multiply(BigDecimal.valueOf(order.getNormalNum())));

        order.setTotalAmount(totalAmount);
        activityOrderMapper.insert(order);

//        将每个出行人和订单相绑定
        List<TravelerVO> travelerVOS = activityOrderDTO.getTravelers();
        for(TravelerVO travelerVO : travelerVOS) {
            ActivityOrderTraveler activityOrderTraveler = new ActivityOrderTraveler();
            activityOrderTraveler.setTravelerId(Long.valueOf(travelerVO.getId()));
            activityOrderTraveler.setOrderNo(order.getOrderNo());
            activityOrderTraveler.setCreateTime(LocalDateTime.now());
            
            // 设置票类型，如果未提供则使用默认值
            Integer tickType = travelerVO.getTickType();
            if (tickType == null || (tickType != TickTypeEnum.EARLY_BIRD.getCode() && tickType != TickTypeEnum.NORMAL.getCode())) {
                // 使用默认值（普通票）
                activityOrderTraveler.setTickType(Constants.DEFAULT_TICK_TYPE);
            } else {
                activityOrderTraveler.setTickType(tickType);
            }
            
            activityOrderTravelerMapper.insert(activityOrderTraveler);
            //          防止一个出行人报名多次同一个活动 需要调用过signup方法后校验才有效
            Enrollment enrollment = new Enrollment();
            QueryWrapper<EnrollmentTraveler> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("traveler_id", Long.valueOf(travelerVO.getId()));
            List<EnrollmentTraveler> enrollmentTravelers = enrollmentTravelerMapper.selectList(queryWrapper);
            if(enrollmentTravelers != null) {
                for(EnrollmentTraveler enrollmentTraveler : enrollmentTravelers) {
                    Long RepetitionEnrollmentId = enrollmentTraveler.getEnrollmentId();
                    QueryWrapper<Enrollment> enrollmentQueryWrapper = new QueryWrapper<>();
                    enrollmentQueryWrapper.eq("id", RepetitionEnrollmentId);
                    List<Enrollment> repetitionEnrollments = enrollmentMapper.selectList(enrollmentQueryWrapper);
                    for(Enrollment repetitionEnrollment : repetitionEnrollments) {
                        if(repetitionEnrollment.getActivityId() == order.getActivityId())
                        {
//                重复报名同一个活动了
                            throw new ServiceException("同一个出行人不能报名同一个活动！");
                        }
                    }
                }

            }
        }
        
        // 生成微信支付预付单
        try {
            WxPaymentRequest paymentRequest = new WxPaymentRequest();
            // 金额单位转换：元 -> 分
            int totalAmountFen = totalAmount.multiply(new BigDecimal(100)).intValue();
            paymentRequest.setTotal(totalAmountFen);
            paymentRequest.setOpenid(openid);
            paymentRequest.setDescription(activity.getTitle()); // 使用活动标题作为订单描述
            paymentRequest.setOutTradeNo(order.getOrderNo());
            
            // 判断是否需要使用云托管环境
            WxPaymentResponseVO paymentResponse;
            if ("prod".equals(activeProfile) && cloudEnvId != null && !cloudEnvId.isEmpty()) {
                // 配置云托管环境参数
                paymentRequest.setEnvId(cloudEnvId);
                paymentRequest.setCallbackType(2); // 2-云托管
                
                // 设置容器信息
                WxPaymentRequest.ContainerInfo containerInfo = new WxPaymentRequest.ContainerInfo();
                containerInfo.setService(cloudContainerService);
                containerInfo.setPath(cloudActivityPath);
                paymentRequest.setContainer(containerInfo);
                
                // 使用云托管环境下单
                log.info("生产环境：使用云托管环境进行微信支付下单, envId: {}, container: {}/{}", 
                        cloudEnvId, cloudContainerService, cloudActivityPath);
                paymentResponse = wxPaymentUtil.wxGetPrePaymentWithCloudEnv(paymentRequest);
            } else {
                // 开发环境：使用传统方式下单
                log.info("开发环境：使用传统方式进行微信支付下单");
                paymentRequest.setNotifyUrl(activityNotifyUrl);
                paymentResponse = wxPaymentUtil.wxGetPrePayment(paymentRequest);
            }
            
            // 保存微信支付信息到关联表
            WxPaymentActivityOrder wxPaymentActivityOrder = new WxPaymentActivityOrder();
            wxPaymentActivityOrder.setOrderNo(order.getOrderNo());
            wxPaymentActivityOrder.setTimeStamp(paymentResponse.getTimeStamp());
            wxPaymentActivityOrder.setNonceStr(paymentResponse.getNonceStr());
            wxPaymentActivityOrder.setPackageStr(paymentResponse.getPackageStr());
            wxPaymentActivityOrder.setSignType(paymentResponse.getSignType());
            wxPaymentActivityOrder.setPaySign(paymentResponse.getPaySign());
            wxPaymentActivityOrder.setPrepayId(paymentResponse.getPrepayId());
            wxPaymentActivityOrder.setCreateTime(LocalDateTime.now());
            
            wxPaymentActivityOrderMapper.insert(wxPaymentActivityOrder);
            log.info("已保存微信支付活动订单信息, orderNo: {}, prepayId: {}", order.getOrderNo(), paymentResponse.getPrepayId());
            
            // 返回订单信息和支付参数
            return OrderPaymentResult.of(order, paymentResponse);
        } catch (Exception e) {
            log.error("生成预付单失败: {}", e.getMessage());
            throw new ServiceException("生成支付参数失败: " + e.getMessage());
        }
    }

    // 新增方法：支付订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(String orderNo, String paymentId,EnrollmentDTO enrollmentDTO) {
        // 查询订单
        ActivityOrder order = activityOrderMapper.selectOne(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getOrderNo, orderNo)
        );

        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 新增校验：如果订单已经是已支付状态，直接返回true，避免重复处理
        if (ActivityOrderStatusEnum.PAID.getStatus() == order.getStatus()) {
            log.info("订单{}已经是已支付状态，无需重复处理", orderNo);
            return true;
        }

        // 仅处理待支付状态订单
        if (ActivityOrderStatusEnum.NONPAYMENT.getStatus() != order.getStatus()) {
            throw new ServiceException("订单状态不正确");
        }

        if (order.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException("订单已过期");
        }

        //  扣减实际库存
        Activity activity = activityMapper.selectById(order.getActivityId());
        int rows = activityMapper.update(null,
                new LambdaUpdateWrapper<Activity>()
                        .setSql("early_bird_quota = early_bird_quota - " + order.getEarlyBirdNum())
                        .setSql("normal_quota = normal_quota - " + order.getNormalNum())
                        .setSql("reserved_early_bird = reserved_early_bird - " + order.getEarlyBirdNum())
                        .setSql("reserved_normal = reserved_normal - " + order.getNormalNum())
                        .setSql("total_sold = total_sold + " + (order.getEarlyBirdNum() + order.getNormalNum()))
                        .eq(Activity::getId, order.getActivityId())
                        .ge(Activity::getReservedEarlyBird, order.getEarlyBirdNum())
                        .ge(Activity::getReservedNormal, order.getNormalNum())
        );

        if (rows == 0) {
            throw new ServiceException("库存不足，支付失败");
        }

//        进行报名操作
        signup(enrollmentDTO,orderNo);
        //  更新订单状态为已支付
        ActivityOrder updateOrder = new ActivityOrder();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(ActivityOrderStatusEnum.PAID.getStatus()); // 2-已支付
        updateOrder.setPaymentTime(LocalDateTime.now());

        return activityOrderMapper.updateById(updateOrder) > 0;
    }
    /**
     * 处理不同状态的取消订单
     * 无退款原因参数，兼容旧接口
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String orderNo, String openid) {
        // 调用带退款原因的方法，使用默认退款原因
        return cancelOrder(orderNo, openid, "用户取消订单", "用户取消订单");
    }
    
    /**
     * 处理不同状态的取消订单
     * 带退款原因参数的方法
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String orderNo, String openid, String reason) {
        // 调用带真实原因的方法，默认真实原因与退款原因相同
        return cancelOrder(orderNo, openid, reason, reason);
    }
    
    /**
     * 处理不同状态的取消订单
     * 带退款原因和真实原因参数的新方法
     * @param orderNo 订单号
     * @param openid 用户openid
     * @param reason 微信退款原因
     * @param realReason 真实退款原因（存储到数据库）
     * @return 是否取消成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String orderNo, String openid, String reason, String realReason) {
        // 查询订单（不限状态）
        ActivityOrder order = activityOrderMapper.selectOne(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getOrderNo, orderNo)
                        .eq(ActivityOrder::getOpenid, openid)
        );

        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 根据订单状态执行不同的取消逻辑
        int currentStatus = order.getStatus();
        if (currentStatus == ActivityOrderStatusEnum.NONPAYMENT.getStatus()) {
            return cancelNonPaymentOrder(order);
        } else if (currentStatus == ActivityOrderStatusEnum.PAID.getStatus()) {
            return cancelPaidOrder(order, reason, realReason);
        } else if (currentStatus == ActivityOrderStatusEnum.CANCELED.getStatus()
                || currentStatus == ActivityOrderStatusEnum.TIMEOUT.getStatus()
                || currentStatus == ActivityOrderStatusEnum.COMPLETED.getStatus()) {
            throw new ServiceException("订单已处于终止状态，无法重复取消");
        } else {
            throw new ServiceException("不支持的订单状态：" + currentStatus);
        }
    }

    /**
     * 处理待支付订单的取消逻辑
     */
    private boolean cancelNonPaymentOrder(ActivityOrder order) {
        // 校验订单状态（双重校验，防止并发问题）
        if (order.getStatus() != ActivityOrderStatusEnum.NONPAYMENT.getStatus()) {
            throw new ServiceException("订单状态已变更，无法取消");
        }
        
        // 尝试关闭微信支付订单
        try {
            wxPaymentUtil.closeOrder(order.getOrderNo());
        } catch (Exception e) {
            log.warn("关闭微信支付订单失败: {}", e.getMessage());
            // 继续处理，不影响订单取消
        }

        // 释放预留库存
        Activity activity = activityMapper.selectById(order.getActivityId());
        int rows = activityMapper.update(null,
                new LambdaUpdateWrapper<Activity>()
                        .setSql("reserved_early_bird = reserved_early_bird - " + order.getEarlyBirdNum())
                        .setSql("reserved_normal = reserved_normal - " + order.getNormalNum())
                        .eq(Activity::getId, order.getActivityId())
                        .ge(Activity::getReservedEarlyBird, order.getEarlyBirdNum())
                        .ge(Activity::getReservedNormal, order.getNormalNum())
        );

        if (rows == 0) {
            throw new ServiceException("库存释放失败");
        }

        // 更新订单状态为已取消（3）
        ActivityOrder updateOrder = new ActivityOrder();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(ActivityOrderStatusEnum.CANCELED.getStatus());

        return activityOrderMapper.updateById(updateOrder) > 0;
    }

    /**
     * 处理已支付订单的取消逻辑（退款逻辑）
     * 
     * @param order 要取消的订单
     * @param reason 退款原因
     * @param realReason 真实退款原因
     * @return 是否取消成功
     */
    private boolean cancelPaidOrder(ActivityOrder order, String reason, String realReason) {
        // 校验订单状态（双重校验，防止并发问题）
        if (order.getStatus() != ActivityOrderStatusEnum.PAID.getStatus()) {
            throw new ServiceException("订单状态已变更，无法执行支付后取消");
        }
        


        try {
            // 1. 回滚库存（增加可用库存，减少已售数量）
            Activity activity = activityMapper.selectById(order.getActivityId());
            int rows = activityMapper.update(null,
                    new LambdaUpdateWrapper<Activity>()
                            .setSql("early_bird_quota = early_bird_quota + " + order.getEarlyBirdNum())
                            .setSql("normal_quota = normal_quota + " + order.getNormalNum())
                            .setSql("total_sold = total_sold - " + (order.getEarlyBirdNum() + order.getNormalNum()))
                            .eq(Activity::getId, order.getActivityId())
                            .ge(Activity::getTotalSold, order.getEarlyBirdNum() + order.getNormalNum())
            );

            if (rows == 0) {
                throw new ServiceException("库存回滚失败");
            }

            // 2. 删除对应的 enrollment 数据（根据 orderNo 关联）
            LambdaQueryWrapper<Enrollment> enrollmentQuery = new LambdaQueryWrapper<>();
            enrollmentQuery.eq(Enrollment::getOrderNo, order.getOrderNo());
            int deletedEnrollmentRows = enrollmentMapper.delete(enrollmentQuery);
            log.info("删除订单 {} 关联的 enrollment 记录数：{}", order.getOrderNo(), deletedEnrollmentRows);
            
            // 3. 申请微信支付退款
            log.info("开始申请活动订单退款，订单号: {}, 退款原因: {}, 真实原因: {}", order.getOrderNo(), reason, realReason);
            
            // 查询是否有原始总金额记录
            String orderNo = order.getOrderNo();
            BigDecimal originalTotalAmount = order.getTotalAmount();
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
                    originalTotalAmount = firstRecord.getTotalAmount();
                    log.info("从退款记录中获取到原始订单总金额: {}, 订单号: {}", originalTotalAmount, orderNo);
                } else {
                    log.info("未找到退款记录，使用订单当前总金额: {}, 订单号: {}", originalTotalAmount, orderNo);
                }
            } catch (Exception e) {
                log.warn("查询原始订单总金额异常，使用当前总金额: {}, 异常: {}", originalTotalAmount, e.getMessage());
            }
            
            // 使用原始总金额调用退款服务
            Refund refund = wxRefundService.refund(order.getOrderNo(), reason, realReason);
            
            // 4. 更新订单状态为已取消（3）以及退款状态为已申请退款（1）
            ActivityOrder updateOrder = new ActivityOrder();
            updateOrder.setId(order.getId());
            updateOrder.setStatus(ActivityOrderStatusEnum.CANCELED.getStatus());
            updateOrder.setRefundStatus(RefundStatusEnum.REFUNDED.getCode().longValue());
            activityOrderMapper.updateById(updateOrder);
            
            log.info("活动订单退款申请成功，订单号: {}, 退款单号: {}", order.getOrderNo(), refund.getOutRefundNo());
            
            return true;
        } catch (Exception e) {
            log.error("取消已支付订单失败: {}", e.getMessage());
            throw new ServiceException("申请退款失败: " + e.getMessage());
        }
    }


    @Override
    public Object getOrder(String orderNo, String openid) {
        QueryWrapper<ActivityOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        queryWrapper.eq("openid", openid);
        ActivityOrder activityOrder = activityOrderMapper.selectOne(queryWrapper);
        if (activityOrder == null) {
            throw new ServiceException("请求订单不存在");
        }


        return activityOrder;
    }

    // 定时处理超时订单
    @Scheduled(cron = "0 */5 * * * ?") // 每5分钟执行一次
    @Transactional(rollbackFor = Exception.class)
    public void processTimeoutOrders() {
        log.info("开始处理超时订单...");

        // 1查询所有超时未支付的订单
        List<ActivityOrder> timeoutOrders = activityOrderMapper.selectList(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getStatus,
                                ActivityOrderStatusEnum.NONPAYMENT.getStatus()) // 待支付
                        .lt(ActivityOrder::getExpireTime, LocalDateTime.now())
                        .last("LIMIT 100") // 每次处理100条，避免影响性能
        );

        if (timeoutOrders.isEmpty()) {
            log.info("没有需要处理的超时订单");
            return;
        }

        log.info("发现 {} 个超时订单需要处理", timeoutOrders.size());

        // 2批量释放库存并更新订单状态
        for (ActivityOrder order : timeoutOrders) {
            try {
                // 尝试关闭微信支付订单
                try {
                    wxPaymentUtil.closeOrder(order.getOrderNo());
                } catch (Exception e) {
                    log.warn("关闭微信支付订单失败: {}", e.getMessage());
                    // 继续处理，不影响订单取消
                }
                
                // 释放库存
                Activity activity = activityMapper.selectById(order.getActivityId());
                if (activity != null) {
                    activityMapper.update(null,
                            new LambdaUpdateWrapper<Activity>()
                                    .setSql("reserved_early_bird = reserved_early_bird - " + order.getEarlyBirdNum())
                                    .setSql("reserved_normal = reserved_normal - " + order.getNormalNum())
                                    .eq(Activity::getId, order.getActivityId())
                                    .ge(Activity::getReservedEarlyBird, order.getEarlyBirdNum())
                                    .ge(Activity::getReservedNormal, order.getNormalNum())
                    );
                }

                // 更新订单状态为已过期
                ActivityOrder updateOrder = new ActivityOrder();
                updateOrder.setId(order.getId());
                updateOrder.setStatus(ActivityOrderStatusEnum.TIMEOUT.getStatus()); // 4-已过期
                activityOrderMapper.updateById(updateOrder);

                log.info("订单 {} 已标记为过期", order.getOrderNo());
            } catch (Exception e) {
                log.error("处理超时订单 {} 失败: {}", order.getOrderNo(), e.getMessage());
            }
        }

        log.info("超时订单处理完成");
    }

    // 生成订单号（日期+随机数）
    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        return   datePart + randomPart;
    }

    /**
     * 部分退款（按照出行人列表进行部分退款）
     *
     * @param orderNo 订单号
     * @param openid 用户openid
     * @param travelerIds 需要退款的出行人ID列表
     * @param reason 退款原因
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean partialRefund(String orderNo, String openid, List<Long> travelerIds, String reason) {
        // 查询订单
        ActivityOrder activityOrder = activityOrderMapper.selectOne(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getOrderNo, orderNo)
                        .eq(ActivityOrder::getOpenid, openid)
        );
        
        if (activityOrder == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 检查订单状态必须为已支付
        if (activityOrder.getStatus() != ActivityOrderStatusEnum.PAID.getStatus()) {
            throw new ServiceException("只有已支付的订单才能进行部分退款");
        }
        
        // 查询活动信息，用于计算退款金额和库存回滚
        Activity activity = activityMapper.selectById(activityOrder.getActivityId());
        if (activity == null) {
            throw new ServiceException("活动不存在");
        }
        
        // 获取订单下所有出行人信息
        List<ActivityOrderTraveler> allOrderTravelers = activityOrderTravelerMapper.selectList(
                new LambdaQueryWrapper<ActivityOrderTraveler>()
                        .eq(ActivityOrderTraveler::getOrderNo, orderNo)
        );
        
        if (allOrderTravelers == null || allOrderTravelers.isEmpty()) {
            throw new ServiceException("订单下无出行人信息");
        }
        
        // 筛选出要退款的出行人列表
        List<ActivityOrderTraveler> selectedTravelers = allOrderTravelers.stream()
                .filter(aot -> travelerIds.contains(aot.getTravelerId()))
                .collect(Collectors.toList());
        
        if (selectedTravelers.size() != travelerIds.size()) {
            throw new ServiceException("部分出行人信息不存在或不属于该订单");
        }
        
        // 统计部分退款的数量和金额
        int earlyBirdRefundCount = 0;
        int normalRefundCount = 0;
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        List<Long> activityOrderTravelerIds = new ArrayList<>();
        
        for (ActivityOrderTraveler traveler : selectedTravelers) {
            // 统计票类型数量
            if (traveler.getTickType() == TickTypeEnum.EARLY_BIRD.getCode()) {
                earlyBirdRefundCount++;
                totalRefundAmount = totalRefundAmount.add(activity.getEarlyBirdPrice());
            } else {
                normalRefundCount++;
                totalRefundAmount = totalRefundAmount.add(activity.getNormalPrice());
            }
            
            // 收集关联ID
            activityOrderTravelerIds.add(traveler.getId());
        }
        
        try {
            // 1. 回滚部分库存
            int rows = activityMapper.update(null,
                    new LambdaUpdateWrapper<Activity>()
                            .setSql("early_bird_quota = early_bird_quota + " + earlyBirdRefundCount)
                            .setSql("normal_quota = normal_quota + " + normalRefundCount)
                            .setSql("total_sold = total_sold - " + (earlyBirdRefundCount + normalRefundCount))
                            .eq(Activity::getId, activity.getId())
                            .ge(Activity::getTotalSold, earlyBirdRefundCount + normalRefundCount)
            );
            
            if (rows == 0) {
                throw new ServiceException("库存回滚失败");
            }
            
            // 2. 删除enrollment_traveler表中对应的出行人信息
            LambdaQueryWrapper<Enrollment> enrollmentQuery = new LambdaQueryWrapper<>();
            enrollmentQuery.eq(Enrollment::getOrderNo, orderNo);
            List<Enrollment> enrollments = enrollmentMapper.selectList(enrollmentQuery);
            
            if (enrollments != null && !enrollments.isEmpty()) {
                for (Enrollment enrollment : enrollments) {
                    for (Long travelerId : travelerIds) {
                        enrollmentTravelerMapper.delete(
                                new LambdaQueryWrapper<EnrollmentTraveler>()
                                        .eq(EnrollmentTraveler::getEnrollmentId, enrollment.getId())
                                        .eq(EnrollmentTraveler::getTravelerId, travelerId)
                        );
                    }
                }
            }
            
            // 3. 删除activity_order_traveler表中对应的出行人信息
            for (Long aotId : activityOrderTravelerIds) {
                activityOrderTravelerMapper.deleteById(aotId);
            }
            
            // 4. 更新activity_order中的价格和票数
            ActivityOrder updateOrder = new ActivityOrder();
            updateOrder.setId(activityOrder.getId());
            
            // 计算退款后的价格和票数
            BigDecimal newTotalAmount = activityOrder.getTotalAmount().subtract(totalRefundAmount);
            int newEarlyBirdNum = activityOrder.getEarlyBirdNum() - earlyBirdRefundCount;
            int newNormalNum = activityOrder.getNormalNum() - normalRefundCount;
            
            // 检查退款后是否所有票都退完了
            if (newTotalAmount.compareTo(BigDecimal.ZERO) <= 0 && newEarlyBirdNum <= 0 && newNormalNum <= 0) {
                // 如果所有票都退完了，将订单状态更新为已取消，不修改价格和数量（保留原记录）
                log.info("部分退款后订单所有票都已退完，将订单状态更新为已取消，订单号：{}", orderNo);
                updateOrder.setStatus(ActivityOrderStatusEnum.CANCELED.getStatus());
            } else {
                // 部分退款，更新价格和票数
                updateOrder.setTotalAmount(newTotalAmount);
                updateOrder.setEarlyBirdNum(newEarlyBirdNum);
                updateOrder.setNormalNum(newNormalNum);
            }
            
            if (activityOrderMapper.updateById(updateOrder) == 0) {
                throw new ServiceException("更新订单信息失败");
            }
            
            // 5. 申请微信支付退款（部分退款）
            log.info("开始申请部分退款，订单号：{}，退款金额：{}", orderNo, totalRefundAmount);
            
            // 生成商户退款单号（格式：partial_refund_订单号_时间戳）
            String outRefundNo = "partial_refund_" + orderNo + "_" + System.currentTimeMillis();
            log.info("生成部分退款单号：{}", outRefundNo);
            
            // 查询订单原始总金额（从最早的退款记录中获取）
            BigDecimal originalTotalAmount = activityOrder.getTotalAmount();
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
                    originalTotalAmount = firstRecord.getTotalAmount();
                    log.info("从退款记录中获取到原始订单总金额: {}, 订单号: {}", originalTotalAmount, orderNo);
                } else {
                    log.info("未找到退款记录，使用订单当前总金额: {}, 订单号: {}", originalTotalAmount, orderNo);
                }
            } catch (Exception e) {
                log.warn("查询原始订单总金额异常，使用当前总金额: {}, 异常: {}", originalTotalAmount, e.getMessage());
            }
            
            // 金额单位转换：元 -> 分
            int totalAmountFen = originalTotalAmount.multiply(new BigDecimal(100)).intValue();
            int refundAmountFen = totalRefundAmount.multiply(new BigDecimal(100)).intValue();
            
            // 生成真实退款原因
            String realReason = "部分退款-" + travelerIds.size() + "位出行人";
            
            // 调用退款服务
            Refund refund;
            try {
                if ("prod".equals(activeProfile)) {
                    // 生产环境：使用云托管退款API
                    log.info("使用微信云托管环境申请部分退款，环境ID: {}, 订单号: {}, 原始总金额: {}, 退款金额: {}", 
                             cloudEnvId, orderNo, totalAmountFen, refundAmountFen);
                    refund = wxRefundUtil.createRefundWithCloudEnv(
                            orderNo, 
                            outRefundNo, 
                            totalAmountFen, 
                            refundAmountFen, 
                            realReason,
                            cloudContainerService,
                            cloudRefundPath
                    );
                } else {
                    // 开发环境：使用原有的退款方式
                    log.info("使用原生SDK申请部分退款，订单号: {}, 原始总金额: {}, 退款金额: {}", 
                             orderNo, totalAmountFen, refundAmountFen);
                    refund = wxRefundUtil.createRefund(
                            orderNo, 
                            outRefundNo, 
                            totalAmountFen, 
                            refundAmountFen, 
                            realReason
                    );
                }
                
                // 保存退款记录
                WxRefundRecord refundRecord = new WxRefundRecord();
                refundRecord.setOutTradeNo(orderNo);
                refundRecord.setOutRefundNo(outRefundNo);
                refundRecord.setRefundId(refund.getRefundId());
                refundRecord.setTotalAmount(originalTotalAmount);
                refundRecord.setRefundAmount(totalRefundAmount);
                refundRecord.setRefundStatus(refund.getStatus().toString());
                refundRecord.setReason(realReason);
                refundRecord.setCreateTime(LocalDateTime.now());
                refundRecord.setUpdateTime(LocalDateTime.now());
                
                // 微信支付退款成功时间可能为null
                if (refund.getSuccessTime() != null) {
                    refundRecord.setSuccessTime(LocalDateTime.now());
                }
                
                wxRefundRecordMapper.insert(refundRecord);
                log.info("部分退款记录保存成功, 商户退款单号: {}", outRefundNo);
                
                return true;
            } catch (Exception e) {
                log.error("部分退款申请失败：{}", e.getMessage(), e);
                throw new ServiceException("部分退款申请失败：" + e.getMessage());
            }
        } catch (Exception e) {
            log.error("部分退款失败: {}", e.getMessage(), e);
            throw new ServiceException("申请部分退款失败: " + e.getMessage());
        }
    }

    /**
     * 特殊退款（退款但保留订单和报名记录）
     *
     * @param orderNo 订单号
     * @param openid 用户openid
     * @param reason 退款原因
     * @param adminRemark 管理员备注
     * @param travelerIds 需要退款的出行人ID列表
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean specialRefund(String orderNo, String openid, String reason, String adminRemark, List<Long> travelerIds) {
        // 调用新方法，默认为false，保持原有删除逻辑
        return specialRefund(orderNo, openid, reason, adminRemark, travelerIds, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean specialRefund(String orderNo, String openid, String reason, String adminRemark, List<Long> travelerIds, boolean useRefundStatus) {
        // 查询订单
        ActivityOrder activityOrder = activityOrderMapper.selectOne(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getOrderNo, orderNo)
                        .eq(ActivityOrder::getOpenid, openid)
        );
        
        if (activityOrder == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 检查订单状态必须为已支付
        if (activityOrder.getStatus() != ActivityOrderStatusEnum.PAID.getStatus()) {
            throw new ServiceException("只有已支付的订单才能进行特殊退款");
        }
        
        // 查询活动信息，用于计算退款金额
        Activity activity = activityMapper.selectById(activityOrder.getActivityId());
        if (activity == null) {
            throw new ServiceException("活动不存在");
        }
        
        // 获取订单下所有出行人信息
        List<ActivityOrderTraveler> allOrderTravelers = activityOrderTravelerMapper.selectList(
                new LambdaQueryWrapper<ActivityOrderTraveler>()
                        .eq(ActivityOrderTraveler::getOrderNo, orderNo)
        );
        
        if (allOrderTravelers == null || allOrderTravelers.isEmpty()) {
            throw new ServiceException("订单下无出行人信息");
        }
        
        // 筛选出要退款的出行人列表
        List<ActivityOrderTraveler> selectedTravelers = allOrderTravelers.stream()
                .filter(aot -> travelerIds.contains(aot.getTravelerId()))
                .collect(Collectors.toList());
        
        if (selectedTravelers.size() != travelerIds.size()) {
            throw new ServiceException("部分出行人信息不存在或不属于该订单");
        }
        
        // 统计退款的数量和金额
        int earlyBirdRefundCount = 0;
        int normalRefundCount = 0;
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        List<Long> activityOrderTravelerIds = new ArrayList<>();
        
        for (ActivityOrderTraveler traveler : selectedTravelers) {
            // 统计票类型数量
            if (traveler.getTickType() == TickTypeEnum.EARLY_BIRD.getCode()) {
                earlyBirdRefundCount++;
                totalRefundAmount = totalRefundAmount.add(activity.getEarlyBirdPrice());
            } else {
                normalRefundCount++;
                totalRefundAmount = totalRefundAmount.add(activity.getNormalPrice());
            }
            
            // 收集关联ID
            activityOrderTravelerIds.add(traveler.getId());
        }
        
        try {
            // 根据useRefundStatus参数决定是删除记录还是标记为不可免单
            if (useRefundStatus) {
                // 将activity_order_traveler表中对应的出行人设置为不可免单
                for (ActivityOrderTraveler traveler : selectedTravelers) {
                    ActivityOrderTraveler updateTraveler = new ActivityOrderTraveler();
                    updateTraveler.setId(traveler.getId());
                    updateTraveler.setRefundStatus(1); // 1-不可免单
                    activityOrderTravelerMapper.updateById(updateTraveler);
                }
                log.info("已将订单{}的{}位出行人设置为不可免单", orderNo, selectedTravelers.size());
            } else {
                // 按原有逻辑删除activity_order_traveler表中对应的出行人信息
            for (Long aotId : activityOrderTravelerIds) {
                activityOrderTravelerMapper.deleteById(aotId);
            }
            log.info("已删除订单{}的{}位出行人关联记录，防止重复特殊退款", orderNo, activityOrderTravelerIds.size());
            }
            
            // 检查是否全部出行人都被标记为不可免单或删除
            int availableTravelers = 0;
            if (useRefundStatus) {
                // 计算可免单的出行人数量
                availableTravelers = activityOrderTravelerMapper.selectCount(
                    new LambdaQueryWrapper<ActivityOrderTraveler>()
                            .eq(ActivityOrderTraveler::getOrderNo, orderNo)
                                .eq(ActivityOrderTraveler::getRefundStatus, 0) // 0-可免单
            ).intValue();
            } else {
                // 计算剩余的出行人数量
                availableTravelers = activityOrderTravelerMapper.selectCount(
                        new LambdaQueryWrapper<ActivityOrderTraveler>()
                                .eq(ActivityOrderTraveler::getOrderNo, orderNo)
                ).intValue();
            }
            
            // 如果没有可免单的出行人，将订单状态更新为"已免单"
            if (availableTravelers == 0) {
                log.info("订单{}没有可免单的出行人，将状态更新为已免单", orderNo);
                activityOrder.setStatus(ActivityOrderStatusEnum.FEE_EXEMPTION.getStatus());
                activityOrderMapper.updateById(activityOrder);
            }
            
            // 查询是否有原始总金额记录
            BigDecimal originalTotalAmount = activityOrder.getTotalAmount();
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
                    originalTotalAmount = firstRecord.getTotalAmount();
                    log.info("从退款记录中获取到原始订单总金额: {}, 订单号: {}", originalTotalAmount, orderNo);
                } else {
                    log.info("未找到退款记录，使用订单当前总金额: {}, 订单号: {}", originalTotalAmount, orderNo);
                }
            } catch (Exception e) {
                log.warn("查询原始订单总金额异常，使用当前总金额: {}, 异常: {}", originalTotalAmount, e.getMessage());
            }
            
            // 生成特殊退款的唯一标识
            String outRefundNo = "special_refund_" + orderNo + "_" + System.currentTimeMillis();
            String realReason = "特殊退款: " + reason + (adminRemark != null ? (" - " + adminRemark) : "");
            
            // 申请微信支付退款（不影响订单状态和报名记录）
            log.info("开始申请特殊退款，订单号：{}，退款金额：{}", orderNo, totalRefundAmount);
            
            // 金额单位转换：元 -> 分
            int totalAmountFen = originalTotalAmount.multiply(new BigDecimal(100)).intValue();
            int refundAmountFen = totalRefundAmount.multiply(new BigDecimal(100)).intValue();
            
            // 执行退款操作
            Refund refund;
            try {
                if ("prod".equals(activeProfile)) {
                    // 生产环境：使用云托管退款API
                    log.info("使用微信云托管环境申请特殊退款，环境ID: {}, 订单号: {}, 原始总金额: {}, 退款金额: {}", 
                             cloudEnvId, orderNo, totalAmountFen, refundAmountFen);
                    refund = wxRefundUtil.createRefundWithCloudEnv(
                            orderNo, 
                            outRefundNo, 
                            totalAmountFen, 
                            refundAmountFen, 
                            realReason,
                            cloudContainerService,
                            cloudRefundPath
                    );
                } else {
                    // 开发环境：使用原有的退款方式
                    log.info("使用原生SDK申请特殊退款，订单号: {}, 原始总金额: {}, 退款金额: {}", 
                             orderNo, totalAmountFen, refundAmountFen);
                    refund = wxRefundUtil.createRefund(
                            orderNo, 
                            outRefundNo, 
                            totalAmountFen, 
                            refundAmountFen, 
                            realReason
                    );
                }
                
                // 保存退款记录
                WxRefundRecord refundRecord = new WxRefundRecord();
                refundRecord.setOutTradeNo(orderNo);
                refundRecord.setOutRefundNo(outRefundNo);
                refundRecord.setRefundId(refund.getRefundId());
                refundRecord.setTotalAmount(originalTotalAmount);
                refundRecord.setRefundAmount(totalRefundAmount);
                refundRecord.setRefundStatus(refund.getStatus().toString());
                refundRecord.setReason(realReason);
                refundRecord.setCreateTime(LocalDateTime.now());
                refundRecord.setUpdateTime(LocalDateTime.now());
                
                // 微信支付退款成功时间可能为null
                if (refund.getSuccessTime() != null) {
                    refundRecord.setSuccessTime(LocalDateTime.now());
                }
                
                wxRefundRecordMapper.insert(refundRecord);
                log.info("特殊退款记录保存成功, 商户退款单号: {}", outRefundNo);
                
                // 更新订单价格（如果更新后价格不为0）
                BigDecimal newTotalAmount = activityOrder.getTotalAmount().subtract(totalRefundAmount);
                if (newTotalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    ActivityOrder updateOrder = new ActivityOrder();
                    updateOrder.setId(activityOrder.getId());
                    updateOrder.setTotalAmount(newTotalAmount);
                    
                    // 使用条件更新，避免高并发问题
                    boolean updated = activityOrderMapper.update(updateOrder,
                            new LambdaUpdateWrapper<ActivityOrder>()
                                    .eq(ActivityOrder::getId, activityOrder.getId())
                                    .eq(ActivityOrder::getTotalAmount, activityOrder.getTotalAmount())
                            ) > 0;
                    
                    if (updated) {
                        log.info("订单{}价格更新成功，新价格：{}", orderNo, newTotalAmount);
                    } else {
                        log.warn("订单{}价格更新失败，可能是并发更新导致", orderNo);
                    }
                } else {
                    log.info("订单{}退款后价格为0或负数，不更新价格", orderNo);
                }
                
                // 不更新订单状态，保留原样（除非没有剩余出行人，已在上方更新为已免单）
                log.info("特殊退款申请成功，订单号: {}, 退款单号: {}", orderNo, refund.getOutRefundNo());
                
                return true;
            } catch (Exception e) {
                log.error("特殊退款申请失败：{}", e.getMessage(), e);
                throw new ServiceException("特殊退款申请失败：" + e.getMessage());
            }
        } catch (Exception e) {
            log.error("特殊退款失败: {}", e.getMessage(), e);
            throw new ServiceException("申请特殊退款失败: " + e.getMessage());
        }
    }

    /**
     * 自定义金额部分退款
     *
     * @param orderNo 订单号
     * @param travelerIds 需要退款的出行人ID列表（可选）
     * @param reason 退款原因
     * @param refundAmount 指定的退款金额
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean customRefund(String orderNo, List<Long> travelerIds, String reason, BigDecimal refundAmount) {
        // 查询订单 - 管理员接口不需要openid条件
        ActivityOrder activityOrder = activityOrderMapper.selectOne(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getOrderNo, orderNo)
        );
        
        if (activityOrder == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 检查订单状态必须为已支付
        if (activityOrder.getStatus() != ActivityOrderStatusEnum.PAID.getStatus()) {
            throw new ServiceException("只有已支付的订单才能进行退款");
        }
        
        // 校验退款金额不能超过订单当前总金额
        if (refundAmount.compareTo(activityOrder.getTotalAmount()) > 0) {
            throw new ServiceException("退款金额不能超过订单总金额");
        }
        
        // 校验退款金额必须大于0.01
        if (refundAmount.compareTo(new BigDecimal("0.01")) < 0) {
            throw new ServiceException("退款金额必须大于0.01元");
        }
        
        // 校验退款金额最多只能有两位小数
        String refundAmountStr = refundAmount.toPlainString();
        if (refundAmountStr.contains(".")) {
            String[] parts = refundAmountStr.split("\\.");
            if (parts.length > 1 && parts[1].length() > 2) {
                throw new ServiceException("退款金额最多只能有两位小数");
            }
        }
        
        // 查询活动信息（用于处理出行人相关逻辑）
        Activity activity = activityMapper.selectById(activityOrder.getActivityId());
        if (activity == null) {
            throw new ServiceException("活动不存在");
        }
        
        // 如果提供了出行人ID列表，则处理出行人相关逻辑
        int earlyBirdRefundCount = 0;
        int normalRefundCount = 0;
        List<Long> activityOrderTravelerIds = new ArrayList<>();
        
        if (travelerIds != null && !travelerIds.isEmpty()) {
            // 获取订单下所有出行人信息
            List<ActivityOrderTraveler> allOrderTravelers = activityOrderTravelerMapper.selectList(
                    new LambdaQueryWrapper<ActivityOrderTraveler>()
                            .eq(ActivityOrderTraveler::getOrderNo, orderNo)
            );
            
            if (allOrderTravelers == null || allOrderTravelers.isEmpty()) {
                throw new ServiceException("订单下无出行人信息");
            }
            
            // 筛选出要退款的出行人列表
            List<ActivityOrderTraveler> selectedTravelers = allOrderTravelers.stream()
                    .filter(aot -> travelerIds.contains(aot.getTravelerId()))
                    .collect(Collectors.toList());
            
            if (selectedTravelers.size() != travelerIds.size()) {
                throw new ServiceException("部分出行人信息不存在或不属于该订单");
            }
            
            // 统计票类型数量（用于库存回滚）
            for (ActivityOrderTraveler traveler : selectedTravelers) {
                if (traveler.getTickType() == TickTypeEnum.EARLY_BIRD.getCode()) {
                    earlyBirdRefundCount++;
                } else {
                    normalRefundCount++;
                }
                
                // 收集关联ID
                activityOrderTravelerIds.add(traveler.getId());
            }
        }
        
        try {
            // 如果有出行人ID列表，则回滚库存并删除相关记录
            if (travelerIds != null && !travelerIds.isEmpty()) {
                // 1. 回滚部分库存
                int rows = activityMapper.update(null,
                        new LambdaUpdateWrapper<Activity>()
                                .setSql("early_bird_quota = early_bird_quota + " + earlyBirdRefundCount)
                                .setSql("normal_quota = normal_quota + " + normalRefundCount)
                                .setSql("total_sold = total_sold - " + (earlyBirdRefundCount + normalRefundCount))
                                .eq(Activity::getId, activity.getId())
                                .ge(Activity::getTotalSold, earlyBirdRefundCount + normalRefundCount)
                );
                
                if (rows == 0) {
                    throw new ServiceException("库存回滚失败");
                }
                
                // 2. 删除enrollment_traveler表中对应的出行人信息
                LambdaQueryWrapper<Enrollment> enrollmentQuery = new LambdaQueryWrapper<>();
                enrollmentQuery.eq(Enrollment::getOrderNo, orderNo);
                List<Enrollment> enrollments = enrollmentMapper.selectList(enrollmentQuery);
                
                if (enrollments != null && !enrollments.isEmpty()) {
                    for (Enrollment enrollment : enrollments) {
                        for (Long travelerId : travelerIds) {
                            enrollmentTravelerMapper.delete(
                                    new LambdaQueryWrapper<EnrollmentTraveler>()
                                            .eq(EnrollmentTraveler::getEnrollmentId, enrollment.getId())
                                            .eq(EnrollmentTraveler::getTravelerId, travelerId)
                            );
                        }
                    }
                }
                
                // 3. 删除activity_order_traveler表中对应的出行人信息
                for (Long aotId : activityOrderTravelerIds) {
                    activityOrderTravelerMapper.deleteById(aotId);
                }
                
                log.info("已删除{}个出行人关联记录，订单号：{}", travelerIds.size(), orderNo);
                
                // 检查该订单是否还有关联的出行人
                int remainingTravelers = activityOrderTravelerMapper.selectCount(
                        new LambdaQueryWrapper<ActivityOrderTraveler>()
                                .eq(ActivityOrderTraveler::getOrderNo, orderNo)
                ).intValue();
                
                // 如果没有剩余出行人，将订单状态更新为已取消
                if (remainingTravelers == 0) {
                    log.info("自定义退款后订单没有剩余出行人，将状态更新为已取消，订单号：{}", orderNo);
                    ActivityOrder updateOrder = new ActivityOrder();
                    updateOrder.setId(activityOrder.getId());
                    updateOrder.setStatus(ActivityOrderStatusEnum.CANCELED.getStatus());
                    
                    if (activityOrderMapper.updateById(updateOrder) == 0) {
                        throw new ServiceException("更新订单状态失败");
                    }
                    
                    return true; // 直接返回，不需要继续处理
                }
            }
            
            // 4. 更新activity_order中的价格
            ActivityOrder updateOrder = new ActivityOrder();
            updateOrder.setId(activityOrder.getId());

            
            // 计算退款后的价格
            BigDecimal newTotalAmount = activityOrder.getTotalAmount().subtract(refundAmount);
            
            // 检查退款后金额是否为0
            if (newTotalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                // 如果金额为0或小于0，将订单状态更新为已免单，不修改价格（保留原记录）
                log.info("自定义金额退款后订单金额小于等于0，将订单状态更新为已免单，订单号：{}", orderNo);
                updateOrder.setStatus(ActivityOrderStatusEnum.FEE_EXEMPTION.getStatus());
            } else {
                // 部分退款，更新价格
                updateOrder.setTotalAmount(newTotalAmount);
                
                // 如果有出行人ID列表，也更新票数量
                if (travelerIds != null && !travelerIds.isEmpty()) {
                    // 更新票数量
                    updateOrder.setEarlyBirdNum(activityOrder.getEarlyBirdNum() - earlyBirdRefundCount);
                    updateOrder.setNormalNum(activityOrder.getNormalNum() - normalRefundCount);
                }
            }
            
            if (activityOrderMapper.updateById(updateOrder) == 0) {
                throw new ServiceException("更新订单信息失败");
            }
            
            // 5. 申请微信支付退款
            log.info("开始申请自定义金额退款，订单号：{}，退款金额：{}", orderNo, refundAmount);
            
            // 生成商户退款单号（格式：custom_refund_订单号_时间戳）
            String outRefundNo = "custom_refund_" + orderNo + "_" + System.currentTimeMillis();
            log.info("生成自定义金额退款单号：{}", outRefundNo);
            
            // 查询订单原始总金额（从最早的退款记录中获取）
            BigDecimal originalTotalAmount = activityOrder.getTotalAmount();
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
                    originalTotalAmount = firstRecord.getTotalAmount();
                    log.info("从退款记录中获取到原始订单总金额: {}, 订单号: {}", originalTotalAmount, orderNo);
                } else {
                    log.info("未找到退款记录，使用订单当前总金额: {}, 订单号: {}", originalTotalAmount, orderNo);
                }
            } catch (Exception e) {
                log.warn("查询原始订单总金额异常，使用当前总金额: {}, 异常: {}", originalTotalAmount, e.getMessage());
            }
            
            // 金额单位转换：元 -> 分
            int totalAmountFen = originalTotalAmount.multiply(new BigDecimal(100)).intValue();
            int refundAmountFen = refundAmount.multiply(new BigDecimal(100)).intValue();
            
            // 生成真实退款原因
            String realReason = reason;
            if (travelerIds != null && !travelerIds.isEmpty()) {
                realReason = reason + "-" + travelerIds.size() + "位出行人";
            }
            
            // 调用退款服务
            Refund refund;
            try {
                if ("prod".equals(activeProfile)) {
                    // 生产环境：使用云托管退款API
                    log.info("使用微信云托管环境申请自定义金额退款，环境ID: {}, 订单号: {}, 原始总金额: {}, 退款金额: {}", 
                             cloudEnvId, orderNo, totalAmountFen, refundAmountFen);
                    refund = wxRefundUtil.createRefundWithCloudEnv(
                            orderNo, 
                            outRefundNo, 
                            totalAmountFen, 
                            refundAmountFen, 
                            realReason,
                            cloudContainerService,
                            cloudRefundPath
                    );
                } else {
                    // 开发环境：使用原有的退款方式
                    log.info("使用原生SDK申请自定义金额退款，订单号: {}, 原始总金额: {}, 退款金额: {}", 
                             orderNo, totalAmountFen, refundAmountFen);
                    refund = wxRefundUtil.createRefund(
                            orderNo, 
                            outRefundNo, 
                            totalAmountFen, 
                            refundAmountFen, 
                            realReason
                    );
                }
                
                // 保存退款记录
                WxRefundRecord refundRecord = new WxRefundRecord();
                refundRecord.setOutTradeNo(orderNo);
                refundRecord.setOutRefundNo(outRefundNo);
                refundRecord.setRefundId(refund.getRefundId());
                refundRecord.setTotalAmount(originalTotalAmount);
                refundRecord.setRefundAmount(refundAmount);
                refundRecord.setRefundStatus(refund.getStatus().toString());
                refundRecord.setReason(realReason);
                refundRecord.setCreateTime(LocalDateTime.now());
                refundRecord.setUpdateTime(LocalDateTime.now());
                
                // 微信支付退款成功时间可能为null
                if (refund.getSuccessTime() != null) {
                    refundRecord.setSuccessTime(LocalDateTime.now());
                }
                
                wxRefundRecordMapper.insert(refundRecord);
                log.info("自定义金额退款记录保存成功, 商户退款单号: {}", outRefundNo);
                
                return true;
            } catch (Exception e) {
                log.error("自定义金额退款申请失败：{}", e.getMessage(), e);
                throw new ServiceException("自定义金额退款申请失败：" + e.getMessage());
            }
        } catch (Exception e) {
            log.error("自定义金额退款失败: {}", e.getMessage(), e);
            throw new ServiceException("申请自定义金额退款失败: " + e.getMessage());
        }
    }

    /**
     * 根据出行人ID列表删除指定出行人
     *
     * @param orderNo 订单号
     * @param travelerIds 需要删除的出行人ID列表
     * @param reason 操作原因
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTravelers(String orderNo, List<Long> travelerIds, String reason) {
        // 查询订单 - 管理员接口不需要openid条件
        ActivityOrder activityOrder = activityOrderMapper.selectOne(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getOrderNo, orderNo)
        );
        
        if (activityOrder == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 检查订单状态必须为已支付
        if (activityOrder.getStatus() != ActivityOrderStatusEnum.PAID.getStatus()) {
            throw new ServiceException("只有已支付的订单才能进行操作");
        }
        
        // 检查出行人列表不能为空
        if (travelerIds == null || travelerIds.isEmpty()) {
            throw new ServiceException("出行人ID列表不能为空");
        }
        
        // 查询活动信息
        Activity activity = activityMapper.selectById(activityOrder.getActivityId());
        if (activity == null) {
            throw new ServiceException("活动不存在");
        }
        
        // 处理出行人相关逻辑
        int earlyBirdRefundCount = 0;
        int normalRefundCount = 0;
        List<Long> activityOrderTravelerIds = new ArrayList<>();
        
        // 获取订单下所有出行人信息
        List<ActivityOrderTraveler> allOrderTravelers = activityOrderTravelerMapper.selectList(
                new LambdaQueryWrapper<ActivityOrderTraveler>()
                        .eq(ActivityOrderTraveler::getOrderNo, orderNo)
        );
        
        if (allOrderTravelers == null || allOrderTravelers.isEmpty()) {
            throw new ServiceException("订单下无出行人信息");
        }
        
        // 筛选出要删除的出行人列表
        List<ActivityOrderTraveler> selectedTravelers = allOrderTravelers.stream()
                .filter(aot -> travelerIds.contains(aot.getTravelerId()))
                .collect(Collectors.toList());
        
        if (selectedTravelers.size() != travelerIds.size()) {
            throw new ServiceException("部分出行人信息不存在或不属于该订单");
        }
        
        // 统计票类型数量（用于库存回滚）
        for (ActivityOrderTraveler traveler : selectedTravelers) {
            if (traveler.getTickType() == TickTypeEnum.EARLY_BIRD.getCode()) {
                earlyBirdRefundCount++;
            } else {
                normalRefundCount++;
            }
            
            // 收集关联ID
            activityOrderTravelerIds.add(traveler.getId());
        }
        
        try {
            // 1. 回滚部分库存
            int rows = activityMapper.update(null,
                    new LambdaUpdateWrapper<Activity>()
                            .setSql("early_bird_quota = early_bird_quota + " + earlyBirdRefundCount)
                            .setSql("normal_quota = normal_quota + " + normalRefundCount)
                            .setSql("total_sold = total_sold - " + (earlyBirdRefundCount + normalRefundCount))
                            .eq(Activity::getId, activity.getId())
                            .ge(Activity::getTotalSold, earlyBirdRefundCount + normalRefundCount)
            );
            
            if (rows == 0) {
                throw new ServiceException("库存回滚失败");
            }
            
            // 2. 删除enrollment_traveler表中对应的出行人信息
            LambdaQueryWrapper<Enrollment> enrollmentQuery = new LambdaQueryWrapper<>();
            enrollmentQuery.eq(Enrollment::getOrderNo, orderNo);
            List<Enrollment> enrollments = enrollmentMapper.selectList(enrollmentQuery);
            
            if (enrollments != null && !enrollments.isEmpty()) {
                for (Enrollment enrollment : enrollments) {
                    for (Long travelerId : travelerIds) {
                        enrollmentTravelerMapper.delete(
                                new LambdaQueryWrapper<EnrollmentTraveler>()
                                        .eq(EnrollmentTraveler::getEnrollmentId, enrollment.getId())
                                        .eq(EnrollmentTraveler::getTravelerId, travelerId)
                        );
                    }
                }
            }
            
            // 3. 删除activity_order_traveler表中对应的出行人信息
            for (Long aotId : activityOrderTravelerIds) {
                activityOrderTravelerMapper.deleteById(aotId);
            }
            
            log.info("已删除{}个出行人关联记录，订单号：{}", travelerIds.size(), orderNo);
            
            // 4. 更新activity_order中的价格和票数量
            ActivityOrder updateOrder = new ActivityOrder();
            updateOrder.setId(activityOrder.getId());
            
            // 更新票数量
            updateOrder.setEarlyBirdNum(activityOrder.getEarlyBirdNum() - earlyBirdRefundCount);
            updateOrder.setNormalNum(activityOrder.getNormalNum() - normalRefundCount);
            
            // 新增：检查该订单是否还有关联的出行人
            int remainingTravelers = activityOrderTravelerMapper.selectCount(
                    new LambdaQueryWrapper<ActivityOrderTraveler>()
                            .eq(ActivityOrderTraveler::getOrderNo, orderNo)
            ).intValue();
            
            // 如果没有剩余出行人，将订单状态更新为已取消
            if (remainingTravelers == 0) {
                log.info("订单{}删除出行人后没有剩余出行人，将状态更新为已取消", orderNo);
                updateOrder.setStatus(ActivityOrderStatusEnum.CANCELED.getStatus());
            }
            
            if (activityOrderMapper.updateById(updateOrder) == 0) {
                throw new ServiceException("更新订单信息失败");
            }
            
            return true;
        } catch (Exception e) {
            log.error("删除出行人失败，订单号：{}，错误信息：{}", orderNo, e.getMessage());
            throw new ServiceException("删除出行人失败: " + e.getMessage());
        }
    }

    /**
     * 指定金额退款
     *
     * @param orderNo 订单号
     * @param reason 退款原因
     * @param refundAmount 退款金额
     * @param orderStatus 指定订单状态(可选)，用于更新订单状态
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean amountRefund(String orderNo, String reason, BigDecimal refundAmount, Integer orderStatus) {
        // 查询订单 - 管理员接口不需要openid条件
        ActivityOrder activityOrder = activityOrderMapper.selectOne(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getOrderNo, orderNo)
        );
        
        if (activityOrder == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 检查订单状态必须为已支付
        if (activityOrder.getStatus() != ActivityOrderStatusEnum.PAID.getStatus()) {
            throw new ServiceException("只有已支付的订单才能进行退款");
        }
        
        // 校验退款金额不能超过订单当前总金额
        if (refundAmount.compareTo(activityOrder.getTotalAmount()) > 0) {
            throw new ServiceException("退款金额不能超过订单总金额");
        }
        
        // 校验退款金额必须大于0.01
        if (refundAmount.compareTo(new BigDecimal("0.01")) < 0) {
            throw new ServiceException("退款金额必须大于0.01元");
        }
        
        // 校验退款金额最多只能有两位小数
        String refundAmountStr = refundAmount.toPlainString();
        if (refundAmountStr.contains(".")) {
            String[] parts = refundAmountStr.split("\\.");
            if (parts.length > 1 && parts[1].length() > 2) {
                throw new ServiceException("退款金额最多只能有两位小数");
            }
        }
        
        try {
            // 更新activity_order中的价格
            ActivityOrder updateOrder = new ActivityOrder();
            updateOrder.setId(activityOrder.getId());
            

            
            // 计算退款后的价格
            BigDecimal newTotalAmount = activityOrder.getTotalAmount().subtract(refundAmount);
            
            // 校验退款后的金额
            if (newTotalAmount.compareTo(BigDecimal.ZERO) < 0) {
                throw new ServiceException("退款后金额不能为负数");
            }
            
            // 在任何情况下，都更新订单金额为退款后的金额
            if (newTotalAmount.compareTo(BigDecimal.ZERO) > 0) {
                // 只有在退款后金额大于0时才更新金额
                updateOrder.setTotalAmount(newTotalAmount);
                
                // 校验退款后的金额最多只能有两位小数
                String newTotalAmountStr = newTotalAmount.toPlainString();
                if (newTotalAmountStr.contains(".")) {
                    String[] parts = newTotalAmountStr.split("\\.");
                    if (parts.length > 1 && parts[1].length() > 2) {
                        throw new ServiceException("退款后金额最多只能有两位小数");
                    }
                }
            }
            
            // 处理订单状态
            // 1. 优先处理指定了已取消状态的情况
            if (orderStatus != null && orderStatus == ActivityOrderStatusEnum.CANCELED.getStatus()) {
                // 如果传入已取消状态，则将订单状态更新为已取消
                log.info("指定金额退款，根据传入参数将订单状态更新为已取消，订单号：{}", orderNo);
                updateOrder.setStatus(ActivityOrderStatusEnum.CANCELED.getStatus());
                
                // 添加删除报名人信息和订单出行人信息的逻辑
                try {
                    // 1. 查询该订单下的所有出行人信息
                    List<ActivityOrderTraveler> allOrderTravelers = activityOrderTravelerMapper.selectList(
                            new LambdaQueryWrapper<ActivityOrderTraveler>()
                                    .eq(ActivityOrderTraveler::getOrderNo, orderNo)
                    );
                    
                    if (allOrderTravelers != null && !allOrderTravelers.isEmpty()) {
                        // 收集出行人ID和活动订单出行人ID
                        List<Long> travelerIds = new ArrayList<>();
                        List<Long> activityOrderTravelerIds = new ArrayList<>();
                        
                        // 用于库存回滚的计数
                        int earlyBirdRefundCount = 0;
                        int normalRefundCount = 0;
                        
                        for (ActivityOrderTraveler traveler : allOrderTravelers) {
                            travelerIds.add(traveler.getTravelerId());
                            activityOrderTravelerIds.add(traveler.getId());
                            
                            // 统计票类型数量（用于库存回滚）
                            if (traveler.getTickType() == TickTypeEnum.EARLY_BIRD.getCode()) {
                                earlyBirdRefundCount++;
                            } else {
                                normalRefundCount++;
                            }
                        }
                        
                        // 处理库存回滚
                        Activity activity = activityMapper.selectById(activityOrder.getActivityId());
                        if (activity != null && (earlyBirdRefundCount > 0 || normalRefundCount > 0)) {
                            int rows = activityMapper.update(null,
                                    new LambdaUpdateWrapper<Activity>()
                                            .setSql("early_bird_quota = early_bird_quota + " + earlyBirdRefundCount)
                                            .setSql("normal_quota = normal_quota + " + normalRefundCount)
                                            .setSql("total_sold = total_sold - " + (earlyBirdRefundCount + normalRefundCount))
                                            .eq(Activity::getId, activity.getId())
                                            .ge(Activity::getTotalSold, earlyBirdRefundCount + normalRefundCount)
                            );
                            
                            if (rows > 0) {
                                log.info("指定金额退款，订单状态为已取消，已回滚库存：早鸟票{}张，普通票{}张，订单号：{}", 
                                        earlyBirdRefundCount, normalRefundCount, orderNo);
                            } else {
                                log.warn("指定金额退款，订单状态为已取消，库存回滚失败，订单号：{}", orderNo);
                            }
                        }
                        
                        // 2. 删除enrollment_traveler表中对应的出行人信息
                        LambdaQueryWrapper<Enrollment> enrollmentQuery = new LambdaQueryWrapper<>();
                        enrollmentQuery.eq(Enrollment::getOrderNo, orderNo);
                        List<Enrollment> enrollments = enrollmentMapper.selectList(enrollmentQuery);
                        
                        if (enrollments != null && !enrollments.isEmpty()) {
                            for (Enrollment enrollment : enrollments) {
                                for (Long travelerId : travelerIds) {
                                    enrollmentTravelerMapper.delete(
                                            new LambdaQueryWrapper<EnrollmentTraveler>()
                                                    .eq(EnrollmentTraveler::getEnrollmentId, enrollment.getId())
                                                    .eq(EnrollmentTraveler::getTravelerId, travelerId)
                                    );
                                }
                            }
                        }
                        
                        // 3. 删除activity_order_traveler表中对应的出行人信息
                        for (Long aotId : activityOrderTravelerIds) {
                            activityOrderTravelerMapper.deleteById(aotId);
                        }
                        
                        log.info("指定金额退款，订单状态为已取消，已删除{}个出行人关联记录，订单号：{}", travelerIds.size(), orderNo);
                    } else {
                        log.info("指定金额退款，订单状态为已取消，但订单下无出行人信息，订单号：{}", orderNo);
                    }
                } catch (Exception e) {
                    log.error("指定金额退款，删除出行人关联记录失败，订单号：{}，错误信息：{}", orderNo, e.getMessage());
                    // 注意：此处不抛出异常，让退款流程继续执行
                }
            }
            // 2. 如果退款后金额为0且未指定已取消状态，则设为已免单
            else if (newTotalAmount.compareTo(BigDecimal.ZERO) == 0 || newTotalAmount.compareTo(new BigDecimal("0.01")) < 0) {
                log.info("指定金额退款后订单金额小于等于0.01，将订单状态更新为已免单，订单号：{}", orderNo);
                updateOrder.setStatus(ActivityOrderStatusEnum.FEE_EXEMPTION.getStatus());
            } 
            // 3. 处理其他指定订单状态的情况
            else if (orderStatus != null) {
                if (orderStatus == ActivityOrderStatusEnum.PAID.getStatus()) {
                    // 如果传入已支付状态，保持状态不变，但仍更新金额
                    log.info("指定金额退款，传入已支付状态，保持订单状态不变，更新订单金额，订单号：{}", orderNo);
                } else {
                    log.warn("指定金额退款，传入的订单状态值无效：{}，忽略状态更新但仍更新金额，订单号：{}", orderStatus, orderNo);
                }
            }
            // 4. 如果退款后金额大于0，且未指定订单状态，不更改状态，仅更新金额
            
            // 检查updateOrder中是否有要更新的字段
            boolean hasUpdateField = 
                updateOrder.getStatus() != null || 
                updateOrder.getTotalAmount() != null || 
                updateOrder.getEarlyBirdNum() != null ||
                updateOrder.getNormalNum() != null ||
                updateOrder.getRefundStatus() != null;
            
            // 只有当有字段需要更新时，才执行更新操作
            if (hasUpdateField && activityOrderMapper.updateById(updateOrder) == 0) {
                throw new ServiceException("更新订单信息失败");
            } else if (!hasUpdateField) {
                log.info("订单{}没有需要更新的字段，跳过更新操作", orderNo);
            }
            
            // 申请微信支付退款
            log.info("开始申请指定金额退款，订单号：{}，退款金额：{}", orderNo, refundAmount);
            
            // 生成商户退款单号（格式：amount_refund_订单号_时间戳）
            String outRefundNo = "amount_refund_" + orderNo + "_" + System.currentTimeMillis();
            log.info("生成指定金额退款单号：{}", outRefundNo);
            
            // 查询订单原始总金额（从最早的退款记录中获取）
            BigDecimal originalTotalAmount = activityOrder.getTotalAmount();
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
                    originalTotalAmount = firstRecord.getTotalAmount();
                    log.info("从退款记录中获取到原始订单总金额: {}, 订单号: {}", originalTotalAmount, orderNo);
                } else {
                    log.info("未找到退款记录，使用订单当前总金额: {}, 订单号: {}", originalTotalAmount, orderNo);
                }
            } catch (Exception e) {
                log.warn("查询原始订单总金额异常，使用当前总金额: {}, 异常: {}", originalTotalAmount, e.getMessage());
            }
            
            // 金额单位转换：元 -> 分
            int totalAmountFen = originalTotalAmount.multiply(new BigDecimal(100)).intValue();
            int refundAmountFen = refundAmount.multiply(new BigDecimal(100)).intValue();
            
            // 调用退款服务
            Refund refund;
            try {
                if ("prod".equals(activeProfile)) {
                    // 生产环境：使用云托管退款API
                    log.info("使用微信云托管环境申请指定金额退款，环境ID: {}, 订单号: {}, 原始总金额: {}, 退款金额: {}", 
                             cloudEnvId, orderNo, totalAmountFen, refundAmountFen);
                    refund = wxRefundUtil.createRefundWithCloudEnv(
                            orderNo, 
                            outRefundNo, 
                            totalAmountFen, 
                            refundAmountFen, 
                            reason,
                            cloudContainerService,
                            cloudRefundPath
                    );
                } else {
                    // 开发环境：使用原有的退款方式
                    log.info("使用原生SDK申请指定金额退款，订单号: {}, 原始总金额: {}, 退款金额: {}", 
                             orderNo, totalAmountFen, refundAmountFen);
                    refund = wxRefundUtil.createRefund(
                            orderNo, 
                            outRefundNo, 
                            totalAmountFen, 
                            refundAmountFen, 
                            reason
                    );
                }
                
                // 保存退款记录
                WxRefundRecord refundRecord = new WxRefundRecord();
                refundRecord.setOutTradeNo(orderNo);
                refundRecord.setOutRefundNo(outRefundNo);
                refundRecord.setRefundId(refund.getRefundId());
                refundRecord.setTotalAmount(originalTotalAmount);
                refundRecord.setRefundAmount(refundAmount);
                refundRecord.setRefundStatus(refund.getStatus().toString());
                refundRecord.setReason(reason);
                refundRecord.setCreateTime(LocalDateTime.now());
                refundRecord.setUpdateTime(LocalDateTime.now());
                
                // 微信支付退款成功时间可能为null
                if (refund.getSuccessTime() != null) {
                    refundRecord.setSuccessTime(LocalDateTime.now());
                }
                
                wxRefundRecordMapper.insert(refundRecord);
                log.info("指定金额退款记录保存成功, 商户退款单号: {}", outRefundNo);
                
                return true;
            } catch (Exception e) {
                log.error("申请指定金额退款失败，订单号：{}，错误信息：{}", orderNo, e.getMessage());
                throw new ServiceException("申请退款失败: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("指定金额退款失败，订单号：{}，错误信息：{}", orderNo, e.getMessage());
            throw new ServiceException("退款失败: " + e.getMessage());
        }
    }
}
