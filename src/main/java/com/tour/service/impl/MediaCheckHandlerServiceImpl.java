package com.tour.service.impl;

import com.alibaba.fastjson.JSON;
import com.tour.common.constant.Constants;
import com.tour.enums.MediaCheckEnum;
import com.tour.model.User;
import com.tour.model.Wish;
import com.tour.model.WishComment;
import com.tour.service.IMediaCheckHandlerService;
import com.tour.service.IUserService;
import com.tour.service.IWishCommentService;
import com.tour.service.IWishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author Abin
 * @Description 媒体检测处理服务实现
 */
@Slf4j
@Service
public class MediaCheckHandlerServiceImpl implements IMediaCheckHandlerService {

    @Autowired
    private IUserService userService;
    
    @Autowired
    private IWishService wishService;
    
    @Autowired
    private IWishCommentService wishCommentService;

    @Override
    public void handleUserAvatar(String userOpenid, String mediaUrl) {
        User user = userService.getUserByOpenid(userOpenid);
        if (user != null) {
            // 将用户头像设置为默认头像或清空
            user.setAvatar(null);
            userService.updateUser(user);
            log.info("用户头像不合规，已清空, openid: {}", userOpenid);
        }
    }

    @Override
    public void handleWishImage(Long wishId, String mediaUrl) {
        Wish wish = wishService.getById(wishId);
        if (wish != null && wish.getImageUrls() != null) {
            try {
                // 解析图片列表
                List<String> imageUrls = JSON.parseArray(wish.getImageUrls(), String.class);
                if (!CollectionUtils.isEmpty(imageUrls)) {
                    // 找到违规图片并替换为默认图片
                    boolean needUpdate = false;
                    for (int i = 0; i < imageUrls.size(); i++) {
                        if (imageUrls.get(i).equals(mediaUrl)) {
                            imageUrls.set(i, Constants.DEFAULT_IMAGE_URL);
                            needUpdate = true;
                            log.info("心愿图片不合规，已替换为默认图片, wishId: {}, imageUrl: {}", wishId, mediaUrl);
                        }
                    }
                    
                    // 如果有图片被替换，更新心愿记录
                    if (needUpdate) {
                        wish.setImageUrls(JSON.toJSONString(imageUrls));
                        wishService.updateById(wish);
                        log.info("心愿图片列表已更新, wishId: {}", wishId);
                    }
                }
            } catch (Exception e) {
                log.error("处理心愿图片检测结果异常", e);
            }
        }
    }

    @Override
    public void handleCommentImage(Long commentId, String mediaUrl) {
        WishComment comment = wishCommentService.getById(commentId);
        if (comment != null && comment.getImageUrls() != null) {
            try {
                // 解析图片列表
                List<String> imageUrls = JSON.parseArray(comment.getImageUrls(), String.class);
                if (!CollectionUtils.isEmpty(imageUrls)) {
                    // 找到违规图片并替换为默认图片
                    boolean needUpdate = false;
                    for (int i = 0; i < imageUrls.size(); i++) {
                        if (imageUrls.get(i).equals(mediaUrl)) {
                            imageUrls.set(i, Constants.DEFAULT_IMAGE_URL);
                            needUpdate = true;
                            log.info("评论图片不合规，已替换为默认图片, commentId: {}, imageUrl: {}", commentId, mediaUrl);
                        }
                    }
                    
                    // 如果有图片被替换，更新评论记录
                    if (needUpdate) {
                        comment.setImageUrls(JSON.toJSONString(imageUrls));
                        wishCommentService.updateById(comment);
                        log.info("评论图片列表已更新, commentId: {}", commentId);
                    }
                }
            } catch (Exception e) {
                log.error("处理评论图片检测结果异常", e);
            }
        }
    }
} 