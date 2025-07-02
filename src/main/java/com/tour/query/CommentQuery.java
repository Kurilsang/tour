package com.tour.query;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论查询参数类
 *
 * @author Kuril
 */
@Data
public class CommentQuery extends BaseParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 用户openid
     */
    private String openid;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 评论状态 1-待审核 2-可见 3-不可见
     */
    private Integer status;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 创建时间起始
     */
    private LocalDateTime startTime;

    /**
     * 创建时间结束
     */
    private LocalDateTime endTime;
} 