package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 大巴上车地点实体类
 *
 * @author kuril
 */
@TableName("bus_location")
@Data
public class BusLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 大巴上车地点ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long id;

    /**
     * 字符串类型的大巴上车地
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String busLocation;

    /**
     * 位置ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long locationId;

    /**
     * 创建时间
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime createTime;
}