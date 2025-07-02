package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理员接受退款申请DTO
 *
 * @Author Kuril
 */
@Data
@ApiModel(description = "管理员接受退款申请参数")
public class AdminRefundAcceptDTO {

    /**
     * 退款申请ID
     */
    @ApiModelProperty(value = "退款申请ID", required = true, example = "1")
    private Long id;

    /**
     * 管理员备注
     */
    @ApiModelProperty(value = "管理员备注", required = false, example = "退款申请已处理，已退款到用户账户")
    private String adminRemark;
    
    /**
     * 指定退款金额（可选，仅适用于活动订单）
     */
    @ApiModelProperty(value = "指定退款金额（可选，仅适用于活动订单）", required = false, example = "199.00")
    private BigDecimal specifiedAmount;
} 