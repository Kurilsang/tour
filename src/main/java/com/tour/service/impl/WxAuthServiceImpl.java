package com.tour.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.hutool.core.bean.BeanUtil;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.JwtUtil;
import com.tour.enums.RoleEnum;
import com.tour.model.User;
import com.tour.service.IUserService;
import com.tour.service.IWxAuthService;
import com.tour.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static com.tour.common.constant.Constants.DEFAULT_IMAGE_URL;
import static com.tour.common.constant.Constants.DEFAULT_NICKNAME;

/**
 * 微信认证服务实现类
 */
@Slf4j
@Service
public class WxAuthServiceImpl implements IWxAuthService {

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IUserService userService;

    /**
     * 默认用户角色
     */
    private static final String DEFAULT_ROLE = RoleEnum.USER.getCode();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> wxLogin(String code) {
        try {
            // 获取微信用户session信息
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
            if (session == null || session.getOpenid() == null) {
                throw new ServiceException(ErrorCode.BIZ_ERROR, "获取微信用户信息失败");
            }

            // 查询用户是否存在
            User user = userService.getUserByOpenid(session.getOpenid());
            
            // 不存在则创建新用户
            if (user == null) {
                user = new User();
                user.setOpenid(session.getOpenid());
                user.setUnionid(session.getUnionid());
                // 使用默认用户信息
                user.setNickname(DEFAULT_NICKNAME);
                user.setAvatar(DEFAULT_IMAGE_URL);
                user.setRole(DEFAULT_ROLE);
                user = userService.createUser(user);
            }
            // 已存在用户直接使用原有信息，不更新

            // 生成token，包含用户角色信息
            String token = jwtUtil.generateToken(session.getOpenid(), user.getRole());

            // 转换为VO对象
            UserVO userVO = new UserVO();
            BeanUtil.copyProperties(user, userVO);

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userInfo", userVO);

            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信登录处理失败", e);
            throw new ServiceException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cloudHostingLogin(String openId, String appId, String unionId) {
        try {
            log.info("云托管环境登录处理: openId={}, appId={}, unionId={}", openId, appId, unionId);
            
            // 查询用户是否存在
            User user = userService.getUserByOpenid(openId);
            
            // 不存在则创建新用户
            if (user == null) {
                log.info("创建新用户: openId={}", openId);
                user = new User();
                user.setOpenid(openId);
                user.setUnionid(unionId); // 可能为null
                
                // 使用默认用户信息
                user.setNickname(DEFAULT_NICKNAME);
                user.setAvatar(DEFAULT_IMAGE_URL);
                user.setRole(DEFAULT_ROLE);
                user = userService.createUser(user);
            } else {
                log.info("用户已存在: userId={}, nickname={}", user.getId(), user.getNickname());
                
                // 如果原来没有unionId但现在有了，则更新
                if ((user.getUnionid() == null || user.getUnionid().isEmpty()) && 
                    unionId != null && !unionId.isEmpty()) {
                    user.setUnionid(unionId);
                    // 调用更新方法，但不要尝试赋值回user变量
                    userService.updateUser(user);
                }
            }

            // 生成token，包含用户角色信息
            String token = jwtUtil.generateToken(openId, user.getRole());

            // 转换为VO对象
            UserVO userVO = new UserVO();
            BeanUtil.copyProperties(user, userVO);

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userInfo", userVO);

            log.info("云托管环境登录成功: userId={}, nickname={}", user.getId(), user.getNickname());
            return result;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("云托管环境登录处理失败", e);
            throw new ServiceException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        }
    }
} 