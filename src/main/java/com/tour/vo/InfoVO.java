/**
 * @Author Abin
 * @Description 信息视图对象
 */
package com.tour.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 信息视图对象
 * 用于向前端返回键值对信息
 */
@Data
@Schema(description = "信息视图对象")
public class InfoVO {

  /**
   * 键名
   */
  @Schema(description = "键名", example = "businessInfo")
  private String key;

  /**
   * 值（JSON格式的字符串）
   */
  @Schema(description = "值（JSON格式的字符串）", example = "{\"contact\":\"13800138000\",\"email\":\"business@example.com\"}")
  private String value;
}