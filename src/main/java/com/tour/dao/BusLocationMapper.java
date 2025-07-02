package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.BusLocation;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author Kuril
 * @Description 上车地接口
 * @DateTime 2025/5/13 10:11
 */
@Mapper
public interface BusLocationMapper extends BaseMapper<BusLocation> {
}
