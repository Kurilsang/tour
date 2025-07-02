package com.tour.common.util;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaMediaAsyncCheckResult;
import cn.binarywang.wx.miniapp.bean.security.WxMaMediaSecCheckCheckRequest;
import com.tour.common.constant.Constants;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.enums.MediaCheckEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author Abin
 * @Description 微信媒体安全检测工具类
 */
@Slf4j
@Component
public class WxMediaCheckUtil {

    @Autowired
    private WxMaService wxMaService;
    
    @Value("${cos.bucket}")
    private String bucket;

    /**
     * 调用微信检测API检测媒体内容
     *
     * @param mediaUrl     媒体URL
     * @param mediaType    媒体类型：1-音频，2-图片
     * @param businessType 业务类型：user_avatar、wish_image、comment_image等
     * @param businessId   业务ID
     * @param userOpenid   用户openid
     * @return 追踪ID
     */
    public String checkMedia(String mediaUrl, Integer mediaType, String businessType, String businessId, String userOpenid) {
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "媒体URL不能为空");
        }
        
        if (mediaType == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "媒体类型不能为空");
        }
        
        try {
            // 处理cloud://开头的fileid格式的URL
            String processedMediaUrl = convertCloudFileIdToHttpUrl(mediaUrl);
            
            // 调用微信小程序API进行异步检测
            log.info("开始调用微信媒体检测API，mediaUrl: {}, processedUrl: {}, mediaType: {}, businessType: {}, businessId: {}", 
                    mediaUrl, processedMediaUrl, mediaType, businessType, businessId);
            
            // 根据业务类型确定场景值
            int scene = getSceneByBusinessType(businessType);
            
            // 构建请求对象
            WxMaMediaSecCheckCheckRequest request = WxMaMediaSecCheckCheckRequest.builder()
                    .mediaUrl(processedMediaUrl)
                    .mediaType(mediaType)
                    .version(Constants.WX_MEDIA_CHECK_API_VERSION) // 固定为2
                    .openid(userOpenid)
                    .scene(scene)
                    .build();
            
            // 调用API
            WxMaMediaAsyncCheckResult result = wxMaService.getSecurityService().mediaCheckAsync(request);
            
            if (result != null && result.getTraceId() != null) {
                log.info("调用微信媒体检测API成功, traceId: {}, businessType: {}, businessId: {}", 
                        result.getTraceId(), businessType, businessId);
                return result.getTraceId();
            } else {
                log.error("调用微信媒体检测API失败, 返回结果为空");
                throw new ServiceException(ErrorCode.SYSTEM_ERROR, "调用微信媒体检测API失败");
            }
        } catch (Exception e) {
            log.error("调用微信媒体检测API异常", e);
            throw new ServiceException(ErrorCode.SYSTEM_ERROR, "调用微信媒体检测API异常: " + e.getMessage());
        }
    }
    
    /**
     * 转换cloud://开头的fileid格式为http URL
     * 
     * @param mediaUrl 原始媒体URL
     * @return 转换后的HTTP URL
     */
    private String convertCloudFileIdToHttpUrl(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            return mediaUrl;
        }
        
        // 检查是否为cloud://格式的URL
        if (mediaUrl.startsWith("cloud://")) {
            try {
                // 提取云环境ID和文件路径
                // 格式：cloud://[环境ID].[存储桶名]-[环境ID]-[数字]/[文件路径]
                Pattern pattern = Pattern.compile("cloud://([^.]+)\\.([^/]+)/(.+)");
                Matcher matcher = pattern.matcher(mediaUrl);
                
                if (matcher.find()) {
                    // 存储桶名和文件路径
                    String filePath = matcher.group(3);
                    
                    // 构建http格式的URL: https://[存储桶名称].tcb.qcloud.la/[文件路径]
                    String httpUrl = "https://" + bucket + ".tcb.qcloud.la/" + filePath;
                    log.info("已将cloud格式URL转换为HTTP格式: {} -> {}", mediaUrl, httpUrl);
                    return httpUrl;
                } else {
                    log.warn("无法解析cloud格式的URL: {}", mediaUrl);
                }
            } catch (Exception e) {
                log.error("转换cloud URL格式异常", e);
            }
        }
        
        // 如果不是cloud格式或转换失败，则返回原URL
        return mediaUrl;
    }
    
    /**
     * 根据业务类型获取对应的场景值
     * 
     * @param businessType 业务类型
     * @return 场景值
     */
    private int getSceneByBusinessType(String businessType) {
        if (MediaCheckEnum.BusinessType.USER_AVATAR.getCode().equals(businessType)) {
            return MediaCheckEnum.Scene.PROFILE.getCode();  // 1:资料
        } else if (MediaCheckEnum.BusinessType.COMMENT_IMAGE.getCode().equals(businessType)) {
            return MediaCheckEnum.Scene.COMMENT.getCode();  // 2:评论
        } else if (MediaCheckEnum.BusinessType.WISH_IMAGE.getCode().equals(businessType)) {
            return MediaCheckEnum.Scene.FORUM.getCode();    // 3:论坛
        } else {
            return MediaCheckEnum.Scene.PROFILE.getCode();  // 默认为资料
        }
    }
} 