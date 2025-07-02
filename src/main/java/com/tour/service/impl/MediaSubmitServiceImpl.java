package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.WxMediaCheckUtil;
import com.tour.dao.MediaCheckRecordMapper;
import com.tour.model.MediaCheckRecord;
import com.tour.service.IMediaSubmitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @Author Abin
 * @Description 媒体检测提交服务实现
 */
@Slf4j
@Service
public class MediaSubmitServiceImpl extends ServiceImpl<MediaCheckRecordMapper, MediaCheckRecord> implements IMediaSubmitService {

    @Autowired
    private WxMediaCheckUtil wxMediaCheckUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MediaCheckRecord submitMediaCheck(String mediaUrl, Integer mediaType, String businessType, String businessId, String userOpenid) {
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "媒体URL不能为空");
        }
        
        if (mediaType == null || mediaType < 1 || mediaType > 3) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "媒体类型无效");
        }
        
        if (businessType == null || businessType.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "业务类型不能为空");
        }
        
        // 查找是否存在相同业务类型和ID的记录，但不进行去重处理
        MediaCheckRecord existRecord = getByBusinessTypeAndId(businessType, businessId);
        if (existRecord != null) {
            log.info("业务内容已更新，创建新的检测记录，业务类型: {}, 业务ID: {}", businessType, businessId);
        }
        
        // 创建新的检测记录
        MediaCheckRecord record = new MediaCheckRecord();
        record.setMediaUrl(mediaUrl);
        record.setMediaType(mediaType);
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setUserOpenid(userOpenid);
        record.setStatus(0);  // 检测中
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        
        // 保存记录
        save(record);
        
        try {
            // 调用微信媒体检测API
            String traceId = wxMediaCheckUtil.checkMedia(mediaUrl, mediaType, businessType, businessId, userOpenid);
            record.setTraceId(traceId);
            record.setUpdateTime(LocalDateTime.now());
            updateById(record);
            log.info("提交媒体检测成功, traceId: {}, 业务类型: {}, 业务ID: {}", traceId, businessType, businessId);
        } catch (Exception e) {
            log.error("提交媒体检测异常", e);
            // 设置一个模拟的traceId，实际场景中可以选择抛出异常或使用默认处理方式
            record.setTraceId("mock_error_" + System.currentTimeMillis());
            record.setDetail("提交检测异常: " + e.getMessage());
            record.setUpdateTime(LocalDateTime.now());
            updateById(record);
        }
        
        return record;
    }
    
    /**
     * 根据业务类型和业务ID查询检测记录
     * 
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 最新的检测记录
     */
    private MediaCheckRecord getByBusinessTypeAndId(String businessType, String businessId) {
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
} 