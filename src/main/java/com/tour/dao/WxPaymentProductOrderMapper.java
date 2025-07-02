package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.WxPaymentProductOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author kuril
 * @Description 微信支付商品订单关联信息Mapper
 */
@Mapper
public interface WxPaymentProductOrderMapper extends BaseMapper<WxPaymentProductOrder> {
    
    /**
     * 根据订单号查询微信支付信息
     *
     * @param orderNo 订单号
     * @return 微信支付信息
     */
    @Select("SELECT * FROM wxpayment_product_order WHERE order_no = #{orderNo}")
    WxPaymentProductOrder selectByOrderNo(@Param("orderNo") String orderNo);
} 