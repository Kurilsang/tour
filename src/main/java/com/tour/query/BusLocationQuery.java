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
@Data
public class BusLocationQuery extends BaseParam implements Serializable {

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




}