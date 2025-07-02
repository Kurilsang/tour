package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tour.common.util.CopyTools;
import com.tour.dao.BannerMapper;
import com.tour.dto.BannerDTO;
import com.tour.model.Banner;
import com.tour.service.BannerService;
import com.tour.vo.BannerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Abin
 * @Description 轮播图服务实现类
 * @DateTime 2025/5/9 13:50
 */
@Service("bannerService")
@Slf4j
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    @Override
    public List<BannerVO> getBannerList() {
        // 查询轮播图列表，按排序号升序排列
        QueryWrapper<Banner> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_order");
        List<Banner> banners = bannerMapper.selectList(queryWrapper);
        
        // 转换为VO对象
        return banners.stream()
                .map(banner -> CopyTools.copy(banner, BannerVO.class))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBannerList(List<BannerDTO> bannerList, String operatorOpenid) {
        if (bannerList == null) {
            return false;
        }
        
        try {
            // 先删除所有现有的轮播图
            bannerMapper.delete(null);
            
            // 批量插入新的轮播图数据
            for (BannerDTO bannerDTO : bannerList) {
                Banner banner = CopyTools.copy(bannerDTO, Banner.class);
                // 设置创建人和更新人
                banner.setCreatedBy(operatorOpenid);
                banner.setUpdatedBy(operatorOpenid);
                // 重置ID，避免前端传入ID导致插入失败
                banner.setId(null);
                bannerMapper.insert(banner);
            }
            
            log.info("轮播图列表更新成功，共{}条记录，操作者: {}", bannerList.size(), operatorOpenid);
            return true;
        } catch (Exception e) {
            log.error("轮播图列表更新失败，操作者: {}, 错误信息: {}", operatorOpenid, e.getMessage(), e);
            return false;
        }
    }
} 