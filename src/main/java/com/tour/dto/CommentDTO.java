package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 评论数据传输对象
 *
 * @Author Kuril
 */
@Data
@ApiModel(description = "评论数据传输对象")
public class CommentDTO {

    /**
     * 关联订单号
     */
    @ApiModelProperty(value = "关联订单号", required = true, example = "202305161234567890")
    private String orderNo;

    /**
     * 评论内容
     */
    @ApiModelProperty(value = "评论内容", required = true, example = "这次活动非常精彩，组织得很好！")
    private String content;
} 