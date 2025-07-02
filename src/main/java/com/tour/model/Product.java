package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description product
 * @author kuril
 * @date 2025-05-08
 */
@TableName("product")
@Data
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 产品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long id;

    /**
     * 产品名称
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String name;

    /**
     * 产品描述
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String description;

    /**
     * 产品价格
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private BigDecimal price;

    /**
     * 产品库存
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer stock;

    /**
     * 产品库存
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer ReserveStock;

    /**
     * 封面图URL
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String coverImage;

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
     * 最后修改时间
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime updateTime;

    /**
     * 最后修改人ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String updatedBy;

    /**
     * 上下架状态
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer status;
}