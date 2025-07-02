package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.WxPaymentActivityOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author Claude
 * @Description 微信支付活动订单关联信息Mapper
 */
@Mapper
public interface WxPaymentActivityOrderMapper extends BaseMapper<WxPaymentActivityOrder> {
    
    /**
     * 根据订单号查询微信支付信息
     *
     * @param orderNo 订单号
     * @return 微信支付信息
     */
    @Select("SELECT * FROM wxpayment_activity_order WHERE order_no = #{orderNo}")
    WxPaymentActivityOrder selectByOrderNo(@Param("orderNo") String orderNo);
} 