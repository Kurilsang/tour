package com.tour.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Abin
 * @Description 媒体检测记录实体类
 */
@Data
@TableName("media_check_record")
public class MediaCheckRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 微信平台追踪ID
     */
    private String traceId;
    
    /**
     * 媒体类型：1-图片，2-音频，3-视频
     */
    private Integer mediaType;
    
    /**
     * 媒体URL
     */
    private String mediaUrl;
    
    /**
     * 业务类型：user_avatar-用户头像，wish_image-心愿图片，comment_image-评论图片
     */
    private String businessType;
    
    /**
     * 业务ID
     */
    private String businessId;
    
    /**
     * 用户openid
     */
    private String userOpenid;
    
    /**
     * 检测状态：0-检测中，1-检测完成
     */
    private Integer status;
    
    /**
     * 检测结果：0-合规，1-不合规，2-疑似
     */
    private Integer result;
    
    /**
     * 检测详情
     */
    private String detail;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 