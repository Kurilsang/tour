package com.tour.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tour.common.Result;
import com.tour.common.exception.ServiceException;
import com.tour.dao.WxRefundRecordMapper;
import com.tour.dto.WxRefundRequestDTO;
import com.tour.model.WxRefundRecord;
import com.tour.service.WxRefundService;
import com.tour.vo.WxRefundQueryResponseVO;
import com.tour.vo.WxRefundResponseVO;
import com.wechat.pay.java.service.refund.model.Refund;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author kuril
 * @Description 微信支付退款控制器
 */
@Api(tags = "微信支付退款接口", description = "微信支付退款相关接口")
@RestController
@RequestMapping("/api/wx/refund")
@Slf4j
public class WxRefundController {

    private final WxRefundService wxRefundService;
    private final WxRefundRecordMapper wxRefundRecordMapper;

    @Autowired
    public WxRefundController(WxRefundService wxRefundService, WxRefundRecordMapper wxRefundRecordMapper) {
        this.wxRefundService = wxRefundService;
        this.wxRefundRecordMapper = wxRefundRecordMapper;
    }

    /**
     * 申请退款
     *
     * @param requestDTO 退款请求DTO
     * @return 退款结果
     */
    @Operation(summary = "申请退款", description = "根据商户订单号申请全额退款")
    @ApiResponses({
            @ApiResponse(code = 200, message = "申请成功", response = Result.class),
            @ApiResponse(code = 400, message = "商户订单号不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/apply")
    public Result<WxRefundResponseVO> refund(@RequestBody WxRefundRequestDTO requestDTO) {

        log.info("申请退款，商户订单号: {}, 退款原因: {}", requestDTO.getOutTradeNo(), requestDTO.getReason());

        try {
            Refund refund = wxRefundService.refund(requestDTO.getOutTradeNo(), requestDTO.getReason());

            // 构建返回结果
            WxRefundResponseVO responseVO = new WxRefundResponseVO();
            responseVO.setOutTradeNo(requestDTO.getOutTradeNo());
            responseVO.setOutRefundNo(refund.getOutRefundNo());
            responseVO.setRefundId(refund.getRefundId());
            responseVO.setStatus(refund.getStatus().toString());
            // 日期类型处理
            if (refund.getSuccessTime() != null) {
                responseVO.setSuccessTime(refund.getSuccessTime());
            }

            if (refund.getAmount() != null) {
                responseVO.setTotalAmount(new BigDecimal(refund.getAmount().getTotal()).divide(new BigDecimal(100)));
                responseVO.setRefundAmount(new BigDecimal(refund.getAmount().getRefund()).divide(new BigDecimal(100)));
                responseVO.setCurrency(refund.getAmount().getCurrency());
            }

            return Result.success(responseVO);
        } catch (ServiceException e) {
            log.error("申请退款业务异常: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("申请退款系统异常: {}", e.getMessage());
            return Result.error("申请退款失败，系统异常");
        }
    }

    /**
     * 查询退款状态
     *
     * @param outTradeNo 商户订单号
     * @return 退款信息
     */
    @Operation(summary = "查询退款状态", description = "根据商户订单号查询退款状态")
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功", response = Result.class),
            @ApiResponse(code = 400, message = "商户订单号不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/query/{outTradeNo}")
    public Result<WxRefundQueryResponseVO> queryRefund(
            @PathVariable @Parameter(description = "商户订单号", required = true) String outTradeNo) {
        
        log.info("查询退款状态，商户订单号: {}", outTradeNo);
        
        try {
            Refund refund = wxRefundService.queryRefund(outTradeNo);
            WxRefundQueryResponseVO responseVO = new WxRefundQueryResponseVO();
            
            if(refund == null) {
                responseVO.setIsRefund(false);
                return Result.success(responseVO);
            }
            
            // 构建返回结果
            responseVO.setIsRefund(true);
            responseVO.setOutTradeNo(refund.getOutTradeNo());
            responseVO.setOutRefundNo(refund.getOutRefundNo());
            responseVO.setRefundId(refund.getRefundId());
            responseVO.setStatus(refund.getStatus().toString());

            // 日期类型处理
            if (refund.getSuccessTime() != null) {
                responseVO.setSuccessTime(refund.getSuccessTime());
            }
            
            // 查询当前商户订单号下的所有退款记录，计算退款金额总和
            QueryWrapper<WxRefundRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("out_trade_no", outTradeNo);
            List<WxRefundRecord> refundRecords = wxRefundRecordMapper.selectList(queryWrapper);
            
            BigDecimal totalRefundAmount = BigDecimal.ZERO;
            if (refundRecords != null && !refundRecords.isEmpty()) {
                for (WxRefundRecord record : refundRecords) {
                    if (record.getRefundAmount() != null) {
                        totalRefundAmount = totalRefundAmount.add(record.getRefundAmount());
                    }
                }
                log.info("商户订单号: {} 的累计退款金额为: {}", outTradeNo, totalRefundAmount);
                
                // 更新refund对象中的退款金额
                if (refund.getAmount() != null) {
                    // 将元转换为分
                    long refundAmountFen = totalRefundAmount.multiply(new BigDecimal(100)).longValue();
                    refund.getAmount().setRefund(refundAmountFen);
                }
            }
            
            if (refund.getAmount() != null) {
                responseVO.setTotalAmount(new BigDecimal(refund.getAmount().getTotal()).divide(new BigDecimal(100)));
                responseVO.setRefundAmount(new BigDecimal(refund.getAmount().getRefund()).divide(new BigDecimal(100)));
                responseVO.setCurrency(refund.getAmount().getCurrency());
            }
            
            return Result.success(responseVO);
        } catch (ServiceException e) {
            log.error("查询退款业务异常: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询退款系统异常: {}", e.getMessage());
            return Result.error("查询退款失败，系统异常");
        }
    }
} 