package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 退款申请DTO
 *
 * @Author Kuril
 */
@Data
@ApiModel(description = "退款申请请求参数")
public class RefundApplyDTO {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    @ApiModelProperty(value = "订单号", required = true, example = "20231201123456")
    private String outTradeOrder;

    /**
     * 退款理由
     */
    @NotBlank(message = "退款理由不能为空")
    @ApiModelProperty(value = "退款理由", required = true, example = "行程有变，无法参与活动")
    private String reason;

    /**
     * 订单类型 1-活动订单 2-商品订单
     */
    @NotNull(message = "订单类型不能为空")
    @ApiModelProperty(value = "订单类型: 1-活动订单, 2-商品订单", required = true, example = "1")
    private Integer orderType;
} 