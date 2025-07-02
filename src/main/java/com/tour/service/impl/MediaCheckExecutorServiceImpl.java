package com.tour.service.impl;

import com.tour.common.constant.Constants;
import com.tour.enums.MediaCheckEnum;
import com.tour.service.IMediaCheckExecutorService;
import com.tour.service.IMediaSubmitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author Abin
 * @Description 媒体检测执行服务实现 - 专门用于异步执行检测任务
 */
@Slf4j
@Service
public class MediaCheckExecutorServiceImpl implements IMediaCheckExecutorService {

    @Autowired
    private IMediaSubmitService mediaSubmitService;
    
    @Override
    @Async("mediaCheckExecutor")
    public void asyncCheckUserAvatar(String avatarUrl, String userOpenid) {
        if (avatarUrl == null || Constants.DEFAULT_IMAGE_URL.equals(avatarUrl)) {
            return;
        }
        
        log.info("【异步线程】开始异步检测用户头像, userOpenid: {}, 线程: {}", userOpenid, Thread.currentThread().getName());
        
        try {
            // 提交异步检测
            mediaSubmitService.submitMediaCheck(
                avatarUrl,
                Constants.MEDIA_TYPE_IMAGE,
                MediaCheckEnum.BusinessType.USER_AVATAR.getCode(),
                userOpenid,
                userOpenid
            );
            log.info("【异步线程】用户头像已提交异步检测, userOpenid: {}", userOpenid);
        } catch (Exception e) {
            // 检测异常，记录日志但不影响主流程
            log.error("【异步线程】用户头像检测异常, userOpenid: {}", userOpenid, e);
        }
        
        log.info("【异步线程】完成异步检测用户头像, userOpenid: {}", userOpenid);
    }
    
    @Override
    @Async("mediaCheckExecutor")
    public void asyncCheckWishImages(List<String> imageUrls, Long wishId, String userOpenid) {
        if (CollectionUtils.isEmpty(imageUrls)) {
            return;
        }
        
        // 图片URL去重
        Set<String> uniqueImageUrls = new HashSet<>(imageUrls);
        List<String> uniqueImageList = new ArrayList<>(uniqueImageUrls);
        
        if (uniqueImageList.size() < imageUrls.size()) {
            log.info("【异步线程】心愿图片去重: 原图片数量 {}, 去重后数量 {}", imageUrls.size(), uniqueImageList.size());
        }
        
        log.info("【异步线程】开始异步检测心愿图片, wishId: {}, imageCount: {}, 线程: {}", wishId, uniqueImageList.size(), Thread.currentThread().getName());
        
        // 遍历所有图片URL进行检测
        for (String imageUrl : uniqueImageList) {
            try {
                // 提交异步检测
                mediaSubmitService.submitMediaCheck(
                    imageUrl,
                    Constants.MEDIA_TYPE_IMAGE,
                    MediaCheckEnum.BusinessType.WISH_IMAGE.getCode(),
                    wishId.toString(),
                    userOpenid
                );
                log.info("【异步线程】心愿图片已提交异步检测, wishId: {}, imageUrl: {}", wishId, imageUrl);
                
                // 为了避免微信API限流，每个图片检测之间稍作延迟
                Thread.sleep(100);
            } catch (Exception e) {
                // 检测异常，记录日志但不影响主流程
                log.error("【异步线程】心愿图片检测异常, wishId: {}, imageUrl: {}", wishId, imageUrl, e);
            }
        }
        
        log.info("【异步线程】完成异步检测心愿图片, wishId: {}", wishId);
    }
    
    @Override
    @Async("mediaCheckExecutor")
    public void asyncCheckCommentImages(List<String> imageUrls, Long commentId, String userOpenid) {
        if (CollectionUtils.isEmpty(imageUrls)) {
            return;
        }
        
        // 图片URL去重
        Set<String> uniqueImageUrls = new HashSet<>(imageUrls);
        List<String> uniqueImageList = new ArrayList<>(uniqueImageUrls);
        
        if (uniqueImageList.size() < imageUrls.size()) {
            log.info("【异步线程】评论图片去重: 原图片数量 {}, 去重后数量 {}", imageUrls.size(), uniqueImageList.size());
        }
        
        log.info("【异步线程】开始异步检测评论图片, commentId: {}, imageCount: {}, 线程: {}", commentId, uniqueImageList.size(), Thread.currentThread().getName());
        
        // 遍历所有图片URL进行检测
        for (String imageUrl : uniqueImageList) {
            try {
                // 提交异步检测
                mediaSubmitService.submitMediaCheck(
                    imageUrl,
                    Constants.MEDIA_TYPE_IMAGE,
                    MediaCheckEnum.BusinessType.COMMENT_IMAGE.getCode(),
                    commentId.toString(),
                    userOpenid
                );
                log.info("【异步线程】评论图片已提交异步检测, commentId: {}, imageUrl: {}", commentId, imageUrl);
                
                // 为了避免微信API限流，每个图片检测之间稍作延迟
                Thread.sleep(100);
            } catch (Exception e) {
                // 检测异常，记录日志但不影响主流程
                log.error("【异步线程】评论图片检测异常, commentId: {}, imageUrl: {}", commentId, imageUrl, e);
            }
        }
        
        log.info("【异步线程】完成异步检测评论图片, commentId: {}", commentId);
    }
} 