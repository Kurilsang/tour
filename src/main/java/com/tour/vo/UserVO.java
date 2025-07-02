package com.tour.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图对象
 *
 * @Author Abin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 用户角色
     */
    private String role;

    /**
     * 用户openid
     */
    @ApiModelProperty(value = "用户openid", example = "1234567890")
    private String openid;

    /**
     * 创建时间
     */
    private Date createTime;
} 