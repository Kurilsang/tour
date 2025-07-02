package com.tour.service;

/**
 * @Author Abin
 * @Description 媒体检测处理服务接口
 */
public interface IMediaCheckHandlerService {
    
    /**
     * 处理用户头像检测结果
     *
     * @param userOpenid 用户openid
     * @param mediaUrl 媒体URL
     */
    void handleUserAvatar(String userOpenid, String mediaUrl);
    
    /**
     * 处理心愿图片检测结果
     *
     * @param wishId 心愿ID
     * @param mediaUrl 媒体URL
     */
    void handleWishImage(Long wishId, String mediaUrl);
    
    /**
     * 处理评论图片检测结果
     *
     * @param commentId 评论ID
     * @param mediaUrl 媒体URL
     */
    void handleCommentImage(Long commentId, String mediaUrl);
} 