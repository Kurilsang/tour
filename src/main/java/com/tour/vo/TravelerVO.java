package com.tour.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 出行人信息视图对象
 *
 * @Author Abin
 */
@Data
@ApiModel(description = "出行人信息视图对象")
public class TravelerVO {

    @ApiModelProperty(value = "出行人ID")
    private String id;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "性别(1-男 2-女)")
    private Integer gender;

    @ApiModelProperty(value = "身份证号")
    private String idCard;

    @ApiModelProperty(value = "生日", example = "1990-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String birthday;

    @ApiModelProperty(value = "紧急联系人")
    private String emergencyName;

    @ApiModelProperty(value = "紧急联系电话")
    private String emergencyPhone;

    @ApiModelProperty(value = "称呼方式")
    private String nickname;
    
    @ApiModelProperty(value = "票类型(1-早鸟票 2-普通票)", example = "2")
    private Integer tickType;
} 