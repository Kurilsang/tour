package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.ProductOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author Kuril
 * @Description 商品订单Mapper接口
 * @DateTime 2025/5/17 10:15
 */
@Mapper
public interface ProductOrderMapper extends BaseMapper<ProductOrder> {
} 