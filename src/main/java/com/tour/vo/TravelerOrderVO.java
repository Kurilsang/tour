package com.tour.vo;

import com.tour.model.Traveler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author Kuril
 * @Description 订单中出行人信息VO，包含票类型和单价信息
 */
@Data
@ApiModel(description = "订单中出行人信息VO")
public class TravelerOrderVO extends Traveler {
    
    /**
     * 票类型 1-早鸟票 2-普通票
     */
    @ApiModelProperty(value = "票类型(1-早鸟票 2-普通票)", example = "2")
    private Integer tickType;
    
    /**
     * 单人票价
     */
    @ApiModelProperty(value = "单人票价", example = "199.00")
    private BigDecimal personAmount;
    
    /**
     * 是否可免单 0-可免单 1-不可退款
     */
    @ApiModelProperty(value = "是否可退款(0-可退款 1-不可退款)", example = "0")
    private Integer refundStatus;
} 