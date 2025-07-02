package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Abin
 * @Description 成就创建数据传输对象
 */
@Data
@ApiModel(description = "成就创建参数")
public class AchievementCreateDTO {

    /**
     * 成就标题
     */
    @ApiModelProperty(value = "成就标题", required = true)
    private String title;

    /**
     * 图标URL
     */
    @ApiModelProperty(value = "图标URL", required = true)
    private String iconUrl;

    /**
     * 成就描述
     */
    @ApiModelProperty(value = "成就描述", required = true)
    private String description;

    /**
     * 关联活动ID
     */
    @ApiModelProperty(value = "关联活动ID", required = false)
    private Long activityId;
} 