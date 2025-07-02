package com.tour.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.dao.AchievementMapper;
import com.tour.dao.ActivityOrderMapper;
import com.tour.dao.UserAchievementMapper;
import com.tour.dto.AchievementCreateDTO;
import com.tour.dto.AchievementUpdateDTO;
import com.tour.model.Achievement;
import com.tour.model.ActivityOrder;
import com.tour.model.UserAchievement;
import com.tour.service.FileService;
import com.tour.service.IAchievementService;
import com.tour.vo.AchievementVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tour.common.constant.Constants.MINICODE_VERSION;

/**
 * @Author Abin
 * @Description 成就服务实现类
 * @DateTime 2025/5/8 15:37
 */
@Slf4j
@Service
public class AchievementServiceImpl implements IAchievementService {

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private UserAchievementMapper userAchievementMapper;

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ActivityOrderMapper activityOrderMapper;

    // 日期时间格式化器
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 随机数生成器
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // 签到码字符集
    private static final String SIGN_IN_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // 签到码长度
    private static final int SIGN_IN_CODE_LENGTH = 16;

    @Override
    public List<AchievementVO> getAchievementList(String openid) {
        if (openid == null || openid.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }

        // 查询所有成就
        List<Achievement> achievements = achievementMapper.selectList(null);
        if (achievements == null || achievements.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询用户已获得的成就
        LambdaQueryWrapper<UserAchievement> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserAchievement::getOpenid, openid);
        List<UserAchievement> userAchievements = userAchievementMapper.selectList(wrapper);

        // 构建用户成就映射
        Map<Long, UserAchievement> userAchievementMap = new HashMap<>();
        if (userAchievements != null && !userAchievements.isEmpty()) {
            userAchievementMap = userAchievements.stream()
                    .collect(Collectors.toMap(UserAchievement::getAchievementId, ua -> ua));
        }

        // 组装成就VO列表
        Map<Long, UserAchievement> finalUserAchievementMap = userAchievementMap;
        return achievements.stream().map(achievement -> {
            AchievementVO vo = new AchievementVO();
            // 复制基本属性
            BeanUtils.copyProperties(achievement, vo);
            // 设置achievementId
            vo.setAchievementId(achievement.getId());

            // 判断用户是否已获得该成就
            UserAchievement userAchievement = finalUserAchievementMap.get(achievement.getId());
            if (userAchievement != null) {
                vo.setObtained(true);
                vo.setId(userAchievement.getId());
                // 检查 obtainTime 是否为 null
                if (userAchievement.getObtainTime() != null) {
                    vo.setObtainTime(userAchievement.getObtainTime().format(DATE_TIME_FORMATTER));
                } else {
                    vo.setObtainTime(null); // 如果为 null，设置为 null
                }
            } else {
                vo.setObtained(false);
                vo.setObtainTime(null);
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AchievementVO> getUserAchievements(String openid) {
        if (openid == null || openid.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }

        // 查询用户已获得的成就
        LambdaQueryWrapper<UserAchievement> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserAchievement::getOpenid, openid);
        List<UserAchievement> userAchievements = userAchievementMapper.selectList(wrapper);

        if (userAchievements == null || userAchievements.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取成就ID列表
        List<Long> achievementIds = userAchievements.stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toList());

        // 查询成就详情
        LambdaQueryWrapper<Achievement> achievementWrapper = Wrappers.lambdaQuery();
        achievementWrapper.in(Achievement::getId, achievementIds);
        List<Achievement> achievements = achievementMapper.selectList(achievementWrapper);

        if (achievements == null || achievements.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建成就ID到用户成就的映射
        Map<Long, UserAchievement> userAchievementMap = userAchievements.stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, ua -> ua));

        // 构建返回VO
        return achievements.stream().map(achievement -> {
            AchievementVO vo = new AchievementVO();
            // 复制基本属性
            BeanUtils.copyProperties(achievement, vo);
            vo.setAchievementId(achievement.getId());
            vo.setObtained(true);

            // 获取成就时间
            UserAchievement userAchievement = userAchievementMap.get(achievement.getId());
            if (userAchievement != null) {
                vo.setId(userAchievement.getId());
                vo.setObtainTime(userAchievement.getObtainTime().format(DATE_TIME_FORMATTER));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AchievementVO signInAchievement(String signInCode, String openid) {
        if (signInCode == null || signInCode.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "成就码不能为空");
        }
        if (openid == null || openid.isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "用户ID不能为空");
        }

        // 查询对应的成就
        LambdaQueryWrapper<Achievement> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Achievement::getSignInCode, signInCode);
        Achievement achievement = achievementMapper.selectOne(wrapper);

        if (achievement == null) {
            throw new ServiceException(ErrorCode.BIZ_ERROR, "无效的成就码");
        }

        // 如果成就关联了活动，则验证用户是否报名
        if (achievement.getActivityId() != null) {
            // 验证用户是否报名了活动
            LambdaQueryWrapper<ActivityOrder> orderWrapper = Wrappers.lambdaQuery();
            orderWrapper.eq(ActivityOrder::getActivityId, achievement.getActivityId())
                    .eq(ActivityOrder::getOpenid, openid)
                    .notIn(ActivityOrder::getStatus, 1, 3, 4); // 排除1-待支付 3-已取消 4-已过期
            List<ActivityOrder> activityOrders = activityOrderMapper.selectList(orderWrapper);

            if (activityOrders.isEmpty()) {
                throw new ServiceException(ErrorCode.BIZ_ERROR, "您尚未报名该活动，请先报名后再扫码");
            }
        }

        // 检查用户是否已获得该成就
        LambdaQueryWrapper<UserAchievement> userAchievementWrapper = Wrappers.lambdaQuery();
        userAchievementWrapper.eq(UserAchievement::getOpenid, openid)
                .eq(UserAchievement::getAchievementId, achievement.getId());
        UserAchievement existingAchievement = userAchievementMapper.selectOne(userAchievementWrapper);

        if (existingAchievement != null) {
            throw new ServiceException(ErrorCode.BIZ_ERROR, "您已获得该成就");
        }

        // 记录用户获得成就
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setOpenid(openid);
        userAchievement.setAchievementId(achievement.getId());
        userAchievement.setObtainTime(LocalDateTime.now());

        // 保存用户成就记录
        if (userAchievementMapper.insert(userAchievement) <= 0) {
            throw new ServiceException(ErrorCode.SYSTEM_ERROR, "保存用户成就记录失败");
        }

        // 构建返回的成就信息VO
        AchievementVO vo = new AchievementVO();
        // 复制基本属性
        BeanUtils.copyProperties(achievement, vo);
        // 设置用户成就相关信息
        vo.setAchievementId(achievement.getId());
        vo.setId(userAchievement.getId());
        vo.setObtained(true);
        // 检查 obtainTime 是否为 null
        if (userAchievement.getObtainTime() != null) {
            vo.setObtainTime(userAchievement.getObtainTime().format(DATE_TIME_FORMATTER));
        } else {
            vo.setObtainTime(null); // 如果为 null，设置为 null
        }

        return vo;
    }

    @Override
    public String generateWxaCode(Long achievementId, String page, String openid) {
        if (achievementId == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "成就ID不能为空");
        }

        // 查询成就信息
        Achievement achievement = achievementMapper.selectById(achievementId);
        if (achievement == null) {
            throw new ServiceException(ErrorCode.BIZ_ERROR, "成就不存在");
        }

        try {
            // 构建场景值，注意长度限制（最大32个可见字符）
            String scene = "code=" + achievement.getSignInCode();
            if (scene.length() > 32) {
                throw new ServiceException(ErrorCode.BIZ_ERROR, "场景值超过32个字符限制");
            }

            // 如果未指定页面，使用默认页面
            if (page == null || page.isEmpty()) {
                page = "pages/index/index";
            }

            // 生成小程序码临时文件
            File qrcodeFile = wxMaService.getQrcodeService().createWxaCodeUnlimit(scene, page,
                    false, MINICODE_VERSION, 430, true, null, true);

            try {
                // 直接使用File类型的上传方法
                String fileUrl = fileService.uploadFile(qrcodeFile, "achievement_minicode", openid).get("url");

                // 更新成就表中的signInUrl字段(可选)
                achievement.setSignInUrl(fileUrl);
                achievementMapper.updateById(achievement);

                return fileUrl;
            } finally {
                // 删除临时文件
                if (qrcodeFile != null && qrcodeFile.exists()) {
                    qrcodeFile.delete();
                }
            }
        } catch (Exception e) {
            log.error("生成小程序码失败：", e);
            throw new ServiceException(ErrorCode.SYSTEM_ERROR, "生成小程序码失败：" + e.getMessage());
        }
    }

    @Override
    public AchievementVO getAchievementById(Long achievementId, String openid) {
        if (achievementId == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "成就ID不能为空");
        }

        // 查询成就信息
        Achievement achievement = achievementMapper.selectById(achievementId);
        if (achievement == null) {
            throw new ServiceException(ErrorCode.BIZ_ERROR, "成就不存在");
        }

        // 构建成就VO
        AchievementVO vo = new AchievementVO();
        // 复制基本属性
        BeanUtils.copyProperties(achievement, vo);
        vo.setAchievementId(achievement.getId());

        // 如果提供了用户openid，检查用户是否已获得该成就
        if (openid != null && !openid.isEmpty()) {
            LambdaQueryWrapper<UserAchievement> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(UserAchievement::getOpenid, openid)
                    .eq(UserAchievement::getAchievementId, achievementId);
            UserAchievement userAchievement = userAchievementMapper.selectOne(wrapper);

            if (userAchievement != null) {
                vo.setObtained(true);
                vo.setId(userAchievement.getId());
                if (userAchievement.getObtainTime() != null) {
                    vo.setObtainTime(userAchievement.getObtainTime().format(DATE_TIME_FORMATTER));
                }
            } else {
                vo.setObtained(false);
            }
        } else {
            vo.setObtained(false);
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AchievementVO createAchievement(AchievementCreateDTO createDTO) {
        if (createDTO == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }
        if (createDTO.getTitle() == null || createDTO.getTitle().trim().isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "成就标题不能为空");
        }
        if (createDTO.getIconUrl() == null || createDTO.getIconUrl().trim().isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "图标URL不能为空");
        }
        if (createDTO.getDescription() == null || createDTO.getDescription().trim().isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "成就描述不能为空");
        }

        // 创建成就实体
        Achievement achievement = new Achievement();
        BeanUtils.copyProperties(createDTO, achievement);

        // 生成全局唯一的16位签到码
        String signInCode = generateUniqueSignInCode();
        achievement.setSignInCode(signInCode);

        // 设置创建时间
        achievement.setCreatedAt(LocalDateTime.now());

        // 保存成就
        if (achievementMapper.insert(achievement) <= 0) {
            throw new ServiceException(ErrorCode.SYSTEM_ERROR, "创建成就失败");
        }

        // 构建返回的VO
        AchievementVO vo = new AchievementVO();
        BeanUtils.copyProperties(achievement, vo);
        vo.setAchievementId(achievement.getId());
        vo.setObtained(false);

        return vo;
    }

    @Override
    public List<AchievementVO> getAllAchievementsForAdmin() {
        // 查询所有成就
        List<Achievement> achievements = achievementMapper.selectList(null);
        if (achievements == null || achievements.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为VO
        return achievements.stream().map(achievement -> {
            AchievementVO vo = new AchievementVO();
            BeanUtils.copyProperties(achievement, vo);
            vo.setAchievementId(achievement.getId());
            vo.setObtained(false); // 管理员视图不关心是否已获得
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 生成全局唯一的16位签到码
     *
     * @return 16位签到码
     */
    private String generateUniqueSignInCode() {
        String signInCode;
        boolean isUnique = false;

        // 循环生成直到找到唯一的签到码
        do {
            signInCode = generateRandomSignInCode();

            // 检查是否已存在
            LambdaQueryWrapper<Achievement> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(Achievement::getSignInCode, signInCode);
            Achievement existingAchievement = achievementMapper.selectOne(wrapper);

            isUnique = (existingAchievement == null);
        } while (!isUnique);

        return signInCode;
    }

    /**
     * 生成随机16位签到码
     *
     * @return 16位随机字符串
     */
    private String generateRandomSignInCode() {
        StringBuilder sb = new StringBuilder(SIGN_IN_CODE_LENGTH);
        for (int i = 0; i < SIGN_IN_CODE_LENGTH; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(SIGN_IN_CODE_CHARS.length());
            sb.append(SIGN_IN_CODE_CHARS.charAt(randomIndex));
        }
        return sb.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AchievementVO updateAchievement(AchievementUpdateDTO updateDTO) {
        if (updateDTO == null || updateDTO.getId() == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "参数不能为空");
        }
        if (updateDTO.getTitle() == null || updateDTO.getTitle().trim().isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "成就标题不能为空");
        }
        if (updateDTO.getIconUrl() == null || updateDTO.getIconUrl().trim().isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "图标URL不能为空");
        }
        if (updateDTO.getDescription() == null || updateDTO.getDescription().trim().isEmpty()) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "成就描述不能为空");
        }

        // 查询成就是否存在
        Achievement achievement = achievementMapper.selectById(updateDTO.getId());
        if (achievement == null) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "成就不存在");
        }

        // 更新成就信息
        achievement.setTitle(updateDTO.getTitle());
        achievement.setIconUrl(updateDTO.getIconUrl());
        achievement.setDescription(updateDTO.getDescription());
        achievement.setActivityId(updateDTO.getActivityId());

        // 保存更新
        if (achievementMapper.updateById(achievement) <= 0) {
            throw new ServiceException(ErrorCode.SYSTEM_ERROR, "更新成就失败");
        }

        // 构建返回的VO
        AchievementVO vo = new AchievementVO();
        BeanUtils.copyProperties(achievement, vo);
        vo.setAchievementId(achievement.getId());

        return vo;
    }

    @Override
    public AchievementVO getAchievementByActivityId(Long activityId, String openid) {
        if (activityId == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "活动ID不能为空");
        }

        // 查询关联活动ID的成就
        LambdaQueryWrapper<Achievement> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Achievement::getActivityId, activityId);
        Achievement achievement = achievementMapper.selectOne(wrapper);

        if (achievement == null) {
            throw new ServiceException(ErrorCode.RESOURCE_NOT_FOUND, "未找到与该活动关联的成就");
        }

        // 复用已有方法获取成就详情
        return getAchievementById(achievement.getId(), openid);
    }
}