package com.tour.controller;

import com.tour.common.Result;
import com.tour.model.WxMessage;
import com.tour.service.IWxMessageService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author Abin
 * @Description 微信消息推送接收控制器
 */
@Api(tags = "微信消息推送接口", description = "接收微信平台各种消息推送")
@Slf4j
@RestController
@RequestMapping("/api/wx/message")
public class WxMessageController {

    @Autowired
    private IWxMessageService wxMessageService;

    /**
     * 统一接收微信消息推送
     */
    @Operation(summary = "接收微信消息推送", description = "统一接收微信平台各种消息推送")
    @PostMapping("/receive")
    public String receiveMessage(@RequestBody Map<String, Object> messageData, HttpServletRequest request) {
        // 获取请求头，用于验证消息来源
        String wxSource = request.getHeader("x-wx-source");
        String wxOpenid = request.getHeader("x-wx-openid");
        String wxAppid = request.getHeader("x-wx-appid");
        
        log.info("收到微信消息推送，来源: {}, openid: {}, appid: {}", wxSource, wxOpenid, wxAppid);
        
        // 打印详细的请求数据，便于调试
        log.info("消息详细内容: {}", messageData);
        
        // 检测是否是配置测试请求
        if (messageData.containsKey("action") && "CheckContainerPath".equals(messageData.get("action"))) {
            log.info("收到配置测试请求");
            return "success";
        }
        
        try {
            // 检查是否是媒体检测回调事件
            if (messageData.containsKey("MsgType") && "event".equals(messageData.get("MsgType")) &&
                messageData.containsKey("Event") && "wxa_media_check".equals(messageData.get("Event"))) {
                
                log.info("收到媒体检测回调事件, trace_id: {}, result: {}", 
                        messageData.getOrDefault("trace_id", "未知"),
                        messageData.getOrDefault("result", "未知"));
            }
            
            // 处理消息
            WxMessage wxMessage = wxMessageService.processMessage(messageData, wxOpenid, wxAppid);
            log.info("消息处理完成, 消息ID: {}, 状态: {}", wxMessage.getId(), wxMessage.getStatus());
        } catch (Exception e) {
            // 即使处理失败，也返回success，避免微信平台重复推送
            log.error("处理微信消息异常", e);
        }
        
        // 统一返回success，避免微信平台重复推送
        return "success";
    }
    
    /**
     * 查询消息处理结果
     */
    @Operation(summary = "查询消息处理结果", description = "查询特定消息的处理结果")
    @GetMapping("/result/{messageId}")
    public Result<WxMessage> getMessageResult(@PathVariable Long messageId) {
        WxMessage message = wxMessageService.getMessageById(messageId);
        return Result.success(message);
    }
} 