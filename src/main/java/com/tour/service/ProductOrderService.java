package com.tour.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.dto.ProductOrderDTO;
import com.tour.dto.ProductRefundDTO;
import com.tour.model.OrderPaymentResult;
import com.tour.model.ProductOrder;
import com.tour.query.ProductOrderQuery;
import com.tour.vo.ProductOrderVO;
import com.tour.vo.WxRefundResponseVO;

/**
 * @Author Kuril
 * @Description 商品订单服务接口
 * @DateTime 2025/5/17 15:30
 */
public interface ProductOrderService {

    /**
     * 根据查询条件加载订单列表
     * @param productOrderQuery 查询条件
     * @return 分页订单列表
     */
    IPage<ProductOrder> loadDataList(ProductOrderQuery productOrderQuery);

    /**
     * 锁定订单
     * 创建新订单，预留库存
     * @param productOrderDTO 订单信息
     * @return 创建的订单信息和支付参数
     */
    OrderPaymentResult<ProductOrder> lockOrder(ProductOrderDTO productOrderDTO);

    /**
     * 取消订单
     * @param orderNo 订单号
     * @param openid 用户openid
     * @return 是否成功取消
     */
    boolean cancelOrder(String orderNo, String openid);

    /**
     * 获取订单详情
     * @param orderNo 订单号
     * @param openid 用户openid，管理员调用时可以为null
     * @return 订单详情VO
     */
    ProductOrderVO getOrderDetail(String orderNo, String openid);
    
    /**
     * 支付订单
     * @param orderNo 订单号
     * @param openid 用户openid
     * @return 是否支付成功
     */
    boolean payOrder(String orderNo, String openid);
    
    /**
     * 更新订单状态
     * @param orderNo 订单号
     * @param status 目标状态
     * @return 是否更新成功
     */
    boolean updateOrderStatus(String orderNo, Integer status);
    
    /**
     * 根据订单号查询订单基础信息
     * 
     * @param orderNo 订单号
     * @return 订单对象，如果未找到返回null
     */
    ProductOrder getOrderByOrderNo(String orderNo);
    
    /**
     * 申请退款
     * 执行微信支付退款，并更新订单状态和库存
     *
     * @param productRefundDTO 退款请求DTO
     * @param openid 用户openid
     * @return 退款结果信息
     */
    WxRefundResponseVO refundOrder(ProductRefundDTO productRefundDTO, String openid);
    
    /**
     * 申请退款（带真实退款原因）
     * 执行微信支付退款，并更新订单状态和库存
     *
     * @param productRefundDTO 退款请求DTO
     * @param openid 用户openid
     * @param realReason 真实退款原因（存储到数据库）
     * @return 退款结果信息
     */
    WxRefundResponseVO refundOrder(ProductRefundDTO productRefundDTO, String openid, String realReason);
} 