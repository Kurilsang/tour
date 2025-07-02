package com.tour.service.impl;

import com.tour.common.constant.Constants;
import com.tour.common.util.WxTextCheckUtil;
import com.tour.service.ITextCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author Abin
 * @Description 文本内容检测服务实现
 */
@Slf4j
@Service
public class TextCheckServiceImpl implements ITextCheckService {

    @Autowired
    private WxTextCheckUtil wxTextCheckUtil;
    
    @Override
    public boolean checkText(String content, String userOpenid, Integer scene) {
        return wxTextCheckUtil.checkText(content, userOpenid, scene);
    }
    
    @Override
    public boolean checkNickname(String nickname, String userOpenid) {
        if (nickname == null || nickname.isEmpty()) {
            return Constants.TEXT_CHECK_DEFAULT_RESULT; // 空昵称默认通过
        }
        return wxTextCheckUtil.checkNickname(nickname, userOpenid); // 使用昵称检测
    }
    
    @Override
    public boolean checkComment(String comment, String userOpenid) {
        if (comment == null || comment.isEmpty()) {
            return Constants.TEXT_CHECK_DEFAULT_RESULT; // 空评论默认通过
        }
        return checkText(comment, userOpenid, Constants.TEXT_CHECK_SCENE_COMMENT); // 使用场景2-评论
    }
    
    @Override
    public boolean checkWishContentOptimized(String title, String description, String userOpenid) {
        if ((title == null || title.isEmpty()) && (description == null || description.isEmpty())) {
            return Constants.TEXT_CHECK_DEFAULT_RESULT; // 标题和描述都为空时默认通过
        }
        
        // 使用优化版本的文本检测方法，带标题参数
        boolean result = wxTextCheckUtil.checkTextWithTitle(
                title, 
                description, 
                userOpenid, 
                Constants.TEXT_CHECK_SCENE_PROFILE, // 标题使用资料场景
                Constants.TEXT_CHECK_SCENE_FORUM    // 描述使用论坛场景
        );
        
        if (!result) {
            log.warn("心愿内容检测不通过，title: {}, description: {}, userOpenid: {}", 
                    title, description, userOpenid);
        }
        
        return result;
    }
    
    @Override
    public boolean checkCommentWithContext(String comment, String wishTitle, String userOpenid) {
        if (comment == null || comment.isEmpty()) {
            return Constants.TEXT_CHECK_DEFAULT_RESULT; // 空评论默认通过
        }
        
        // 如果心愿标题为空，则使用普通评论检测
        if (wishTitle == null || wishTitle.isEmpty()) {
            return checkComment(comment, userOpenid);
        }
        
        // 使用带标题的文本检测方法
        boolean result = wxTextCheckUtil.checkTextWithTitle(
                wishTitle,  // 使用心愿标题作为上下文
                comment,    // 评论内容
                userOpenid,
                null,       // 不检测标题，传null
                Constants.TEXT_CHECK_SCENE_COMMENT  // 评论场景
        );
        
        if (!result) {
            log.warn("评论内容检测不通过，comment: {}, wishTitle: {}, userOpenid: {}", 
                    comment, wishTitle, userOpenid);
        }
        
        return result;
    }
} 