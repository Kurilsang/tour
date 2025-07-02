package com.tour.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.tour.vo.TravelerVO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 报名表实体类
 *
 * @author kuril
 */
@Data
@ApiModel(description = "报名信息请求参数")
public class EnrollmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;


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
     * 出行人列表
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private List<TravelerVO> travelers;


}