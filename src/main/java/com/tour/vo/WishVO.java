package com.tour.vo;

import com.tour.model.Location;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 心愿路线视图对象
 *
 * @Author Abin
 */
@Data
@ApiModel(description = "心愿路线信息")
public class WishVO {

    /**
     * 心愿ID
     */
    @ApiModelProperty(value = "心愿ID")
    private Long id;

    /**
     * 发起人昵称
     */
    @ApiModelProperty(value = "发起人昵称")
    private String nickname;

    /**
     * 发起人头像
     */
    @ApiModelProperty(value = "发起人头像")
    private String avatar;

    /**
     * 心愿标题
     */
    @ApiModelProperty(value = "心愿标题")
    private String title;

    /**
     * 详细描述
     */
    @ApiModelProperty(value = "详细描述")
    private String description;

    /**
     * 目的地信息
     */
    @ApiModelProperty(value = "目的地信息")
    private Location location;

    /**
     * 图片链接列表
     */
    @ApiModelProperty(value = "图片链接列表")
    private List<String> imageUrls;

    /**
     * 状态：0-待成团，1-已成团，2-已关闭
     */
    @ApiModelProperty(value = "状态：0-待成团，1-已成团，2-已关闭")
    private Integer status;

    /**
     * 投票数
     */
    @ApiModelProperty(value = "投票数")
    private Long voteCount;
    
    /**
     * 评论数
     */
    @ApiModelProperty(value = "评论数")
    private Long commentCount;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 当前用户是否已投票
     */
    @ApiModelProperty(value = "当前用户是否已投票")
    private Boolean hasVoted;
    
    /**
     * 当前用户是否是心愿所有者
     */
    @ApiModelProperty(value = "当前用户是否是心愿所有者")
    private Boolean isOwner;
    
    /**
     * 最新评论列表（只在详情查询时返回前3条）
     */
    @ApiModelProperty(value = "最新评论列表（只在详情查询时返回前3条）")
    private List<WishCommentVO> comments;
    
    /**
     * 投票用户列表（只返回前5个）
     */
    @ApiModelProperty(value = "投票用户列表（只返回前5个）")
    private List<UserVoteInfo> voteUsers;
    
    /**
     * 投票用户简要信息
     */
    @Data
    @ApiModel(description = "投票用户简要信息")
    public static class UserVoteInfo {
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
    }
} 