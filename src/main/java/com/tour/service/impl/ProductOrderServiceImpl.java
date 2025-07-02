package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tour.common.constant.Constants;
import com.tour.common.enums.RefundStatusEnum;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.UserContext;
import com.tour.common.util.WxPaymentUtil;
import com.tour.dao.LocationMapper;
import com.tour.dao.ProductMapper;
import com.tour.dao.ProductOrderMapper;
import com.tour.dto.ProductOrderDTO;
import com.tour.dto.ProductRefundDTO;
import com.tour.enums.PageSize;
import com.tour.enums.ProductOrderStatusEnum;
import com.tour.dao.WxPaymentProductOrderMapper;
import com.tour.enums.ProductStatusEnum;
import com.tour.model.*;
import com.tour.query.ProductOrderQuery;
import com.tour.service.ProductOrderService;
import com.tour.service.WxRefundService;
import com.tour.vo.ProductOrderVO;
import com.tour.vo.WxRefundResponseVO;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * @Author Kuril
 * @Description 商品订单服务实现类
 * @DateTime 2025/5/17 15:40
 */
@Service("productOrderService")
@Slf4j
public class ProductOrderServiceImpl implements ProductOrderService {

    private final ProductOrderMapper productOrderMapper;
    private final ProductMapper productMapper;
    private final LocationMapper locationMapper;
    private final WxPaymentUtil wxPaymentUtil;
    private final WxPaymentProductOrderMapper wxPaymentProductOrderMapper;
    private final WxRefundService wxRefundService;
    
    @Value("${wechat.pay.product-notify-url}")
    private String productNotifyUrl;
    
    @Value("${spring.profiles.active}")
    private String activeProfile;
    
    @Value("${wechat.pay.cloud.env-id:}")
    private String cloudEnvId;
    
    @Value("${wechat.pay.cloud.container.service:}")
    private String cloudContainerService;
    
    @Value("${wechat.pay.cloud.container.path:}")
    private String cloudContainerPath;
    
    @Value("${wechat.pay.cloud.container.product-path:}")
    private String cloudProductPath;

    public ProductOrderServiceImpl(ProductOrderMapper productOrderMapper, ProductMapper productMapper, 
                                  LocationMapper locationMapper, WxPaymentUtil wxPaymentUtil,
                                  WxPaymentProductOrderMapper wxPaymentProductOrderMapper,
                                  WxRefundService wxRefundService) {
        this.productOrderMapper = productOrderMapper;
        this.productMapper = productMapper;
        this.locationMapper = locationMapper;
        this.wxPaymentUtil = wxPaymentUtil;
        this.wxPaymentProductOrderMapper = wxPaymentProductOrderMapper;
        this.wxRefundService = wxRefundService;
    }

    /**
     * 根据查询条件加载商品订单列表
     *
     * @param productOrderQuery 查询条件
     * @return 分页订单列表
     */
    @Override
    public IPage<ProductOrder> loadDataList(ProductOrderQuery productOrderQuery) {
        // 构建分页参数
        Integer pageNo = productOrderQuery.getPageNo();
        Integer pageSize = productOrderQuery.getPageSize();
        if (pageNo == null) {
            pageNo = Constants.defaultPageNo;
        }
        if (pageSize == null) {
            pageSize = PageSize.SIZE10.getSize();
        }
        Page<ProductOrder> page = new Page<>(pageNo, pageSize);

        // 构建查询条件
        QueryWrapper<ProductOrder> queryWrapper = new QueryWrapper<>();
        
        // 根据查询条件动态添加查询条件
        if (productOrderQuery.getId() != null) {
            queryWrapper.eq("id", productOrderQuery.getId());
        }
        if (productOrderQuery.getOrderNo() != null && !productOrderQuery.getOrderNo().isEmpty()) {
            queryWrapper.eq("order_no", productOrderQuery.getOrderNo());
        }
        if (productOrderQuery.getOpenid() != null && !productOrderQuery.getOpenid().isEmpty()) {
            queryWrapper.eq("openid", productOrderQuery.getOpenid());
        }
        if (productOrderQuery.getProductId() != null) {
            queryWrapper.eq("product_id", productOrderQuery.getProductId());
        }
        if (productOrderQuery.getQuantity() != null) {
            queryWrapper.eq("quantity", productOrderQuery.getQuantity());
        }
        if (productOrderQuery.getTotalAmount() != null) {
            queryWrapper.eq("total_amount", productOrderQuery.getTotalAmount());
        }
        if (productOrderQuery.getPickupLocation() != null && !productOrderQuery.getPickupLocation().isEmpty()) {
            queryWrapper.like("pickup_location", productOrderQuery.getPickupLocation());
        }
        if (productOrderQuery.getStatus() != null) {
            queryWrapper.eq("status", productOrderQuery.getStatus());
        }
        
        // 处理时间范围查询
        if (productOrderQuery.getStartTime() != null) {
            queryWrapper.ge("create_time", productOrderQuery.getStartTime());
        }
        if (productOrderQuery.getEndTime() != null) {
            queryWrapper.le("create_time", productOrderQuery.getEndTime());
        }
        if (productOrderQuery.getExpireStartTime() != null) {
            queryWrapper.ge("expire_time", productOrderQuery.getExpireStartTime());
        }
        if (productOrderQuery.getExpireEndTime() != null) {
            queryWrapper.le("expire_time", productOrderQuery.getExpireEndTime());
        }

        // 处理排序信息
        String orderBy = productOrderQuery.getOrderBy();
        if (orderBy != null && !orderBy.isEmpty()) {
            queryWrapper.last("ORDER BY " + orderBy);
        } else {
            queryWrapper.orderByDesc("create_time"); // 默认按创建时间倒序
        }

        // 执行分页查询并返回结果
        return productOrderMapper.selectPage(page, queryWrapper);
    }

    /**
     * 锁定订单
     * 创建新订单，预留库存
     *
     * @param productOrderDTO 订单信息
     * @return 创建的订单信息和支付参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPaymentResult<ProductOrder> lockOrder(ProductOrderDTO productOrderDTO) {
        // 从上下文获取openid
        String openid = UserContext.getOpenId();
        if (openid == null || openid.isEmpty()) {
            throw new ServiceException("用户未登录");
        }
        
        // 校验是否存在未支付订单
        QueryWrapper<ProductOrder> existingOrderQuery = new QueryWrapper<>();
        existingOrderQuery.eq("openid", openid);
        existingOrderQuery.eq("status", ProductOrderStatusEnum.NONPAYMENT.getCode());
        ProductOrder existingOrder = productOrderMapper.selectOne(existingOrderQuery);
        if (existingOrder != null) {
            throw new ServiceException("当前存在未支付订单，请先取消订单或支付订单");
        }
        
        // 校验商品是否存在且有库存
        Product product = productMapper.selectById(productOrderDTO.getProductId());
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
//        检查上架状态是否正确
        if(product.getStatus()!= ProductStatusEnum.ON.getCode())
        {
            throw new ServiceException("商品未上架");
        }
        // 检查库存是否足够
        if (product.getStock() < productOrderDTO.getQuantity()) {
            throw new ServiceException("商品库存不足");
        }
        
        // 预留库存（减少可用库存）
        int updateResult = productMapper.update(null, 
            new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, productOrderDTO.getProductId())
                .ge(Product::getStock, productOrderDTO.getQuantity())
                .setSql("reserve_stock = reserve_stock + " + productOrderDTO.getQuantity())
                .setSql("stock = stock - " + productOrderDTO.getQuantity())
        );
        
        if (updateResult == 0) {
            throw new ServiceException("库存不足，无法下单");
        }
        
        // 创建订单
        ProductOrder order = new ProductOrder();

        order.setOpenid(openid);
        order.setProductId(productOrderDTO.getProductId());
        order.setQuantity(productOrderDTO.getQuantity());
        QueryWrapper<Location> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("location_product_id", productOrderDTO.getProductId());
        Location location = locationMapper.selectOne(queryWrapper);
        order.setPickupLocation(location.getName());
        order.setStatus(ProductOrderStatusEnum.NONPAYMENT.getCode());
        order.setCreateTime(LocalDateTime.now());
        order.setExpireTime(LocalDateTime.now().plusMinutes(Constants.productOrderExpireTime));
        order.setOrderNo(generateOrderNo());
        
        // 计算总金额
        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(productOrderDTO.getQuantity()));
        order.setTotalAmount(totalAmount);
        
        // 保存订单
        productOrderMapper.insert(order);
        
        // 生成微信支付预付单
        try {
            WxPaymentRequest paymentRequest = new WxPaymentRequest();
            // 金额单位转换：元 -> 分
            int totalAmountFen = totalAmount.multiply(new BigDecimal(100)).intValue();
            log.info("该商品金额为{}分",totalAmountFen);
            paymentRequest.setTotal(totalAmountFen);
            paymentRequest.setOpenid(openid);
            paymentRequest.setDescription(product.getName()); // 使用商品名称作为订单描述
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
                containerInfo.setPath(cloudProductPath);
                paymentRequest.setContainer(containerInfo);
                
                // 使用云托管环境下单
                log.info("生产环境：使用云托管环境进行微信支付下单, envId: {}, container: {}/{}", 
                        cloudEnvId, cloudContainerService, cloudProductPath);
                paymentResponse = wxPaymentUtil.wxGetPrePaymentWithCloudEnv(paymentRequest);
            } else {
                // 开发环境：使用传统方式下单
                log.info("开发环境：使用传统方式进行微信支付下单");
                paymentRequest.setNotifyUrl(productNotifyUrl);
                paymentResponse = wxPaymentUtil.wxGetPrePayment(paymentRequest);
            }
            
            // 保存微信支付信息到关联表
            WxPaymentProductOrder wxPaymentProductOrder = new WxPaymentProductOrder();
            wxPaymentProductOrder.setOrderNo(order.getOrderNo());
            wxPaymentProductOrder.setTimeStamp(paymentResponse.getTimeStamp());
            wxPaymentProductOrder.setNonceStr(paymentResponse.getNonceStr());
            wxPaymentProductOrder.setPackageStr(paymentResponse.getPackageStr());
            wxPaymentProductOrder.setSignType(paymentResponse.getSignType());
            wxPaymentProductOrder.setPaySign(paymentResponse.getPaySign());
            wxPaymentProductOrder.setPrepayId(paymentResponse.getPrepayId());
            wxPaymentProductOrder.setCreateTime(LocalDateTime.now());
            
            wxPaymentProductOrderMapper.insert(wxPaymentProductOrder);
            log.info("已保存微信支付订单信息, orderNo: {}, prepayId: {}", order.getOrderNo(), paymentResponse.getPrepayId());
            
            // 返回订单信息和支付参数
            return OrderPaymentResult.of(order, paymentResponse);
        } catch (Exception e) {
            log.error("生成预付单失败: {}", e.getMessage());
            throw new ServiceException("生成支付参数失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     * 释放预留库存，更新订单状态
     *
     * @param orderNo 订单号
     * @param openid 用户openid
     * @return 是否成功取消
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String orderNo, String openid) {
        // 查询订单
        QueryWrapper<ProductOrder> orderQuery = new QueryWrapper<>();
        orderQuery.eq("order_no", orderNo);
        orderQuery.eq("openid", openid);
        ProductOrder order = productOrderMapper.selectOne(orderQuery);
        
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 只有未支付和待提货状态的订单可以取消
        if (order.getStatus() != ProductOrderStatusEnum.NONPAYMENT.getCode() && 
            order.getStatus() != ProductOrderStatusEnum.PENDING_PICKUP.getCode()) {
            throw new ServiceException("当前订单状态不可取消");
        }
        
        try {
            // 恢复库存
            int updateResult = productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, order.getProductId())
                    .setSql("reserve_stock = reserve_stock - " + order.getQuantity())
                    .setSql("stock = stock + " + order.getQuantity())
            );
            
            if (updateResult == 0) {
                throw new ServiceException("恢复库存失败");
            }
            
            // 若为未支付状态，则需要关闭微信支付订单
            if (order.getStatus() == ProductOrderStatusEnum.NONPAYMENT.getCode()) {
                try {
                    wxPaymentUtil.closeOrder(order.getOrderNo());
                } catch (Exception e) {
                    log.warn("关闭微信支付订单失败: {}", e.getMessage());
                    // 继续处理，不影响订单取消
                }
            }
            
            // 更新订单状态为已取消
            ProductOrder updateOrder = new ProductOrder();
            updateOrder.setId(order.getId());
            updateOrder.setStatus(ProductOrderStatusEnum.CANCELED.getCode());
            
            int result = productOrderMapper.updateById(updateOrder);
            return result > 0;
        } catch (Exception e) {
            log.error("取消订单失败: {}", e.getMessage());
            throw new ServiceException("取消订单失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单详情
     *
     * @param orderNo 订单号
     * @param openid 用户openid，管理员调用时可以为null
     * @return 订单详情VO
     */
    @Override
    public ProductOrderVO getOrderDetail(String orderNo, String openid) {
        // 查询订单
        QueryWrapper<ProductOrder> orderQuery = new QueryWrapper<>();
        orderQuery.eq("order_no", orderNo);
        // 只有在用户访问时才检查openid
        if (openid != null && !openid.isEmpty()) {
            orderQuery.eq("openid", openid);
        }
        ProductOrder order = productOrderMapper.selectOne(orderQuery);
        
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 查询商品信息
        Product product = productMapper.selectById(order.getProductId());
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        
        // 查询地点信息
        QueryWrapper<Location> locationQuery = new QueryWrapper<>();
        locationQuery.eq("location_product_id", product.getId());
        Location location = locationMapper.selectOne(locationQuery);
        
        // 构建返回对象
        ProductOrderVO vo = new ProductOrderVO();
        vo.setProductName(product.getName());
        vo.setCoverImage(product.getCoverImage());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setQuantity(order.getQuantity());
        vo.setCreateTime(order.getCreateTime());
        vo.setStatus(order.getStatus());
        vo.setOrderNo(order.getOrderNo());
        vo.setLocation(location);
        vo.setOpenid(order.getOpenid());

        vo.setWxPaymentProductOrder(wxPaymentProductOrderMapper.selectByOrderNo(orderNo));
        
        return vo;
    }
    
    /**
     * 定时处理超时订单
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void processTimeoutOrders() {
        log.info("开始处理商品超时订单...");
        
        // 查询所有超时未支付的订单
        List<ProductOrder> timeoutOrders = productOrderMapper.selectList(
            new LambdaQueryWrapper<ProductOrder>()
                .eq(ProductOrder::getStatus, ProductOrderStatusEnum.NONPAYMENT.getCode())
                .lt(ProductOrder::getExpireTime, LocalDateTime.now())
                .last("LIMIT " + Constants.timeoutOrderBatchSize)
        );
        
        if (timeoutOrders.isEmpty()) {
            log.info("没有需要处理的商品超时订单");
            return;
        }
        
        log.info("发现 {} 个商品超时订单需要处理", timeoutOrders.size());
        
        // 处理每个超时订单
        for (ProductOrder order : timeoutOrders) {
            try {
                // 关闭微信支付订单
                try {
                    wxPaymentUtil.closeOrder(order.getOrderNo());
                } catch (Exception e) {
                    log.warn("关闭微信支付订单失败: {}", e.getMessage());
                    // 继续处理，不影响订单取消
                }
                
                // 恢复库存
                productMapper.update(null,
                    new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, order.getProductId())
                        .setSql("reserve_stock = reserve_stock - " + order.getQuantity())
                        .setSql("stock = stock + " + order.getQuantity())
                );
                
                // 更新订单状态为已取消
                ProductOrder updateOrder = new ProductOrder();
                updateOrder.setId(order.getId());
                updateOrder.setStatus(ProductOrderStatusEnum.CANCELED.getCode());
                productOrderMapper.updateById(updateOrder);
                
                log.info("订单 {} 已标记为已取消", order.getOrderNo());
            } catch (Exception e) {
                log.error("处理超时订单 {} 失败: {}", order.getOrderNo(), e.getMessage());
            }
        }
        
        log.info("商品超时订单处理完成");
    }
    
    /**
     * 生成订单号
     * 格式：PO + 时间戳 + 8位随机字符
     * @return 生成的订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return Constants.productOrderPrefix + timestamp + randomPart;
    }

    /**
     * 支付订单
     * 正式扣减库存，更新订单状态为待提货
     *
     * @param orderNo 订单号
     * @param openid 用户openid
     * @return 是否支付成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(String orderNo, String openid) {
        // 查询订单
        QueryWrapper<ProductOrder> orderQuery = new QueryWrapper<>();
        orderQuery.eq("order_no", orderNo);
        orderQuery.eq("openid", openid);
        ProductOrder order = productOrderMapper.selectOne(orderQuery);
        
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 新增校验：如果订单已经是待提货状态，直接返回true，避免重复处理
        if (ProductOrderStatusEnum.PENDING_PICKUP.getCode().equals(order.getStatus())) {
            log.info("订单{}已经是待提货状态，无需重复处理", orderNo);
            return true;
        }
        
        // 仅允许处理未支付状态的订单
        if (!ProductOrderStatusEnum.NONPAYMENT.getCode().equals(order.getStatus())) {
            throw new ServiceException("订单状态不正确");
        }
        
        // 检查订单是否已过期
        if (order.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException("订单已过期");
        }
        
        try {
            // 更新商品库存状态
            // 1. 减少预留库存(已在锁单时减少过可用库存)
            Product product = productMapper.selectById(order.getProductId());
            if (product == null) {
                throw new ServiceException("商品不存在");
            }
            
            // 2. 修改预留库存
            productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, order.getProductId())
                    .setSql("reserve_stock = reserve_stock - " + order.getQuantity())
            );
            
            // 3. 更新订单状态为待提货
            ProductOrder updateOrder = new ProductOrder();
            updateOrder.setId(order.getId());
            updateOrder.setStatus(ProductOrderStatusEnum.PENDING_PICKUP.getCode());
            int result = productOrderMapper.updateById(updateOrder);
            
            return result > 0;
        } catch (Exception e) {
            log.error("支付订单失败: {}", e.getMessage());
            throw new ServiceException("支付订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新订单状态
     * 例如将待提货状态设置为已完成
     *
     * @param orderNo 订单号
     * @param status 目标状态
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(String orderNo, Integer status) {
        if (orderNo == null || orderNo.isEmpty()) {
            throw new ServiceException("订单号不能为空");
        }
        if (status == null) {
            throw new ServiceException("目标状态不能为空");
        }
        
        // 查询订单
        QueryWrapper<ProductOrder> orderQuery = new QueryWrapper<>();
        orderQuery.eq("order_no", orderNo);
        ProductOrder order = productOrderMapper.selectOne(orderQuery);
        
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 禁止跨状态修改，例如从未支付直接到已完成
        if (order.getStatus() == ProductOrderStatusEnum.NONPAYMENT.getCode() && 
            status == ProductOrderStatusEnum.COMPLETED.getCode()) {
            throw new ServiceException("不允许从未支付状态直接更新为已完成状态");
        }
        
        if (order.getStatus() == status) {
            // 状态相同，无需修改
            return true;
        }
        
        // 更新订单状态
        ProductOrder updateOrder = new ProductOrder();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(status);
        
        int result = productOrderMapper.updateById(updateOrder);
        return result > 0;
    }

    /**
     * 申请退款
     * 执行微信支付退款，并更新订单状态和库存
     *
     * @param productRefundDTO 退款请求DTO
     * @param openid 用户openid
     * @return 退款结果信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxRefundResponseVO refundOrder(ProductRefundDTO productRefundDTO, String openid) {
        // 调用带realReason参数的方法，默认realReason与reason相同
        return refundOrder(productRefundDTO, openid, productRefundDTO.getReason());
    }
    
    /**
     * 申请退款（带真实退款原因）
     * 执行微信支付退款，并更新订单状态和库存
     *
     * @param productRefundDTO 退款请求DTO
     * @param openid 用户openid
     * @param realReason 真实退款原因（存储到数据库）
     * @return 退款结果信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxRefundResponseVO refundOrder(ProductRefundDTO productRefundDTO, String openid, String realReason) {
        String orderNo = productRefundDTO.getOutTradeNo();
        String reason = productRefundDTO.getReason();
        
        log.info("开始处理商品订单退款，订单号: {}, 退款原因: {}, 真实原因: {}", orderNo, reason, realReason);
        
        // 查询订单
        QueryWrapper<ProductOrder> orderQuery = new QueryWrapper<>();
        orderQuery.eq("order_no", orderNo);
        if (openid != null && !openid.isEmpty()) {
            orderQuery.eq("openid", openid);
        }
        ProductOrder order = productOrderMapper.selectOne(orderQuery);
        
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 检查订单状态是否可退款
        if (order.getStatus() != ProductOrderStatusEnum.PENDING_PICKUP.getCode()) {
            throw new ServiceException("当前订单状态不可退款");
        }
        
        try {
            // 回滚库存，但不回滚预留库存
            productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                    .eq(Product::getId, order.getProductId())
                    .setSql("stock = stock + " + order.getQuantity())
            );
            
            // 申请微信支付退款
            Refund refund = wxRefundService.refund(orderNo, reason, realReason);
            
            // 更新订单退款状态
            order.setRefundStatus(RefundStatusEnum.REFUNDED.getCode());
            order.setStatus(ProductOrderStatusEnum.CANCELED.getCode());
            productOrderMapper.updateById(order);
            
            log.info("商品订单退款处理完成，订单号: {}, 退款单号: {}", orderNo, refund.getOutRefundNo());
            
            // 构建返回结果
            WxRefundResponseVO responseVO = new WxRefundResponseVO();
            responseVO.setOutTradeNo(orderNo);
            responseVO.setOutRefundNo(refund.getOutRefundNo());
            responseVO.setRefundId(refund.getRefundId());
            responseVO.setStatus(refund.getStatus().toString());
            
            // 日期处理
            if (refund.getSuccessTime() != null) {
                responseVO.setSuccessTime(refund.getSuccessTime().toString());
            }
            
            // 金额处理
            if (refund.getAmount() != null) {
                responseVO.setTotalAmount(new BigDecimal(refund.getAmount().getTotal()).divide(new BigDecimal(100)));
                responseVO.setRefundAmount(new BigDecimal(refund.getAmount().getRefund()).divide(new BigDecimal(100)));
                responseVO.setCurrency(refund.getAmount().getCurrency());
            }
            
            return responseVO;
        } catch (Exception e) {
            log.error("商品订单退款处理失败: {}", e.getMessage());
            throw new ServiceException("申请退款失败: " + e.getMessage());
        }
    }

    /**
     * 根据订单号查询订单基础信息
     *
     * @param orderNo 订单号
     * @return 订单对象，如果未找到返回null
     */
    @Override
    public ProductOrder getOrderByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isEmpty()) {
            return null;
        }
        
        QueryWrapper<ProductOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        return productOrderMapper.selectOne(queryWrapper);
    }
} 