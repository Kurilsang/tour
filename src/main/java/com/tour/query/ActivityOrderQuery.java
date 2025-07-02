package com.tour.query;

import com.baomidou.mybatisplus.annotation.*;
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
public class ActivityOrderQuery extends BaseParam implements Serializable {

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
    private LocalDateTime paymentTime;

    /**
     * 订单过期时间（创建时间+15分钟）
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 查询时间区间起始
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime startTime;
    /**
     * 查询时间区间结束
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime endTime;


}    