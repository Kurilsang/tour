package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.Traveler;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出行人数据访问层
 *
 * @Author Abin
 */
@Mapper
public interface TravelerMapper extends BaseMapper<Traveler> {
} 