package com.tour.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tour.common.constant.Constants;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.dao.WxMessageMapper;
import com.tour.enums.MediaCheckEnum;
import com.tour.model.WxMessage;
import com.tour.service.IMediaCheckResultService;
import com.tour.service.IWxMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @Author Abin
 * @Description 微信消息处理服务实现
 */
@Slf4j
@Service
public class WxMessageServiceImpl extends ServiceImpl<WxMessageMapper, WxMessage> implements IWxMessageService {

    @Autowired
    private IMediaCheckResultService mediaCheckResultService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxMessage processMessage(Map<String, Object> messageData, String openid, String appid) {
        if (messageData == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "消息数据不能为空");
        }
        
        // 创建消息记录
        WxMessage wxMessage = new WxMessage();
        wxMessage.setUserOpenid(openid);
        wxMessage.setAppid(appid);
        wxMessage.setRawContent(messageData);
        wxMessage.setCreateTime(LocalDateTime.now());
        wxMessage.setUpdateTime(LocalDateTime.now());
        wxMessage.setStatus(MediaCheckEnum.MessageStatus.PENDING.getCode()); // 待处理状态
        
        // 判断消息类型
        if (messageData.containsKey("MsgType")) {
            wxMessage.setMessageType(messageData.get("MsgType").toString());
        }
        
        if (messageData.containsKey("Event")) {
            wxMessage.setEventType(messageData.get("Event").toString());
        }
        
        // 保存消息记录
        save(wxMessage);
        
        // 根据消息类型分发处理
        try {
            if (Constants.WX_MESSAGE_TYPE_EVENT.equals(wxMessage.getMessageType())) {
                // 处理事件消息
                processEventMessage(wxMessage);
            } else {
                // 设置为已处理状态
                wxMessage.setStatus(MediaCheckEnum.MessageStatus.PROCESSED.getCode());
                wxMessage.setRemark("未知消息类型，无需处理");
                updateById(wxMessage);
            }
        } catch (Exception e) {
            log.error("处理微信消息异常", e);
            wxMessage.setStatus(MediaCheckEnum.MessageStatus.FAILED.getCode()); // 处理失败
            wxMessage.setRemark("处理异常: " + e.getMessage());
            updateById(wxMessage);
        }
        
        return wxMessage;
    }

    @Override
    public WxMessage getMessageById(Long messageId) {
        if (messageId == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "消息ID不能为空");
        }
        return getById(messageId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleMediaCheckCallback(Map<String, Object> messageData) {
        if (messageData == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "消息数据不能为空");
        }
        
        // 检查消息类型
        if (!messageData.containsKey("MsgType") || !Constants.WX_MESSAGE_TYPE_EVENT.equals(messageData.get("MsgType")) ||
            !messageData.containsKey("Event") || !Constants.WX_EVENT_MEDIA_CHECK.equals(messageData.get("Event"))) {
            log.warn("非媒体检测回调消息: {}", messageData);
            return false;
        }
        
        try {
            // 提取回调结果数据
            // 确保trace_id字段存在
            if (!messageData.containsKey("trace_id")) {
                log.error("媒体检测回调缺少trace_id字段: {}", messageData);
                return false;
            }
            
            String traceId = messageData.get("trace_id").toString();
            
            // 获取结果信息，微信可能有两种格式
            // 1. 直接包含result字段（整数）
            // 2. 包含result对象，其中有suggest字段
            int resultCode = MediaCheckEnum.Result.PASS.getCode(); // 默认通过
            String detail = null;
            
            if (messageData.containsKey("result")) {
                Object resultObj = messageData.get("result");
                
                if (resultObj instanceof Map) {
                    // 新格式：是个对象，包含suggest字段
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resultMap = (Map<String, Object>) resultObj;
                    String suggest = resultMap.containsKey("suggest") ? resultMap.get("suggest").toString() : MediaCheckEnum.Result.PASS.getSuggest();
                    
                    // 根据suggest字段确定结果码
                    resultCode = MediaCheckEnum.Result.getCodeBySuggest(suggest);
                    
                    // 将result对象序列化为JSON字符串作为detail
                    detail = resultObj.toString();
                }
            }
            
            // 如果detail还没有赋值，尝试使用detail字段
            if (detail == null && messageData.containsKey("detail")) {
                Object detailObj = messageData.get("detail");
                detail = detailObj.toString();
            }
            
            log.info("处理媒体检测回调, trace_id: {}, result_code: {}, detail: {}", traceId, resultCode, detail);
            
            // 更新媒体检测记录
            return mediaCheckResultService.handleCheckResult(traceId, resultCode, detail);
        } catch (Exception e) {
            log.error("处理媒体检测回调异常", e);
            return false;
        }
    }
    
    /**
     * 处理事件类型消息
     */
    private void processEventMessage(WxMessage wxMessage) {
        Map<String, Object> messageData = wxMessage.getRawContent();
        String event = wxMessage.getEventType();
        
        if (Constants.WX_EVENT_MEDIA_CHECK.equals(event)) {
            // 媒体检测结果回调
            log.info("收到媒体检测结果回调: {}", messageData);
            boolean success = handleMediaCheckCallback(messageData);
            
            wxMessage.setStatus(MediaCheckEnum.MessageStatus.PROCESSED.getCode());
            wxMessage.setResult(success ? MediaCheckEnum.Result.PASS.getCode() : MediaCheckEnum.Result.RISKY.getCode()); // 0-正常，1-违规
            wxMessage.setRemark(success ? "媒体检测结果处理成功" : "媒体检测结果处理失败");
            updateById(wxMessage);
        } else {
            // 其他事件类型
            wxMessage.setStatus(MediaCheckEnum.MessageStatus.PROCESSED.getCode());
            wxMessage.setRemark("未知事件类型，无需处理: " + event);
            updateById(wxMessage);
        }
    }
} 