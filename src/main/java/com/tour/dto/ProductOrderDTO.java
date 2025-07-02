package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商品订单数据传输对象
 *
 * @Author Kuril
 */
@Data
@ApiModel(description = "商品订单数据传输对象")
public class ProductOrderDTO {

    /**
     * 商品ID
     */
    @ApiModelProperty(value = "商品ID", required = true, example = "1")
    private Long productId;

    /**
     * 商品数量
     */
    @ApiModelProperty(value = "商品数量", required = true, example = "2")
    private Integer quantity;

} 