package com.tour.service;

import com.tour.dto.ActivityOrderDTO;
import com.tour.dto.EnrollmentDTO;
import com.tour.model.Achievement;
import com.tour.model.ActivityOrder;
import com.tour.model.OrderPaymentResult;
import com.tour.vo.AchievementVO;
import com.tour.vo.SignUpListVO;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.io.File;

/**
 * @Author kuril
 * @Description 成就服务接口
 * @DateTime 2025/5/10 19:29
 */
public interface SignUpService {

    /**
     * 报名接口
     *
     * @param enrollmentDTO 报名信息，含各类id和travelers信息
     * @return null
     */
    public void signup(EnrollmentDTO enrollmentDTO,String orderNo);


    List<SignUpListVO> loadSignupListByActivityId(Long id);

    OrderPaymentResult<ActivityOrder> lockOrder(ActivityOrderDTO activityOrderDTO);

    // 新增方法：支付订单
    @Transactional(rollbackFor = Exception.class)
    boolean payOrder(String orderNo, String paymentId,EnrollmentDTO enrollmentDTO);

    // 取消订单方法（无退款原因，兼容旧接口）
    @Transactional(rollbackFor = Exception.class)
    boolean cancelOrder(String orderNo, String openid);
    
    // 取消订单并申请退款（带退款原因）
    @Transactional(rollbackFor = Exception.class)
    boolean cancelOrder(String orderNo, String openid, String reason);

    // 取消订单并申请退款（带退款原因和真实原因，真实原因存入数据库）
    @Transactional(rollbackFor = Exception.class)
    boolean cancelOrder(String orderNo, String openid, String reason, String realReason);

    /**
     * 部分退款（按照出行人列表进行部分退款）
     *
     * @param orderNo 订单号
     * @param openid 用户openid
     * @param travelerIds 需要退款的出行人ID列表
     * @param reason 退款原因
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    boolean partialRefund(String orderNo, String openid, List<Long> travelerIds, String reason);

    /**
     * 特殊退款（退款但保留订单和报名记录）
     *
     * @param orderNo 订单号
     * @param openid 用户openid
     * @param reason 退款原因
     * @param adminRemark 管理员备注
     * @param travelerIds 需要退款的出行人ID列表
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    boolean specialRefund(String orderNo, String openid, String reason, String adminRemark, List<Long> travelerIds);

    /**
     * 特殊退款，可选择是否将出行人标记为不可免单
     *
     * @param orderNo 订单号
     * @param openid 用户OpenID
     * @param reason 退款原因
     * @param adminRemark 管理员备注
     * @param travelerIds 出行人ID列表
     * @param useRefundStatus 是否使用退款状态标记（true-标记为不可免单，false-删除出行人关联记录）
     * @return 是否成功
     */
    boolean specialRefund(String orderNo, String openid, String reason, String adminRemark, List<Long> travelerIds, boolean useRefundStatus);

    Object getOrder(String orderNo, String openid);

    /**
     * 自定义金额部分退款
     *
     * @param orderNo 订单号
     * @param travelerIds 需要退款的出行人ID列表（可选）
     * @param reason 退款原因
     * @param refundAmount 退款金额
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    boolean customRefund(String orderNo, List<Long> travelerIds, String reason, BigDecimal refundAmount);

    /**
     * 根据出行人ID列表删除指定出行人
     *
     * @param orderNo 订单号
     * @param travelerIds 需要删除的出行人ID列表
     * @param reason 操作原因
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    boolean deleteTravelers(String orderNo, List<Long> travelerIds, String reason);

    /**
     * 指定金额退款
     *
     * @param orderNo 订单号
     * @param reason 退款原因
     * @param refundAmount 退款金额
     * @param orderStatus 指定订单状态(可选)，用于更新订单状态
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    boolean amountRefund(String orderNo, String reason, BigDecimal refundAmount, Integer orderStatus);
}