package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款申请与出行人关联实体类
 *
 * @author Kuril
 */
@TableName("refund_apply_traveler")
@Data
public class RefundApplyTraveler implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 退款申请ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long refundApplyId;

    /**
     * 出行人ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long travelerId;

    /**
     * 活动订单出行人关联ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long activityOrderTravelerId;

    /**
     * 订单号（冗余字段，便于查询）
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String orderNo;

    /**
     * 票类型 1-早鸟票 2-普通票
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer tickType;

    /**
     * 该出行人对应的退款金额
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private BigDecimal refundAmount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
} 