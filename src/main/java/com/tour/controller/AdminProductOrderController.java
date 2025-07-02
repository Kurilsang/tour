package com.tour.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.common.Result;
import com.tour.dao.WxRefundRecordMapper;
import com.tour.model.ProductOrder;
import com.tour.model.WxRefundRecord;
import com.tour.query.ProductOrderQuery;
import com.tour.service.ProductOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员商品订单接口
 *
 * @Author Kuril
 * @DateTime 2025/5/18 10:00
 */
@Api(tags = "管理员商品订单接口", description = "管理员商品订单相关接口")
@RestController
@RequestMapping("/api/admin/product/order")
@Slf4j
public class AdminProductOrderController {

    private final ProductOrderService productOrderService;
    private final WxRefundRecordMapper wxRefundRecordMapper;

    public AdminProductOrderController(ProductOrderService productOrderService, WxRefundRecordMapper wxRefundRecordMapper) {
        this.productOrderService = productOrderService;
        this.wxRefundRecordMapper = wxRefundRecordMapper;
    }

    /**
     * 加载商品订单列表
     *
     * @param productOrderQuery 查询条件
     * @return 分页订单列表
     */
    @ApiOperation(value = "加载商品订单列表", notes = "管理员根据条件加载商品订单列表")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/loadDataList")
    public Result loadDataList(@RequestBody ProductOrderQuery productOrderQuery) {
        IPage<ProductOrder> ordersPage = productOrderService.loadDataList(productOrderQuery);
        
        // 如果有结果，查询每个订单是否有对应的退款记录
        if (ordersPage != null && ordersPage.getRecords() != null && !ordersPage.getRecords().isEmpty()) {
            // 收集所有订单号
            Set<String> orderNos = ordersPage.getRecords().stream()
                    .map(ProductOrder::getOrderNo)
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
            for (ProductOrder order : ordersPage.getRecords()) {
                order.setHasRefundRecord(orderNosWithRefund.contains(order.getOrderNo()));
            }
            
            log.info("已设置管理员商品订单退款记录标志，有退款记录的订单数量: {}", orderNosWithRefund.size());
        }
        
        return Result.success(ordersPage);
    }

    /**
     * 更新订单状态
     *
     * @param orderNo 订单号
     * @param status 目标状态（使用ProductOrderStatusEnum中的状态码）
     * @return 更新结果
     */
    @ApiOperation(value = "更新订单状态", notes = "管理员更新商品订单状态，如将待提货(1)改为已完成(2)等")
    @ApiResponses({
            @ApiResponse(code = 200, message = "更新成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PutMapping("/updateStatus/{orderNo}/{status}")
    public Result updateOrderStatus(@PathVariable String orderNo, @PathVariable Integer status) {
        boolean result = productOrderService.updateOrderStatus(orderNo, status);
        return Result.success(result);
    }

    /**
     * 获取订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @ApiOperation(value = "获取订单详情", notes = "管理员获取商品订单详细信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/getOrder/{orderNo}")
    public Result getOrderDetail(@PathVariable String orderNo) {
        return Result.success(productOrderService.getOrderDetail(orderNo, null));
    }
} 