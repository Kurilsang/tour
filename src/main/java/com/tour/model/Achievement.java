package com.tour.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author Abin
 * @Description 成就实体类
 * @DateTime 2025/5/8 14:15
 */
@Data
public class Achievement implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 成就标题
     */
    private String title;

    /**
     * 图标URL
     */
    private String iconUrl;

    /**
     * 成就描述
     */
    private String description;

    /**
     * 签到码
     */
    private String signInCode;

    /**
     * 签到二维码链接
     */
    private String signInUrl;

    /**
     * 关联活动ID
     */
    private Long activityId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    public Achievement() {}
} 