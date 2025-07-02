package com.tour.dto;

import com.tour.model.Location;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新心愿路线请求DTO
 *
 * @Author Abin
 */
@Data
@ApiModel(description = "更新心愿路线请求参数")
public class WishUpdateDTO {

    /**
     * 心愿ID
     */
    @NotNull(message = "心愿ID不能为空")
    @ApiModelProperty(value = "心愿ID", required = true, example = "1")
    private Long id;
    
    /**
     * 心愿标题
     */
    @NotBlank(message = "心愿标题不能为空")
    @ApiModelProperty(value = "心愿标题", required = true, example = "想去三亚看海")
    private String title;

    /**
     * 详细描述
     */
    @ApiModelProperty(value = "详细描述", example = "想和朋友一起去三亚看海，感受阳光沙滩")
    private String description;

    /**
     * 目的地位置信息
     */
    @NotNull(message = "目的地不能为空")
    @ApiModelProperty(value = "目的地位置信息", required = true)
    private Location location;

    /**
     * 图片链接列表
     */
    @ApiModelProperty(value = "图片链接列表", example = "[\"http://example.com/image1.jpg\", \"http://example.com/image2.jpg\"]")
    private List<String> imageUrls;
} 