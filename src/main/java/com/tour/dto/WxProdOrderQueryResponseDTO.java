package com.tour.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 微信支付查询订单API (prod环境) 响应DTO
 */
@Data
@NoArgsConstructor
public class WxProdOrderQueryResponseDTO {
    
    /**
     * 错误码
     */
    private Integer errcode;
    
    /**
     * 错误信息
     */
    private String errmsg;
    
    /**
     * 响应数据
     */
    private OrderResponseData respdata;
    
    @Data
    @NoArgsConstructor
    public static class OrderResponseData {
        /**
         * 返回状态码
         */
        private String return_code;
        
        /**
         * 返回信息
         */
        private String return_msg;
        
        /**
         * 服务商的APPID
         */
        private String appid;
        
        /**
         * 商户号
         */
        private String mch_id;
        
        /**
         * 小程序的APPID
         */
        private String sub_appid;
        
        /**
         * 子商户号
         */
        private String sub_mch_id;
        
        /**
         * 随机字符串
         */
        private String nonce_str;
        
        /**
         * 签名
         */
        private String sign;
        
        /**
         * 业务结果
         */
        private String result_code;
        
        /**
         * 错误码
         */
        private String err_code;
        
        /**
         * 错误码描述
         */
        private String err_code_des;
        
        /**
         * 用户在商户appid下的唯一标识
         */
        private String openid;
        
        /**
         * 用户是否关注公众账号，Y-关注，N-未关注
         */
        private String is_subscribe;
        
        /**
         * 用户在子商户appid下的唯一标识
         */
        private String sub_openid;
        
        /**
         * 用户是否关注子公众账号，Y-关注，N-未关注
         */
        private String sub_is_subscribe;
        
        /**
         * 调用接口提交的交易类型: JSAPI，NATIVE，APP，MICROPAY
         */
        private String trade_type;
        
        /**
         * 交易状态
         * SUCCESS—支付成功
         * REFUND—转入退款
         * NOTPAY—未支付
         * CLOSED—已关闭
         * REVOKED—已撤销（刷卡支付）
         * USERPAYING--用户支付中
         * PAYERROR--支付失败(其他原因，如银行返回失败)
         */
        private String trade_state;
        
        /**
         * 银行类型
         */
        private String bank_type;
        
        /**
         * 订单总金额，单位为分
         */
        private Integer total_fee;
        
        /**
         * 货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY
         */
        private String fee_type;
        
        /**
         * 当订单使用了免充值型优惠券后返回该参数，应结订单金额=订单金额-免充值优惠券金额
         */
        private Integer settlement_total_fee;
        
        /**
         * 现金支付金额订单现金支付金额
         */
        private Integer cash_fee;
        
        /**
         * 货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY
         */
        private String cash_fee_type;
        
        /**
         * 代金券或立减优惠金额<=订单总金额，订单总金额-代金券或立减优惠金额=现金支付金额
         */
        private Integer coupon_fee;
        
        /**
         * 代金券或立减优惠使用数量
         */
        private Integer coupon_count;
        
        /**
         * 代金券或立减优惠ID列表
         */
        private List<String> coupon_id_list;
        
        /**
         * 代金券或立减优惠类型列表
         */
        private List<String> coupon_type_list;
        
        /**
         * 代金券或立减优惠金额列表
         */
        private List<Integer> coupon_fee_list;
        
        /**
         * 微信支付订单号
         */
        private String transaction_id;
        
        /**
         * 商户系统内部订单号
         */
        private String out_trade_no;
        
        /**
         * 附加数据
         */
        private String attach;
        
        /**
         * 订单支付时间，格式为yyyyMMddHHmmss
         */
        private String time_end;
        
        /**
         * 对当前查询订单状态的描述和下一步操作的指引
         */
        private String trade_state_desc;
    }
} 