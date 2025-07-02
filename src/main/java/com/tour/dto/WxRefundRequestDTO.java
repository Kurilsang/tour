package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Claude
 * @Description 微信退款请求DTO
 */
@Data
@ApiModel(value = "微信退款请求DTO", description = "微信支付退款请求参数")
public class WxRefundRequestDTO {

    @ApiModelProperty(value = "商户订单号", required = true, example = "202405010001")
    private String outTradeNo;
    
    @ApiModelProperty(value = "退款原因", example = "用户取消订单")
    private String reason;
} 