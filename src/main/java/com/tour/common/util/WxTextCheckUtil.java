package com.tour.common.util;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.security.WxMaMsgSecCheckCheckRequest;
import cn.binarywang.wx.miniapp.bean.security.WxMaMsgSecCheckCheckResponse;
import com.tour.common.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author Abin
 * @Description 微信文本内容安全检测工具类
 */
@Slf4j
@Component
public class WxTextCheckUtil {

    @Autowired
    private WxMaService wxMaService;
    
    /**
     * 检测文本内容是否合规
     * 
     * @param content 待检测的文本内容
     * @param openid 用户openid
     * @param scene 场景值，1-资料；2-评论；3-论坛；4-社交日志
     * @return 是否通过检测，true-通过，false-不通过
     */
    public boolean checkText(String content, String openid, Integer scene) {
        if (content == null || content.isEmpty()) {
            return Constants.TEXT_CHECK_DEFAULT_RESULT; // 空内容默认通过
        }
        
        if (scene == null || scene < 1 || scene > 4) {
            scene = Constants.TEXT_CHECK_DEFAULT_SCENE; // 默认使用场景1-资料
        }
        
        try {
            log.info("开始调用微信文本检测API，content: {}, openid: {}, scene: {}", 
                    content, openid, scene);
            
            // 构建请求对象
            WxMaMsgSecCheckCheckRequest request = WxMaMsgSecCheckCheckRequest.builder()
                    .content(content)
                    .openid(openid)
                    .scene(scene)
                    .version(String.valueOf(Constants.WX_MSG_CHECK_API_VERSION)) // 固定为2
                    .build();
            
            // 调用API
            WxMaMsgSecCheckCheckResponse result = wxMaService.getSecurityService().checkMessage(request);
            
            if (result != null) {
                boolean isPass = true;
                if (result.getResult() != null) {
                    // 检查建议
                    String suggest = result.getResult().getSuggest();
                    isPass = !Constants.TEXT_CHECK_RESULT_RISKY.equals(suggest);
                    
                    // 记录检测结果
                    String label = result.getResult().getLabel();
                    log.info("文本检测结果: content: {}, suggest: {}, label: {}, traceId: {}", 
                            content, suggest, label, result.getTraceId());
                    
                    if (!isPass) {
                        log.warn("文本内容不合规: {}, label: {}", content, label);
                    }
                }
                return isPass;
            } else {
                log.error("调用微信文本检测API失败, 返回结果为空");
                return Constants.TEXT_CHECK_DEFAULT_RESULT; // 检测失败时默认通过，避免影响正常业务
            }
        } catch (Exception e) {
            log.error("调用微信文本检测API异常", e);
            return Constants.TEXT_CHECK_DEFAULT_RESULT; // 检测异常时默认通过，避免影响正常业务
        }
    }
    
    /**
     * 带标题的文本检测方法，将标题和内容合并为一次检测
     * 
     * @param title 标题
     * @param content 内容
     * @param openid 用户openid
     * @param titleScene 标题检测场景
     * @param contentScene 内容检测场景
     * @return 是否通过检测，true-全部通过，false-任一不通过
     */
    public boolean checkTextWithTitle(String title, String content, String openid, Integer titleScene, Integer contentScene) {
        if ((title == null || title.isEmpty()) && (content == null || content.isEmpty())) {
            return Constants.TEXT_CHECK_DEFAULT_RESULT; // 标题和内容都为空，默认通过
        }
        
        try {
            // 确定使用哪个场景
            Integer scene = contentScene != null ? contentScene : titleScene;
            if (scene == null) {
                scene = Constants.TEXT_CHECK_DEFAULT_SCENE;
            }
            
            // 组合内容进行检测，如果标题和内容都存在，用换行符连接
            StringBuilder combinedContent = new StringBuilder();
            if (title != null && !title.isEmpty()) {
                combinedContent.append(title);
            }
            
            if (content != null && !content.isEmpty()) {
                if (combinedContent.length() > 0) {
                    combinedContent.append("\n");  // 用换行符分隔标题和内容
                }
                combinedContent.append(content);
            }
            
            log.info("开始调用微信文本检测API（合并检测），content: {}, openid: {}, scene: {}", 
                    combinedContent.toString(), openid, scene);
            
            // 构建请求对象
            WxMaMsgSecCheckCheckRequest request = WxMaMsgSecCheckCheckRequest.builder()
                    .content(combinedContent.toString())
                    .openid(openid)
                    .scene(scene)
                    .title(title)  // 保留标题参数，提高检测准确性
                    .version(String.valueOf(Constants.WX_MSG_CHECK_API_VERSION))
                    .build();
            
            // 调用API
            WxMaMsgSecCheckCheckResponse result = wxMaService.getSecurityService().checkMessage(request);
            
            if (result != null && result.getResult() != null) {
                String suggest = result.getResult().getSuggest();
                boolean isPass = !Constants.TEXT_CHECK_RESULT_RISKY.equals(suggest);
                
                String label = result.getResult().getLabel();
                log.info("合并内容检测结果: title: {}, content: {}, suggest: {}, label: {}, traceId: {}", 
                        title, content, suggest, label, result.getTraceId());
                
                if (!isPass) {
                    log.warn("内容不合规: title: {}, content: {}, label: {}", title, content, label);
                }
                
                return isPass;
            } else {
                log.error("调用微信文本检测API失败, 返回结果为空");
                return Constants.TEXT_CHECK_DEFAULT_RESULT; // 检测失败时默认通过，避免影响正常业务
            }
        } catch (Exception e) {
            log.error("调用微信文本联合检测API异常", e);
            return Constants.TEXT_CHECK_DEFAULT_RESULT; // 检测异常时默认通过，避免影响正常业务
        }
    }
    
    /**
     * 检测用户昵称是否合规
     * 
     * @param nickname 用户昵称
     * @param openid 用户openid
     * @return 是否通过检测，true-通过，false-不通过
     */
    public boolean checkNickname(String nickname, String openid) {
        return checkText(nickname, openid, Constants.TEXT_CHECK_SCENE_PROFILE); // 使用场景1-资料
    }
} 