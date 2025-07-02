package com.tour.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Abin
 * @Description 心愿评论查询条件
 */
@Data
@ApiModel(description = "心愿评论查询参数")
public class WishCommentQuery {

    @ApiModelProperty(value = "搜索关键词，可匹配评论内容")
    private String keyword;
    
    @ApiModelProperty(value = "用户昵称")
    private String nickname;
    
    @ApiModelProperty(value = "心愿ID")
    private Long wishId;
    
    @ApiModelProperty(value = "创建开始时间")
    private LocalDateTime startTime;
    
    @ApiModelProperty(value = "创建结束时间")
    private LocalDateTime endTime;
    
    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;
    
    @ApiModelProperty(value = "每页记录数")
    private Integer pageSize = 10;
} 