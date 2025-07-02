package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Abin
 * @Description 用户角色更新数据传输对象
 */
@Data
@ApiModel(description = "用户角色更新参数")
public class UserRoleDTO {
    
    @ApiModelProperty(value = "用户openid", required = true)
    private String openid;
    
    @ApiModelProperty(value = "角色代码", required = true, notes = "user-普通用户，admin-管理员，super_admin-超级管理员")
    private String role;
} 