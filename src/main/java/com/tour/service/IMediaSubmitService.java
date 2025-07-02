package com.tour.service;

import com.tour.model.MediaCheckRecord;

/**
 * @Author Abin
 * @Description 媒体检测提交服务接口
 */
public interface IMediaSubmitService {
    
    /**
     * 提交媒体检测
     *
     * @param mediaUrl 媒体URL
     * @param mediaType 媒体类型：1-图片，2-音频，3-视频
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param userOpenid 用户openid
     * @return 检测记录
     */
    MediaCheckRecord submitMediaCheck(String mediaUrl, Integer mediaType, 
                                     String businessType, String businessId, 
                                     String userOpenid);
} 