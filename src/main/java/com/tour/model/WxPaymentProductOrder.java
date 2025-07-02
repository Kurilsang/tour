package com.tour.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author kuril
 * @Description 微信支付商品订单关联信息
 */
@Data
@TableName("wxpayment_product_order")
public class WxPaymentProductOrder {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 商户订单号，关联product_order表
     */
    @TableField("order_no")
    private String orderNo;
    
    /**
     * 时间戳
     */
    @TableField("time_stamp")
    private String timeStamp;
    
    /**
     * 随机字符串
     */
    @TableField("nonce_str")
    private String nonceStr;
    
    /**
     * 订单详情扩展字符串
     */
    @TableField("package_str")
    private String packageStr;
    
    /**
     * 签名方式
     */
    @TableField("sign_type")
    private String signType;
    
    /**
     * 签名
     */
    @TableField("pay_sign")
    private String paySign;
    
    /**
     * 预支付交易会话标识
     */
    @TableField("prepay_id")
    private String prepayId;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
} 