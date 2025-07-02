package com.tour.vo;

import com.tour.model.ActivityOrder;
import com.tour.model.Traveler;
import com.tour.model.WxPaymentActivityOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author Kuril
 * @Description 返回的订单详细信息VO
 * @DateTime 2025/5/12 19:23
 */
@Data
@ApiModel(description = "轮播图信息")
public class ActivityOrderDetail {

    /**
     * 活动订单所有信息
     */
    @ApiModelProperty(value = "活动订单所有信息", example = "")
    private ActivityOrder activityOrder;
    /**
     * 活动标题
     */
    @ApiModelProperty(value = "活动标题", example = "")
    private String activityTitle;
    /**
     * 出行人名称列表
     */
    @ApiModelProperty(value = "出行人名称列表", example = "")
    private List<TravelerOrderVO> travelerNameList;
    /**
     * 上车点信息及位置信息
     */
    @ApiModelProperty(value = "上车点信息及位置信息", example = "")
    private BusLocationVO busLocationVO;
    /**
     * 前端收银台信息
     */
    @ApiModelProperty(value = "前端收银台信息", example = "")
    private WxPaymentActivityOrder wxPaymentActivityOrder;
    
    /**
     * 订单下单时的原始总价（从wx_refund_record表获取）
     */
    @ApiModelProperty(value = "订单原始总价", example = "100.00")
    private BigDecimal originalTotalAmount;

}