package com.tour.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tour.model.Traveler;

import java.util.List;

/**
 * 出行人服务接口
 *
 * @Author Abin
 */
public interface ITravelerService extends IService<Traveler> {

    /**
     * 根据用户openid查询出行人列表
     *
     * @param openid 用户openid
     * @return 出行人列表
     */
    List<Traveler> getTravelersByOpenid(String openid);

    /**
     * 保存出行人信息
     *
     * @param traveler 出行人信息
     * @return 是否保存成功
     */
    boolean saveTraveler(Traveler traveler);

    /**
     * 根据ID和openid查询出行人信息
     *
     * @param id 出行人ID
     * @param openid 用户openid
     * @return 出行人信息
     */
    Traveler getTravelerByIdAndOpenid(String id, String openid);

    /**
     * 根据ID和openid删除出行人信息
     * 确保只能删除属于自己的出行人
     *
     * @param id 出行人ID
     * @param openid 用户openid
     * @return 是否删除成功
     * @throws IllegalArgumentException 当出行人不存在或不属于该用户时
     */
    boolean deleteTravelerByIdAndOpenid(String id, String openid);
} 