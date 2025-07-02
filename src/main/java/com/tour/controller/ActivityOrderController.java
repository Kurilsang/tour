package com.tour.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.common.Result;
import com.tour.dao.WxRefundRecordMapper;
import com.tour.dto.ActivityDTO;
import com.tour.model.ActivityOrder;
import com.tour.model.WxRefundRecord;
import com.tour.query.ActivityOrderQuery;
import com.tour.query.ActivityQuery;
import com.tour.service.ActivityService;
import com.tour.service.impl.ActivityOrderServiceImpl;
import com.tour.service.impl.ActivityServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 活动订单管理员接口
 *
 * @Author Kuril
 */
@Api(tags = "活动订单管理员接口", description = "活动订单管理员接口")
@RestController
@RequestMapping("/api/admin/activityOrder")
@Slf4j
public class ActivityOrderController {

    private final ActivityOrderServiceImpl activityOrderService;
    private final WxRefundRecordMapper wxRefundRecordMapper;

    public ActivityOrderController(ActivityOrderServiceImpl activityOrderService, WxRefundRecordMapper wxRefundRecordMapper) {
        this.activityOrderService = activityOrderService;
        this.wxRefundRecordMapper = wxRefundRecordMapper;
    }

    /**
     * 加载所有活动订单
     *
     * @return 返回所有活动订单
     */
    @Operation(summary = "根据条件加载所有活动订单", description = "根据条件获取所有活动订单")
    @ApiResponses({
            @ApiResponse(code = 200, message = "加载成功", response = Result.class),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/loadDataList")
    public Result loadDataList(@RequestBody ActivityOrderQuery activityOrderQuery) {

        IPage<ActivityOrder> activityOrdersPage = activityOrderService.loadDataList(activityOrderQuery);
        
        // 如果有结果，查询每个订单是否有对应的退款记录
        if (activityOrdersPage != null && activityOrdersPage.getRecords() != null && !activityOrdersPage.getRecords().isEmpty()) {
            // 收集所有订单号
            Set<String> orderNos = activityOrdersPage.getRecords().stream()
                    .map(ActivityOrder::getOrderNo)
                    .collect(Collectors.toSet());
            
            // 查询这些订单号对应的退款记录
            QueryWrapper<WxRefundRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("out_trade_no", orderNos);
            List<WxRefundRecord> refundRecords = wxRefundRecordMapper.selectList(queryWrapper);
            
            // 将有退款记录的订单号存入集合
            Set<String> orderNosWithRefund = refundRecords.stream()
                    .map(WxRefundRecord::getOutTradeNo)
                    .collect(Collectors.toSet());
            
            // 设置每个订单的hasRefundRecord属性
            for (ActivityOrder order : activityOrdersPage.getRecords()) {
                order.setHasRefundRecord(orderNosWithRefund.contains(order.getOrderNo()));
            }
            
            log.info("已设置管理员活动订单退款记录标志，有退款记录的订单数量: {}", orderNosWithRefund.size());
        }
        
        return Result.success(activityOrdersPage);
    }


}