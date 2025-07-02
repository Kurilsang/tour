package com.tour.service;

import java.util.List;

/**
 * @Author Abin
 * @Description 媒体检测执行服务接口 - 专门用于异步执行检测任务
 */
public interface IMediaCheckExecutorService {
    
    /**
     * 异步检测用户头像
     *
     * @param avatarUrl 头像URL
     * @param userOpenid 用户openid
     */
    void asyncCheckUserAvatar(String avatarUrl, String userOpenid);
    
    /**
     * 异步检测心愿图片
     *
     * @param imageUrls 图片URL列表
     * @param wishId 心愿ID
     * @param userOpenid 用户openid
     */
    void asyncCheckWishImages(List<String> imageUrls, Long wishId, String userOpenid);
    
    /**
     * 异步检测评论图片
     *
     * @param imageUrls 图片URL列表
     * @param commentId 评论ID
     * @param userOpenid 用户openid
     */
    void asyncCheckCommentImages(List<String> imageUrls, Long commentId, String userOpenid);
} 