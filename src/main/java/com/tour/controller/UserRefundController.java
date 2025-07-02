package com.tour.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.common.Result;
import com.tour.common.util.UserContext;
import com.tour.dao.WxRefundRecordMapper;
import com.tour.dto.PartialRefundApplyDTO;
import com.tour.dto.RefundApplyDTO;
import com.tour.enums.OrderType;
import com.tour.enums.RefundStatus;
import com.tour.model.RefundApply;
import com.tour.model.WxRefundRecord;
import com.tour.query.RefundApplyQuery;
import com.tour.service.RefundApplyService;
import com.tour.vo.RefundApplyVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户退款接口
 *
 * @Author Kuril
 */
@Api(tags = "用户退款接口", description = "用户申请退款和查询退款信息接口")
@RestController
@RequestMapping("/api/user/refund")
@Slf4j
public class UserRefundController {

    private final RefundApplyService refundApplyService;
    private final WxRefundRecordMapper wxRefundRecordMapper;

    public UserRefundController(RefundApplyService refundApplyService, WxRefundRecordMapper wxRefundRecordMapper) {
        this.refundApplyService = refundApplyService;
        this.wxRefundRecordMapper = wxRefundRecordMapper;
    }

    /**
     * 申请退款
     *
     * @param refundApplyDTO 退款申请DTO
     * @return 处理结果
     */
    @ApiOperation(value = "申请退款", notes = "用户提交退款申请")
    @ApiResponses({
            @ApiResponse(code = 200, message = "申请成功"),
            @ApiResponse(code = 400, message = "参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/apply")
    public Result apply(@Validated @RequestBody RefundApplyDTO refundApplyDTO) {
        log.info("收到退款申请请求：{}", refundApplyDTO);
        
        // 从上下文获取openid
        String openid = UserContext.getOpenId();
        if (openid == null || openid.isEmpty()) {
            return Result.error("用户未登录");
        }
        
        // 检查订单是否已有退款请求
        try {
            refundApplyService.checkRefundStatus(refundApplyDTO.getOutTradeOrder());
        } catch (Exception e) {
            log.error("订单退款状态检查失败", e);
            return Result.error(e.getMessage());
        }
        
        // 如果是活动订单，检查以下条件
        if (OrderType.ACTIVITY.getCode() == refundApplyDTO.getOrderType()) {
            try {
                // 检查订单是否为已免单状态
                refundApplyService.checkOrderFeeExemption(refundApplyDTO.getOutTradeOrder(), openid);
                
                // 检查是否有可退款出行人
                refundApplyService.checkAvailableTravelers(refundApplyDTO.getOutTradeOrder(), openid);
                
                // 检查活动订单是否在可退款时间范围内
                refundApplyService.checkActivityRefundTime(refundApplyDTO.getOutTradeOrder(), openid);
            } catch (Exception e) {
                log.error("活动订单退款检查失败", e);
                return Result.error(e.getMessage());
            }
        }
        
        // 创建退款申请
        Long id = refundApplyService.createRefundApply(refundApplyDTO, openid);
        return Result.success(id);
    }
    
    /**
     * 申请部分出行人退款
     *
     * @param partialRefundApplyDTO 部分退款申请DTO
     * @return 处理结果
     */
    @ApiOperation(value = "申请部分出行人退款", notes = "用户提交部分出行人退款申请，只适用于活动订单")
    @ApiResponses({
            @ApiResponse(code = 200, message = "申请成功"),
            @ApiResponse(code = 400, message = "参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/applyPartial")
    public Result applyPartial(@Validated @RequestBody PartialRefundApplyDTO partialRefundApplyDTO) {
        log.info("收到部分出行人退款申请请求：{}", partialRefundApplyDTO);
        
        // 从上下文获取openid
        String openid = UserContext.getOpenId();
        if (openid == null || openid.isEmpty()) {
            return Result.error("用户未登录");
        }
        
        // 检查订单是否已有退款请求
        try {
            refundApplyService.checkRefundStatus(partialRefundApplyDTO.getOutTradeOrder());
        } catch (Exception e) {
            log.error("订单退款状态检查失败", e);
            return Result.error(e.getMessage());
        }
        
        try {
            // 检查订单是否为已免单状态
            refundApplyService.checkOrderFeeExemption(partialRefundApplyDTO.getOutTradeOrder(), openid);
            
            // 检查是否有可退款出行人
            refundApplyService.checkAvailableTravelers(partialRefundApplyDTO.getOutTradeOrder(), openid);
            
            // 检查活动订单是否在可退款时间范围内
            refundApplyService.checkActivityRefundTime(partialRefundApplyDTO.getOutTradeOrder(), openid);
        } catch (Exception e) {
            log.error("活动订单退款检查失败", e);
            return Result.error(e.getMessage());
        }
        
        // 检查出行人是否已申请退款
        try {
            refundApplyService.checkTravelerRefundStatus(
                    partialRefundApplyDTO.getTravelerIds(), 
                    partialRefundApplyDTO.getOutTradeOrder());
        } catch (Exception e) {
            log.error("出行人退款状态检查失败", e);
            return Result.error(e.getMessage());
        }
        
        // 创建部分退款申请
        Long id = refundApplyService.createPartialRefundApply(partialRefundApplyDTO, openid);
        return Result.success(id);
    }

    /**
     * 查询退款信息
     *
     * @param outTradeOrder 订单号
     * @return 退款申请信息
     */
    @ApiOperation(value = "查询退款信息", notes = "根据订单号查询退款申请信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功"),
            @ApiResponse(code = 404, message = "退款申请不存在"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/query/{outTradeOrder}")
    public Result query(@ApiParam(value = "订单号", required = true)
                        @PathVariable String outTradeOrder) {
        log.info("收到查询退款信息请求，订单号：{}", outTradeOrder);
        
        // 调用服务获取退款申请信息
        RefundApplyVO vo = refundApplyService.queryRefundApply(outTradeOrder);
        
        // 查询订单在wx_refund_record表中的原始金额和退款记录
        QueryWrapper<WxRefundRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", outTradeOrder);
        List<WxRefundRecord> refundRecords = wxRefundRecordMapper.selectList(queryWrapper);
        
        // 如果有退款记录，处理原始金额和已退款金额
        if (refundRecords != null && !refundRecords.isEmpty()) {
            // 获取第一条记录的total_amount作为原始总金额
            WxRefundRecord firstRecord = refundRecords.get(0);
            vo.setTotalAmount(firstRecord.getTotalAmount());
            log.info("订单 {} 的原始总金额: {}", outTradeOrder, firstRecord.getTotalAmount());
            
            // 计算所有记录的退款总额
            BigDecimal totalRefundAmount = BigDecimal.ZERO;
            for (WxRefundRecord record : refundRecords) {
                if (record.getRefundAmount() != null) {
                    totalRefundAmount = totalRefundAmount.add(record.getRefundAmount());
                }
            }
            
            // 更新退款金额
            vo.setRefundAmount(totalRefundAmount);
            log.info("订单 {} 的累计退款金额: {}", outTradeOrder, totalRefundAmount);
        }
//        没有退款记录
        else{
            vo.setRefundAmount(BigDecimal.ZERO);
        }
        
        return Result.success(vo);
    }
    
    /**
     * 加载用户的退款申请列表
     *
     * @param refundApplyQuery 查询条件
     * @return 退款申请列表
     */
    @ApiOperation(value = "加载退款申请列表", notes = "根据条件查询当前用户的退款申请列表")
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/loadDataList")
    public Result loadDataList(@RequestBody RefundApplyQuery refundApplyQuery) {
        // 从上下文获取openid
        String openid = UserContext.getOpenId();
        if (openid == null || openid.isEmpty()) {
            return Result.error("用户未登录");
        }
        
        // 设置查询用户的openid
        refundApplyQuery.setOpenid(openid);
        log.info("用户退款申请loadDataList，参数：{}", refundApplyQuery);
        
        // 查询退款申请列表
        IPage<RefundApplyVO> refundApplies = refundApplyService.loadDataList(refundApplyQuery);
        return Result.success(refundApplies);
    }
    
    /**
     * 取消退款申请
     *
     * @param id 退款申请ID
     * @return 处理结果
     */
    @ApiOperation(value = "取消退款申请", notes = "用户取消已提交的退款申请，仅待审核状态可取消")
    @ApiResponses({
            @ApiResponse(code = 200, message = "取消成功"),
            @ApiResponse(code = 400, message = "参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/cancel/{id}")
    public Result cancel(@ApiParam(value = "退款申请ID", required = true)
                         @PathVariable Long id) {
        log.info("收到取消退款申请请求，退款申请ID：{}", id);
        
        // 从上下文获取openid
        String openid = UserContext.getOpenId();
        if (openid == null || openid.isEmpty()) {
            return Result.error("用户未登录");
        }
        
        try {
            boolean result = refundApplyService.cancelRefundApply(id, openid);
            if (result) {
                return Result.success("取消退款申请成功");
            } else {
                return Result.error("取消退款申请失败");
            }
        } catch (Exception e) {
            log.error("取消退款申请失败", e);
            return Result.error(e.getMessage());
        }
    }
} 