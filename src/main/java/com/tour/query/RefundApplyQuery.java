package com.tour.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 退款申请查询参数
 *
 * @Author Kuril
 */
@Data
@ApiModel(description = "退款申请查询参数")
public class RefundApplyQuery extends BaseParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String outTradeOrder;

    /**
     * 申请人openid
     */
    @ApiModelProperty(value = "申请人openid")
    private String openid;

    /**
     * 退款状态
     */
    @ApiModelProperty(value = "退款状态: 1-待审核, 2-退款中, 3-退款完成, 4-拒绝退款")
    private Integer status;

    /**
     * 订单类型
     */
    @ApiModelProperty(value = "订单类型: 1-活动订单, 2-商品订单")
    private Integer orderType;

    /**
     * 查询时间范围起始
     */
    @ApiModelProperty(value = "查询起始时间")
    private LocalDateTime startTime;

    /**
     * 查询时间范围结束
     */
    @ApiModelProperty(value = "查询结束时间")
    private LocalDateTime endTime;
} 