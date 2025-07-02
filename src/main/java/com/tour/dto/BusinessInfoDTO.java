/**
 * @Author Abin
 * @Description 商务信息数据传输对象
 */
package com.tour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商务信息数据传输对象
 * 用于接收和传递商务合作信息和客服链接
 */
@Data
@Schema(description = "商务信息数据传输对象")
public class BusinessInfoDTO {

  /**
   * 商务合作信息
   */
  @Schema(description = "商务合作信息", example = "商务合作请联系：13800138000")
  private String businessCooperation;

  /**
   * 客服链接
   */
  @Schema(description = "客服链接", example = "https://work.weixin.qq.com/kfid/kfc123456789")
  private String customerServiceLink;
}