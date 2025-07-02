package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tour.common.constant.Constants;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.dao.*;
import com.tour.enums.PageSize;
import com.tour.model.*;
import com.tour.query.ActivityOrderQuery;
import com.tour.service.ActivityOrderService;
import com.tour.service.FileService;
import com.tour.vo.ActivityOrderDetail;
import com.tour.vo.BusLocationVO;
import com.tour.vo.TravelerOrderVO;
import cn.binarywang.wx.miniapp.api.WxMaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Kuril
 * @Description 活动订单服务类
 * @DateTime 2025/5/12 12:25
 */
@Service("activityOrderService")
@Slf4j
public class ActivityOrderServiceImpl implements ActivityOrderService {

  private final ActivityOrderMapper activityOrderMapper;
  private final ActivityMapper activityMapper;
  private final ActivityOrderTravelerMapper activityOrderTravelerMapper;
  private final TravelerMapper travelerMapper;
  private final BusLocationServiceImpl busLocationService;
  private final BusLocationMapper busLocationMapper;
  private final LocationMapper locationMapper;
  private final WxPaymentActivityOrderMapper wxPaymentActivityOrderMapper;
  private final WxRefundRecordMapper wxRefundRecordMapper;
  
  @Autowired
  private WxMaService wxMaService;
  
  @Autowired
  private FileService fileService;

  public ActivityOrderServiceImpl(ActivityOrderMapper activityOrderMapper, ActivityMapper activityMapper,
      ActivityOrderTravelerMapper activityOrderTravelerMapper, TravelerMapper travelerMapper,
      BusLocationServiceImpl busLocationService, BusLocationMapper busLocationMapper, LocationMapper locationMapper,
      WxPaymentActivityOrderMapper wxPaymentActivityOrderMapper, WxRefundRecordMapper wxRefundRecordMapper) {
    this.activityOrderMapper = activityOrderMapper;
    this.activityMapper = activityMapper;
    this.activityOrderTravelerMapper = activityOrderTravelerMapper;
    this.travelerMapper = travelerMapper;
    this.busLocationService = busLocationService;
    this.busLocationMapper = busLocationMapper;
    this.locationMapper = locationMapper;
    this.wxPaymentActivityOrderMapper = wxPaymentActivityOrderMapper;
    this.wxRefundRecordMapper = wxRefundRecordMapper;
  }

  @Override
  public IPage<ActivityOrder> loadDataList(ActivityOrderQuery activityOrderQuery) {
    // 构建分页参数
    Integer pageNo = activityOrderQuery.getPageNo();
    Integer pageSize = activityOrderQuery.getPageSize();
    if (pageNo == null) {
      pageNo = Constants.defaultPageNo; // 假设Constants类存在
    }
    if (pageSize == null) {
      pageSize = PageSize.SIZE10.getSize(); // 假设PageSize枚举存在
    }
    Page<ActivityOrder> page = new Page<>(pageNo, pageSize);

    QueryWrapper<ActivityOrder> queryWrapper = new QueryWrapper<>();

    // 根据 ActivityOrderQuery 中的属性动态添加查询条件
    if (activityOrderQuery.getId() != null) {
      queryWrapper.eq("id", activityOrderQuery.getId());
    }
    if (activityOrderQuery.getOrderNo() != null && !activityOrderQuery.getOrderNo().isEmpty()) {
      queryWrapper.eq("order_no", activityOrderQuery.getOrderNo());
    }
    if (activityOrderQuery.getOpenid() != null && !activityOrderQuery.getOpenid().isEmpty()) {
      queryWrapper.eq("openid", activityOrderQuery.getOpenid());
    }
    if (activityOrderQuery.getActivityId() != null) {
      queryWrapper.eq("activity_id", activityOrderQuery.getActivityId());
    }
    if (activityOrderQuery.getEarlyBirdNum() != null) {
      queryWrapper.eq("early_bird_num", activityOrderQuery.getEarlyBirdNum());
    }
    if (activityOrderQuery.getNormalNum() != null) {
      queryWrapper.eq("normal_num", activityOrderQuery.getNormalNum());
    }
    if (activityOrderQuery.getTotalAmount() != null) {
      queryWrapper.eq("total_amount", activityOrderQuery.getTotalAmount());
    }
    if (activityOrderQuery.getStatus() != null) {
      queryWrapper.eq("status", activityOrderQuery.getStatus());
    }
    if (activityOrderQuery.getPaymentTime() != null) {
      queryWrapper.ge("payment_time", activityOrderQuery.getPaymentTime());
    }
    if (activityOrderQuery.getExpireTime() != null) {
      queryWrapper.le("expire_time", activityOrderQuery.getExpireTime());
    }
    if (activityOrderQuery.getCreateTime() != null) {
      queryWrapper.ge("create_time", activityOrderQuery.getCreateTime());
    }
    if (activityOrderQuery.getStartTime() != null) {
      queryWrapper.ge("create_time", activityOrderQuery.getStartTime());
    }
    if (activityOrderQuery.getEndTime() != null) {
      queryWrapper.le("create_time", activityOrderQuery.getEndTime());
    }

    // 处理排序信息
    String orderBy = activityOrderQuery.getOrderBy();
    if (orderBy != null && !orderBy.isEmpty()) {
      queryWrapper.last("ORDER BY " + orderBy);
    }

    // 使用自定义查询方法，确保返回结果包含totalAmount字段
    IPage<ActivityOrder> result = activityOrderMapper.selectPageWithAmount(page, queryWrapper);
    
    // 检查每个订单是否为部分免单
    if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
      for (ActivityOrder order : result.getRecords()) {
        // 默认设置为非部分免单
        order.setIsPartialRefund(false);
        
        // 检查是否有退款记录
        String orderNo = order.getOrderNo();
        if (orderNo != null && !orderNo.isEmpty()) {
          // 查询该订单的所有退款记录
          QueryWrapper<WxRefundRecord> refundWrapper = new QueryWrapper<>();
          refundWrapper.eq("out_trade_no", orderNo);
          List<WxRefundRecord> refundRecords = wxRefundRecordMapper.selectList(refundWrapper);
          
          // 如果有退款记录，计算退款总金额
          if (refundRecords != null && !refundRecords.isEmpty()) {
            BigDecimal totalRefundAmount = BigDecimal.ZERO;
            BigDecimal originalTotalAmount = null;
            
            // 计算所有退款记录的退款金额之和
            for (WxRefundRecord refundRecord : refundRecords) {
              if (refundRecord.getRefundAmount() != null) {
                totalRefundAmount = totalRefundAmount.add(refundRecord.getRefundAmount());
              }
              
              // 记录原始订单总金额（所有退款记录中的total_amount应该相同）
              if (originalTotalAmount == null && refundRecord.getTotalAmount() != null) {
                originalTotalAmount = refundRecord.getTotalAmount();
              }
            }
            
            // 使用第一条退款记录的total_amount，如果为空则使用订单当前的total_amount
            if (originalTotalAmount == null) {
              originalTotalAmount = order.getTotalAmount();
            }
            
            // 判断是否是部分免单：有退款记录且退款总额小于订单总金额
            if (originalTotalAmount != null && 
                totalRefundAmount.compareTo(BigDecimal.ZERO) > 0 && 
                totalRefundAmount.compareTo(originalTotalAmount) < 0) {
              order.setIsPartialRefund(true);
              log.info("订单 {} 是部分免单，退款总额: {}, 订单总金额: {}", 
                      orderNo, totalRefundAmount, originalTotalAmount);
            }
          }
        }
      }
    }
    
    return result;
  }

  @Override
  public ActivityOrderDetail findActivityOrderByOrderNo(String orderNo) {
    ActivityOrderDetail activityOrderDetail = new ActivityOrderDetail();
    // 对应订单对象
    QueryWrapper<ActivityOrder> activityOrderQueryWrapper = new QueryWrapper<>();
    activityOrderQueryWrapper.eq("order_no", orderNo);
    ActivityOrder activityOrder = activityOrderMapper.selectOne(activityOrderQueryWrapper);
    activityOrderDetail.setActivityOrder(activityOrder);
    // 对应活动标题
    Activity activity = activityMapper.selectById(activityOrder.getActivityId());
    activityOrderDetail.setActivityTitle(activity.getTitle());
    // 对应出行人名称列表
    QueryWrapper<ActivityOrderTraveler> activityOrderTravelerQueryWrapper = new QueryWrapper<>();
    activityOrderTravelerQueryWrapper.eq("order_no", orderNo);
    List<ActivityOrderTraveler> activityOrderTravelerList = activityOrderTravelerMapper
        .selectList(activityOrderTravelerQueryWrapper);
    List<TravelerOrderVO> travelerList = new ArrayList<>();
    // 只要id和名字
    for (ActivityOrderTraveler activityOrderTraveler : activityOrderTravelerList) {
      Long travelerId = activityOrderTraveler.getTravelerId();
      Traveler traveler = travelerMapper.selectById(travelerId);
      TravelerOrderVO resultTraveler = new TravelerOrderVO();
      resultTraveler.setId(travelerId);
      resultTraveler.setName(traveler.getName());
      // 设置票类型
      resultTraveler.setTickType(activityOrderTraveler.getTickType());
      // 根据票类型设置单人价格
      if (activityOrderTraveler.getTickType() != null) {
        if (activityOrderTraveler.getTickType() == 1) {
          // 早鸟票
          resultTraveler.setPersonAmount(activity.getEarlyBirdPrice());
        } else {
          // 普通票
          resultTraveler.setPersonAmount(activity.getNormalPrice());
        }
      }
      // 设置退款状态
      resultTraveler.setRefundStatus(activityOrderTraveler.getRefundStatus());
      travelerList.add(resultTraveler);
    }
    activityOrderDetail.setTravelerNameList(travelerList);

    return activityOrderDetail;
  }

  @Override
  public ActivityOrderDetail findActivityOrderAndBusLocationByOrderNo(String orderNo) {
    ActivityOrderDetail activityOrderDetail = findActivityOrderByOrderNo(orderNo);
    // 找到对应的activityOrder并拿到它的busLocationId
    QueryWrapper<ActivityOrder> activityOrderQueryWrapper = new QueryWrapper<>();
    activityOrderQueryWrapper.eq("order_no", orderNo);
    ActivityOrder activityOrder = activityOrderMapper.selectOne(activityOrderQueryWrapper);

    // 获取活动信息，从活动的busLocations字段中解析上车点信息
    Activity activity = activityMapper.selectById(activityOrder.getActivityId());

    if (activity != null && activity.getBusLocations() != null && !activity.getBusLocations().isEmpty()) {
      try {
        // 将JSON字符串转换为上车点列表
        List<BusLocationVO> busLocationList = com.alibaba.fastjson.JSON.parseArray(activity.getBusLocations(),
            BusLocationVO.class);

        // 根据订单中的busLocationId找到对应的上车点
        if (busLocationList != null && !busLocationList.isEmpty()) {
          for (BusLocationVO busLocationVO : busLocationList) {
            if (busLocationVO.getId() != null && busLocationVO.getId().equals(activityOrder.getBusLocationId())) {
              // 找到匹配的上车点
              activityOrderDetail.setBusLocationVO(busLocationVO);
              break;
            }
          }
        }
      } catch (Exception e) {
        log.error("解析活动上车点信息失败", e);
      }
    }

    // 如果从JSON中没有找到匹配的上车点，尝试从bus_location表中查询（兼容旧数据）
    if (activityOrderDetail.getBusLocationVO() == null) {
      log.warn("从活动JSON中未找到上车点信息，尝试从bus_location表查询, orderNo: {}, busLocationId: {}",
          orderNo, activityOrder.getBusLocationId());
      BusLocation busLocation = busLocationMapper.selectById(activityOrder.getBusLocationId());
      if (busLocation != null) {
        BusLocationVO busLocationVO = convertToVO(busLocation);
        activityOrderDetail.setBusLocationVO(busLocationVO);
      }
    }

    activityOrderDetail.setWxPaymentActivityOrder(wxPaymentActivityOrderMapper.selectByOrderNo(orderNo));
    return activityOrderDetail;
  }

  // 实体到VO的转换方法
  private BusLocationVO convertToVO(BusLocation busLocation) {
    BusLocationVO vo = new BusLocationVO();
    vo.setId(busLocation.getId());
    vo.setBusLocation(busLocation.getBusLocation());

    Location location = locationMapper.selectById(busLocation.getLocationId());
    vo.setLocation(location);

    return vo;
  }
  
  @Override
  public String getGroupQrcodeByOrderNo(String orderNo) {
    if (orderNo == null || orderNo.isEmpty()) {
      return null;
    }
    
    try {
      // 1. 通过订单号查询ActivityOrder获取活动ID
      QueryWrapper<ActivityOrder> orderQueryWrapper = new QueryWrapper<>();
      orderQueryWrapper.eq("order_no", orderNo);
      ActivityOrder activityOrder = activityOrderMapper.selectOne(orderQueryWrapper);
      
      if (activityOrder == null) {
        log.warn("未找到订单号为 {} 的订单", orderNo);
        return null;
      }
      
      // 2. 通过活动ID查询Activity获取群二维码
      Long activityId = activityOrder.getActivityId();
      Activity activity = activityMapper.selectById(activityId);
      
      if (activity == null) {
        log.warn("未找到ID为 {} 的活动", activityId);
        return null;
      }
      
      // 3. 返回群二维码URL
      return activity.getGroupQrcode();
    } catch (Exception e) {
      log.error("获取活动群二维码时出错, 订单号: {}", orderNo, e);
      return null;
    }
  }

  /**
   * 通过订单号查询活动订单
   *
   * @param orderNo 订单号
   * @return ActivityOrder对象，如果未找到返回null
   */
  @Override
  public ActivityOrder getActivityOrderByOrderNo(String orderNo) {
    if (orderNo == null || orderNo.isEmpty()) {
      return null;
    }
    
    QueryWrapper<ActivityOrder> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("order_no", orderNo);
    return activityOrderMapper.selectOne(queryWrapper);
  }

}