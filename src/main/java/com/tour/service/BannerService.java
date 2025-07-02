package com.tour.service;

import com.tour.dto.BannerDTO;
import com.tour.vo.BannerVO;

import java.util.List;

/**
 * @Author Abin
 * @Description 轮播图服务接口
 * @DateTime 2025/5/9 13:48
 */
public interface BannerService {

    /**
     * 获取轮播图列表
     * 
     * @return 轮播图列表
     */
    List<BannerVO> getBannerList();
    
    /**
     * 更新轮播图列表
     * 
     * @param bannerList 轮播图列表
     * @param operatorOpenid 操作者openid
     * @return 是否更新成功
     */
    boolean updateBannerList(List<BannerDTO> bannerList, String operatorOpenid);
} 