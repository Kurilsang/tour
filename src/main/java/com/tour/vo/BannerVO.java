package com.tour.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Abin
 * @Description 轮播图信息VO
 * @DateTime 2025/5/9 13:45
 */
@Data
@ApiModel(description = "轮播图信息")
public class BannerVO {

    /**
     * 轮播图ID
     */
    @ApiModelProperty(value = "轮播图ID", example = "1")
    private Long id;

    /**
     * 图片URL
     */
    @ApiModelProperty(value = "图片URL", example = "https://example.com/banner.jpg")
    private String imageUrl;

    /**
     * 标题
     */
    @ApiModelProperty(value = "标题", example = "春季旅游特惠")
    private String title;

    /**
     * 链接类型：1-活动详情 2-外部链接
     */
    @ApiModelProperty(value = "链接类型：1-活动详情 2-外部链接", example = "1")
    private Integer linkType;

    /**
     * 链接值：活动ID或URL
     */
    @ApiModelProperty(value = "链接值：活动ID或URL", example = "1001")
    private String linkValue;

    /**
     * 排序号
     */
    @ApiModelProperty(value = "排序号", example = "100")
    private Integer sortOrder;
} 