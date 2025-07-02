package com.tour.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.tour.model.ActivityOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ActivityOrderMapper extends BaseMapper<ActivityOrder> {

    /**
     * 自定义分页查询方法，确保返回结果包含totalAmount字段
     * @param page 分页参数
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    @Select("SELECT * FROM activity_order ${ew.customSqlSegment}")
    IPage<ActivityOrder> selectPageWithAmount(IPage<ActivityOrder> page, @Param(Constants.WRAPPER) Wrapper<ActivityOrder> queryWrapper);
}
