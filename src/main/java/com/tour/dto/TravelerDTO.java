package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 出行人信息传输对象
 *
 * @Author Abin
 */
@Data
@ApiModel(description = "出行人信息传输对象")
public class TravelerDTO {

    @ApiModelProperty(value = "姓名", required = true)
    private String name;

    @ApiModelProperty(value = "手机号", required = true)
    private String phone;

    @ApiModelProperty(value = "性别(1-男 2-女)", required = true)
    private Integer gender;

    @ApiModelProperty(value = "身份证号", required = true)
    private String idCard;

    @ApiModelProperty(value = "紧急联系人")
    private String emergencyName;

    @ApiModelProperty(value = "紧急联系电话")
    private String emergencyPhone;

    @ApiModelProperty(value = "称呼方式")
    private String nickname;
} 