package com.tour.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tour.dao.ActivityMapper;
import com.tour.dao.ActivityOrderMapper;
import com.tour.enums.OrderRefundStatusEnum;
import com.tour.model.Activity;
import com.tour.model.ActivityOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 退款状态检查定时任务
 * 
 * @Author Kuril
 */
@Slf4j
@Component
public class RefundStatusCheckTask {

    private final ActivityMapper activityMapper;
    private final ActivityOrderMapper activityOrderMapper;

    public RefundStatusCheckTask(ActivityMapper activityMapper, ActivityOrderMapper activityOrderMapper) {
        this.activityMapper = activityMapper;
        this.activityOrderMapper = activityOrderMapper;
    }

    /**
     * 定时检查活动退款截止时间并更新订单退款状态
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void checkActivityRefundDeadline() {
        log.info("开始执行活动退款状态检查定时任务...");
        LocalDateTime now = LocalDateTime.now();
        
        // 查询所有活动
        List<Activity> activities = activityMapper.selectList(null);
        int updatedOrdersCount = 0;
        
        for (Activity activity : activities) {
            // 获取退款截止时间
            LocalDateTime refundDeadline = activity.getEndRefundTime();
            
            if (refundDeadline == null) {
                continue;
            }
            
            // 如果当前时间已过退款截止时间
            if (now.isAfter(refundDeadline)) {
                // 查询该活动下所有状态为"未申请退款"的订单
                List<ActivityOrder> orders = activityOrderMapper.selectList(
                    new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getActivityId, activity.getId())
                        .eq(ActivityOrder::getRefundStatus, OrderRefundStatusEnum.NOT_APPLIED.getCode())
                );
                
                if (!orders.isEmpty()) {
                    // 批量更新订单的退款状态为"不可退款"
                    activityOrderMapper.update(null,
                        new LambdaUpdateWrapper<ActivityOrder>()
                            .eq(ActivityOrder::getActivityId, activity.getId())
                            .eq(ActivityOrder::getRefundStatus, OrderRefundStatusEnum.NOT_APPLIED.getCode())
                            .set(ActivityOrder::getRefundStatus, OrderRefundStatusEnum.NOT_REFUNDABLE.getCode().longValue())
                    );
                    
                    updatedOrdersCount += orders.size();
                    log.info("活动ID: {} 已过退款截止时间，更新了 {} 个订单的退款状态为不可退款",
                            activity.getId(), orders.size());
                }
            }
        }
        
        log.info("活动退款状态检查定时任务执行完毕，共更新 {} 个订单状态", updatedOrdersCount);
    }
} 