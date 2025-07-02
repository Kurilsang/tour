package com.tour.query;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品订单查询参数类
 *
 * @author Kuril
 */
@Data
public class ProductOrderQuery extends BaseParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户openid
     */
    private String openid;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 自提点地址（模糊查询）
     */
    private String pickupLocation;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 创建时间开始
     */
    private LocalDateTime startTime;

    /**
     * 创建时间结束
     */
    private LocalDateTime endTime;

    /**
     * 订单过期时间开始
     */
    private LocalDateTime expireStartTime;

    /**
     * 订单过期时间结束
     */
    private LocalDateTime expireEndTime;
} 