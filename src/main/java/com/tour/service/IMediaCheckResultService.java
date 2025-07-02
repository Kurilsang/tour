package com.tour.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tour.model.MediaCheckRecord;

/**
 * @Author Abin
 * @Description 媒体检测结果处理服务接口
 */
public interface IMediaCheckResultService extends IService<MediaCheckRecord> {
    
    /**
     * 处理媒体检测回调结果
     *
     * @param traceId 追踪ID
     * @param result 检测结果
     * @param detail 检测详情
     * @return 是否处理成功
     */
    boolean handleCheckResult(String traceId, Integer result, String detail);
    
    /**
     * 根据业务类型和业务ID查询检测记录
     * 如果有多条记录（比如用户多次更新头像），返回最新创建的记录
     *
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 最新的检测记录
     */
    MediaCheckRecord getByBusinessTypeAndId(String businessType, String businessId);
} 