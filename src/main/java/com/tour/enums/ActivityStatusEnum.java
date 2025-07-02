/**
 * @Author Kuril
 * @Description 活动状态枚举
 */
package com.tour.enums;

public enum ActivityStatusEnum {
  /**
   * 未发布
   */
  UNPUBLISHED(0, "未发布"),

  /**
   * 已发布
   */
  PUBLISHED(1, "已发布"),

  /**
   * 已关闭
   */
  CLOSED(2, "已关闭"),

  /**
   * 进行中
   */
  IN_PROGRESS(3, "进行中"),

  /**
   * 已结束
   */
  FINISHED(4, "已结束");

  /**
   * 状态代码
   */
  private final Integer code;

  /**
   * 状态名称
   */
  private final String name;

  ActivityStatusEnum(Integer code, String name) {
    this.code = code;
    this.name = name;
  }

  public Integer getCode() {
    return this.code;
  }

  public String getName() {
    return this.name;
  }
}
