package com.tour.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tour.model.Traveler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author kuril
 * @Description 报名名单信息VO
 * @DateTime 2025/5/10 20:23
 */
@Data
@ApiModel(description = "报名名单信息")
public class SignUpListVO {
    /**
     * 报名ID
     */
    @ApiModelProperty(value = "报名ID", example = "1")
    private Long id;
    /**
     * 用户openid
     */
    @ApiModelProperty(value = "用户openid", example = "1")
    private String openId;
    /**
     * 用户名称
     */
    @ApiModelProperty(value = "用户名称", example = "小明")
    private String nickname;
    /**
     * 出行人姓名
     */
    @ApiModelProperty(value = "出行人姓名", example = "小明")
    private String name;
    /**
     * 出行人电话
     */
    @ApiModelProperty(value = "出行人电话", example = "134283631301")
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
    private String travelerNickname;


    /**
     * 总金额
     */
    @ApiModelProperty(value = "这次报名对应的总金额", example = "6666.66")
    private BigDecimal price;

    /**
     * 上车点
     */
    @ApiModelProperty(value = "上车点", example = "广州市大巴站")
    private String busLocation;
}