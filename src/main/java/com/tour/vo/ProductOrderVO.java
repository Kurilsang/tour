package com.tour.vo;

import com.tour.model.Location;
import com.tour.model.WxPaymentProductOrder;
import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductOrderVO {
//    产品表中的名字
    private String productName;
//    产品表中的封面图
    private String coverImage;
//    订单表中经过计算得到总价
    private BigDecimal totalAmount;
//    订单表中的数量
    private Integer quantity;
//    订单创建时间
    private LocalDateTime createTime;
//    订单状态
    private Integer status;
//    订单号
    private String orderNo;
    //    用户id
    private String openid;

//    地点信息，用于渲染自提点
    Location location;
//    支付信息，用于用户调起收银台
    WxPaymentProductOrder wxPaymentProductOrder;
}
