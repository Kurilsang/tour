package com.tour.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.common.Result;
import com.tour.common.constant.Constants;
import com.tour.common.enums.RefundStatusEnum;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.UserContext;
import com.tour.dao.WxRefundRecordMapper;
import com.tour.dto.ProductOrderDTO;
import com.tour.dto.ProductRefundDTO;
import com.tour.model.OrderPaymentResult;
import com.tour.model.ProductOrder;
import com.tour.model.WxRefundRecord;
import com.tour.query.ProductOrderQuery;
import com.tour.service.ProductOrderService;
import com.tour.vo.ProductOrderDetailVO;
import com.tour.vo.ProductOrderVO;
import com.tour.vo.WxRefundResponseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品订单接口
 *
 * @Author Kuril
 * @DateTime 2025/5/17 16:00
 */
@Api(tags = "商品订单接口", description = "商品订单相关接口")
@RestController
@RequestMapping("/api/product/order")
@Slf4j
public class ProductOrderController {

    private final ProductOrderService productOrderService;
    private final WxRefundRecordMapper wxRefundRecordMapper;

    public ProductOrderController(ProductOrderService productOrderService, WxRefundRecordMapper wxRefundRecordMapper) {
        this.productOrderService = productOrderService;
        this.wxRefundRecordMapper = wxRefundRecordMapper;
    }

    /**
     * 加载商品订单列表
     *
     * @param productOrderQuery 查询条件
     * @return 分页订单列表
     */
    @ApiOperation(value = "加载商品订单列表", notes = "根据条件加载商品订单列表")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/loadDataList")
    public Result loadDataList(@RequestBody ProductOrderQuery productOrderQuery) {
        String openid = UserContext.getOpenId();
        productOrderQuery.setOpenid(openid);
        log.info("用户商品订单loadDataList,参数:{}", productOrderQuery);
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
            
            log.info("已设置商品订单退款记录标志，有退款记录的订单数量: {}", orderNosWithRefund.size());
        }
        
        return Result.success(ordersPage);
    }

    /**
     * 锁单（创建订单）
     *
     * @param productOrderDTO 订单信息
     * @return 创建的订单
     */
    @ApiOperation(value = "锁单", notes = "创建商品订单，预留库存")
    @ApiResponses({
            @ApiResponse(code = 200, message = "创建成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/lockOrder")
    public Result lockOrder(@RequestBody ProductOrderDTO productOrderDTO) {
        OrderPaymentResult<ProductOrder> orderOrderPaymentResult = productOrderService.lockOrder(productOrderDTO);
        return Result.success(orderOrderPaymentResult);
    }

    /**
     * 支付订单
     *
     * @param orderNo 订单号
     * @return 支付结果
     */
    @ApiOperation(value = "支付订单", notes = "支付商品订单，更新库存状态")
    @ApiResponses({
            @ApiResponse(code = 200, message = "支付成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/payOrder/{orderNo}")
    public Result payOrder(@PathVariable String orderNo) {
        String openid = UserContext.getOpenId();
        boolean result = productOrderService.payOrder(orderNo, openid);
        return Result.success(result);
    }

    /**
     * 取消订单
     *
     * @param orderNo 订单号
     * @return 取消结果
     */
    @ApiOperation(value = "取消订单", notes = "取消商品订单，释放预留库存")
    @ApiResponses({
            @ApiResponse(code = 200, message = "取消成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/cancelOrder/{orderNo}")
    public Result cancelOrder(@PathVariable String orderNo) {
        String openid = UserContext.getOpenId();
        boolean result = productOrderService.cancelOrder(orderNo, openid);
        return Result.success(result);
    }

    /**
     * 获取订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @ApiOperation(value = "获取订单详情", notes = "获取商品订单详细信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/getOrder/{orderNo}")
    public Result getOrderDetail(@PathVariable String orderNo) {
        String openid = UserContext.getOpenId();
        
        // 先查询订单基础信息，验证权限
        ProductOrder order = productOrderService.getOrderByOrderNo(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 验证订单所有者
        if (!order.getOpenid().equals(openid)) {
            throw new ServiceException("无权限访问此订单");
        }
        
        // 权限验证通过，获取详细信息
        ProductOrderVO baseVO = productOrderService.getOrderDetail(orderNo, openid);
        
        // 创建扩展VO对象，并设置退款状态
        ProductOrderDetailVO detailVO = new ProductOrderDetailVO();
        BeanUtils.copyProperties(baseVO, detailVO);
        
        // 设置退款状态
        detailVO.setRefundStatus(order.getRefundStatus());
        
        // 设置退款状态描述
        if (order.getRefundStatus() != null) {
            RefundStatusEnum refundStatus = RefundStatusEnum.getByCode(order.getRefundStatus());
            if (refundStatus != null) {
                detailVO.setRefundStatusDesc(refundStatus.getDesc());
            }
        }
        
        return Result.success(detailVO);
    }
    
    /**
     * 更新订单状态
     *
     * @param orderNo 订单号
     * @param status 目标状态（使用ProductOrderStatusEnum中的状态码）
     * @return 更新结果
     */
    @ApiOperation(value = "更新订单状态", notes = "更新商品订单状态，如将待提货(1)改为已完成(2)等")
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
     * 申请退款
     * 
     * @param productRefundDTO 退款请求参数
     * @return 退款结果
     */
    @ApiOperation(value = "申请退款", notes = "申请商品订单退款，回滚库存，更新退款状态")
    @ApiResponses({
            @ApiResponse(code = 200, message = "申请成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/refund")
    public Result<WxRefundResponseVO> refundOrder(@RequestBody ProductRefundDTO productRefundDTO) {
        String openid = UserContext.getOpenId();
        WxRefundResponseVO responseVO = productOrderService.refundOrder(productRefundDTO, openid);
        return Result.success(responseVO);
    }


    /**
     * 申请退款
     *
     * @param productRefundDTO 退款请求参数
     * @return 退款结果
     */
    @ApiOperation(value = "用户申请退款", notes = "申请商品订单退款，回滚库存，更新退款状态")
    @ApiResponses({
            @ApiResponse(code = 200, message = "申请成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/user/refund")
    public Result<WxRefundResponseVO> userRefundOrder(@RequestBody ProductRefundDTO productRefundDTO) {
        String openid = UserContext.getOpenId();
        String reason = productRefundDTO.getReason();
        productRefundDTO.setReason(Constants.deafultUserRefundReason);
        WxRefundResponseVO responseVO = productOrderService.refundOrder(productRefundDTO, openid,reason);
        return Result.success(responseVO);
    }
} 