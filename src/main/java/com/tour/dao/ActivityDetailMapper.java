package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.Activity;
import com.tour.model.ActivityDetail;
import com.tour.model.Counter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ActivityDetailMapper extends BaseMapper<ActivityDetail> {


}
