package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品订单实体类
 *
 * @author Kuril
 */
@TableName("product_order")
@Data
public class ProductOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户openid
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String openid;

    /**
     * 商品ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long productId;

    /**
     * 商品数量
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer quantity;

    /**
     * 订单总金额
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private BigDecimal totalAmount;

    /**
     * 自提点地址
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String pickupLocation;

    /**
     * 订单状态：1-待提货 2-已完成 3-已取消
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 订单过期时间
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime expireTime;

    /**
     * 订单号
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String orderNo;


    /**
     * 订单号
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer refundStatus;

    /**
     * 是否有退款记录
     * (不存入数据库，由程序计算得出)
     */
    @TableField(exist = false)
    private Boolean hasRefundRecord;
} 