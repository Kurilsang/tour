package com.tour.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @Author Abin
 * @Description 微信消息实体类
 */
@Data
@TableName(value = "wx_message", autoResultMap = true)
public class WxMessage {

    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 消息类型，例如：media_check_async、text_check_async等
     */
    private String messageType;
    
    /**
     * 事件类型，例如：CheckResult等
     */
    private String eventType;
    
    /**
     * 关联的用户openid
     */
    private String userOpenid;
    
    /**
     * 关联的小程序appid
     */
    private String appid;
    
    /**
     * 处理状态：0-待处理，1-已处理，2-处理失败
     */
    private Integer status;
    
    /**
     * 处理结果，例如安全检测结果：0-正常，1-违规，2-处理中
     */
    private Integer result;
    
    /**
     * 原始消息内容
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> rawContent;
    
    /**
     * 备注信息
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 