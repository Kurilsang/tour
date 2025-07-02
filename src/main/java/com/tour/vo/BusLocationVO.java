package com.tour.vo;

import com.tour.model.Location;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 大巴上车地点数据传输对象
 *
 * @author kuril
 */
@Data
@ApiModel(description = "大巴上车地点请求参数")
public class BusLocationVO {
    /**
     * 上车点id
     */
    @ApiModelProperty(value = "上车点id", example = "1")
    private Long id;
    /**
     * 字符串类型的大巴上车地
     */
    @ApiModelProperty(value = "字符串类型的大巴上车地", example = "广州市天河区体育中心")
    private String busLocation;

    /**
     * 位置信息
     */
    @ApiModelProperty(value = "位置信息", example = "")
    private Location location;
}