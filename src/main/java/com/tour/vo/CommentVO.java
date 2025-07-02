 package com.tour.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Kuril
 * @Description 评论信息VO
 * @DateTime 2025/5/15 10:25
 */
@Data
@ApiModel(description = "评论信息")
public class CommentVO {

    /**
     * 评论ID
     */
    @ApiModelProperty(value = "评论ID", example = "1")
    private Long id;

    /**
     * 用户openid
     */
    @ApiModelProperty(value = "用户openid", example = "oXhGP5Jc8RdkN3yR6ibRXj_aS-vA")
    private String openid;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称", example = "旅行者小明")
    private String nickName;

    /**
     * 评论状态
     */
    @ApiModelProperty(value = "评论状态", example = "1")
    private Integer status;

    /**
     * 评论内容
     */
    @ApiModelProperty(value = "评论内容", example = "这次活动非常精彩，组织得很好！")
    private String content;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", example = "2025-05-15 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}