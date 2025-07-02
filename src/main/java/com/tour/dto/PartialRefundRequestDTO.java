package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 部分退款请求DTO
 *
 * @Author Kuril
 */
@Data
@ApiModel(description = "部分退款请求DTO")
public class PartialRefundRequestDTO {

    @ApiModelProperty(value = "订单号", required = true)
    @NotEmpty(message = "订单号不能为空")
    private String outTradeNo;

    @ApiModelProperty(value = "出行人ID列表", required = true)
    @NotNull(message = "出行人ID列表不能为空")
    private List<Long> travelerIds;

    @ApiModelProperty(value = "退款原因")
    private String reason;
} 