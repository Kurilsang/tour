package com.tour.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 心愿路线评论提交DTO
 *
 * @Author Abin
 */
@Data
@ApiModel(description = "心愿路线评论提交参数")
public class WishCommentDTO {

    /**
     * 心愿ID
     */
    @NotNull(message = "心愿ID不能为空")
    @ApiModelProperty(value = "心愿ID", required = true, example = "1")
    private Long wishId;
    
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @ApiModelProperty(value = "评论内容", required = true, example = "这个地方真不错，我也想去！")
    private String content;
    
    /**
     * 图片链接列表
     */
    @ApiModelProperty(value = "图片链接列表", example = "[\"http://example.com/image1.jpg\", \"http://example.com/image2.jpg\"]")
    private List<String> imageUrls;
} 