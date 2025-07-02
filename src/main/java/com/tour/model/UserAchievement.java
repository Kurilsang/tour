package com.tour.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author Abin
 * @Description 用户成就类
 * @DateTime 2025/5/8 14:19
 */
@Data
public class UserAchievement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    /**
     * id
     */
    private Long id;

    /**
     * 用户openid
     */
    private String openid;

    /**
     * achievement_id
     */
    private Long achievementId;

    /**
     * obtain_time
     */
    private LocalDateTime obtainTime;

    public UserAchievement() {}
}