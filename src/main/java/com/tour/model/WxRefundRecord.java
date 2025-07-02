package com.tour.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Author Claude
 * @Description 微信退款记录实体类
 */
@Data
@TableName("wx_refund_record")
public class WxRefundRecord {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 商户订单号
     */
    private String outTradeNo;
    
    /**
     * 商户退款单号
     */
    private String outRefundNo;
    
    /**
     * 微信退款单号
     */
    private String refundId;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 退款状态
     */
    private String refundStatus;
    
    /**
     * 退款原因
     */
    private String reason;
    
    /**
     * 退款成功时间
     */
    private LocalDateTime successTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 