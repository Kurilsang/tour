package com.tour.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tour.dao.TravelerMapper;
import com.tour.model.Traveler;
import com.tour.service.ITravelerService;
import com.tour.common.exception.ServiceException;
import com.tour.common.constant.ErrorCode;
import com.tour.common.util.IdCardUtil;
import com.tour.common.util.EncryptUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 出行人服务实现类
 *
 * @Author Abin
 */
@Service
@RequiredArgsConstructor
public class TravelerServiceImpl extends ServiceImpl<TravelerMapper, Traveler> implements ITravelerService {

    private final EncryptUtil encryptUtil;

    @Override
    public List<Traveler> getTravelersByOpenid(String openid) {
        List<Traveler> travelers = list(new LambdaQueryWrapper<Traveler>()
                .eq(Traveler::getOpenid, openid)
                .eq(Traveler::getIsDeleted, 0));
                
        // 解密并脱敏身份证号
        return travelers.stream()
                .peek(traveler -> {
                    String idCard = encryptUtil.decrypt(traveler.getIdCard());
                    traveler.setIdCard(IdCardUtil.desensitize(idCard));
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveTraveler(Traveler traveler) {
        // 参数校验
        validateTraveler(traveler);
        
        // 解析身份证信息
        String idCard = traveler.getIdCard();
        
        // 检查该用户是否已添加过相同身份证的联系人
        String encryptedIdCard = encryptUtil.encrypt(idCard);
        boolean exists = count(new LambdaQueryWrapper<Traveler>()
                .eq(Traveler::getOpenid, traveler.getOpenid())
                .eq(Traveler::getIdCard, encryptedIdCard)
                .eq(Traveler::getIsDeleted, 0)) > 0;
        if (exists) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "该出行人已存在");
        }
        
        // 设置生日
        traveler.setBirthday(IdCardUtil.getBirthday(idCard));
        // 如果未设置性别，从身份证中获取
        if (traveler.getGender() == null) {
            traveler.setGender(IdCardUtil.getGender(idCard));
        }
        
        // 加密身份证号
        traveler.setIdCard(encryptedIdCard);
        
        // 保存信息
        return save(traveler);
    }

    @Override
    public Traveler getTravelerByIdAndOpenid(String id, String openid) {
        Traveler traveler = getOne(new LambdaQueryWrapper<Traveler>()
                .eq(Traveler::getId, id)
                .eq(Traveler::getOpenid, openid)
                .eq(Traveler::getIsDeleted, 0));
                
        if (traveler == null) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权访问该出行人信息");
        }
        
        // 解密并脱敏身份证号
        String idCard = encryptUtil.decrypt(traveler.getIdCard());
        traveler.setIdCard(IdCardUtil.desensitize(idCard));
        return traveler;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTravelerByIdAndOpenid(String id, String openid) {
        // 查询是否存在该出行人信息
        Traveler traveler = getOne(new LambdaQueryWrapper<Traveler>()
                .eq(Traveler::getId, id)
                .eq(Traveler::getOpenid, openid)
                .eq(Traveler::getIsDeleted, 0));
                
        if (traveler == null) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权删除该出行人信息");
        }
        
        // 使用软删除方式，更新isDeleted字段和deleteTime字段
        boolean updated = update(new LambdaUpdateWrapper<Traveler>()
                .eq(Traveler::getId, id)
                .eq(Traveler::getOpenid, openid)
                .set(Traveler::getIsDeleted, 1)
                .set(Traveler::getDeleteTime, LocalDateTime.now()));
                
        if (!updated) {
            throw new ServiceException(ErrorCode.SYSTEM_ERROR, "删除出行人信息失败");
        }
        return true;
    }

    /**
     * 校验出行人信息
     *
     * @param traveler 出行人信息
     */
    private void validateTraveler(Traveler traveler) {
        if (traveler == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "出行人信息不能为空");
        }
        
        if (StrUtil.isBlank(traveler.getName())) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "姓名不能为空");
        }
        
        if (StrUtil.isBlank(traveler.getPhone())) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "手机号不能为空");
        }
        
        if (!traveler.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "手机号格式不正确");
        }
        
        if (StrUtil.isBlank(traveler.getIdCard())) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "身份证号不能为空");
        }
        
        // 校验身份证号是否有效
        if (!IdCardUtil.isValid(traveler.getIdCard())) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "身份证号无效");
        }
        
        if (traveler.getGender() == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "性别不能为空");
        }
        
        // 校验性别是否正确
        if (traveler.getGender() != 1 && traveler.getGender() != 2) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "性别格式不正确");
        }
        
        if (StrUtil.isBlank(traveler.getEmergencyName())) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "紧急联系人不能为空");
        }
        
        if (StrUtil.isBlank(traveler.getEmergencyPhone())) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "紧急联系电话不能为空");
        }
        
        if (!traveler.getEmergencyPhone().matches("^1[3-9]\\d{9}$")) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "紧急联系电话格式不正确");
        }
    }
} 