package com.tour.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Abin
 * @Description 成就信息VO
 * @DateTime 2025/5/8 15:32
 */
@Data
@ApiModel(description = "成就信息")
public class AchievementVO {

    /**
     * 用户成就ID
     */
    @ApiModelProperty(value = "用户成就ID", example = "1")
    private Long id;

    /**
     * 成就ID
     */
    @ApiModelProperty(value = "成就ID", example = "1")
    private Long achievementId;

    /**
     * 成就标题
     */
    @ApiModelProperty(value = "成就标题", example = "首次登录")
    private String title;

    /**
     * 成就图标
     */
    @ApiModelProperty(value = "成就图标URL", example = "https://example.com/icon.png")
    private String iconUrl;

    /**
     * 成就描述
     */
    @ApiModelProperty(value = "成就描述", example = "首次登录系统即可获得该成就")
    private String description;

    /**
     * 活动ID
     */
    @ApiModelProperty(value = "关联活动ID", example = "10001")
    private Long activityId;

    /**
     * 是否已获得
     */
    @ApiModelProperty(value = "是否已获得该成就", example = "true")
    private Boolean obtained;

    /**
     * 获得时间
     */
    @ApiModelProperty(value = "获得时间", example = "2025-05-08 15:30:00")
    private String obtainTime;
} 