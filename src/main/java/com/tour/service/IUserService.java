package com.tour.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.dto.UserRoleDTO;
import com.tour.model.User;
import com.tour.query.UserQuery;

/**
 * 用户服务接口
 */
public interface IUserService {
    
    /**
     * 根据 openid 查询用户
     *
     * @param openid 微信openid
     * @return 用户信息
     */
    User getUserByOpenid(String openid);
    
    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUser(User user);
    
    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 创建的用户
     */
    User createUser(User user);
    
    /**
     * 分页查询用户列表
     *
     * @param query 查询条件
     * @return 分页用户列表
     */
    IPage<User> queryUserList(UserQuery query);
    
    /**
     * 更新用户角色
     *
     * @param roleDTO 角色信息
     * @param operatorOpenid 操作者openid（必须是超级管理员）
     * @return 是否成功
     */
    boolean updateUserRole(UserRoleDTO roleDTO, String operatorOpenid);
    
    /**
     * 重置用户资料
     * 将用户的头像和昵称恢复为默认值
     *
     * @param targetOpenid 目标用户openid
     * @param operatorOpenid 操作者openid（必须是管理员）
     * @return 是否成功
     */
    boolean resetUserProfile(String targetOpenid, String operatorOpenid);
}