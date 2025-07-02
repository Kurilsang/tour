package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import com.tour.enums.OrderType;
import com.tour.enums.RefundStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款申请实体类
 *
 * @Author Kuril
 */
@Data
@TableName("refund_apply")
public class RefundApply {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long id;

    /**
     * 订单号
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String outTradeOrder;

    /**
     * 申请人openid
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String openid;

    /**
     * 退款状态 {@link RefundStatus}
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer status;

    /**
     * 退款理由
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String reason;

    /**
     * 订单类型 {@link OrderType}
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer orderType;

    /**
     * 退款金额
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private BigDecimal refundAmount;

    /**
     * 管理员备注
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String adminRemark;

    /**
     * 创建时间
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime updateTime;
} 