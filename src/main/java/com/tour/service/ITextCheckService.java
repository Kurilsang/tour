package com.tour.service;

/**
 * @Author Abin
 * @Description 文本内容检测服务接口
 */
public interface ITextCheckService {
    
    /**
     * 检测文本内容是否合规
     * 
     * @param content 文本内容
     * @param userOpenid 用户openid
     * @param scene 场景值，1-资料；2-评论；3-论坛；4-社交日志
     * @return 是否通过检测，true-通过，false-不通过
     */
    boolean checkText(String content, String userOpenid, Integer scene);
    
    /**
     * 检测用户昵称是否合规
     * 
     * @param nickname 用户昵称
     * @param userOpenid 用户openid
     * @return 是否通过检测，true-通过，false-不通过
     */
    boolean checkNickname(String nickname, String userOpenid);
    
    /**
     * 检测评论内容是否合规
     * 
     * @param comment 评论内容
     * @param userOpenid 用户openid
     * @return 是否通过检测，true-通过，false-不通过
     */
    boolean checkComment(String comment, String userOpenid);

    /**
     * 检测心愿内容是否合规（优化版，使用title参数进行关联检测）
     * 
     * @param title 心愿标题
     * @param description 心愿描述
     * @param userOpenid 用户openid
     * @return 是否通过检测，true-通过，false-不通过
     */
    boolean checkWishContentOptimized(String title, String description, String userOpenid);
    
    /**
     * 检测评论内容是否合规（优化版，带关联标题）
     * 
     * @param comment 评论内容
     * @param wishTitle 心愿标题（关联上下文）
     * @param userOpenid 用户openid
     * @return 是否通过检测，true-通过，false-不通过
     */
    boolean checkCommentWithContext(String comment, String wishTitle, String userOpenid);
} 