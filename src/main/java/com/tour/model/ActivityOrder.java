package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动订单实体类
 *
 * @author kuril
 */
@TableName("activity_order")
@Data
public class ActivityOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long id;

    /**
     * 订单号（日期+随机数）
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String orderNo;

    /**
     * 用户openid
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String openid;

    /**
     * 活动ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long activityId;

    /**
     * 早鸟票数量
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer earlyBirdNum;

    /**
     * 普通票数量
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer normalNum;

    /**
     * 订单总金额
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private BigDecimal totalAmount;

    /**
     * 订单状态：1-待支付 2-已支付 3-已取消 4-已过期
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer status;

    /**
     * 支付时间
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime paymentTime;

    /**
     * 订单过期时间（创建时间+15分钟）
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime createTime;

    /**
     * 上车点id
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long busLocationId;

    /**
     * 退款状态 0- 未申请退款 1- 已申请退款
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long refundStatus;
    
    /**
     * 是否部分免单
     * (不存入数据库，由程序计算得出)
     */
    @TableField(exist = false)
    private Boolean isPartialRefund;
    
    /**
     * 是否有退款记录
     * (不存入数据库，由程序计算得出)
     */
    @TableField(exist = false)
    private Boolean hasRefundRecord;
}    