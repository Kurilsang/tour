package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 退款拒绝DTO
 *
 * @Author Kuril
 */
@Data
@ApiModel(value = "退款拒绝DTO", description = "退款拒绝DTO")
public class RefundRejectDTO {

    @ApiModelProperty(value = "退款申请ID", required = true, example = "1")
    @NotNull(message = "退款申请ID不能为空")
    private Long id;

    @ApiModelProperty(value = "管理员备注", required = true, example = "不符合退款条件")
    @NotBlank(message = "管理员备注不能为空")
    private String adminRemark;
} 