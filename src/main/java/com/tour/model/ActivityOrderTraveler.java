package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动订单-出行人关联实体类
 *
 * @author kuril
 */
@TableName("activity_order_traveler")
@Data
public class ActivityOrderTraveler implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联表ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long id;

    /**
     * 订单号
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String orderNo;

    /**
     * 出行人ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long travelerId;

    /**
     * 票类型 1-早鸟票 2-普通票
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer tickType;

    /**
     * 是否可免单 0-可免单 1-不可退
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer refundStatus;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}    