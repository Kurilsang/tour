package com.tour.dto;

import com.tour.model.Location;
import com.tour.vo.BusLocationVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动信息数据传输对象
 *
 * @Author Kuril
 */
@Data
@ApiModel(description = "活动信息请求参数")
public class ActivityDTO {

  /**
   * 活动标题
   */
  @ApiModelProperty(value = "活动标题", example = "夏日狂欢活动")
  private String title;

  /**
   * 封面图URL
   */
  @ApiModelProperty(value = "封面图URL", example = "https://example.com/cover.jpg")
  private String coverImage;
  /**
   * 集合地点
   */
  @ApiModelProperty(value = "集合地点", example = "广东省广州市xxx")
  private String activityPosition;

  /**
   * 早鸟价
   */
  @ApiModelProperty(value = "早鸟价", example = "99.99")
  private BigDecimal earlyBirdPrice;

  /**
   * 普通价
   */
  @ApiModelProperty(value = "普通价", example = "199.99")
  private BigDecimal normalPrice;

  /**
   * 早鸟价库存
   */
  @ApiModelProperty(value = "早鸟价库存", example = "100")
  private Integer earlyBirdQuota;

  /**
   * 普通价库存
   */
  @ApiModelProperty(value = "普通价库存", example = "200")
  private Integer normalQuota;

  /**
   * 预留早鸟库存（默认0，前端可传可不传，后端处理默认值）
   */
  @ApiModelProperty(value = "预留早鸟库存，默认0，前端可传可不传，后端处理默认值", example = "10")
  private Integer reservedEarlyBird;

  /**
   * 预留普通库存（默认0，前端可传可不传，后端处理默认值）
   */
  @ApiModelProperty(value = "预留普通库存，默认0，前端可传可不传，后端处理默认值", example = "20")
  private Integer reservedNormal;
  /**
   * 活动报名截止时间
   */
  @ApiModelProperty(value = "活动报名截止时间", example = "2025-06-01T08:00:00")
  private LocalDateTime signEndTime;
  /**
   * 简介
   */
  @ApiModelProperty(value = "简介", example = "我是简介")
  private String description;
  /**
   * 活动开始时间
   */
  @ApiModelProperty(value = "活动开始时间", example = "2025-06-01T09:00:00")
  private LocalDateTime startTime;

  /**
   * 活动结束时间
   */
  @ApiModelProperty(value = "活动结束时间", example = "2025-06-01T18:00:00")
  private LocalDateTime endTime;
  
  /**
   * 可退款截止时间
   */
  @ApiModelProperty(value = "可退款截止时间", example = "2025-06-01T00:00:00")
  private LocalDateTime endRefundTime;

  /**
   * 创建人ID
   */
  @ApiModelProperty(value = "创建人ID，若前端需显式传入，否则可由后端从用户会话获取", example = "1")
  private String createdBy;

  /**
   * 最后修改人ID
   */
  @ApiModelProperty(value = "最后修改人ID，若前端需显式传入，否则可由后端从用户会话获取", example = "2")
  private String updatedBy;

  /**
   * HTML格式内容
   */
  @ApiModelProperty(value = "HTML格式内容", example = "<p>活动详情内容</p>")
  private String content;

  /**
   * 排序权重（默认0，前端可传可不传，后端处理默认值）
   */
  @ApiModelProperty(value = "排序权重，默认0，前端可传可不传，后端处理默认值", example = "1")
  private Integer sortOrder;

  /**
   * 总销量，这个可以选择传或者不传，不传默认不更改，在添加中传了会初始化为0，在修改接口中传了会更改对应值
   */
  @ApiModelProperty(value = "总销量，可选择传或者不传，不传默认不更改，在添加中传了会初始化为0，在修改接口中传了会更改对应值", example = "50")
  private Integer totalSold;

  /**
   * 地图信息
   */
  @ApiModelProperty(value = "地图信息", example = "")
  private Location location;

  /**
   * 发布状态 0-未发布 1-已发布 2-已关闭
   */
  @ApiModelProperty(value = "发布状态 0-未发布 1-已发布 2-已关闭", example = "0")
  private Integer status;

  /**
   * 上车点列表
   */
  @ApiModelProperty(value = "上车点列表")
  private List<BusLocationVO> busLocations;

  /**
   * 活动群二维码URL
   */
  @ApiModelProperty(value = "活动群二维码URL", example = "https://example.com/qrcode.jpg")
  private String groupQrcode;

  /**
   * 活动小程序码URL
   */
  @ApiModelProperty(value = "活动小程序码URL", example = "https://example.com/minicode.jpg")
  private String minicode;
}