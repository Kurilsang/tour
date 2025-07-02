package com.tour.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author Claude
 * @Description 微信退款响应VO
 */
@Data
@ApiModel(value = "微信退款响应VO", description = "微信支付退款响应结果")
public class WxRefundResponseVO {

    @ApiModelProperty(value = "商户订单号", example = "202405010001")
    private String outTradeNo;
    
    @ApiModelProperty(value = "商户退款单号", example = "refund_1651234567890")
    private String outRefundNo;
    
    @ApiModelProperty(value = "微信支付退款单号", example = "wx123456789")
    private String refundId;
    
    @ApiModelProperty(value = "退款状态", example = "SUCCESS")
    private String status;
    
    @ApiModelProperty(value = "退款成功时间")
    private String successTime;
    
    @ApiModelProperty(value = "订单总金额", example = "99.00")
    private BigDecimal totalAmount;
    
    @ApiModelProperty(value = "退款金额", example = "99.00")
    private BigDecimal refundAmount;
    
    @ApiModelProperty(value = "退款币种", example = "CNY")
    private String currency;
} 