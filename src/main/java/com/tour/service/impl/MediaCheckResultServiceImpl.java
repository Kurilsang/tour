package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.dao.MediaCheckRecordMapper;
import com.tour.enums.MediaCheckEnum;
import com.tour.model.MediaCheckRecord;
import com.tour.service.IMediaCheckHandlerService;
import com.tour.service.IMediaCheckResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Abin
 * @Description 媒体检测结果处理服务实现
 */
@Slf4j
@Service
public class MediaCheckResultServiceImpl extends ServiceImpl<MediaCheckRecordMapper, MediaCheckRecord> implements IMediaCheckResultService {

    @Autowired
    private IMediaCheckHandlerService mediaCheckHandlerService;
    
    // 业务处理器映射
    private final Map<String, BusinessHandler> businessHandlers = new HashMap<>();
    
    /**
     * 构造函数中初始化业务处理器
     */
    public MediaCheckResultServiceImpl() {
        // 初始化各种业务类型的处理器
        businessHandlers.put(MediaCheckEnum.BusinessType.USER_AVATAR.getCode(), 
            record -> {
                if (record.getResult() != null && record.getResult() == MediaCheckEnum.Result.RISKY.getCode()) {
                    mediaCheckHandlerService.handleUserAvatar(record.getUserOpenid(), record.getMediaUrl());
                }
            });
        
        businessHandlers.put(MediaCheckEnum.BusinessType.WISH_IMAGE.getCode(), 
            record -> {
                if (record.getResult() != null && record.getResult() == MediaCheckEnum.Result.RISKY.getCode()) {
                    mediaCheckHandlerService.handleWishImage(Long.parseLong(record.getBusinessId()), record.getMediaUrl());
                }
            });
        
        businessHandlers.put(MediaCheckEnum.BusinessType.COMMENT_IMAGE.getCode(), 
            record -> {
                if (record.getResult() != null && record.getResult() == MediaCheckEnum.Result.RISKY.getCode()) {
                    mediaCheckHandlerService.handleCommentImage(Long.parseLong(record.getBusinessId()), record.getMediaUrl());
                }
            });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleCheckResult(String traceId, Integer result, String detail) {
        if (traceId == null || traceId.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "追踪ID不能为空");
        }
        
        // 查询检测记录
        LambdaQueryWrapper<MediaCheckRecord> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MediaCheckRecord::getTraceId, traceId);
        MediaCheckRecord record = getOne(wrapper);
        
        if (record == null) {
            log.warn("未找到对应的媒体检测记录, traceId: {}", traceId);
            return false;
        }
        
        // 更新检测结果
        record.setStatus(1);  // 检测完成
        record.setResult(result);
        record.setDetail(detail);
        record.setUpdateTime(LocalDateTime.now());
        updateById(record);
        
        // 根据业务类型处理检测结果
        String businessType = record.getBusinessType();
        BusinessHandler handler = businessHandlers.get(businessType);
        
        if (handler != null) {
            try {
                handler.handle(record);
                log.info("处理媒体检测结果成功, traceId: {}, 业务类型: {}", traceId, businessType);
                return true;
            } catch (Exception e) {
                log.error("处理媒体检测结果异常, traceId: {}, 业务类型: {}", traceId, businessType, e);
                return false;
            }
        } else {
            log.warn("未找到对应的业务处理器, 业务类型: {}", businessType);
            return false;
        }
    }

    @Override
    public MediaCheckRecord getByBusinessTypeAndId(String businessType, String businessId) {
        if (businessType == null || businessType.isEmpty() || businessId == null || businessId.isEmpty()) {
            return null;
        }
        
        LambdaQueryWrapper<MediaCheckRecord> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MediaCheckRecord::getBusinessType, businessType)
               .eq(MediaCheckRecord::getBusinessId, businessId)
               .orderByDesc(MediaCheckRecord::getCreateTime); // 按创建时间倒序，取最新的记录
        
        // 获取第一条记录（最新的）
        return getOne(wrapper, false); // false表示不抛出异常如果有多条
    }
    
    /**
     * 业务处理器接口
     */
    @FunctionalInterface
    private interface BusinessHandler {
        void handle(MediaCheckRecord record);
    }
} 