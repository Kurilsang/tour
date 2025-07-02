/**
 * @Author Abin
 * @Description 合同信息DTO
 */
package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 合同信息数据传输对象
 */
@Data
@ApiModel(description = "合同信息数据传输对象")
public class ContractInfoDTO {

    /**
     * 合同内容（富文本格式）
     */
    @ApiModelProperty(value = "合同内容（富文本格式）")
    private String content;
} 