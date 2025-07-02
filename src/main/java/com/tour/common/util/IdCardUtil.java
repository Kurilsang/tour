package com.tour.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

import java.util.Date;

/**
 * 身份证工具类
 *
 * @Author Abin
 */
@UtilityClass
public class IdCardUtil {

    /**
     * 校验身份证号是否有效
     *
     * @param idCard 身份证号
     * @return 是否有效
     */
    public boolean isValid(String idCard) {
        return IdcardUtil.isValidCard(idCard);
    }

    /**
     * 从身份证号中获取生日
     *
     * @param idCard 身份证号
     * @return 生日（格式：yyyy-MM-dd）
     */
    public String getBirthday(String idCard) {
        Date birthDate = IdcardUtil.getBirthDate(idCard);
        return DateUtil.format(birthDate, "yyyy-MM-dd");
    }

    /**
     * 从身份证号中获取性别
     * 
     * @param idCard 身份证号
     * @return 性别（1-男，2-女）
     */
    public Integer getGender(String idCard) {
        return IdcardUtil.getGenderByIdCard(idCard);
    }

    /**
     * 对身份证号进行脱敏处理
     * 规则：保留前3位（部分地区码）和出生日期部分（第7-14位），其他用*代替
     * 
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public String desensitize(String idCard) {
        if (StrUtil.isBlank(idCard)) {
            return idCard;
        }
        // 保留前4位（地区码）
        String prefix = idCard.substring(0, 3);
        // 保留出生日期部分（第7-14位）
        String birthday = idCard.substring(6, 14);
        // 第4-6位和后4位用*代替
        return prefix + "***" + birthday + "****";
    }
} 