package com.tour.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品订单详情视图对象，扩展原有ProductOrderVO
 *
 * @Author Kuril
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "商品订单详情视图对象")
public class ProductOrderDetailVO extends ProductOrderVO {
    
    /**
     * 退款状态: 0-未退款 1-已退款
     */
    @ApiModelProperty(value = "退款状态: 0-未退款 1-已退款", example = "0")
    private Integer refundStatus;
    
    /**
     * 退款状态描述
     */
    @ApiModelProperty(value = "退款状态描述", example = "未退款")
    private String refundStatusDesc;
} 