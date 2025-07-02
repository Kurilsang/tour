package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动详情实体类
 *
 * @author kuril
 */
@TableName("activity_detail")
@Data
public class ActivityDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 活动详情ID
     */
    @TableId(value = "id",type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long id;

    /**
     * 活动ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long activityId;

    /**
     * HTML格式内容
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String content;

    /**
     * 排序权重
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer sortOrder;
}    