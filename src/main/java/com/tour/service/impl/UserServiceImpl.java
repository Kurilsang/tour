package com.tour.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tour.common.constant.Constants;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.dao.UserMapper;
import com.tour.dto.UserRoleDTO;
import com.tour.enums.MediaCheckEnum;
import com.tour.enums.PageSize;
import com.tour.enums.RoleEnum;
import com.tour.model.User;
import com.tour.query.UserQuery;
import com.tour.service.IMediaSubmitService;
import com.tour.service.IMediaCheckExecutorService;
import com.tour.service.ITextCheckService;
import com.tour.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 * @author Abin
 */
@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private IMediaSubmitService mediaSubmitService;
    
    @Autowired
    private IMediaCheckExecutorService mediaCheckExecutorService;

    @Autowired
    private ITextCheckService textCheckService;
    
    @Override
    public User getUserByOpenid(String openid) {
        if (openid == null || openid.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "openid不能为空");
        }
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getOpenid, openid);
        return userMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(User user) {
        if (user == null || user.getOpenid() == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "用户信息不能为空");
        }
        
        // 获取当前用户信息，检查是否存在
        if (!userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getOpenid, user.getOpenid()))) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        
        // 检查昵称是否合规
        if (user.getNickname() != null && !user.getNickname().isEmpty() 
                && !Constants.DEFAULT_NICKNAME.equals(user.getNickname())) {
            boolean isNicknameValid = textCheckService.checkNickname(user.getNickname(), user.getOpenid());
            if (!isNicknameValid) {
                // 如果昵称不合规，返回错误
                log.warn("用户昵称不合规: {}, openid: {}", user.getNickname(), user.getOpenid());
                throw new ServiceException(ErrorCode.CONTENT_RISKY, "昵称包含敏感内容，请修改后重试");
            }
            log.info("用户昵称检测通过: {}, openid: {}", user.getNickname(), user.getOpenid());
        }
        
        // 保存用户头像的URL，用于后续异步检测
        String avatarUrl = null;
        if (user.getAvatar() != null && !user.getAvatar().isEmpty() && !Constants.DEFAULT_IMAGE_URL.equals(user.getAvatar())) {
            avatarUrl = user.getAvatar();
        }
        
        // 使用openid作为更新条件
        LambdaUpdateWrapper<User> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(User::getOpenid, user.getOpenid());
        boolean updateResult = userMapper.update(user, wrapper) > 0;
        
        // 数据库更新成功后，再进行异步图像检测
        if (updateResult && avatarUrl != null) {
            log.info("用户信息更新成功，开始异步检测头像，openid: {}, avatar: {}", user.getOpenid(), avatarUrl);
            // 使用异步执行服务检测头像
            mediaCheckExecutorService.asyncCheckUserAvatar(avatarUrl, user.getOpenid());
        }
        
        return updateResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        if (user == null || user.getOpenid() == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "用户信息不能为空");
        }
        userMapper.insert(user);
        return user;
    }
    
    @Override
    public IPage<User> queryUserList(UserQuery query) {
        if (query == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "查询参数不能为空");
        }
        
        // 设置默认分页参数
        if (query.getPageNo() == null) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null) {
            query.setPageSize(PageSize.SIZE10.getSize());
        }
        
        // 设置默认排序方向
        if (query.getSortDirection() == null) {
            query.setSortDirection("desc");
        }
        
        // 创建分页对象
        Page<User> page = new Page<>(query.getPageNo(), query.getPageSize());
        
        // 调用Mapper执行查询
        return userMapper.queryUserList(page, query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserRole(UserRoleDTO roleDTO, String operatorOpenid) {
        if (roleDTO == null || roleDTO.getOpenid() == null || roleDTO.getRole() == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "角色信息不能为空");
        }
        
        // 校验角色值是否合法
        boolean isValidRole = false;
        for (RoleEnum role : RoleEnum.values()) {
            if (role.getCode().equals(roleDTO.getRole())) {
                isValidRole = true;
                break;
            }
        }
        
        if (!isValidRole) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, 
                    "无效的角色值，应为user、admin或super_admin");
        }
        
        // 查询目标用户
        User targetUser = getUserByOpenid(roleDTO.getOpenid());
        if (targetUser == null) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        
        // 检查目标用户是否是超级管理员，如果是则不允许降级
        if (RoleEnum.SUPER_ADMIN.getCode().equals(targetUser.getRole()) && 
            !RoleEnum.SUPER_ADMIN.getCode().equals(roleDTO.getRole())) {
            log.warn("尝试降级超级管理员 [{}] 的权限被拒绝", targetUser.getNickname());
            throw new ServiceException(ErrorCode.FORBIDDEN, "不能降级超级管理员的权限");
        }
        
        // 查询操作者
        User operator = getUserByOpenid(operatorOpenid);
        if (operator == null) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "操作者不存在");
        }
        
        // 确认操作者是超级管理员
        if (!RoleEnum.SUPER_ADMIN.getCode().equals(operator.getRole())) {
            log.warn("非超级管理员 [{}] 尝试修改用户角色被拒绝", operator.getNickname());
            throw new ServiceException(ErrorCode.FORBIDDEN, "只有超级管理员可以修改用户角色");
        }
        
        // 执行角色更新
        LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate(User.class)
                .eq(User::getOpenid, roleDTO.getOpenid())
                .set(User::getRole, roleDTO.getRole());
        
        int result = userMapper.update(null, updateWrapper);
        
        if (result > 0) {
            log.info("超级管理员 [{}] 将用户 [{}] 的角色修改为 [{}]", 
                    operator.getNickname(), targetUser.getNickname(), roleDTO.getRole());
        }
        
        return result > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetUserProfile(String targetOpenid, String operatorOpenid) {
        if (targetOpenid == null || targetOpenid.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "目标用户openid不能为空");
        }
        
        if (operatorOpenid == null || operatorOpenid.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "操作者openid不能为空");
        }
        
        // 查询目标用户
        User targetUser = getUserByOpenid(targetOpenid);
        if (targetUser == null) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "目标用户不存在");
        }
        
        // 记录用户原资料用于日志
        String originalNickname = targetUser.getNickname();
        String originalAvatar = targetUser.getAvatar();
        
        // 如果用户已经是默认头像和昵称，则无需操作
        if (Constants.DEFAULT_NICKNAME.equals(targetUser.getNickname()) && 
            Constants.DEFAULT_IMAGE_URL.equals(targetUser.getAvatar())) {
            log.info("用户 [{}] 的头像和昵称已经是默认值，无需重置", targetOpenid);
            return true;
        }
        
        // 重置用户头像和昵称为默认值
        LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate(User.class)
                .eq(User::getOpenid, targetOpenid)
                .set(User::getNickname, Constants.DEFAULT_NICKNAME)
                .set(User::getAvatar, Constants.DEFAULT_IMAGE_URL);
        
        int result = userMapper.update(null, updateWrapper);
        
        if (result > 0) {
            log.info("管理员 [{}] 重置了用户 [{}] 的资料，昵称从 [{}] 重置为默认，头像从 [{}] 重置为默认", 
                    operatorOpenid, originalNickname, originalNickname, originalAvatar);
        }
        
        return result > 0;
    }
}