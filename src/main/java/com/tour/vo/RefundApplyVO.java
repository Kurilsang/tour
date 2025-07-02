package com.tour.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 退款申请VO
 *
 * @Author Kuril
 */
@Data
@ApiModel(description = "退款申请响应信息")
public class RefundApplyVO {

    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号", example = "20231201123456")
    private String outTradeOrder;

    /**
     * 申请人openid
     */
    @ApiModelProperty(value = "申请人openid", example = "o4GgauNFn2jGjH8Q12345")
    private String openid;

    /**
     * 退款状态
     */
    @ApiModelProperty(value = "退款状态: 1-待审核, 2-退款中, 3-退款完成, 4-拒绝退款", example = "1")
    private Integer status;
    
    /**
     * 退款状态描述
     */
    @ApiModelProperty(value = "退款状态描述", example = "待审核")
    private String statusDesc;

    /**
     * 退款理由
     */
    @ApiModelProperty(value = "退款理由", example = "行程有变，无法参与活动")
    private String reason;

    /**
     * 订单类型
     */
    @ApiModelProperty(value = "订单类型: 1-活动订单, 2-商品订单", example = "1")
    private Integer orderType;
    
    /**
     * 订单类型描述
     */
    @ApiModelProperty(value = "订单类型描述", example = "活动订单")
    private String orderTypeDesc;

    /**
     * 管理员备注
     */
    @ApiModelProperty(value = "管理员备注", example = "已处理")
    private String adminRemark;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", example = "2023-12-01T12:34:56")
    private LocalDateTime createTime;
    
    /**
     * 订单总金额
     */
    @ApiModelProperty(value = "订单总金额", example = "199.00")
    private BigDecimal totalAmount;
    
    /**
     * 退款金额
     */
    @ApiModelProperty(value = "退款金额", example = "99.00")
    private BigDecimal refundAmount;
    
    /**
     * 商品/票总数量
     */
    @ApiModelProperty(value = "商品/票总数量", example = "2")
    private Integer itemQuantity;
    
    /**
     * 早鸟票数量（活动订单专用）
     */
    @ApiModelProperty(value = "早鸟票数量", example = "1")
    private Integer earlyBirdNum;
    
    /**
     * 普通票数量（活动订单专用）
     */
    @ApiModelProperty(value = "普通票数量", example = "1")
    private Integer normalNum;
    
    /**
     * 活动名称或商品名称
     */
    @ApiModelProperty(value = "活动名称或商品名称", example = "周末登山活动")
    private String itemName;

    /**
     * 是否是部分退款
     */
    @ApiModelProperty(value = "是否是部分退款", example = "false")
    private Boolean isPartialRefund = false;
    
    /**
     * 退款的出行人列表
     */
    @ApiModelProperty(value = "退款的出行人列表")
    private List<RefundTravelerVO> refundTravelers;
} 