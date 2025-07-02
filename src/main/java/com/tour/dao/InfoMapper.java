/**
 * @Author Abin
 * @Description 信息存储Mapper接口
 */
package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.Info;
import org.apache.ibatis.annotations.Mapper;

/**
 * 信息存储Mapper接口
 * 用于操作info表
 */
@Mapper
public interface InfoMapper extends BaseMapper<Info> {
}