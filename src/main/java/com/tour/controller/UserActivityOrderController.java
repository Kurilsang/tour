package com.tour.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.common.Result;
import com.tour.common.util.UserContext;
import com.tour.dao.WxRefundRecordMapper;
import com.tour.model.ActivityOrder;
import com.tour.model.WxRefundRecord;
import com.tour.query.ActivityOrderQuery;
import com.tour.service.impl.ActivityOrderServiceImpl;
import com.tour.service.impl.ActivityServiceImpl;
import com.tour.vo.ActivityOrderDetail;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.tour.common.exception.ServiceException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 活动订单用户接口
 *
 * @Author Kuril
 */
@Api(tags = "活动订单用户接口", description = "活动订单用户接口")
@RestController
@RequestMapping("/api/activityOrder")
@Slf4j
public class UserActivityOrderController {


    private final ActivityOrderServiceImpl activityOrderService;
    private final ActivityServiceImpl activityService;
    private final WxRefundRecordMapper wxRefundRecordMapper;

    public UserActivityOrderController(ActivityOrderServiceImpl activityOrderService, ActivityServiceImpl activityService,
                                       WxRefundRecordMapper wxRefundRecordMapper) {
        this.activityOrderService = activityOrderService;
        this.activityService = activityService;
        this.wxRefundRecordMapper = wxRefundRecordMapper;
    }

    /**
     * 加载该用户下所有活动订单
     *
     * @return 返回该用户下所有活动订单
     */
    @Operation(summary = "根据条件加载所有活动订单", description = "根据条件获取所有活动订单")
    @ApiResponses({
            @ApiResponse(code = 200, message = "加载成功", response = Result.class),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/loadDataList")
    public Result loadDataList(@RequestBody ActivityOrderQuery activityOrderQuery) {
//        从上下文获取openid
        String openid = UserContext.getOpenId();

        activityOrderQuery.setOpenid(openid);
        log.info("用户活动loadDataList,参数{}", activityOrderQuery);
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
            
            log.info("已设置订单退款记录标志，有退款记录的订单数量: {}", orderNosWithRefund.size());
        }
        
        return Result.success(activityOrdersPage);
    }

    /**
     * 通过订单号查找订单详细信息（目前为出行人列表，活动标题，总价等）
     *
     * @return 返回订单号对应的出行人列表，活动标题和订单详细信息
     */
    @Operation(summary = "通过订单号查找订单详细信息",
            description = "通过订单号查找订单详细信息（目前为出行人列表，活动标题，总价等）")
    @ApiResponses({
            @ApiResponse(code = 200, message = "加载成功", response = Result.class),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/findActivityOrderByOrderNo/{orderNo}")
    public Result findActivityOrderByOrderNo(@PathVariable String orderNo) {
        // 获取当前登录用户的openid
        String currentUserOpenid = UserContext.getOpenId();
        
        // 先查询订单信息，检查是否属于当前用户
        ActivityOrder order = activityOrderService.getActivityOrderByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 验证订单所有者
        if (!order.getOpenid().equals(currentUserOpenid)) {
            throw new ServiceException("无权限访问此订单");
        }

        // 获取订单详细信息
        ActivityOrderDetail activityOrderDetail = activityOrderService.findActivityOrderAndBusLocationByOrderNo(orderNo);
        
        // 查询订单在wx_refund_record表中的原始金额
        QueryWrapper<WxRefundRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", orderNo);
        List<WxRefundRecord> refundRecords = wxRefundRecordMapper.selectList(queryWrapper);
        
        // 如果有退款记录，取第一条的total_amount作为原始总金额
        if (refundRecords != null && !refundRecords.isEmpty()) {
            WxRefundRecord firstRecord = refundRecords.get(0);
            activityOrderDetail.setOriginalTotalAmount(firstRecord.getTotalAmount());
            log.info("订单 {} 的原始总金额: {}", orderNo, firstRecord.getTotalAmount());
        } else {
            // 如果没有退款记录，使用当前订单金额作为原始总金额
            activityOrderDetail.setOriginalTotalAmount(order.getTotalAmount());
            log.info("订单 {} 没有退款记录，使用当前订单金额: {}", orderNo, order.getTotalAmount());
        }

        return Result.success(activityOrderDetail);
    }

    /**
     * 通过订单号获取活动的群二维码URL
     *
     * @param orderNo 订单号
     * @return 活动群二维码URL
     */
    @Operation(summary = "通过订单号获取活动群二维码",
            description = "根据活动订单号查询对应活动的群二维码URL")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 404, message = "未找到相关数据"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/getGroupQrcodeByOrderNo/{orderNo}")
    public Result getGroupQrcodeByOrderNo(@PathVariable @Parameter(description = "订单号", required = true) String orderNo) {
        // 获取当前登录用户的openid
        String currentUserOpenid = UserContext.getOpenId();
        
        // 先查询订单信息，检查是否属于当前用户
        ActivityOrder order = activityOrderService.getActivityOrderByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 验证订单所有者
        if (!order.getOpenid().equals(currentUserOpenid)) {
            throw new ServiceException("无权限访问此订单");
        }
        
        String groupQrcode = activityOrderService.getGroupQrcodeByOrderNo(orderNo);
        if (groupQrcode == null) {
            return Result.error("未找到订单对应的活动群二维码");
        }
        return Result.success(groupQrcode);
    }

}