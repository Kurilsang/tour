package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tour.model.Wish;
import com.tour.query.WishQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 心愿路线 Mapper 接口
 *
 * @Author Abin
 */
@Mapper
public interface WishMapper extends BaseMapper<Wish> {
    
    /**
     * 高级查询心愿路线列表，支持关键词搜索和排序
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页心愿路线列表
     */
    IPage<Wish> queryWishList(Page<Wish> page, @Param("query") WishQuery query);
    
} 