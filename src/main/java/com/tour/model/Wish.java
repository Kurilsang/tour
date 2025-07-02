package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 心愿路线实体类
 *
 * @Author Abin
 */
@Data
@TableName("wish")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wish implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发起人openid（关联用户表openid）
     */
    private String userOpenid;

    /**
     * 心愿标题
     */
    private String title;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 目的地ID
     */
    private Long locationId;

    /**
     * 图片链接（JSON数组格式，如 ["url1", "url2"]）
     */
    private String imageUrls;

    /**
     * 状态：0-待成团，1-已成团，2-已关闭
     */
    private Integer status;

    /**
     * 投票数
     */
    private Long voteCount;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
} 