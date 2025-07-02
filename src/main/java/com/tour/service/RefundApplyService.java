package com.tour.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.dto.PartialRefundApplyDTO;
import com.tour.dto.RefundApplyDTO;
import com.tour.model.RefundApply;
import com.tour.query.RefundApplyQuery;
import com.tour.vo.RefundApplyVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 退款申请服务接口
 *
 * @Author Kuril
 */
public interface RefundApplyService {

    /**
     * 创建退款申请
     *
     * @param dto 退款申请DTO
     * @param openid 用户openid
     * @return 退款申请ID
     */
    Long createRefundApply(RefundApplyDTO dto, String openid);
    
    /**
     * 创建部分出行人退款申请
     *
     * @param dto 部分退款申请DTO
     * @param openid 用户openid
     * @return 退款申请ID
     */
    Long createPartialRefundApply(PartialRefundApplyDTO dto, String openid);

    /**
     * 根据订单号查询退款申请
     *
     * @param outTradeOrder 订单号
     * @return 退款申请VO
     */
    RefundApplyVO queryRefundApply(String outTradeOrder);
    
    /**
     * 加载退款申请列表
     *
     * @param refundApplyQuery 查询条件
     * @return 退款申请分页列表
     */
    IPage<RefundApplyVO> loadDataList(RefundApplyQuery refundApplyQuery);
    
    /**
     * 拒绝退款申请
     *
     * @param id 退款申请ID
     * @param adminRemark 管理员备注
     * @return 是否成功
     */
    boolean refuseRefundApply(Long id, String adminRemark);
    
    /**
     * 检查订单退款状态
     * 
     * @param outTradeOrder 订单号
     * @return 如果可以申请退款返回true，如果不能申请退款抛出异常
     */
    boolean checkRefundStatus(String outTradeOrder);
    
    /**
     * 接受退款申请
     *
     * @param id 退款申请ID
     * @param adminRemark 管理员备注
     * @return 是否成功
     */
    boolean acceptRefundApply(Long id, String adminRemark);
    
    /**
     * 接受退款申请（带指定金额）
     *
     * @param id 退款申请ID
     * @param adminRemark 管理员备注
     * @param specifiedAmount 指定退款金额（可选，仅适用于活动订单）
     * @return 是否成功
     */
    boolean acceptRefundApply(Long id, String adminRemark, BigDecimal specifiedAmount);
    
    /**
     * 检查活动订单是否在可退款时间范围内
     * 
     * @param orderNo 活动订单号
     * @param openid 用户openid
     * @return 如果在可退款时间范围内返回true，否则抛出异常
     */
    boolean checkActivityRefundTime(String orderNo, String openid);
    
    /**
     * 检查出行人是否已经申请退款
     * 
     * @param travelerIds 出行人ID列表
     * @param orderNo 订单号
     * @return 如果没有申请退款返回true，否则抛出异常
     */
    boolean checkTravelerRefundStatus(List<Long> travelerIds, String orderNo);
    
    /**
     * 取消退款申请
     * 
     * @param id 退款申请ID
     * @param openid 用户openid
     * @return 是否成功
     */
    boolean cancelRefundApply(Long id, String openid);
    
    /**
     * 检查活动订单是否为已免单状态
     * 
     * @param orderNo 订单号
     * @param openid 用户openid
     * @return 如果不是已免单返回true，如果是已免单抛出异常
     */
    boolean checkOrderFeeExemption(String orderNo, String openid);
    
    /**
     * 检查活动订单是否有可退款出行人
     * 
     * @param orderNo 订单号
     * @param openid 用户openid
     * @return 如果有可退款出行人返回true，如果没有抛出异常
     */
    boolean checkAvailableTravelers(String orderNo, String openid);
} 