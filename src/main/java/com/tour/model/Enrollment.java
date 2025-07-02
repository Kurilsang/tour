package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报名表实体类
 *
 * @author kuril
 */
@TableName("enrollment")
@Data
public class Enrollment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报名表ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long id;

    /**
     * 用户ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String userId;

    /**
     * 活动ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long activityId;

    /**
     * 支付价格
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private BigDecimal price;

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

    /**
     * 订单编号
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String orderNo;

}