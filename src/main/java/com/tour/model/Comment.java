package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论实体类
 *
 * @author Kuril
 */
@TableName("comment")
@Data
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户openid
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String openid;

    /**
     * 活动ID
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Long activityId;

    /**
     * 关联订单号
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String orderNo;

    /**
     * 评论状态 1-待审核 2-可见 3-不可见
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private Integer status;

    /**
     * 评论内容
     */
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String content;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
} 