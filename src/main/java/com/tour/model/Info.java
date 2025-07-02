/**
 * @Author Abin
 * @Description 信息存储实体类
 */
package com.tour.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 信息存储实体类
 * 用于存储键值对数据，模拟Redis功能
 */
@Data
@TableName("info")
public class Info {

  /**
   * 主键ID
   */
  @TableId(type = IdType.AUTO)
  private Long id;

  /**
   * 键名
   */
  private String infoKey;

  /**
   * JSON格式的值
   */
  private String infoValue;

  /**
   * 创建时间
   */
  private Date createTime;

  /**
   * 更新时间
   */
  private Date updateTime;
}