package com.tour.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 自定义金额退款请求DTO
 */
@Data
public class CustomRefundRequestDTO {
    /**
     * 订单号
     */
    private String outTradeNo;
    
    /**
     * 退款出行人ID列表（可选）
     */
    private List<Long> travelerIds;
    
    /**
     * 退款原因
     */
    private String reason;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 订单状态(可选)，用于指定退款后的订单状态
     * 可传入"已取消"或"已支付"状态值
     */
    private Integer orderStatus;
} 