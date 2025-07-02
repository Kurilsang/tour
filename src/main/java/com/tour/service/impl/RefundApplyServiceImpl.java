package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tour.common.constant.Constants;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.WxRefundUtil;
import com.tour.dao.ActivityOrderMapper;
import com.tour.dao.ProductMapper;
import com.tour.dao.ProductOrderMapper;
import com.tour.dao.RefundApplyMapper;
import com.tour.dao.ActivityMapper;
import com.tour.dao.ActivityOrderTravelerMapper;
import com.tour.dao.RefundApplyTravelerMapper;
import com.tour.dao.TravelerMapper;
import com.tour.dao.EnrollmentMapper;
import com.tour.dao.EnrollmentTravelerMapper;
import com.tour.dao.WxRefundRecordMapper;
import com.tour.dto.PartialRefundApplyDTO;
import com.tour.dto.ProductRefundDTO;
import com.tour.dto.RefundApplyDTO;
import com.tour.dto.WxRefundRequestDTO;
import com.tour.enums.ActivityOrderStatusEnum;
import com.tour.enums.OrderRefundStatusEnum;
import com.tour.enums.OrderType;
import com.tour.enums.PageSize;
import com.tour.enums.RefundStatus;
import com.tour.enums.TickTypeEnum;
import com.tour.model.ActivityOrder;
import com.tour.model.ActivityOrderTraveler;
import com.tour.model.Product;
import com.tour.model.ProductOrder;
import com.tour.model.RefundApply;
import com.tour.model.RefundApplyTraveler;
import com.tour.model.Activity;
import com.tour.model.Traveler;
import com.tour.model.Enrollment;
import com.tour.model.EnrollmentTraveler;
import com.tour.model.WxRefundRecord;
import com.tour.query.RefundApplyQuery;
import com.tour.service.ProductOrderService;
import com.tour.service.RefundApplyService;
import com.tour.service.SignUpService;
import com.tour.service.WxRefundService;
import com.tour.vo.RefundApplyVO;
import com.tour.vo.RefundTravelerVO;
import com.tour.vo.WxRefundResponseVO;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退款申请服务实现类
 *
 * @Author Kuril
 */
@Service
@Slf4j
public class RefundApplyServiceImpl implements RefundApplyService {

    private final RefundApplyMapper refundApplyMapper;
    private final ActivityOrderMapper activityOrderMapper;
    private final ProductOrderMapper productOrderMapper;
    private final ActivityMapper activityMapper;
    private final ProductMapper productMapper;
    private final ProductOrderService productOrderService;
    private final SignUpService signUpService;
    private final RefundApplyTravelerMapper refundApplyTravelerMapper;
    private final ActivityOrderTravelerMapper activityOrderTravelerMapper;
    private final TravelerMapper travelerMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final EnrollmentTravelerMapper enrollmentTravelerMapper;
    private final WxRefundService wxRefundService;
    private final WxRefundUtil wxRefundUtil;
    private final WxRefundRecordMapper wxRefundRecordMapper;
    
    @Value("${spring.profiles.active}")
    private String activeProfile;
    
    @Value("${wechat.pay.cloud.env-id:}")
    private String cloudEnvId;
    
    @Value("${wechat.pay.cloud.container.service:}")
    private String cloudContainerService;
    
    @Value("${wechat.pay.cloud.container.refund-path:}")
    private String cloudRefundPath;

    public RefundApplyServiceImpl(RefundApplyMapper refundApplyMapper,
                                 ActivityOrderMapper activityOrderMapper,
                                 ProductOrderMapper productOrderMapper,
                                 ActivityMapper activityMapper,
                                 ProductMapper productMapper,
                                 ProductOrderService productOrderService,
                                 SignUpService signUpService,
                                 RefundApplyTravelerMapper refundApplyTravelerMapper,
                                 ActivityOrderTravelerMapper activityOrderTravelerMapper,
                                 TravelerMapper travelerMapper,
                                 EnrollmentMapper enrollmentMapper,
                                 EnrollmentTravelerMapper enrollmentTravelerMapper,
                                 WxRefundService wxRefundService,
                                 WxRefundUtil wxRefundUtil,
                                 WxRefundRecordMapper wxRefundRecordMapper) {
        this.refundApplyMapper = refundApplyMapper;
        this.activityOrderMapper = activityOrderMapper;
        this.productOrderMapper = productOrderMapper;
        this.activityMapper = activityMapper;
        this.productMapper = productMapper;
        this.productOrderService = productOrderService;
        this.signUpService = signUpService;
        this.refundApplyTravelerMapper = refundApplyTravelerMapper;
        this.activityOrderTravelerMapper = activityOrderTravelerMapper;
        this.travelerMapper = travelerMapper;
        this.enrollmentMapper = enrollmentMapper;
        this.enrollmentTravelerMapper = enrollmentTravelerMapper;
        this.wxRefundService = wxRefundService;
        this.wxRefundUtil = wxRefundUtil;
        this.wxRefundRecordMapper = wxRefundRecordMapper;
    }

    /**
     * 创建退款申请
     *
     * @param dto 退款申请DTO
     * @param openid 用户openid
     * @return 退款申请ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRefundApply(RefundApplyDTO dto, String openid) {
        log.info("创建退款申请，订单号：{}，用户openid：{}", dto.getOutTradeOrder(), openid);

        // 检查订单是否存在
        String orderNo = dto.getOutTradeOrder();
        Integer orderType = dto.getOrderType();

        // 检查是否可以申请退款(包括检查是否已有申请)
        checkRefundStatus(orderNo);

        // 计算退款金额
        BigDecimal refundAmount = BigDecimal.ZERO;
        
        if (OrderType.ACTIVITY.getCode() == orderType) {
            // 检查活动订单
            ActivityOrder activityOrder = activityOrderMapper.selectOne(
                    new LambdaQueryWrapper<ActivityOrder>()
                            .eq(ActivityOrder::getOrderNo, orderNo)
                            .eq(ActivityOrder::getOpenid, openid)
            );
            
            if (activityOrder == null) {
                log.error("活动订单不存在，订单号：{}", orderNo);
                throw new ServiceException("订单不存在");
            }
            
            // 检查退款时间
            checkActivityRefundTime(orderNo, openid);
            
            // 更新活动订单的退款状态为"已申请退款"
            activityOrder.setRefundStatus(OrderRefundStatusEnum.APPLIED.getCode().longValue());
            activityOrderMapper.updateById(activityOrder);
            log.info("已更新活动订单退款状态为已申请退款，订单号：{}", orderNo);
            
            // 全额退款情况下，退款金额为订单总金额
            refundAmount = activityOrder.getTotalAmount();
            
        } else if (OrderType.PRODUCT.getCode() == orderType) {
            // 检查商品订单
            ProductOrder productOrder = productOrderMapper.selectOne(
                    new LambdaQueryWrapper<ProductOrder>()
                            .eq(ProductOrder::getOrderNo, orderNo)
                            .eq(ProductOrder::getOpenid, openid)
            );
            
            if (productOrder == null) {
                log.error("商品订单不存在，订单号：{}", orderNo);
                throw new ServiceException("订单不存在");
            }
            
            // 商品订单不需要检查退款截止时间
            
            // 更新商品订单的退款状态为"已申请退款"
            productOrder.setRefundStatus(OrderRefundStatusEnum.APPLIED.getCode());
            productOrderMapper.updateById(productOrder);
            log.info("已更新商品订单退款状态为已申请退款，订单号：{}", orderNo);
            
            // 全额退款情况下，退款金额为订单总金额
            refundAmount = productOrder.getTotalAmount();
            
        } else {
            throw new ServiceException("订单类型错误");
        }

        // 创建退款申请
        RefundApply refundApply = new RefundApply();
        refundApply.setOutTradeOrder(orderNo);
        refundApply.setOpenid(openid);
        refundApply.setStatus(RefundStatus.PENDING.getCode());
        refundApply.setReason(dto.getReason());
        refundApply.setOrderType(orderType);
        refundApply.setRefundAmount(refundAmount); // 设置退款金额
        refundApply.setCreateTime(LocalDateTime.now());
        refundApply.setUpdateTime(LocalDateTime.now());

        refundApplyMapper.insert(refundApply);
        log.info("退款申请创建成功，ID：{}，退款金额：{}", refundApply.getId(), refundAmount);

        return refundApply.getId();
    }

    /**
     * 根据订单号查询退款申请
     *
     * @param outTradeOrder 订单号
     * @return 退款申请VO
     */
    @Override
    public RefundApplyVO queryRefundApply(String outTradeOrder) {
        log.info("查询退款申请，订单号：{}", outTradeOrder);

        // 修改为查询多条记录，按创建时间倒序排序
        List<RefundApply> refundApplies = refundApplyMapper.selectList(
                new LambdaQueryWrapper<RefundApply>()
                        .eq(RefundApply::getOutTradeOrder, outTradeOrder)
                        .orderByDesc(RefundApply::getCreateTime)
        );

        if (refundApplies == null || refundApplies.isEmpty()) {
            log.error("退款申请不存在，订单号：{}", outTradeOrder);
            throw new ServiceException("退款申请不存在");
        }

        // 取最新的一条记录
        RefundApply refundApply = refundApplies.get(0);
        log.info("找到最新的退款申请记录，ID：{}，订单号：{}", refundApply.getId(), outTradeOrder);

        RefundApplyVO vo = new RefundApplyVO();
        BeanUtils.copyProperties(refundApply, vo);
        
        // 设置状态和订单类型的描述
        vo.setStatusDesc(RefundStatus.getDescByCode(refundApply.getStatus()));
        vo.setOrderTypeDesc(OrderType.getDescByCode(refundApply.getOrderType()));
        
        // 设置退款金额（使用保存的退款金额）
        vo.setRefundAmount(refundApply.getRefundAmount());
        
        // 添加退款出行人信息（仅适用于活动订单）
        if (OrderType.ACTIVITY.getCode() == refundApply.getOrderType()) {
            List<RefundApplyTraveler> refundApplyTravelers = refundApplyTravelerMapper.selectByRefundApplyId(refundApply.getId());
            
            if (refundApplyTravelers != null && !refundApplyTravelers.isEmpty()) {
                // 有关联的出行人信息，说明是部分退款
                vo.setIsPartialRefund(true);
                
                // 统计票类型数量
                int earlyBirdCount = 0;
                int normalCount = 0;
                
                List<RefundTravelerVO> refundTravelerVOS = new ArrayList<>();
                for (RefundApplyTraveler rat : refundApplyTravelers) {
                    RefundTravelerVO travelerVO = new RefundTravelerVO();
                    travelerVO.setTravelerId(rat.getTravelerId());
                    travelerVO.setTickType(rat.getTickType());
                    travelerVO.setTickTypeDesc(rat.getTickType() == TickTypeEnum.EARLY_BIRD.getCode() ? 
                            TickTypeEnum.EARLY_BIRD.getDesc() : TickTypeEnum.NORMAL.getDesc());
                    travelerVO.setRefundAmount(rat.getRefundAmount());
                    
                    // 统计票类型数量
                    if (rat.getTickType() == TickTypeEnum.EARLY_BIRD.getCode()) {
                        earlyBirdCount++;
                    } else {
                        normalCount++;
                    }
                    
                    // 获取出行人信息
                    Traveler traveler = travelerMapper.selectById(rat.getTravelerId());
                    if (traveler != null) {
                        travelerVO.setTravelerName(traveler.getName());
                        travelerVO.setTravelerPhone(traveler.getPhone());
                    }
                    
                    refundTravelerVOS.add(travelerVO);
                }
                
                // 设置票数
                vo.setEarlyBirdNum(earlyBirdCount);
                vo.setNormalNum(normalCount);
                vo.setItemQuantity(earlyBirdCount + normalCount);
                vo.setRefundTravelers(refundTravelerVOS);
            }
        }

        // 添加订单信息
        addOrderInfoToVO(vo, refundApply);
        
        return vo;
    }
    
    /**
     * 加载退款申请列表
     *
     * @param refundApplyQuery 查询条件
     * @return 退款申请分页列表
     */
    @Override
    public IPage<RefundApplyVO> loadDataList(RefundApplyQuery refundApplyQuery) {
        log.info("查询退款申请列表，参数：{}", refundApplyQuery);
        
        // 构建分页参数
        Integer pageNo = refundApplyQuery.getPageNo();
        Integer pageSize = refundApplyQuery.getPageSize();
        if (pageNo == null) {
            pageNo = Constants.defaultPageNo;
        }
        if (pageSize == null) {
            pageSize = PageSize.SIZE10.getSize();
        }
        Page<RefundApply> page = new Page<>(pageNo, pageSize);
        
        // 构建查询条件
        QueryWrapper<RefundApply> queryWrapper = new QueryWrapper<>();
        
        // 动态添加查询条件
        if (refundApplyQuery.getOutTradeOrder() != null && !refundApplyQuery.getOutTradeOrder().isEmpty()) {
            queryWrapper.eq("out_trade_order", refundApplyQuery.getOutTradeOrder());
        }
        if (refundApplyQuery.getOpenid() != null && !refundApplyQuery.getOpenid().isEmpty()) {
            queryWrapper.eq("openid", refundApplyQuery.getOpenid());
        }
        if (refundApplyQuery.getStatus() != null) {
            queryWrapper.eq("status", refundApplyQuery.getStatus());
        }
        if (refundApplyQuery.getOrderType() != null) {
            queryWrapper.eq("order_type", refundApplyQuery.getOrderType());
        }
        
        // 处理时间范围查询
        if (refundApplyQuery.getStartTime() != null) {
            queryWrapper.ge("create_time", refundApplyQuery.getStartTime());
        }
        if (refundApplyQuery.getEndTime() != null) {
            queryWrapper.le("create_time", refundApplyQuery.getEndTime());
        }
        
        // 处理排序信息
        String orderBy = refundApplyQuery.getOrderBy();
        if (orderBy != null && !orderBy.isEmpty()) {
            queryWrapper.last("ORDER BY " + orderBy);
        } else {
            // 默认按创建时间降序排序
            queryWrapper.orderByDesc("create_time");
        }
        
        // 执行分页查询
        IPage<RefundApply> refundApplyPage = refundApplyMapper.selectPage(page, queryWrapper);
        
        // 创建新的VO分页对象
        Page<RefundApplyVO> voPage = new Page<>(refundApplyPage.getCurrent(), refundApplyPage.getSize(), refundApplyPage.getTotal());
        
        // 转换每条记录并添加订单信息
        List<RefundApplyVO> voList = refundApplyPage.getRecords().stream().map(refundApply -> {
            RefundApplyVO vo = new RefundApplyVO();
            BeanUtils.copyProperties(refundApply, vo);
            
            // 设置状态和订单类型的描述
            vo.setStatusDesc(RefundStatus.getDescByCode(refundApply.getStatus()));
            vo.setOrderTypeDesc(OrderType.getDescByCode(refundApply.getOrderType()));
            
            // 获取订单额外信息
            addOrderInfoToVO(vo, refundApply);
            
            // 检查是否是部分退款（仅适用于活动订单）
            if (OrderType.ACTIVITY.getCode() == refundApply.getOrderType()) {
                List<RefundApplyTraveler> refundApplyTravelers = refundApplyTravelerMapper.selectByRefundApplyId(refundApply.getId());
                
                if (refundApplyTravelers != null && !refundApplyTravelers.isEmpty()) {
                    // 有关联的出行人信息，说明是部分退款
                    vo.setIsPartialRefund(true);
                    
                    // 设置退款金额（使用保存的退款金额）
                    vo.setRefundAmount(refundApply.getRefundAmount());
                    
                    // 统计票类型数量
                    int earlyBirdCount = 0;  // 早鸟票数量
                    int normalCount = 0;     // 普通票数量
                    
                    for (RefundApplyTraveler rat : refundApplyTravelers) {
                        // 统计票类型数量
                        if (rat.getTickType() == TickTypeEnum.EARLY_BIRD.getCode()) {
                            earlyBirdCount++;
                        } else {
                            normalCount++;
                        }
                    }
                    
                    // 更新票数量
                    vo.setEarlyBirdNum(earlyBirdCount);   // 更新早鸟票数量
                    vo.setNormalNum(normalCount);         // 更新普通票数量
                    vo.setItemQuantity(earlyBirdCount + normalCount); // 更新总票数
                    
                    // 设置退款出行人信息
                    List<RefundTravelerVO> refundTravelerVOS = new ArrayList<>();
                    for (RefundApplyTraveler rat : refundApplyTravelers) {
                        RefundTravelerVO travelerVO = new RefundTravelerVO();
                        travelerVO.setTravelerId(rat.getTravelerId());
                        travelerVO.setTickType(rat.getTickType());
                        travelerVO.setTickTypeDesc(rat.getTickType() == TickTypeEnum.EARLY_BIRD.getCode() ? 
                                TickTypeEnum.EARLY_BIRD.getDesc() : TickTypeEnum.NORMAL.getDesc());
                        travelerVO.setRefundAmount(rat.getRefundAmount());
                        
                        // 获取出行人信息
                        Traveler traveler = travelerMapper.selectById(rat.getTravelerId());
                        if (traveler != null) {
                            travelerVO.setTravelerName(traveler.getName());
                            travelerVO.setTravelerPhone(traveler.getPhone());
                        }
                        
                        refundTravelerVOS.add(travelerVO);
                    }
                    
                    vo.setRefundTravelers(refundTravelerVOS);
                } else {
                    // 非部分退款，使用保存的退款金额
                    vo.setRefundAmount(refundApply.getRefundAmount());
                }
            } else {
                // 商品订单，使用保存的退款金额
                vo.setRefundAmount(refundApply.getRefundAmount());
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    /**
     * 拒绝退款申请
     *
     * @param id 退款申请ID
     * @param adminRemark 管理员备注
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refuseRefundApply(Long id, String adminRemark) {
        log.info("拒绝退款申请，ID：{}，管理员备注：{}", id, adminRemark);
        
        // 查询退款申请
        RefundApply refundApply = refundApplyMapper.selectById(id);
        if (refundApply == null) {
            log.error("退款申请不存在，ID：{}", id);
            throw new ServiceException("退款申请不存在");
        }
        
        // 检查状态是否为待审核
        if (refundApply.getStatus() != RefundStatus.PENDING.getCode()) {
            log.error("退款申请状态不正确，当前状态：{}，无法拒绝", RefundStatus.getDescByCode(refundApply.getStatus()));
            throw new ServiceException("退款申请状态不正确，无法拒绝");
        }
        
        // 获取订单号和订单类型
        String orderNo = refundApply.getOutTradeOrder();
        Integer orderType = refundApply.getOrderType();
        
        // 更新订单表的退款状态为未申请退款(0)
        boolean updateOrderResult = false;
        
        // 根据订单类型更新对应订单表
        if (OrderType.ACTIVITY.getCode() == orderType) {
            // 活动订单
            ActivityOrder activityOrder = activityOrderMapper.selectOne(
                    new LambdaQueryWrapper<ActivityOrder>()
                            .eq(ActivityOrder::getOrderNo, orderNo)
            );
            
            if (activityOrder != null) {
                activityOrder.setRefundStatus(OrderRefundStatusEnum.NOT_APPLIED.getCode().longValue());
                updateOrderResult = activityOrderMapper.updateById(activityOrder) > 0;
                log.info("更新活动订单退款状态为未申请退款，订单号：{}", orderNo);
            } else {
                log.warn("活动订单不存在，订单号：{}", orderNo);
            }
            
        } else if (OrderType.PRODUCT.getCode() == orderType) {
            // 商品订单
            ProductOrder productOrder = productOrderMapper.selectOne(
                    new LambdaQueryWrapper<ProductOrder>()
                            .eq(ProductOrder::getOrderNo, orderNo)
            );
            
            if (productOrder != null) {
                productOrder.setRefundStatus(OrderRefundStatusEnum.NOT_APPLIED.getCode());
                updateOrderResult = productOrderMapper.updateById(productOrder) > 0;
                log.info("更新商品订单退款状态为未申请退款，订单号：{}", orderNo);
            } else {
                log.warn("商品订单不存在，订单号：{}", orderNo);
            }
            
        } else {
            log.error("订单类型错误，orderType：{}", orderType);
            throw new ServiceException("订单类型错误");
        }
        
        if (!updateOrderResult) {
            log.warn("更新订单退款状态失败，订单号：{}", orderNo);
        }
        
        // 更新退款申请状态为拒绝退款
        refundApply.setStatus(RefundStatus.REJECTED.getCode());
        refundApply.setAdminRemark(adminRemark);
        refundApply.setUpdateTime(LocalDateTime.now());
        
        int rows = refundApplyMapper.updateById(refundApply);
        return rows > 0;
    }
    
    /**
     * 检查订单退款状态
     * 
     * @param outTradeOrder 订单号
     * @return 如果可以申请退款返回true，如果不能申请退款抛出异常
     */
    @Override
    public boolean checkRefundStatus(String outTradeOrder) {
        // 检查是否已有申请
        List<RefundApply> pendingApplies = refundApplyMapper.selectList(
                new LambdaQueryWrapper<RefundApply>()
                        .eq(RefundApply::getOutTradeOrder, outTradeOrder)
                        .eq(RefundApply::getStatus, RefundStatus.PENDING.getCode())
        );

        if (pendingApplies != null && !pendingApplies.isEmpty()) {
            // 存在待处理的退款申请，不允许再次申请
            log.error("该订单已有待处理的退款申请，订单号：{}", outTradeOrder);
            throw new ServiceException("该订单已申请退款，请勿重复申请");
        }
        
        // 查询是否有拒绝的退款申请（仅用于日志记录）
        List<RefundApply> rejectedApplies = refundApplyMapper.selectList(
                new LambdaQueryWrapper<RefundApply>()
                        .eq(RefundApply::getOutTradeOrder, outTradeOrder)
                        .eq(RefundApply::getStatus, RefundStatus.REJECTED.getCode())
        );
        
        if (rejectedApplies != null && !rejectedApplies.isEmpty()) {
            log.info("该订单有{}条已被拒绝的退款申请，允许重新申请，订单号：{}", 
                    rejectedApplies.size(), outTradeOrder);
        }
        
        // 没有待处理的退款申请，可以申请
        return true;
    }
    
    /**
     * 为退款申请VO添加订单相关信息
     * 
     * @param vo 退款申请VO
     * @param refundApply 退款申请实体
     */
    private void addOrderInfoToVO(RefundApplyVO vo, RefundApply refundApply) {
        String outTradeOrder = refundApply.getOutTradeOrder();
        
        // 根据订单类型获取额外信息
        if (OrderType.ACTIVITY.getCode() == refundApply.getOrderType()) {
            // 活动订单
            ActivityOrder activityOrder = activityOrderMapper.selectOne(
                    new LambdaQueryWrapper<ActivityOrder>()
                            .eq(ActivityOrder::getOrderNo, outTradeOrder)
            );
            
            if (activityOrder != null) {
                // 如果是部分退款（vo.getRefundTravelers不为空或isPartialRefund为true），则不覆盖金额和票数
                boolean isPartialRefund = (vo.getRefundTravelers() != null && !vo.getRefundTravelers().isEmpty()) || 
                        Boolean.TRUE.equals(vo.getIsPartialRefund());
                        
                if (!isPartialRefund) {
                    vo.setTotalAmount(activityOrder.getTotalAmount());
                    vo.setEarlyBirdNum(activityOrder.getEarlyBirdNum());
                    vo.setNormalNum(activityOrder.getNormalNum());
                    vo.setItemQuantity(activityOrder.getEarlyBirdNum() + activityOrder.getNormalNum());
                }
                
                // 获取活动名称
                Activity activity = activityMapper.selectById(activityOrder.getActivityId());
                if (activity != null) {
                    vo.setItemName(activity.getTitle());
                }
            }
        } else if (OrderType.PRODUCT.getCode() == refundApply.getOrderType()) {
            // 商品订单
            ProductOrder productOrder = productOrderMapper.selectOne(
                    new LambdaQueryWrapper<ProductOrder>()
                            .eq(ProductOrder::getOrderNo, outTradeOrder)
            );
            
            if (productOrder != null) {
                vo.setTotalAmount(productOrder.getTotalAmount());
                vo.setItemQuantity(productOrder.getQuantity());
                
                // 获取商品名称
                try {
                    Product product = productMapper.selectById(productOrder.getProductId());
                    if (product != null) {
                        vo.setItemName(product.getName());
                    }
                } catch (Exception e) {
                    log.warn("获取商品信息失败", e);
                }
            }
        }
    }

    /**
     * 接受退款申请
     *
     * @param id 退款申请ID
     * @param adminRemark 管理员备注
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptRefundApply(Long id, String adminRemark) {
        // 调用带指定金额的方法，传入null表示不指定金额
        return acceptRefundApply(id, adminRemark, null);
    }
    
    /**
     * 接受退款申请（带指定金额）
     *
     * @param id 退款申请ID
     * @param adminRemark 管理员备注
     * @param specifiedAmount 指定退款金额（可选，仅适用于活动订单）
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptRefundApply(Long id, String adminRemark, BigDecimal specifiedAmount) {
        log.info("接受退款申请，ID：{}，管理员备注：{}，指定金额：{}", id, adminRemark, specifiedAmount);
        
        // 如果指定了金额，进行校验
        if (specifiedAmount != null) {
            // 校验金额是否为0
            if (specifiedAmount.compareTo(BigDecimal.ZERO) == 0) {
                log.error("指定退款金额不能为0");
                throw new ServiceException("指定退款金额不能为0");
            }
            
            // 校验金额是否大于等于0.01
            if (specifiedAmount.compareTo(new BigDecimal("0.01")) < 0) {
                log.error("指定退款金额不能小于0.01");
                throw new ServiceException("指定退款金额不能小于0.01");
            }
            
            // 校验小数位不超过两位
            if (specifiedAmount.scale() > 2) {
                log.error("指定退款金额小数位不能超过两位");
                throw new ServiceException("指定退款金额小数位不能超过两位");
            }
            
            log.info("指定退款金额校验通过：{}", specifiedAmount);
        }
        
        // 查询退款申请
        RefundApply refundApply = refundApplyMapper.selectById(id);
        if (refundApply == null) {
            log.error("退款申请不存在，ID：{}", id);
            throw new ServiceException("退款申请不存在");
        }
        
        // 检查状态是否为待审核
        if (refundApply.getStatus() != RefundStatus.PENDING.getCode()) {
            log.error("退款申请状态不正确，当前状态：{}，无法处理", RefundStatus.getDescByCode(refundApply.getStatus()));
            throw new ServiceException("退款申请状态不正确，无法处理");
        }
        
        // 获取订单信息
        String orderNo = refundApply.getOutTradeOrder();
        String openid = refundApply.getOpenid();
        Integer orderType = refundApply.getOrderType();
        String reason = refundApply.getReason();
        
        // 更新状态为退款中
        refundApply.setStatus(RefundStatus.REFUNDING.getCode());
        refundApply.setAdminRemark(adminRemark);
        refundApply.setUpdateTime(LocalDateTime.now());
        
        // 如果指定了退款金额，更新refundApply中的refundAmount字段
        if (specifiedAmount != null && OrderType.ACTIVITY.getCode() == orderType) {
            log.info("更新退款申请的退款金额，原金额：{}，新金额：{}", refundApply.getRefundAmount(), specifiedAmount);
            refundApply.setRefundAmount(specifiedAmount);
        }
        
        refundApplyMapper.updateById(refundApply);
        
        boolean refundResult = false;
        
        try {
            // 根据订单类型调用不同的退款服务
            if (OrderType.ACTIVITY.getCode() == orderType) {
                // 检查是否是部分退款（查询是否有关联的出行人信息）
                List<RefundApplyTraveler> refundApplyTravelers = refundApplyTravelerMapper.selectByRefundApplyId(refundApply.getId());
                boolean isPartialRefund = refundApplyTravelers != null && !refundApplyTravelers.isEmpty();
                
                if (isPartialRefund) {
                    // 部分退款处理逻辑
                    log.info("进行部分退款处理，订单号：{}，退款申请ID：{}", orderNo, id);
                    
                    // 查询活动订单
                    ActivityOrder activityOrder = activityOrderMapper.selectOne(
                            new LambdaQueryWrapper<ActivityOrder>()
                                    .eq(ActivityOrder::getOrderNo, orderNo)
                                    .eq(ActivityOrder::getOpenid, openid)
                    );
                    
                    if (activityOrder == null) {
                        throw new ServiceException("订单不存在");
                    }
                    
                    // 查询活动信息，用于库存回滚
                    Activity activity = activityMapper.selectById(activityOrder.getActivityId());
                    if (activity == null) {
                        throw new ServiceException("活动不存在");
                    }
                    
                    // 统计部分退款的数量和金额
                    int earlyBirdRefundCount = 0;
                    int normalRefundCount = 0;
                    BigDecimal totalRefundAmount = BigDecimal.ZERO;
                    List<Long> travelerIds = new ArrayList<>();
                    List<Long> activityOrderTravelerIds = new ArrayList<>();
                    
                    for (RefundApplyTraveler rat : refundApplyTravelers) {
                        // 统计票类型数量
                        if (rat.getTickType() == TickTypeEnum.EARLY_BIRD.getCode()) {
                            earlyBirdRefundCount++;
                        } else {
                            normalRefundCount++;
                        }
                        
                        // 累加退款金额
                        totalRefundAmount = totalRefundAmount.add(rat.getRefundAmount());
                        
                        // 收集出行人ID和关联ID
                        travelerIds.add(rat.getTravelerId());
                        activityOrderTravelerIds.add(rat.getActivityOrderTravelerId());
                    }
                    
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
                            // 删除enrollment_traveler表中对应的出行人信息
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
                        updateOrder.setRefundStatus(OrderRefundStatusEnum.APPLIED.getCode().longValue());
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
                    
                    // 生成部分退款信息
                    String realReason = "部分退款-" + travelerIds.size() + "位出行人";
                    
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
                    
                    // 直接调用WxRefundUtil执行退款
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
                                    reason,
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
                                    reason
                            );
                        }
                        
                        // 保存退款记录
                        WxRefundRecord refundRecord = new WxRefundRecord();
                        refundRecord.setOutTradeNo(orderNo);
                        refundRecord.setOutRefundNo(outRefundNo);
                        refundRecord.setRefundId(refund.getRefundId());
                        refundRecord.setTotalAmount(originalTotalAmount); // 保存原始总金额
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
                        
                        refundResult = true;
                        log.info("部分退款申请成功，订单号：{}，退款单号：{}", orderNo, outRefundNo);
                    } catch (Exception e) {
                        log.error("部分退款申请失败：{}", e.getMessage(), e);
                        throw new ServiceException("部分退款申请失败：" + e.getMessage());
                    }
                } else {
                    // 全部退款，使用原有逻辑
                    log.info("进行全部退款处理，订单号：{}", orderNo);

                    WxRefundRequestDTO requestDTO = new WxRefundRequestDTO();
                    requestDTO.setOutTradeNo(orderNo);
                    requestDTO.setReason(reason);
                    
                    // 调用活动订单退款逻辑，使用指定金额
                    if (specifiedAmount != null && OrderType.ACTIVITY.getCode() == orderType) {
                        // 如果指定了金额且是活动订单，使用指定金额
                        log.info("使用指定金额退款，订单号：{}，指定金额：{}", orderNo, specifiedAmount);
                        
                        // 使用amountRefund方法处理指定金额退款并执行取消订单操作
                        // 指定订单状态为已取消(3)
                        refundResult = signUpService.amountRefund(orderNo, reason, specifiedAmount, ActivityOrderStatusEnum.CANCELED.getStatus());
                    } else {
                        // 使用原有逻辑
                        refundResult = signUpService.cancelOrder(orderNo, openid, Constants.deafultUserRefundReason, reason);
                    }
                }
            } else if (OrderType.PRODUCT.getCode() == orderType) {
                // 商品订单退款
                ProductRefundDTO productRefundDTO = new ProductRefundDTO();
                productRefundDTO.setOutTradeNo(orderNo);
                productRefundDTO.setReason(Constants.deafultUserRefundReason);
                
                // 调用商品订单退款逻辑，与ProductOrderController.userRefundOrder相同
                WxRefundResponseVO responseVO = productOrderService.refundOrder(productRefundDTO, openid, reason);
                refundResult = responseVO != null; // 如果返回非空，说明退款成功
                
            } else {
                log.error("订单类型错误，orderType：{}", orderType);
                throw new ServiceException("订单类型错误");
            }
            
            if (refundResult) {
                // 退款成功，更新状态为退款完成
                refundApply.setStatus(RefundStatus.COMPLETED.getCode());
                refundApply.setUpdateTime(LocalDateTime.now());
                refundApplyMapper.updateById(refundApply);
                log.info("退款成功，订单号：{}", orderNo);
            } else {
                // 退款失败，恢复状态为待审核
                refundApply.setStatus(RefundStatus.PENDING.getCode());
                refundApply.setUpdateTime(LocalDateTime.now());
                refundApplyMapper.updateById(refundApply);
                log.error("退款失败，订单号：{}", orderNo);
                throw new ServiceException("退款处理失败，请稍后重试");
            }
        } catch (Exception e) {
            // 发生异常，恢复状态为待审核
            refundApply.setStatus(RefundStatus.PENDING.getCode());
            refundApply.setUpdateTime(LocalDateTime.now());
            refundApplyMapper.updateById(refundApply);
            log.error("退款处理异常，订单号：{}，异常：{}", orderNo, e.getMessage(), e);
            throw new ServiceException("退款处理异常：" + e.getMessage());
        }
        
        return refundResult;
    }

    /**
     * 检查活动订单是否在可退款时间范围内
     * 
     * @param orderNo 活动订单号
     * @param openid 用户openid
     * @return 如果在可退款时间范围内返回true，否则抛出异常
     */
    @Override
    public boolean checkActivityRefundTime(String orderNo, String openid) {
        log.info("检查活动订单退款时间，订单号：{}，用户openid：{}", orderNo, openid);
        
        // 查询活动订单
        ActivityOrder activityOrder = activityOrderMapper.selectOne(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getOrderNo, orderNo)
                        .eq(ActivityOrder::getOpenid, openid)
        );
        
        if (activityOrder == null) {
            log.error("活动订单不存在，订单号：{}", orderNo);
            throw new ServiceException("订单不存在");
        }
        
        // 检查退款截止时间
        Long activityId = activityOrder.getActivityId();
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.error("活动不存在，活动ID：{}", activityId);
            throw new ServiceException("活动不存在");
        }
        
        // 获取可退款截止时间
        LocalDateTime refundDeadline = activity.getEndRefundTime();
        if (refundDeadline == null) {
            log.error("活动退款截止时间未设置，活动ID：{}", activityId);
            throw new ServiceException("活动退款配置有误");
        }
        
        // 检查当前时间是否超过退款截止时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(refundDeadline)) {
            log.error("已超过可退款截止时间，订单号：{}，退款截止时间：{}", orderNo, refundDeadline);
            throw new ServiceException("已超过可退款截止时间，无法申请退款");
        }
        
        return true;
    }

    /**
     * 创建部分出行人退款申请
     *
     * @param dto 部分退款申请DTO
     * @param openid 用户openid
     * @return 退款申请ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPartialRefundApply(PartialRefundApplyDTO dto, String openid) {
        log.info("创建部分出行人退款申请，订单号：{}，用户openid：{}", dto.getOutTradeOrder(), openid);

        // 检查订单是否存在
        String orderNo = dto.getOutTradeOrder();
        Integer orderType = dto.getOrderType();

        // 检查是否可以申请退款(检查订单级别的退款状态)
        checkRefundStatus(orderNo);

        // 部分退款只支持活动订单
        if (OrderType.ACTIVITY.getCode() != orderType) {
            throw new ServiceException("部分退款只支持活动订单");
        }

        // 检查活动订单
        ActivityOrder activityOrder = activityOrderMapper.selectOne(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getOrderNo, orderNo)
                        .eq(ActivityOrder::getOpenid, openid)
        );
        
        if (activityOrder == null) {
            log.error("活动订单不存在，订单号：{}", orderNo);
            throw new ServiceException("订单不存在");
        }
        
        // 检查退款时间
        checkActivityRefundTime(orderNo, openid);
        
        // 检查出行人是否已申请退款
        List<Long> travelerIds = dto.getTravelerIds();
        checkTravelerRefundStatus(travelerIds, orderNo);
        
        // 获取活动信息，用于计算退款金额
        Activity activity = activityMapper.selectById(activityOrder.getActivityId());
        if (activity == null) {
            throw new ServiceException("活动信息不存在");
        }
        
        // 验证活动订单出行人关联ID列表是否合法
        List<Long> activityOrderTravelerIds = dto.getActivityOrderTravelerIds();
        log.info("activityOrderTravelerIds: {}", activityOrderTravelerIds);
        
        // 获取订单下的所有出行人关联记录
        List<ActivityOrderTraveler> orderTravelers = activityOrderTravelerMapper.selectList(
            new LambdaQueryWrapper<ActivityOrderTraveler>()
                .eq(ActivityOrderTraveler::getOrderNo, orderNo)
        );
        
        // 验证传入的出行人ID在订单关联记录中是否存在
        List<Long> orderTravelerIds = orderTravelers.stream()
            .map(ActivityOrderTraveler::getTravelerId)
            .collect(Collectors.toList());
        
        log.info("订单{}所有出行人ID: {}", orderNo, orderTravelerIds);
        log.info("用户选中的出行人ID: {}", travelerIds);
        
        // 筛选出要退款的出行人关联记录
        List<ActivityOrderTraveler> selectedTravelers = orderTravelers.stream()
            .filter(aot -> travelerIds.contains(aot.getTravelerId()))
            .collect(Collectors.toList());
        
        if (selectedTravelers.size() != travelerIds.size()) {
            throw new ServiceException("部分出行人信息不存在");
        }
        
        // 检查每个选中的出行人ID是否在订单出行人列表中
        for (Long selectedTravelerId : travelerIds) {
            if (!orderTravelerIds.contains(selectedTravelerId)) {
                log.error("出行人ID {}不属于订单 {}", selectedTravelerId, orderNo);
                throw new ServiceException("部分出行人不属于该订单");
            }
        }
        
        // 计算退款金额并创建退款申请
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        int earlyBirdCount = 0;
        int normalCount = 0;
        
        for (ActivityOrderTraveler traveler : selectedTravelers) {
            if (traveler.getTickType() == TickTypeEnum.EARLY_BIRD.getCode()) {
                earlyBirdCount++;
            } else {
                normalCount++;
            }
        }
        
        BigDecimal earlyBirdRefundAmount = activity.getEarlyBirdPrice().multiply(new BigDecimal(earlyBirdCount));
        BigDecimal normalRefundAmount = activity.getNormalPrice().multiply(new BigDecimal(normalCount));
        totalRefundAmount = earlyBirdRefundAmount.add(normalRefundAmount);
        
        // 创建退款申请
        RefundApply refundApply = new RefundApply();
        refundApply.setOutTradeOrder(orderNo);
        refundApply.setOpenid(openid);
        refundApply.setStatus(RefundStatus.PENDING.getCode());
        refundApply.setReason(dto.getReason());
        refundApply.setOrderType(orderType);
        refundApply.setRefundAmount(totalRefundAmount); // 设置部分退款金额
        refundApply.setCreateTime(LocalDateTime.now());
        refundApply.setUpdateTime(LocalDateTime.now());

        refundApplyMapper.insert(refundApply);
        log.info("部分退款申请创建成功，ID：{}，退款金额：{}", refundApply.getId(), totalRefundAmount);
        
        // 创建退款申请与出行人的关联记录
        List<RefundApplyTraveler> refundApplyTravelers = new ArrayList<>();
        
        for (ActivityOrderTraveler traveler : selectedTravelers) {
            RefundApplyTraveler refundApplyTraveler = new RefundApplyTraveler();
            refundApplyTraveler.setRefundApplyId(refundApply.getId());
            refundApplyTraveler.setTravelerId(traveler.getTravelerId());
            refundApplyTraveler.setActivityOrderTravelerId(traveler.getId());
            refundApplyTraveler.setOrderNo(orderNo);
            refundApplyTraveler.setTickType(traveler.getTickType());
            
            // 根据票类型设置退款金额
            if (traveler.getTickType() == TickTypeEnum.EARLY_BIRD.getCode()) {
                refundApplyTraveler.setRefundAmount(activity.getEarlyBirdPrice());
            } else {
                refundApplyTraveler.setRefundAmount(activity.getNormalPrice());
            }
            
            refundApplyTravelers.add(refundApplyTraveler);
        }
        
        if (!refundApplyTravelers.isEmpty()) {
            refundApplyTravelerMapper.batchInsert(refundApplyTravelers);
        }
        
        // 注意：这里不更新活动订单的退款状态，因为这是部分退款
        
        return refundApply.getId();
    }
    
    /**
     * 检查出行人是否已经申请过退款
     *
     * @param travelerIds 出行人ID列表
     * @param orderNo 订单号
     * @return 如果未申请过退款返回true，否则抛出异常
     */
    @Override
    public boolean checkTravelerRefundStatus(List<Long> travelerIds, String orderNo) {
        if (travelerIds == null || travelerIds.isEmpty() || orderNo == null || orderNo.isEmpty()) {
            throw new ServiceException("参数错误");
        }
        
        log.info("检查出行人是否已申请退款，出行人IDs：{}，订单号：{}", travelerIds, orderNo);
        
        // 查询已存在的退款申请
        List<RefundApplyTraveler> existingRefundTravelers = refundApplyTravelerMapper.selectByOrderNo(orderNo);
        
        if (existingRefundTravelers != null && !existingRefundTravelers.isEmpty()) {
            for (RefundApplyTraveler existingTraveler : existingRefundTravelers) {
                if (travelerIds.contains(existingTraveler.getTravelerId())) {
                    // 查询退款申请状态
                    RefundApply refundApply = refundApplyMapper.selectById(existingTraveler.getRefundApplyId());
                    if (refundApply != null && refundApply.getStatus() == RefundStatus.PENDING.getCode()) {
                        log.error("出行人ID{}已有待处理的退款申请", existingTraveler.getTravelerId());
                        throw new ServiceException("部分出行人已申请退款，请勿重复申请");
                    }
                }
            }
        }
        
        return true;
    }

    /**
     * 取消退款申请
     * 
     * @param id 退款申请ID
     * @param openid 用户openid
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRefundApply(Long id, String openid) {
        log.info("取消退款申请，ID：{}，用户openid：{}", id, openid);
        
        // 查询退款申请
        RefundApply refundApply = refundApplyMapper.selectById(id);
        if (refundApply == null) {
            log.error("退款申请不存在，ID：{}", id);
            throw new ServiceException("退款申请不存在");
        }
        
        // 检查退款申请是否属于当前用户
        if (!refundApply.getOpenid().equals(openid)) {
            log.error("无权取消此退款申请，申请ID：{}，当前用户openid：{}", id, openid);
            throw new ServiceException("无权取消此退款申请");
        }
        
        // 检查状态是否为待审核（只有待审核状态才能取消）
        if (refundApply.getStatus() != RefundStatus.PENDING.getCode()) {
            log.error("退款申请状态不正确，当前状态：{}，无法取消", RefundStatus.getDescByCode(refundApply.getStatus()));
            throw new ServiceException("退款申请状态不正确，无法取消");
        }
        
        // 获取订单号和订单类型
        String orderNo = refundApply.getOutTradeOrder();
        Integer orderType = refundApply.getOrderType();
        
        // 更新订单表的退款状态为未申请退款(0)
        boolean updateOrderResult = false;
        
        // 根据订单类型更新对应订单表
        if (OrderType.ACTIVITY.getCode() == orderType) {
            // 活动订单
            ActivityOrder activityOrder = activityOrderMapper.selectOne(
                    new LambdaQueryWrapper<ActivityOrder>()
                            .eq(ActivityOrder::getOrderNo, orderNo)
            );
            
            if (activityOrder != null) {
                activityOrder.setRefundStatus(OrderRefundStatusEnum.NOT_APPLIED.getCode().longValue());
                updateOrderResult = activityOrderMapper.updateById(activityOrder) > 0;
                log.info("更新活动订单退款状态为未申请退款，订单号：{}", orderNo);
            } else {
                log.warn("活动订单不存在，订单号：{}", orderNo);
            }
            
        } else if (OrderType.PRODUCT.getCode() == orderType) {
            // 商品订单
            ProductOrder productOrder = productOrderMapper.selectOne(
                    new LambdaQueryWrapper<ProductOrder>()
                            .eq(ProductOrder::getOrderNo, orderNo)
            );
            
            if (productOrder != null) {
                productOrder.setRefundStatus(OrderRefundStatusEnum.NOT_APPLIED.getCode());
                updateOrderResult = productOrderMapper.updateById(productOrder) > 0;
                log.info("更新商品订单退款状态为未申请退款，订单号：{}", orderNo);
            } else {
                log.warn("商品订单不存在，订单号：{}", orderNo);
            }
            
        } else {
            log.error("订单类型错误，orderType：{}", orderType);
            throw new ServiceException("订单类型错误");
        }
        
        if (!updateOrderResult) {
            log.warn("更新订单退款状态失败，订单号：{}", orderNo);
        }
        
        // 更新退款申请状态为已取消
        refundApply.setStatus(RefundStatus.CANCELED.getCode());
        refundApply.setUpdateTime(LocalDateTime.now());
        
        int rows = refundApplyMapper.updateById(refundApply);
        return rows > 0;
    }

    /**
     * 检查活动订单是否为已免单状态
     * 
     * @param orderNo 订单号
     * @param openid 用户openid
     * @return 如果不是已免单返回true，如果是已免单抛出异常
     */
    @Override
    public boolean checkOrderFeeExemption(String orderNo, String openid) {
        log.info("检查订单是否为已免单状态，订单号：{}，用户openid：{}", orderNo, openid);
        
        ActivityOrder activityOrder = activityOrderMapper.selectOne(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getOrderNo, orderNo)
                        .eq(ActivityOrder::getOpenid, openid)
        );
        
        if (activityOrder == null) {
            log.error("活动订单不存在，订单号：{}", orderNo);
            throw new ServiceException("订单不存在");
        }
        
        if (activityOrder.getStatus() == ActivityOrderStatusEnum.FEE_EXEMPTION.getStatus()) {
            log.error("活动订单已免单，不能申请退款，订单号：{}", orderNo);
            throw new ServiceException("此订单已免单");
        }
        
        return true;
    }
    
    /**
     * 检查活动订单是否有可退款出行人
     * 
     * @param orderNo 订单号
     * @param openid 用户openid
     * @return 如果有可退款出行人返回true，如果没有抛出异常
     */
    @Override
    public boolean checkAvailableTravelers(String orderNo, String openid) {
        log.info("检查订单是否有可退款出行人，订单号：{}，用户openid：{}", orderNo, openid);
        
        ActivityOrder activityOrder = activityOrderMapper.selectOne(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getOrderNo, orderNo)
                        .eq(ActivityOrder::getOpenid, openid)
        );
        
        if (activityOrder == null) {
            log.error("活动订单不存在，订单号：{}", orderNo);
            throw new ServiceException("订单不存在");
        }
        
        // 获取订单下所有出行人信息
        List<ActivityOrderTraveler> allOrderTravelers = activityOrderTravelerMapper.selectList(
                new LambdaQueryWrapper<ActivityOrderTraveler>()
                        .eq(ActivityOrderTraveler::getOrderNo, orderNo)
        );
        
        if (allOrderTravelers == null || allOrderTravelers.isEmpty()) {
            log.error("活动订单下无可退款出行人，订单号：{}", orderNo);
            throw new ServiceException("没有可退款出行人");
        }
        
        return true;
    }
} 