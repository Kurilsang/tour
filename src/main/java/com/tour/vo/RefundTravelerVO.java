package com.tour.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款出行人VO
 *
 * @Author Kuril
 */
@Data
@ApiModel(description = "退款出行人信息")
public class RefundTravelerVO {

    /**
     * 出行人ID
     */
    @ApiModelProperty(value = "出行人ID", example = "1")
    private Long travelerId;

    /**
     * 出行人姓名
     */
    @ApiModelProperty(value = "出行人姓名", example = "张三")
    private String travelerName;

    /**
     * 出行人手机号
     */
    @ApiModelProperty(value = "出行人手机号", example = "13800138000")
    private String travelerPhone;

    /**
     * 票类型
     */
    @ApiModelProperty(value = "票类型：1-早鸟票，2-普通票", example = "1")
    private Integer tickType;

    /**
     * 票类型描述
     */
    @ApiModelProperty(value = "票类型描述", example = "早鸟票")
    private String tickTypeDesc;

    /**
     * 退款金额
     */
    @ApiModelProperty(value = "退款金额", example = "99.00")
    private BigDecimal refundAmount;
} 