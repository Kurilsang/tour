package com.tour.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author Abin
 * @Description 心愿评论VO
 */
@Data
@ApiModel(description = "心愿评论信息")
public class WishCommentVO {

    /**
     * 评论ID
     */
    @ApiModelProperty(value = "评论ID")
    private Long id;

    /**
     * 心愿ID
     */
    @ApiModelProperty(value = "心愿ID")
    private Long wishId;

    /**
     * 心愿标题，仅管理员查询或用户查询自己的评论时返回
     */
    @ApiModelProperty(value = "心愿标题", notes = "仅管理员查询或用户查询自己的评论时返回")
    private String wishTitle;

    /**
     * 用户openid
     */
    @ApiModelProperty(value = "用户openid")
    private String userOpenid;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像")
    private String avatar;

    /**
     * 评论内容
     */
    @ApiModelProperty(value = "评论内容")
    private String content;

    /**
     * 图片列表
     */
    @ApiModelProperty(value = "图片列表")
    private List<String> imageUrls;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 是否是当前用户发表的评论
     */
    @ApiModelProperty(value = "是否是当前用户发表的评论")
    private Boolean isOwner;
} 