package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 心愿路线投票实体类
 *
 * @Author Abin
 */
@Data
@TableName("wish_vote")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishVote implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 心愿ID
     */
    private Long wishVoteId;

    /**
     * 用户openid
     */
    private String userOpenid;

    /**
     * 投票时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime voteTime;
} 