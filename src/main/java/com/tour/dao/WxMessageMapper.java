package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.WxMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author Abin
 * @Description 微信消息数据访问接口
 */
@Mapper
public interface WxMessageMapper extends BaseMapper<WxMessage> {
    
} 