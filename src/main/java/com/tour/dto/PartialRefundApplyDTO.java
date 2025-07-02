package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 部分出行人退款申请DTO
 *
 * @Author Kuril
 */
@Data
@ApiModel(description = "部分出行人退款申请请求参数")
public class PartialRefundApplyDTO {

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
    @ApiModelProperty(value = "退款理由", required = true, example = "部分出行人无法参与活动")
    private String reason;

    /**
     * 订单类型，对于部分退款，只能是活动订单，固定为1
     */
    @ApiModelProperty(value = "订单类型: 1-活动订单", required = true, example = "1", notes = "部分退款只支持活动订单")
    private Integer orderType = 1;
    
    /**
     * 选中的出行人ID列表
     */
    @NotEmpty(message = "出行人列表不能为空")
    @ApiModelProperty(value = "选中的出行人ID列表", required = true, example = "[1, 2]")
    private List<Long> travelerIds;
    
    /**
     * 选中的活动订单出行人关联ID列表
     */
    @NotEmpty(message = "活动订单出行人关联ID列表不能为空")
    @ApiModelProperty(value = "选中的活动订单出行人关联ID列表", required = true, example = "[1, 2]", notes = "存储的是出行人ID列表，与travelerIds相同")
    private List<Long> activityOrderTravelerIds;
} 