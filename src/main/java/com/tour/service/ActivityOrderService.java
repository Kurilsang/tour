package com.tour.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.model.ActivityOrder;
import com.tour.query.ActivityOrderQuery;
import com.tour.vo.ActivityOrderDetail;

import java.util.List;

/**
 * @Author kuril
 * @Description 活动订单服务类
 * @DateTime 2025/5/12 12:18
 */
public interface ActivityOrderService {

    /**
     * 获取活动订单列表
     *
     * @return 活动订单列表
     */
    IPage<ActivityOrder> loadDataList(ActivityOrderQuery activityOrderQuery);


    ActivityOrderDetail findActivityOrderByOrderNo(String orderNo);

    ActivityOrderDetail findActivityOrderAndBusLocationByOrderNo(String orderNo);
    
    /**
     * 通过订单号获取对应活动的群二维码URL
     * 
     * @param orderNo 订单号
     * @return 活动群二维码URL，如果未找到返回null
     */
    String getGroupQrcodeByOrderNo(String orderNo);
    
    /**
     * 通过订单号查询活动订单
     *
     * @param orderNo 订单号
     * @return ActivityOrder对象，如果未找到返回null
     */
    ActivityOrder getActivityOrderByOrderNo(String orderNo);
}