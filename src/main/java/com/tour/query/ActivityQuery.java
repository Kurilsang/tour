package com.tour.query;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动实体类
 *
 * @author kuril
 */
@Data
public class ActivityQuery extends BaseParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 活动ID
     */
    @TableId(value = "id",type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long id;

    /**
     * 活动标题
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String title;

    /**
     * 封面图URL
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String coverImage;
    /**
     * 集合地点
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String activityPosition;
    /**
     * 早鸟价
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private BigDecimal earlyBirdPrice;

    /**
     * 普通价
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private BigDecimal normalPrice;

    /**
     * 早鸟价库存
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer earlyBirdQuota;

    /**
     * 普通价库存
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer normalQuota;

    /**
     * 预留早鸟库存
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer reservedEarlyBird;

    /**
     * 预留普通库存
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer reservedNormal;

    /**
     * 总销量
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer totalSold;


    /**
     * 活动报名截止时间
     */
   @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime signEndTime;
    /**
     * 活动开始时间
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime startTime;
    /**
     * 简介
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String description;
    /**
     * 活动结束时间
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime endTime;
    
    /**
     * 可退款截止时间
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime endRefundTime;

    /**
     * 创建时间
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String createdBy;

    /**
     * 最后修改人ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String updatedBy;
    /**
     * 发布状态 0-未发布 1-已发布 2-已关闭
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer status;
    
    /**
     * 活动群二维码URL
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String groupQrcode;
    
    /**
     * 活动小程序码URL
     */
    private String minicode;
}    