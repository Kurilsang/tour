/**
 * @Author Abin
 * @Description 信息数据传输对象
 */
package com.tour.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 信息数据传输对象
 * 用于接收和传递键值对信息
 */
@Data
@Schema(description = "信息数据传输对象")
public class InfoDTO {

  /**
   * 键名
   */
  @Schema(description = "键名", required = true, example = "businessInfo")
  private String key;

  /**
   * 值（JSON格式的字符串）
   */
  @Schema(description = "值（JSON格式的字符串）", required = true, example = "{\"contact\":\"13800138000\",\"email\":\"business@example.com\"}")
  private String value;
}