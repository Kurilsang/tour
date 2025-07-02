package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Claude
 * @Description 商品订单退款DTO
 */
@Data
@ApiModel(value = "商品订单退款DTO", description = "商品订单退款请求参数")
public class ProductRefundDTO {

    @ApiModelProperty(value = "商户订单号", required = true, example = "202405010001")
    private String outTradeNo;
    
    @ApiModelProperty(value = "退款原因", example = "用户申请退款")
    private String reason;
}