package com.tour.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author Abin
 * @Description 出行人信息类
 * @DateTime 2025/5/9 13:41
 */
@Data
@TableName("traveler")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Traveler implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    /**
     * id
     */
    private Long id;

    /**
     * 关联用户的openid
     */
    private String openid;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 1-男 2-女
     */
    private Integer gender;

    /**
     * 加密后的身份证号
     */
    private String idCard;

    /**
     * 根据身份证号解析
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private String birthday;

    /**
     * 紧急联系人
     */
    private String emergencyName;

    /**
     * 紧急联系电话
     */
    private String emergencyPhone;

    /**
     * 称呼方式
     */
    private String nickname;
    
    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDeleted;
    
    /**
     * 删除时间
     */
    private LocalDateTime deleteTime;
}