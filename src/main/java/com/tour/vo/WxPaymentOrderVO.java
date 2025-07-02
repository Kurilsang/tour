package com.tour.vo;

import com.tour.enums.WxPaymentStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Author kuril
 * @Description 微信支付订单查询结果
 */
@Data
public class WxPaymentOrderVO {
    
    /**
     * 商户订单号
     */
    private String outTradeNo;
    
    /**
     * 微信支付订单号
     */
    private String transactionId;
    
    /**
     * 预支付交易会话标识
     */
    private String prepayId;
    
    /**
     * 交易状态
     */
    private String tradeState;
    
    /**
     * 交易状态描述
     */
    private String tradeStateDesc;
    
    /**
     * 付款银行
     */
    private String bankType;
    
    /**
     * 支付完成时间
     */
    private LocalDateTime successTime;
    
    /**
     * 订单金额（元）
     */
    private BigDecimal totalAmount;
    
    /**
     * 用户标识（用户在商户对应appid下的唯一标识）
     */
    private String openid;
    
    /**
     * 交易类型
     */
    private String tradeType;
    
    /**
     * 附加数据
     */
    private String attach;
    
    /**
     * 商品描述
     */
    private String description;
    
    /**
     * 查询是否成功
     */
    private Boolean success;
    
    /**
     * 错误码（当查询失败时返回）
     */
    private String errorCode;
    
    /**
     * 错误信息（当查询失败时返回）
     */
    private String errorMessage;
    
    /**
     * 判断订单是否已支付成功
     * 
     * @return 是否已支付成功
     */
    public boolean isPaid() {
        return WxPaymentStatusEnum.SUCCESS.getCode().equals(tradeState);
    }
    
    /**
     * 判断订单是否已关闭
     * 
     * @return 是否已关闭
     */
    public boolean isClosed() {
        return WxPaymentStatusEnum.CLOSED.getCode().equals(tradeState);
    }
    
    /**
     * 判断订单是否未支付
     * 
     * @return 是否未支付
     */
    public boolean isNotPaid() {
        return WxPaymentStatusEnum.NOTPAY.getCode().equals(tradeState);
    }
} 