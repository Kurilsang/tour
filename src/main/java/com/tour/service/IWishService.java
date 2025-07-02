package com.tour.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tour.dto.WishCreateDTO;
import com.tour.dto.WishUpdateDTO;
import com.tour.model.Wish;
import com.tour.query.WishQuery;
import com.tour.vo.WishVO;

/**
 * 心愿路线服务接口
 *
 * @Author Abin
 */
public interface IWishService extends IService<Wish> {
    
    /**
     * 创建心愿路线
     *
     * @param wishCreateDTO 创建参数
     * @param userOpenid 用户openid
     * @return 创建的心愿路线信息
     */
    WishVO createWish(WishCreateDTO wishCreateDTO, String userOpenid);
    
    /**
     * 更新心愿路线
     *
     * @param wishUpdateDTO 更新参数
     * @param userOpenid 用户openid
     * @return 更新后的心愿路线信息
     */
    WishVO updateWish(WishUpdateDTO wishUpdateDTO, String userOpenid);
    
    /**
     * 根据ID查询心愿路线详情
     *
     * @param id 心愿路线ID
     * @return 心愿路线详情
     */
    WishVO getWishById(Long id);
    
    /**
     * 根据ID查询心愿路线详情，并判断当前用户是否是所有者
     *
     * @param id 心愿路线ID
     * @param currentUserOpenid 当前用户openid
     * @return 心愿路线详情
     */
    WishVO getWishById(Long id, String currentUserOpenid);
    
    /**
     * 高级查询心愿路线列表
     *
     * @param query 查询参数
     * @return 分页心愿路线列表
     */
    IPage<WishVO> queryWishList(WishQuery query);
    
    /**
     * 查询用户创建的心愿路线列表
     *
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param userOpenid 用户openid
     * @return 分页心愿路线列表
     */
    IPage<WishVO> queryUserWishList(Integer pageNo, Integer pageSize, String userOpenid);
    
    /**
     * 查询用户已投票的心愿路线列表
     *
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param userOpenid 用户openid
     * @return 分页心愿路线列表
     */
    IPage<WishVO> queryUserVotedWishList(Integer pageNo, Integer pageSize, String userOpenid);
    
    /**
     * 用户投票
     *
     * @param wishId 心愿路线ID
     * @param userOpenid 用户openid
     * @return 是否成功
     */
    boolean voteWish(Long wishId, String userOpenid);
    
    /**
     * 取消投票
     *
     * @param wishId 心愿路线ID
     * @param userOpenid 用户openid
     * @return 是否成功
     */
    boolean cancelVote(Long wishId, String userOpenid);
    
    /**
     * 关闭心愿路线（仅心愿创建者可操作）
     *
     * @param wishId 心愿ID
     * @param userOpenid 用户openid
     * @return 是否成功
     */
    boolean closeWish(Long wishId, String userOpenid);
} 