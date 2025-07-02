package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报名表-出行人关联实体类
 *
 * @author kuril
 */
@TableName("enrollment_traveler")
@Data
public class EnrollmentTraveler implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联表ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long id;

    /**
     * 报名表ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long enrollmentId;

    /**
     * 出行人ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long travelerId;

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