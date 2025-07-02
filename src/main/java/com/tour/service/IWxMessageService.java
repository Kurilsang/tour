package com.tour.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tour.model.WxMessage;

import java.util.Map;

/**
 * @Author Abin
 * @Description 微信消息处理服务接口
 */
public interface IWxMessageService extends IService<WxMessage> {
    
    /**
     * 处理微信消息推送
     *
     * @param messageData 消息数据
     * @param openid 用户openid
     * @param appid 小程序appid
     * @return 处理结果
     */
    WxMessage processMessage(Map<String, Object> messageData, String openid, String appid);
    
    /**
     * 根据消息ID获取消息
     *
     * @param messageId 消息ID
     * @return 消息实体
     */
    WxMessage getMessageById(Long messageId);
    
    /**
     * 处理媒体检测异步回调
     *
     * @param messageData 消息数据
     * @return 处理结果
     */
    boolean handleMediaCheckCallback(Map<String, Object> messageData);
} 