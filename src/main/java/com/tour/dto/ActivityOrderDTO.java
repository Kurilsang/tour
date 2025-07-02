package com.tour.dto;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.tour.vo.TravelerVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动订单数据传输对象
 *
 * @Author Kuril
 */
@Data
@ApiModel(description = "活动订单数据传输对象")
public class ActivityOrderDTO {

    @ApiModelProperty(value = "活动ID", required = true)
    private Long activityId;

    @ApiModelProperty(value = "早鸟票数量", required = true)
    private Integer earlyBirdNum;

    @ApiModelProperty(value = "普通票数量", required = true)
    private Integer normalNum;

    @ApiModelProperty(value = "出行人信息列表")
    private List<TravelerVO> travelers;

    @ApiModelProperty(value = "上车点id")
    private Long busLocationId;
}    