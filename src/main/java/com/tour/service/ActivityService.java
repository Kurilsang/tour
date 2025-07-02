package com.tour.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.dto.ActivityDTO;
import com.tour.model.Activity;
import com.tour.query.ActivityQuery;
import com.tour.vo.SignUpListVO;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService {
  /**
   * 加载所有活动
   */
  List<Activity> loadDataList();

  /**
   * 添加活动
   */
  Long addActivity(ActivityDTO activityDTO);

  /**
   * 根据ID删除活动
   */
  void deleteActivityById(int id);

  /**
   * 根据ID更新活动
   */
  void updateActivityById(Long id, ActivityDTO activityDTO);

  /**
   * 根据ID查询活动详情
   */
  Object selectActivityById(Long id);

  /**
   * 用户查询活动详情
   */
  Object UserSelectActivityById(Long id);

  /**
   * 分页查询活动列表
   */
  IPage<Activity> findListByPage(ActivityQuery activityQuery);

  /**
   * 用户分页查询活动列表
   */
  IPage<Activity> UserFindListByPage(ActivityQuery activityQuery);

  /**
   * 切换活动状态
   */
  void switchActivityStatusById(Long id, Integer status);

  /**
   * 完成活动报名处理
   */
  //boolean completeActivitySignUp(List<SignUpListVO> signUpListVOList);

  /**
   * 获取可用于轮播图的活动列表（已发布和进行中）
   * 
   * @param activityQuery 查询条件
   * @return 分页活动列表
   */
  IPage<Activity> findAvailableActivities(ActivityQuery activityQuery);

  /**
   * 结束活动
   * 
   * @param activityId 活动ID
   * @return 是否成功
   */
  boolean completeActivity(Long activityId);

  /**
   * 查询已结束的活动列表
   * 
   * @param activityQuery 查询条件
   * @return 分页活动列表
   */
  IPage<Activity> findFinishedActivities(ActivityQuery activityQuery);

  /**
   * 根据ID批量删除活动
   * 
   * @param ids 活动ID列表
   * @return 成功删除的数量
   */
//  int deleteActivitiesByIds(List<Long> ids);

  /**
   * 为活动生成小程序码
   *
   * @param activityId 活动ID
   * @param page 小程序页面路径
   * @param openid 用户openid
   * @return 生成的小程序码URL
   */
  String generateActivityWxaCode(Long activityId, String page, String openid);

  /**
   * 生成活动海报
   * 
   * @param activityId 活动ID
   * @param page 小程序页面路径
   * @param openid 用户openid
   * @return 生成的海报URL
   */
  String generateActivityPoster(Long activityId, String page, String openid);
}
