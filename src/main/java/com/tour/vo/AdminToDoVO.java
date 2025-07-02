package com.tour.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理员待办事项VO
 *
 * @Author Kuril
 */
@Data
@ApiModel(description = "管理员待办事项统计")
public class AdminToDoVO {

    /**
     * 待审核评论数量
     */
    @ApiModelProperty(value = "待审核评论数量", example = "5")
    private Long pendingCommentCount;

    /**
     * 待处理退款申请数量
     */
    @ApiModelProperty(value = "待处理退款申请数量", example = "3")
    private Long pendingRefundCount;
} 