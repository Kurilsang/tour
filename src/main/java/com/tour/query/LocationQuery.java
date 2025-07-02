package com.tour.query;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 位置信息实体类
 *
 * @author tour
 */
@Data
public class LocationQuery extends BaseParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 地址信息id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long id;

    /**
     * 位置名称
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String name;

    /**
     * 详细地址
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String address;

    /**
     * 纬度
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private BigDecimal latitude;

    /**
     * 经度
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private BigDecimal longitude;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 关联活动ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long LocationActivityId;

    /**
     * 关联产品ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long LocationProductId;
}