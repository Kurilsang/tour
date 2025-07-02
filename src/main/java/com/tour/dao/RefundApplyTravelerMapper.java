package com.tour.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tour.model.RefundApplyTraveler;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 退款申请与出行人关联数据访问层
 *
 * @author Kuril
 */
@Mapper
public interface RefundApplyTravelerMapper extends BaseMapper<RefundApplyTraveler> {

    /**
     * 根据退款申请ID查询关联的出行人信息
     *
     * @param refundApplyId 退款申请ID
     * @return 关联的出行人信息列表
     */
    List<RefundApplyTraveler> selectByRefundApplyId(@Param("refundApplyId") Long refundApplyId);

    /**
     * 根据订单号查询关联的退款申请出行人信息
     *
     * @param orderNo 订单号
     * @return 关联的退款申请出行人信息列表
     */
    List<RefundApplyTraveler> selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据退款申请ID和订单号查询关联的出行人信息
     *
     * @param refundApplyId 退款申请ID
     * @param orderNo 订单号
     * @return 关联的出行人信息列表
     */
    List<RefundApplyTraveler> selectByRefundApplyIdAndOrderNo(@Param("refundApplyId") Long refundApplyId, @Param("orderNo") String orderNo);
    
    /**
     * 批量插入退款申请与出行人关联记录
     *
     * @param list 退款申请与出行人关联记录列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("list") List<RefundApplyTraveler> list);
} 