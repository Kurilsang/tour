package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.WxRefundRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author Claude
 * @Description 微信退款记录Mapper接口
 */
@Mapper
public interface WxRefundRecordMapper extends BaseMapper<WxRefundRecord> {
} 