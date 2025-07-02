package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 心愿路线评论实体类
 *
 * @Author Abin
 */
@Data
@TableName("wish_comment")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 心愿ID
     */
    private Long wishId;

    /**
     * 用户openid
     */
    private String userOpenid;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 图片链接（JSON数组格式，如 ["url1", "url2"]）
     */
    private String imageUrls;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
} 