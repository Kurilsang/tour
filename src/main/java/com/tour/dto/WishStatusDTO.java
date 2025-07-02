/**
 * @Author Abin
 * @Description 心愿状态更新DTO (用户版)
 */
package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用户修改心愿状态DTO
 */
@Data
@ApiModel(description = "用户修改心愿状态请求参数")
public class WishStatusDTO {

    /**
     * 心愿ID
     */
    @NotNull(message = "心愿ID不能为空")
    @ApiModelProperty(value = "心愿ID", required = true, example = "1")
    private Long wishId;
} 