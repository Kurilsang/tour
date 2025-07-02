package com.tour.service;

import com.tour.dto.AchievementCreateDTO;
import com.tour.dto.AchievementUpdateDTO;
import com.tour.model.Achievement;
import com.tour.vo.AchievementVO;

import java.util.List;
import java.io.File;

/**
 * @Author Abin
 * @Description 成就服务接口
 * @DateTime 2025/5/8 15:35
 */
public interface IAchievementService {

    /**
     * 获取所有成就列表
     * 
     * @param openid 用户openid
     * @return 成就列表
     */
    List<AchievementVO> getAchievementList(String openid);

    /**
     * 获取用户已获得的成就列表
     * 
     * @param openid 用户openid
     * @return 已获得的成就列表
     */
    List<AchievementVO> getUserAchievements(String openid);

    /**
     * 获取单个成就详情
     *
     * @param achievementId 成就ID
     * @param openid 用户openid，可为null
     * @return 成就详情
     */
    AchievementVO getAchievementById(Long achievementId, String openid);
    
    /**
     * 根据活动ID获取成就详情
     *
     * @param activityId 活动ID
     * @param openid 用户openid，可为null
     * @return 成就详情
     */
    AchievementVO getAchievementByActivityId(Long activityId, String openid);

    /**
     * 扫码签到获取成就
     *
     * @param signInCode 签到码
     * @param openid 用户openid
     * @return 获得的成就信息
     */
    AchievementVO signInAchievement(String signInCode, String openid);

    /**
     * 生成小程序码
     *
     * @param achievementId 成就ID
     * @param page 小程序页面路径
     * @param openid 用户openid
     * @return 小程序码URL
     */
    String generateWxaCode(Long achievementId, String page, String openid);

    /**
     * 创建成就（管理员）
     *
     * @param createDTO 成就创建参数
     * @return 创建的成就信息
     */
    AchievementVO createAchievement(AchievementCreateDTO createDTO);
    
    /**
     * 更新成就（管理员）
     *
     * @param updateDTO 成就更新参数
     * @return 更新后的成就信息
     */
    AchievementVO updateAchievement(AchievementUpdateDTO updateDTO);

    /**
     * 获取所有成就列表（管理员视图）
     *
     * @return 所有成就列表
     */
    List<AchievementVO> getAllAchievementsForAdmin();
} 