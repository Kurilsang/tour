package com.tour.service;

import com.tour.model.User;
import java.util.Map;

/**
 * 微信认证服务接口
 */
public interface IWxAuthService {
    
    /**
     * 微信登录
     *
     * @param code 登录码
     * @return 登录结果
     */
    Map<String, Object> wxLogin(String code);
    
    /**
     * 云托管环境下的登录
     * 通过请求头中的OpenID等信息直接登录，无需code换取
     *
     * @param openId 用户OpenID
     * @param appId 小程序AppID
     * @param unionId 用户UnionID（如果有）
     * @return 登录结果
     */
    Map<String, Object> cloudHostingLogin(String openId, String appId, String unionId);
} 