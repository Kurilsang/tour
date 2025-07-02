package com.tour.controller;

import com.tour.common.Result;
import com.tour.common.exception.ServiceException;
import com.tour.service.WxPaymentService;
import com.tour.vo.WxPaymentOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author kuril
 * @Description 微信支付控制器
 */
@Api(tags = "微信支付接口", description = "微信支付相关接口")
@RestController
@RequestMapping("/api/wx/payment")
@Slf4j
public class WxPaymentController {

    private final WxPaymentService wxPaymentService;

    @Autowired
    public WxPaymentController(WxPaymentService wxPaymentService) {
        this.wxPaymentService = wxPaymentService;
    }

    /**
     * 通过商户订单号查询订单
     *
     * @param outTradeNo 商户订单号
     * @return 订单查询结果
     */
    @Operation(summary = "通过商户订单号查询订单", description = "根据商户订单号查询微信支付订单信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功", response = Result.class),
            @ApiResponse(code = 400, message = "商户订单号不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/order/query/out-trade-no/{outTradeNo}")
    public Result<WxPaymentOrderVO> queryOrderByOutTradeNo(@PathVariable @Parameter(description = "商户订单号", required = true) String outTradeNo) {
        log.info("通过商户订单号查询订单: {}", outTradeNo);
        
        try {
            WxPaymentOrderVO result = wxPaymentService.queryOrderByOutTradeNo(outTradeNo);
            return Result.success(result);
        } catch (ServiceException e) {
            log.error("查询订单业务异常: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询订单系统异常: {}", e.getMessage());
            return Result.error("查询订单失败，系统异常");
        }
    }

    /**
     * 通过预支付交易会话标识查询订单
     *
     * @param prepayId 预支付交易会话标识
     * @return 订单查询结果
     */
    @Operation(summary = "通过预支付交易会话标识查询订单", description = "根据预支付交易会话标识查询微信支付订单信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功", response = Result.class),
            @ApiResponse(code = 400, message = "预支付交易会话标识不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/order/query/prepay-id/{prepayId}")
    public Result<WxPaymentOrderVO> queryOrderByPrepayId(@PathVariable @Parameter(description = "预支付交易会话标识", required = true) String prepayId) {
        log.info("通过预支付交易会话标识查询订单: {}", prepayId);
        
        try {
            WxPaymentOrderVO result = wxPaymentService.queryOrderByPrepayId(prepayId);
            return Result.success(result);
        } catch (ServiceException e) {
            log.error("查询订单业务异常: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询订单系统异常: {}", e.getMessage());
            return Result.error("查询订单失败，系统异常");
        }
    }

    /**
     * 关闭订单
     *
     * @param outTradeNo 商户订单号
     * @return 关闭结果
     */
    @Operation(summary = "关闭订单", description = "根据商户订单号关闭微信支付订单")
    @ApiResponses({
            @ApiResponse(code = 200, message = "关闭成功", response = Result.class),
            @ApiResponse(code = 400, message = "商户订单号不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/order/close/{outTradeNo}")
    public Result closeOrder(@PathVariable @Parameter(description = "商户订单号", required = true) String outTradeNo) {
        log.info("关闭订单: {}", outTradeNo);
        
        try {
            boolean result = wxPaymentService.closeOrder(outTradeNo);
            if (result) {
                return Result.success(null);
            } else {
                return Result.error("订单关闭失败");
            }
        } catch (ServiceException e) {
            log.error("关闭订单业务异常: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("关闭订单系统异常: {}", e.getMessage());
            return Result.error("关闭订单失败，系统异常");
        }
    }
} 