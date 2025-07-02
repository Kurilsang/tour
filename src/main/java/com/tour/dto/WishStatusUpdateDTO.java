/**
 * @Author Abin
 * @Description 心愿状态更新DTO
 */
package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 心愿状态更新DTO
 */
@Data
@ApiModel(description = "心愿状态更新请求参数")
public class WishStatusUpdateDTO {

    /**
     * 心愿ID
     */
    @ApiModelProperty(value = "心愿ID", required = true, example = "1")
    private Long wishId;

    /**
     * 状态：0-待成团，1-已成团，2-已关闭
     */
    @ApiModelProperty(value = "状态：0-待成团，1-已成团，2-已关闭", required = true, example = "1")
    private Integer status;
} 