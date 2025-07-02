package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户信息更新DTO
 *
 * @Author Abin
 */
@Data
@ApiModel(description = "用户信息更新请求参数")
public class UserUpdateDTO {

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称", example = "微信用户")
    private String nickname;

    /**
     * 用户头像URL
     */
    @ApiModelProperty(value = "用户头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
} 