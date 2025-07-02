package com.tour.controller;

import com.tour.common.Result;

import com.tour.common.constant.Constants;
import com.tour.common.util.UserContext;
import com.tour.dto.ActivityOrderDTO;
import com.tour.dto.EnrollmentDTO;
import com.tour.dto.PartialRefundRequestDTO;
import com.tour.dto.SpecialRefundRequestDTO;
import com.tour.dto.WxRefundRequestDTO;
import com.tour.dto.CustomRefundRequestDTO;
import com.tour.model.ActivityOrder;
import com.tour.model.OrderPaymentResult;
import com.tour.model.User;
import com.tour.model.WxPaymentResponseVO;
import com.tour.service.SignUpService;
import com.tour.service.impl.SignUpServiceImpl;
import io.swagger.annotations.Api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员使用的Activity接口
 *
 * @Author Kuril
 */
@Api(tags = "报名活动接口", description = "报名活动接口相关接口")
@RestController
@RequestMapping("/api/signup")
public class SignUpController {

    private final SignUpServiceImpl signUpService;

    public SignUpController(SignUpServiceImpl signUpService) {
        this.signUpService = signUpService;
    }
//    @ApiOperation(value = "报名", notes = "报名")
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
//            @ApiResponse(code = 401, message = "未登录或token已过期"),
//            @ApiResponse(code = 500, message = "服务器内部错误")
//    })
//    @PostMapping("/signup")
//    public Result signup(@RequestBody EnrollmentDTO enrollmentDTO)
//    {
//        signUpService.signup(enrollmentDTO);
//
//        return Result.success(null);
//    }


    @ApiOperation(value = "获取报名该活动的名单", notes = "获取报名该活动的名单")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/loadSignupListByActivityId/{id}")
    public Result loadSignupListByActivityId(@PathVariable Long id)
    {

        return Result.success(signUpService.loadSignupListByActivityId(id));
    }


    @ApiOperation(value = "锁单", notes = "新建活动订单,添加预留库存，减少可用库存，并生成微信支付预付单信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "锁单成功，返回订单信息和微信支付参数", response = Result.class),
            @ApiResponse(code = 400, message = "参数错误或库存不足"),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/lockOrder")
    public Result<OrderPaymentResult<ActivityOrder>> lockOrder(@RequestBody ActivityOrderDTO activityOrderDTO)
    {
        // 调用服务层方法创建订单并生成微信支付参数
        OrderPaymentResult<ActivityOrder> orderPaymentResult = signUpService.lockOrder(activityOrderDTO);
        
        // 返回包含订单信息和支付参数的组合结果
        // orderPaymentResult.getOrder() - 活动订单信息
        // orderPaymentResult.getPayment() - 微信支付参数(timeStamp, nonceStr, packageStr, signType, paySign等)
        return Result.success(orderPaymentResult);
    }

    @ApiOperation(value = "微信支付成功切换订单状态并添加报名信息", notes = "微信支付成功切换订单状态并添加报名信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/payOrder/{orderNo}")
    public Result payOrder(@PathVariable String orderNo,@RequestBody EnrollmentDTO enrollmentDTO)
    {
        String openid = UserContext.getOpenId();
        return Result.success(signUpService.payOrder(orderNo, openid,enrollmentDTO));
    }

    @ApiOperation(value = "取消未支付订单", notes = "取消未支付状态的订单，释放预留库存，无需退款流程")
    @ApiResponses({
            @ApiResponse(code = 200, message = "取消成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/cancelOrder/{orderNo}")
    public Result cancelOrder(@PathVariable String orderNo)
    {
        String openid = UserContext.getOpenId();
        // 调用service层方法，使用空的WxRefundRequestDTO对象确保兼容
        WxRefundRequestDTO requestDTO = new WxRefundRequestDTO();
        requestDTO.setOutTradeNo(orderNo);
        requestDTO.setReason("用户取消订单");
        return Result.success(signUpService.cancelOrder(orderNo, openid));
    }
    
//    @ApiOperation(value = "申请活动订单退款", notes = "取消已支付的订单，回滚库存并申请微信退款")
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "申请退款成功", response = Result.class),
//            @ApiResponse(code = 400, message = "订单状态不正确或已退款"),
//            @ApiResponse(code = 401, message = "未登录或token已过期"),
//            @ApiResponse(code = 500, message = "服务器内部错误")
//    })
//    @PostMapping("/refund")
//    public Result refundOrder(@RequestBody WxRefundRequestDTO refundRequestDTO)
//    {
//        String openid = UserContext.getOpenId();
//        String orderNo = refundRequestDTO.getOutTradeNo();
//        String reason = refundRequestDTO.getReason();
//
//        if (orderNo == null || orderNo.isEmpty()) {
//            return Result.error("订单号不能为空");
//        }
//
//        // 调用带退款原因的cancelOrder方法
//        boolean result = signUpService.cancelOrder(orderNo, openid, reason);
//        return Result.success(result);
//    }

    @ApiOperation(value = "用户申请活动订单退款", notes = "取消已支付的订单，回滚库存并申请微信退款")
    @ApiResponses({
            @ApiResponse(code = 200, message = "申请退款成功", response = Result.class),
            @ApiResponse(code = 400, message = "订单状态不正确或已退款"),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/user/refund")
    public Result userRefundOrder(@RequestBody WxRefundRequestDTO refundRequestDTO)
    {
        String openid = UserContext.getOpenId();
        String orderNo = refundRequestDTO.getOutTradeNo();
        String realReason = refundRequestDTO.getReason();
//        用户设置默认
        String reason =Constants.deafultUserRefundReason;

        if (orderNo == null || orderNo.isEmpty()) {
            return Result.error("订单号不能为空");
        }

        // 调用带退款原因的cancelOrder方法
        boolean result = signUpService.cancelOrder(orderNo, openid, reason,realReason);
        return Result.success(result);
    }

    // @ApiOperation(value = "部分退款", notes = "根据出行人列表进行部分退款，回滚对应库存和删除出行人关联信息")
    // @ApiResponses({
    //         @ApiResponse(code = 200, message = "申请退款成功", response = Result.class),
    //         @ApiResponse(code = 400, message = "订单状态不正确或参数错误"),
    //         @ApiResponse(code = 401, message = "未登录或token已过期"),
    //         @ApiResponse(code = 500, message = "服务器内部错误")
    // })
    // @PostMapping("/partial-refund")
    // public Result partialRefund(@RequestBody PartialRefundRequestDTO requestDTO)
    // {
    //     String openid = UserContext.getOpenId();
    //     String orderNo = requestDTO.getOutTradeNo();
    //     List<Long> travelerIds = requestDTO.getTravelerIds();
    //     String reason = requestDTO.getReason() != null ? requestDTO.getReason() : Constants.deafultUserRefundReason;

    //     if (orderNo == null || orderNo.isEmpty()) {
    //         return Result.error("订单号不能为空");
    //     }
        
    //     if (travelerIds == null || travelerIds.isEmpty()) {
    //         return Result.error("出行人ID列表不能为空");
    //     }

    //     try {
    //         // 调用部分退款方法
    //         boolean result = signUpService.partialRefund(orderNo, openid, travelerIds, reason);
    //         return Result.success(result);
    //     } catch (Exception e) {
    //         return Result.error(e.getMessage());
    //     }
    // }
    
    // @ApiOperation(value = "特殊退款", notes = "进行退款但保留订单和报名记录，适用于免单操作")
    // @ApiResponses({
    //         @ApiResponse(code = 200, message = "申请退款成功", response = Result.class),
    //         @ApiResponse(code = 400, message = "订单状态不正确或参数错误"),
    //         @ApiResponse(code = 401, message = "未登录或token已过期"),
    //         @ApiResponse(code = 500, message = "服务器内部错误")
    // })
    // @PostMapping("/special-refund")
    // public Result specialRefund(@RequestBody SpecialRefundRequestDTO requestDTO)
    // {
    //     String openid = UserContext.getOpenId();
    //     String orderNo = requestDTO.getOutTradeNo();
    //     String reason = requestDTO.getReason() != null ? requestDTO.getReason() : "特殊退款";
    //     String adminRemark = requestDTO.getAdminRemark();
    //     List<Long> travelerIds = requestDTO.getTravelerIds();

    //     if (orderNo == null || orderNo.isEmpty()) {
    //         return Result.error("订单号不能为空");
    //     }
        
    //     if (travelerIds == null || travelerIds.isEmpty()) {
    //         return Result.error("出行人ID列表不能为空");
    //     }

    //     try {
    //         // 调用特殊退款方法，增加一个参数表示不删除活动订单出行人关联记录，而是设置refundStatus为1（不可免单）
    //         boolean result = signUpService.specialRefund(orderNo, openid, reason, adminRemark, travelerIds, true);
    //         return Result.success(result);
    //     } catch (Exception e) {
    //         return Result.error(e.getMessage());
    //     }
    // }

    @ApiOperation(value = "获取报名订单信息", notes = "获取报名订单信息,用于微信支付调用前或者提交前校验状态是否正常")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/getOrder/{orderNo}")
    public Result getOrder(@PathVariable String orderNo)
    {
        String openid = UserContext.getOpenId();
        return Result.success(signUpService.getOrder(orderNo,openid));
    }

//    @ApiOperation(value = "指定金额部分退款", notes = "根据指定的金额进行部分退款，可选择性退指定出行人")
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "申请退款成功", response = Result.class),
//            @ApiResponse(code = 400, message = "订单状态不正确或参数错误"),
//            @ApiResponse(code = 401, message = "未登录或token已过期"),
//            @ApiResponse(code = 500, message = "服务器内部错误")
//    })
//    @PostMapping("/custom-refund")
//    public Result customRefund(@RequestBody CustomRefundRequestDTO requestDTO)
//    {
//        String openid = UserContext.getOpenId();
//        String orderNo = requestDTO.getOutTradeNo();
//        List<Long> travelerIds = requestDTO.getTravelerIds();
//        String reason = requestDTO.getReason() != null ? requestDTO.getReason() : Constants.deafultUserRefundReason;
//
//        if (orderNo == null || orderNo.isEmpty()) {
//            return Result.error("订单号不能为空");
//        }
//
//        if (requestDTO.getRefundAmount() == null) {
//            return Result.error("退款金额不能为空");
//        }
//
//        try {
//            // 调用自定义金额退款方法
//            boolean result = signUpService.customRefund(orderNo, travelerIds, reason, requestDTO.getRefundAmount());
//            return Result.success(result);
//        } catch (Exception e) {
//            return Result.error(e.getMessage());
//        }
//    }
    
    @ApiOperation(value = "根据出行人ID删除指定出行人", notes = "根据出行人ID列表删除指定出行人")
    @ApiResponses({
            @ApiResponse(code = 200, message = "操作成功", response = Result.class),
            @ApiResponse(code = 400, message = "订单状态不正确或参数错误"),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/delete-travelers")
    public Result deleteTravelers(@RequestBody CustomRefundRequestDTO requestDTO)
    {
        String openid = UserContext.getOpenId();
        String orderNo = requestDTO.getOutTradeNo();
        List<Long> travelerIds = requestDTO.getTravelerIds();
        String reason = requestDTO.getReason() != null ? requestDTO.getReason() : Constants.deafultUserRefundReason;
        
        if (orderNo == null || orderNo.isEmpty()) {
            return Result.error("订单号不能为空");
        }
        
        if (travelerIds == null || travelerIds.isEmpty()) {
            return Result.error("出行人ID列表不能为空");
        }

        try {
            // 调用删除出行人方法
            boolean result = signUpService.deleteTravelers(orderNo, travelerIds, reason);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @ApiOperation(value = "指定金额退款", notes = "根据指定的金额进行退款")
    @ApiResponses({
            @ApiResponse(code = 200, message = "申请退款成功", response = Result.class),
            @ApiResponse(code = 400, message = "订单状态不正确或参数错误"),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/amount-refund")
    public Result amountRefund(@RequestBody CustomRefundRequestDTO requestDTO)
    {
        String openid = UserContext.getOpenId();
        String orderNo = requestDTO.getOutTradeNo();
        String reason = requestDTO.getReason() != null ? requestDTO.getReason() : Constants.deafultUserRefundReason;
        Integer orderStatus = requestDTO.getOrderStatus(); // 获取订单状态参数
        
        if (orderNo == null || orderNo.isEmpty()) {
            return Result.error("订单号不能为空");
        }
        
        if (requestDTO.getRefundAmount() == null) {
            return Result.error("退款金额不能为空");
        }

        try {
            // 调用指定金额退款方法，传入订单状态参数
            boolean result = signUpService.amountRefund(orderNo, reason, requestDTO.getRefundAmount(), orderStatus);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}