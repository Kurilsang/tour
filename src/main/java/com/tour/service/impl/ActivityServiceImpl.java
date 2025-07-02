package com.tour.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tour.common.constant.Constants;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.CopyTools;
import com.tour.dao.*;
import com.tour.dto.ActivityDTO;
import com.tour.enums.ActivityOrderStatusEnum;
import com.tour.enums.ActivityStatusEnum;
import com.tour.enums.PageSize;
import com.tour.model.*;
import com.tour.query.ActivityQuery;
import com.tour.service.ActivityService;
import com.tour.service.FileService;
import com.tour.vo.ActivityOrderDetail;
import com.tour.vo.BusLocationVO;
import com.tour.vo.SignUpListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import static com.tour.common.constant.Constants.MINICODE_VERSION;
import static com.tour.common.constant.Constants.defaultEndActivitySold;

/**
 * @Author Kuril
 * @Description 活动服务实现类
 * @DateTime 2025/5/12 12:25
 */
@Service("activityService")
@Slf4j
public class ActivityServiceImpl implements ActivityService {
    @Autowired
    ActivityMapper activityMapper;
    @Autowired
    private ActivityDetailMapper activityDetailMapper;
    @Autowired
    private LocationMapper locationMapper;
    @Autowired
    private EnrollmentMapper enrollmentMapper;
    @Autowired
    private ActivityOrderMapper activityOrderMapper;
    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private FileServiceImpl fileServiceImpl;

    // 日期时间格式化器
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public List<Activity> loadDataList() {
        return activityMapper.selectList(new QueryWrapper<Activity>());
    }

    @Override
    @Transactional
    public Long addActivity(ActivityDTO activityDTO) {
        // 校验价格
        validateActivityPrice(activityDTO);
        
        activityDTO.setReservedEarlyBird(Constants.defaultReserveEarlyBird);
        activityDTO.setReservedNormal(Constants.defaultReserveNormal);
        // 前端设置的内容进行复制
        Activity activity = CopyTools.copy(activityDTO, Activity.class);

        // 后端设置
        // 设置总销量，默认设置为 0
        activity.setTotalSold(0);
        // 设置创建时间为当前时间
        // 如果没有传入报名截止时间
        if (activity.getSignEndTime() == null) {
            activity.setSignEndTime(LocalDateTime.now().minusHours(Constants.defaultSignEndTime));
        }
        // 如果没有传入可退款截止时间，默认设置为活动开始时间
        if (activity.getEndRefundTime() == null) {
            activity.setEndRefundTime(activity.getStartTime());
        }
        activity.setCreateTime(LocalDateTime.now());
        if (activity.getSignEndTime().isAfter(activity.getStartTime())) {
            throw new ServiceException("报名截止时间不能比活动开始时间还晚");
        }

        // 处理上车点信息
        if (activityDTO.getBusLocations() != null && !activityDTO.getBusLocations().isEmpty()) {
            try {
                // 使用Fastjson将上车点列表转为JSON字符串
                String busLocationsJson = com.alibaba.fastjson.JSON.toJSONString(activityDTO.getBusLocations());
                activity.setBusLocations(busLocationsJson);
            } catch (Exception e) {
                log.error("处理上车点信息失败", e);
                throw new ServiceException("处理上车点信息失败：" + e.getMessage());
            }
        }

        // 插入 Activity 记录到数据库
        activityMapper.insert(activity);

        // 获取插入后生成的活动 ID
        Long activityId = activity.getId();
        log.info("生成ID{}", activityId);
        
        // 生成活动小程序码并保存URL
        try {
            // 默认使用首页路径
            String page = "pages/index/index";
            // 获取创建人的openid作为上传者
            String openid = activity.getCreatedBy();
            String minicodeUrl = generateActivityWxaCode(activityId, page, openid);
            
            // 更新活动表，保存小程序码URL
            Activity updateActivity = new Activity();
            updateActivity.setId(activityId);
            updateActivity.setMinicode(minicodeUrl);
            activityMapper.updateById(updateActivity);
            
            log.info("活动小程序码生成成功，URL: {}", minicodeUrl);
        } catch (Exception e) {
            log.error("生成活动小程序码失败", e);
            // 这里选择继续执行，不影响活动创建的主流程
        }
        
        // 初始化 ActivityDetail 对象
        // 前端内容设置
        ActivityDetail activityDetail = CopyTools.copy(activityDTO, ActivityDetail.class);

        // 后端设置
        // 设置关联的活动 ID，使用插入 Activity 后生成的 ID
        activityDetail.setActivityId(activityId);

        // 插入 ActivityDetail 记录到数据库
        activityDetailMapper.insert(activityDetail);

        // 对地址信息处理
        Location location = activityDTO.getLocation();
        if (location != null) {
            if (location.getId() == null) {
                location.setId(null);
            }
            location.setCreateTime(LocalDateTime.now());
            location.setUpdatedTime(LocalDateTime.now());
            location.setLocationActivityId(activityId);
            location.setLocationProductId(null);
            log.info("这是location{}", location.toString());
            locationMapper.insert(location);
        }

        // 返回活动ID
        return activityId;
    }

    @Override
    public void deleteActivityById(int id) {
        activityMapper.deleteById(id);
        // 删除关联地址
        QueryWrapper<Location> locationQueryWrapper = new QueryWrapper<>();
        locationQueryWrapper.eq("location_activity_id", id);
        locationMapper.delete(locationQueryWrapper);

    }

    @Override
    @Transactional
    public void updateActivityById(Long id, ActivityDTO activityDTO) {
        // 校验价格
        validateActivityPrice(activityDTO);
        
        activityDTO.setReservedEarlyBird(null);
        activityDTO.setReservedNormal(null);
        // 更新活动表
        Activity activity = CopyTools.copy(activityDTO, Activity.class);
        activity.setId(id);
        activity.setCreatedBy(null);
        if (activity.getSignEndTime().isAfter(activity.getStartTime())) {
            throw new ServiceException("报名截止时间不能比活动开始时间还晚");
        }

        // 处理上车点信息
        if (activityDTO.getBusLocations() != null && !activityDTO.getBusLocations().isEmpty()) {
            try {
                // 使用Fastjson将上车点列表转为JSON字符串
                String busLocationsJson = com.alibaba.fastjson.JSON.toJSONString(activityDTO.getBusLocations());
                activity.setBusLocations(busLocationsJson);
            } catch (Exception e) {
                log.error("处理上车点信息失败", e);
                throw new ServiceException("处理上车点信息失败：" + e.getMessage());
            }
        }

        activityMapper.updateById(activity);
        
        // 检查是否需要更新或生成小程序码
        Activity existingActivity = activityMapper.selectById(id);
        if (existingActivity.getMinicode() == null || existingActivity.getMinicode().isEmpty()) {
            try {
                // 默认使用首页路径
                String page = "pages/index/index";
                // 获取更新人的openid作为上传者
                String openid = activity.getUpdatedBy();
                String minicodeUrl = generateActivityWxaCode(id, page, openid);
                
                // 更新活动表，保存小程序码URL
                Activity updateActivity = new Activity();
                updateActivity.setId(id);
                updateActivity.setMinicode(minicodeUrl);
                activityMapper.updateById(updateActivity);
                
                log.info("活动小程序码生成并更新成功，URL: {}", minicodeUrl);
            } catch (Exception e) {
                log.error("生成活动小程序码失败", e);
                // 这里选择继续执行，不影响活动更新的主流程
            }
        }

        // 更新活动详情表
        QueryWrapper<ActivityDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activity_id", id);

        ActivityDetail newActivityDetail = CopyTools.copy(activityDTO, ActivityDetail.class);
        activityDetailMapper.update(newActivityDetail, queryWrapper);

        // 更新地址
        QueryWrapper<Location> locationQueryWrapper = new QueryWrapper<>();
        locationQueryWrapper.eq("location_activity_id", id);

        Location newLocation = activityDTO.getLocation();
        newLocation.setId(null);
        newLocation.setUpdatedTime(LocalDateTime.now());
        locationMapper.update(newLocation, locationQueryWrapper);
    }

    /**
     * 验证活动价格
     * 
     * @param activityDTO 活动DTO
     * @throws ServiceException 价格不符合规则时抛出异常
     */
    private void validateActivityPrice(ActivityDTO activityDTO) {
        // 1. 检查早鸟价
        BigDecimal earlyBirdPrice = activityDTO.getEarlyBirdPrice();
        if (earlyBirdPrice == null) {
            throw new ServiceException("早鸟价不能为空");
        }
        
        // 检查价格不为0且不小于0.01
        if (earlyBirdPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("早鸟价不能小于或等于0");
        }
        
        if (earlyBirdPrice.compareTo(new BigDecimal("0.01")) < 0) {
            throw new ServiceException("早鸟价最小为0.01");
        }
        
        // 检查价格小数位数不超过2位
        if (earlyBirdPrice.scale() > 2) {
            throw new ServiceException("早鸟价小数位不能超过2位");
        }
        
        // 2. 检查普通价
        BigDecimal normalPrice = activityDTO.getNormalPrice();
        if (normalPrice == null) {
            throw new ServiceException("普通价不能为空");
        }
        
        // 检查价格不为0且不小于0.01
        if (normalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("普通价不能小于或等于0");
        }
        
        if (normalPrice.compareTo(new BigDecimal("0.01")) < 0) {
            throw new ServiceException("普通价最小为0.01");
        }
        
        // 检查价格小数位数不超过2位
        if (normalPrice.scale() > 2) {
            throw new ServiceException("普通价小数位不能超过2位");
        }
        
        // 3. 检查可退款截止时间
        LocalDateTime endRefundTime = activityDTO.getEndRefundTime();
        if (endRefundTime == null) {
            throw new ServiceException("可退款截止时间不能为空");
        }
        
        // 检查可退款截止时间是否在活动开始时间之前
        LocalDateTime startTime = activityDTO.getStartTime();
        if (startTime != null && !endRefundTime.isBefore(startTime)) {
            throw new ServiceException("可退款截止时间必须在活动开始时间之前");
        }
        
        // 4. 检查群二维码URL是否为空
        String groupQrcode = activityDTO.getGroupQrcode();
        if (groupQrcode == null || groupQrcode.trim().isEmpty()) {
            throw new ServiceException("活动群二维码URL不能为空");
        }
    }

    @Override
    public Object selectActivityById(Long id) {

        Activity activity = activityMapper.selectById(id);

        ActivityDTO activityDTO = CopyTools.copy(activity, ActivityDTO.class);
        QueryWrapper<ActivityDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activity_id", id);
        ActivityDetail activityDetail = activityDetailMapper.selectOne(queryWrapper);
        activityDTO.setContent(activityDetail.getContent());
        activityDTO.setSortOrder(activityDetail.getSortOrder());
        // 查找地图信息
        QueryWrapper<Location> locationQueryWrapper = new QueryWrapper<>();
        locationQueryWrapper.eq("location_activity_id", id);
        Location location = locationMapper.selectOne(locationQueryWrapper);
        activityDTO.setLocation(location);

        // 处理上车点信息
        if (activity.getBusLocations() != null && !activity.getBusLocations().isEmpty()) {
            try {
                // 将JSON字符串转换为上车点列表
                List<BusLocationVO> busLocationList = com.alibaba.fastjson.JSON.parseArray(activity.getBusLocations(),
                        BusLocationVO.class);
                activityDTO.setBusLocations(busLocationList);
            } catch (Exception e) {
                log.error("解析上车点信息失败", e);
                // 解析失败时，返回空列表
                activityDTO.setBusLocations(new java.util.ArrayList<>());
            }
        }

        return activityDTO;
    }

    /**
     * @Author Kuril
     * @Description 用户查询活动详情，只允许查看已发布状态的活动
     * @param id 活动ID
     * @return 活动详情数据
     */
    @Override
    public Object UserSelectActivityById(Long id) {

        Activity activity = activityMapper.selectById(id);

        // 校验是否请求返回的status为发布状态
        if (!activity.getStatus().equals(ActivityStatusEnum.PUBLISHED.getCode())
                && !activity.getStatus().equals(ActivityStatusEnum.IN_PROGRESS.getCode())
                && !activity.getStatus().equals(ActivityStatusEnum.FINISHED.getCode())) {
            throw new ServiceException("用户请求的数据不为可查看状态");
        }

        ActivityDTO activityDTO = CopyTools.copy(activity, ActivityDTO.class);
        QueryWrapper<ActivityDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activity_id", id);
        ActivityDetail activityDetail = activityDetailMapper.selectOne(queryWrapper);
        activityDTO.setContent(activityDetail.getContent());
        activityDTO.setSortOrder(activityDetail.getSortOrder());
        // 查找地图信息
        QueryWrapper<Location> locationQueryWrapper = new QueryWrapper<>();
        locationQueryWrapper.eq("location_activity_id", id);
        Location location = locationMapper.selectOne(locationQueryWrapper);
        activityDTO.setLocation(location);

        // 处理上车点信息
        if (activity.getBusLocations() != null && !activity.getBusLocations().isEmpty()) {
            try {
                // 将JSON字符串转换为上车点列表
                List<BusLocationVO> busLocationList = com.alibaba.fastjson.JSON.parseArray(activity.getBusLocations(),
                        BusLocationVO.class);
                activityDTO.setBusLocations(busLocationList);
            } catch (Exception e) {
                log.error("解析上车点信息失败", e);
                // 解析失败时，返回空列表
                activityDTO.setBusLocations(new java.util.ArrayList<>());
            }
        }

        return activityDTO;
    }

    @Override
    public IPage<Activity> findListByPage(ActivityQuery activityQuery) {
        // 构建分页参数
        Integer pageNo = activityQuery.getPageNo();
        Integer pageSize = activityQuery.getPageSize();
        if (pageNo == null) {
            pageNo = Constants.defaultPageNo;
        }
        if (pageSize == null) {
            pageSize = PageSize.SIZE10.getSize();
        }
        Page<Activity> page = new Page<>(pageNo, pageSize);

        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();

        // 根据 ActivityQuery 中的属性动态添加查询条件
        if (activityQuery.getId() != null) {
            queryWrapper.eq("id", activityQuery.getId());
        }
        if (activityQuery.getTitle() != null && !activityQuery.getTitle().isEmpty()) {
            queryWrapper.like("title", activityQuery.getTitle());
        }
        if (activityQuery.getCoverImage() != null && !activityQuery.getCoverImage().isEmpty()) {
            queryWrapper.eq("cover_image", activityQuery.getCoverImage());
        }
        if (activityQuery.getActivityPosition() != null && !activityQuery.getActivityPosition().isEmpty()) {
            queryWrapper.eq("activity_position", activityQuery.getActivityPosition());
        }
        if (activityQuery.getEarlyBirdPrice() != null) {
            queryWrapper.eq("early_bird_price", activityQuery.getEarlyBirdPrice());
        }
        if (activityQuery.getNormalPrice() != null) {
            queryWrapper.eq("normal_price", activityQuery.getNormalPrice());
        }
        if (activityQuery.getEarlyBirdQuota() != null) {
            queryWrapper.eq("early_bird_quota", activityQuery.getEarlyBirdQuota());
        }
        if (activityQuery.getNormalQuota() != null) {
            queryWrapper.eq("normal_quota", activityQuery.getNormalQuota());
        }
        if (activityQuery.getReservedEarlyBird() != null) {
            queryWrapper.eq("reserved_early_bird", activityQuery.getReservedEarlyBird());
        }
        if (activityQuery.getReservedNormal() != null) {
            queryWrapper.eq("reserved_normal", activityQuery.getReservedNormal());
        }
        if (activityQuery.getTotalSold() != null) {
            queryWrapper.eq("total_sold", activityQuery.getTotalSold());
        }
        if (activityQuery.getSignEndTime() != null) {
            queryWrapper.ge("sign_end_time", activityQuery.getSignEndTime());
        }
        if (activityQuery.getStartTime() != null) {
            queryWrapper.ge("start_time", activityQuery.getStartTime());
        }
        if (activityQuery.getStartTime() != null) {
            queryWrapper.ge("start_time", activityQuery.getStartTime());
        }
        if (activityQuery.getEndTime() != null) {
            queryWrapper.le("end_time", activityQuery.getEndTime());
        }
        if (activityQuery.getCreateTime() != null) {
            queryWrapper.ge("create_time", activityQuery.getCreateTime());
        }
        if (activityQuery.getCreatedBy() != null) {
            queryWrapper.eq("created_by", activityQuery.getCreatedBy());
        }
        if (activityQuery.getUpdatedBy() != null) {
            queryWrapper.eq("updated_by", activityQuery.getUpdatedBy());
        }
        if (activityQuery.getStatus() != null) {
            queryWrapper.eq("status", activityQuery.getStatus());
        }
        if (activityQuery.getGroupQrcode() != null && !activityQuery.getGroupQrcode().isEmpty()) {
            queryWrapper.eq("group_qrcode", activityQuery.getGroupQrcode());
        }

        // 处理排序信息
        String orderBy = activityQuery.getOrderBy();
        log.info("{}内容", orderBy);
        queryWrapper.last("ORDER BY " + orderBy);

        return activityMapper.selectPage(page, queryWrapper);
    }

    /**
     * @Author Kuril
     * @Description 用户查询活动列表，只返回已发布状态的活动
     * @param activityQuery 查询条件
     * @return 分页活动列表
     */
    @Override
    public IPage<Activity> UserFindListByPage(ActivityQuery activityQuery) {
        // 只给用户返回已经发布的数据
        activityQuery.setStatus(ActivityStatusEnum.PUBLISHED.getCode());
        return findListByPage(activityQuery);
    }

    /**
     * @Author Kuril
     * @Description 切换活动状态
     * @param id     活动ID
     * @param status 目标状态码
     */
    @Override
    public void switchActivityStatusById(Long id, Integer status) {
        if (!status.equals(ActivityStatusEnum.PUBLISHED.getCode())
                && !status.equals(ActivityStatusEnum.CLOSED.getCode())
                && !status.equals(ActivityStatusEnum.IN_PROGRESS.getCode())) {
            throw new ServiceException("请求状态不存在");
        }
        Activity activity = new Activity();
        activity.setId(id);
        activity.setStatus(status);
        activityMapper.updateById(activity);
    }

    /**
     * @Author Kuril
     * @Description 定时任务：自动更新活动状态
     *              每5分钟执行一次，检查活动时间并更新状态
     *              - 当前时间在start_time和end_time之间，更新为进行中状态
     *              - 当前时间晚于end_time，更新为已关闭状态
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void autoUpdateActivityStatus() {
        log.info("开始执行活动状态自动更新定时任务...");
        LocalDateTime now = LocalDateTime.now();

        // 查询所有非关闭状态的活动
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", ActivityStatusEnum.CLOSED.getCode());
        List<Activity> activities = activityMapper.selectList(queryWrapper);

        for (Activity activity : activities) {
            // 已经结束的活动，更新为已关闭状态
            if (now.isAfter(activity.getEndTime())) {
                // 改为手动关闭，不然容易出bug
                // if (!activity.getStatus().equals(ActivityStatusEnum.CLOSED.getCode())) {
                // log.info("活动ID: {} 已结束，更新为已关闭状态", activity.getId());
                // activity.setStatus(ActivityStatusEnum.CLOSED.getCode());
                // activityMapper.updateById(activity);
                // }
            }
            // 正在进行的活动，更新为进行中状态
            else if (now.isAfter(activity.getStartTime()) && now.isBefore(activity.getEndTime())) {
                if (!activity.getStatus().equals(ActivityStatusEnum.IN_PROGRESS.getCode())) {
                    log.info("活动ID: {} 正在进行中，更新为进行中状态", activity.getId());
                    activity.setStatus(ActivityStatusEnum.IN_PROGRESS.getCode());
                    activityMapper.updateById(activity);
                }
            }
        }
        log.info("活动状态自动更新定时任务执行完毕");
    }

    /**
     * @Author Kuril
     * @Description 完成活动报名处理
     * @param signUpListVOList 报名列表数据集合
     * @return 处理结果
     */
    // @Override
    // @Transactional(rollbackFor = Exception.class)
    // public boolean completeActivitySignUp(List<SignUpListVO> signUpListVOList) {
    // if (signUpListVOList == null || signUpListVOList.isEmpty()) {
    // log.warn("传入的报名列表为空，无法处理");
    // return false;
    // }
    //
    // try {
    // // 记录处理的活动ID，用于最后更新活动状态
    // Long activityId = null;
    // // 用于记录需要回滚的早鸟票和普通票数量
    // int totalEarlyBirdTickets = 0;
    // int totalNormalTickets = 0;
    //
    // for (SignUpListVO signUpListVO : signUpListVOList) {
    // Long enrollmentId = signUpListVO.getId();
    //
    // // 1. 根据enrollmentId查询对应的orderNo
    // QueryWrapper<Enrollment> queryWrapper = new QueryWrapper<>();
    // queryWrapper.eq("id", enrollmentId);
    // Enrollment enrollment = enrollmentMapper.selectOne(queryWrapper);
    //
    // if (enrollment == null) {
    // log.warn("未找到ID为{}的报名信息", enrollmentId);
    // continue;
    // }
    //
    // String orderNo = enrollment.getOrderNo();
    // activityId = enrollment.getActivityId();
    //
    // // 2. 根据orderNo查询订单信息，获取早鸟票和普通票数量
    // QueryWrapper<ActivityOrder> orderQuery = new QueryWrapper<>();
    // orderQuery.eq("order_no", orderNo);
    // ActivityOrder order = activityOrderMapper.selectOne(orderQuery);
    //
    // if (order != null) {
    // // 累计需要回滚的票数
    // totalEarlyBirdTickets += order.getEarlyBirdNum();
    // totalNormalTickets += order.getNormalNum();
    //
    // // 3. 更新订单状态为已完成
    // ActivityOrder activityOrder = new ActivityOrder();
    // activityOrder.setStatus(ActivityOrderStatusEnum.COMPLETED.getStatus()); //
    // 设置为已完成状态
    // int orderUpdateResult = activityOrderMapper.update(activityOrder,
    // orderQuery);
    // log.info("更新订单[{}]状态为已完成，结果：{}", orderNo, orderUpdateResult > 0);
    // }
    //
    // // 4. 删除报名数据
    // int enrollmentDeleteResult = enrollmentMapper.deleteById(enrollmentId);
    // log.info("删除报名信息ID[{}]，结果：{}", enrollmentId, enrollmentDeleteResult > 0);
    // }
    //
    // // 5. 如果找到了活动ID，则更新活动状态为已关闭，并回滚库存和销量
    // if (activityId != null) {
    // // 回滚库存和销量
    // Activity activity = activityMapper.selectById(activityId);
    // if (activity != null) {
    // // 回滚库存和销量
    // activity.setEarlyBirdQuota(activity.getEarlyBirdQuota() +
    // totalEarlyBirdTickets);
    // activity.setNormalQuota(activity.getNormalQuota() + totalNormalTickets);
    // if (defaultEndActivitySold) {
    // activity.setTotalSold(activity.getTotalSold() - totalEarlyBirdTickets -
    // totalNormalTickets);
    // }
    // activity.setStatus(ActivityStatusEnum.CLOSED.getCode());
    //
    // int activityUpdateResult = activityMapper.updateById(activity);
    // log.info("更新活动ID[{}]状态为已关闭，并回滚库存(早鸟票:{}，普通票:{})和销量，结果：{}",
    // activityId, totalEarlyBirdTickets, totalNormalTickets, activityUpdateResult >
    // 0);
    // }
    // }
    //
    // return true;
    // } catch (Exception e) {
    // log.error("处理完成活动报名过程中发生错误", e);
    // throw new ServiceException("处理完成活动报名失败：" + e.getMessage());
    // }
    // }

    /**
     * @Author Abin
     * @Description 获取可用于轮播图的活动列表（已发布和进行中）
     * @param activityQuery 查询条件
     * @return 分页活动列表
     */
    @Override
    public IPage<Activity> findAvailableActivities(ActivityQuery activityQuery) {
        // 构建分页参数
        Integer pageNo = activityQuery.getPageNo();
        Integer pageSize = activityQuery.getPageSize();
        if (pageNo == null) {
            pageNo = Constants.defaultPageNo;
        }
        if (pageSize == null) {
            pageSize = PageSize.SIZE10.getSize();
        }
        Page<Activity> page = new Page<>(pageNo, pageSize);

        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();

        // 设置状态条件：只查询已发布(1)和进行中(3)的活动
        queryWrapper.in("status", ActivityStatusEnum.PUBLISHED.getCode(), ActivityStatusEnum.IN_PROGRESS.getCode());

        // 根据 ActivityQuery 中的其他属性动态添加查询条件
        if (activityQuery.getId() != null) {
            queryWrapper.eq("id", activityQuery.getId());
        }
        if (activityQuery.getTitle() != null && !activityQuery.getTitle().isEmpty()) {
            queryWrapper.like("title", activityQuery.getTitle());
        }
        if (activityQuery.getCoverImage() != null && !activityQuery.getCoverImage().isEmpty()) {
            queryWrapper.eq("cover_image", activityQuery.getCoverImage());
        }
        if (activityQuery.getActivityPosition() != null && !activityQuery.getActivityPosition().isEmpty()) {
            queryWrapper.eq("activity_position", activityQuery.getActivityPosition());
        }
        if (activityQuery.getEarlyBirdPrice() != null) {
            queryWrapper.eq("early_bird_price", activityQuery.getEarlyBirdPrice());
        }
        if (activityQuery.getNormalPrice() != null) {
            queryWrapper.eq("normal_price", activityQuery.getNormalPrice());
        }

        // 处理排序信息
        String orderBy = activityQuery.getOrderBy();
        // 优先按状态排序（报名中的排在前面），然后按创建时间降序排序
        // 报名中状态代码为3，已发布状态代码为1
        // 使用 FIELD 函数优先显示报名中的活动
        queryWrapper.last("ORDER BY FIELD(status, 1, 3) ASC, " + orderBy);

        return activityMapper.selectPage(page, queryWrapper);
    }

    /**
     * @Author Abin
     * @Description 结束活动，将活动相关订单设为已完成，活动状态设为已结束
     * @param activityId 活动ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeActivity(Long activityId) {
        log.info("开始结束活动，活动ID: {}", activityId);

        try {
            // 1. 检查活动是否存在
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                log.warn("活动不存在，ID: {}", activityId);
                throw new ServiceException("活动不存在");
            }

            // 2. 查询该活动的所有订单
            QueryWrapper<ActivityOrder> orderQueryWrapper = new QueryWrapper<>();
            orderQueryWrapper.eq("activity_id", activityId);
            // 只处理已支付的订单
            orderQueryWrapper.eq("status", ActivityOrderStatusEnum.PAID.getStatus());
            List<ActivityOrder> orders = activityOrderMapper.selectList(orderQueryWrapper);

            log.info("活动ID: {} 找到 {} 个需要处理的订单", activityId, orders.size());

            // 3. 将所有订单状态更新为已完成
            for (ActivityOrder order : orders) {
                ActivityOrder updateOrder = new ActivityOrder();
                updateOrder.setId(order.getId());
                updateOrder.setStatus(ActivityOrderStatusEnum.COMPLETED.getStatus());
                activityOrderMapper.updateById(updateOrder);
                log.info("订单ID: {}, 订单号: {} 已更新为已完成状态", order.getId(), order.getOrderNo());
            }

            // 4. 更新活动状态为已结束
            Activity updateActivity = new Activity();
            updateActivity.setId(activityId);
            updateActivity.setStatus(ActivityStatusEnum.FINISHED.getCode());
            activityMapper.updateById(updateActivity);

            log.info("活动ID: {} 已更新为已结束状态", activityId);

            return true;
        } catch (Exception e) {
            log.error("结束活动过程中发生错误，活动ID: {}", activityId, e);
            throw new ServiceException("结束活动失败: " + e.getMessage());
        }
    }

    /**
     * @Author Abin
     * @Description 查询已结束的活动列表
     * @param activityQuery 查询条件
     * @return 分页活动列表
     */
    @Override
    public IPage<Activity> findFinishedActivities(ActivityQuery activityQuery) {
        // 构建分页参数
        Integer pageNo = activityQuery.getPageNo();
        Integer pageSize = activityQuery.getPageSize();
        if (pageNo == null) {
            pageNo = Constants.defaultPageNo;
        }
        if (pageSize == null) {
            pageSize = PageSize.SIZE10.getSize();
        }
        Page<Activity> page = new Page<>(pageNo, pageSize);

        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();

        // 设置状态条件：只查询已结束(4)的活动
        queryWrapper.eq("status", ActivityStatusEnum.FINISHED.getCode());

        // 根据 ActivityQuery 中的其他属性动态添加查询条件
        if (activityQuery.getId() != null) {
            queryWrapper.eq("id", activityQuery.getId());
        }
        if (activityQuery.getTitle() != null && !activityQuery.getTitle().isEmpty()) {
            queryWrapper.like("title", activityQuery.getTitle());
        }
        if (activityQuery.getCoverImage() != null && !activityQuery.getCoverImage().isEmpty()) {
            queryWrapper.eq("cover_image", activityQuery.getCoverImage());
        }
        if (activityQuery.getActivityPosition() != null && !activityQuery.getActivityPosition().isEmpty()) {
            queryWrapper.eq("activity_position", activityQuery.getActivityPosition());
        }

        // 处理排序信息
        String orderBy = activityQuery.getOrderBy();
        if (orderBy != null && !orderBy.isEmpty()) {
            log.info("排序条件: {}", orderBy);
            queryWrapper.last("ORDER BY " + orderBy);
        } else {
            // 默认按创建时间降序排序
            queryWrapper.last("ORDER BY create_time DESC");
        }

        return activityMapper.selectPage(page, queryWrapper);
    }

    @Override
    public String generateActivityWxaCode(Long activityId, String page, String openid) {
        if (activityId == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "活动ID不能为空");
        }

        // 查询活动信息
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new ServiceException(ErrorCode.BIZ_ERROR, "活动不存在");
        }

        try {
            // 构建场景值，直接使用活动ID
            String scene = String.valueOf(activityId);
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
                String fileUrl = fileServiceImpl.uploadFile(qrcodeFile, "activity_minicode", openid).get("url");
                return fileUrl;
            } finally {
                // 删除临时文件
                if (qrcodeFile != null && qrcodeFile.exists()) {
                    qrcodeFile.delete();
                }
            }
        } catch (Exception e) {
            log.error("生成活动小程序码失败：", e);
            throw new ServiceException(ErrorCode.SYSTEM_ERROR, "生成活动小程序码失败：" + e.getMessage());
        }
    }

    @Override
    public String generateActivityPoster(Long activityId, String page, String openid) {
        if (activityId == null) {
            throw new ServiceException(ErrorCode.PARAM_ERROR, "活动ID不能为空");
        }

        // 查询活动信息
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new ServiceException(ErrorCode.BIZ_ERROR, "活动不存在");
        }

        try {
            // 1. 生成小程序码
            String scene = String.valueOf(activityId);
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
            
            File coverImageFile = null;
            try {
                // 2. 下载活动封面图
                log.info("开始下载活动封面图: {}", activity.getCoverImage());
                coverImageFile = downloadImage(activity.getCoverImage());
            } catch (Exception e) {
                log.error("下载活动封面图失败，将使用默认图片: {}", e.getMessage());
                // 尝试加载默认图片
                try {
                    String defaultImagePath = "src/main/resources/static/images/default_activity.jpg";
                    File defaultImageFile = new File(defaultImagePath);
                    if (defaultImageFile.exists()) {
                        coverImageFile = defaultImageFile;
                    } else {
                        // 创建简单的纯色图片作为封面
                        coverImageFile = createDefaultImage(750, 450);
                    }
                } catch (Exception ex) {
                    log.error("创建默认图片也失败: {}", ex.getMessage());
                    // 继续尝试创建一个纯色图片
                    coverImageFile = createDefaultImage(750, 450);
                }
            }
            
            try {
                // 3. 生成海报
                File posterFile = createPoster(coverImageFile, qrcodeFile, activity);
                
                // 4. 上传海报
                String posterUrl = fileServiceImpl.uploadFile(posterFile, "activity_poster", openid).get("url");
                
                return posterUrl;
            } finally {
                // 清理临时文件
                if (qrcodeFile != null && qrcodeFile.exists()) {
                    qrcodeFile.delete();
                }
                
                // 如果封面图是临时创建的，也需要清理
                if (coverImageFile != null && coverImageFile.exists() && 
                    coverImageFile.getName().startsWith("temp_") || coverImageFile.getName().startsWith("cover_")) {
                    coverImageFile.delete();
                }
            }
        } catch (Exception e) {
            log.error("生成活动海报失败：", e);
            throw new ServiceException(ErrorCode.SYSTEM_ERROR, "生成活动海报失败：" + e.getMessage());
        }
    }

    /**
     * 创建默认的纯色图片
     * 
     * @param width 图片宽度
     * @param height 图片高度
     * @return 临时图片文件
     * @throws IOException 创建失败时抛出异常
     */
    private File createDefaultImage(int width, int height) throws IOException {
        // 创建一个纯色的图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置背景颜色为浅蓝色
        g2d.setColor(new Color(135, 206, 250)); // 天蓝色
        g2d.fillRect(0, 0, width, height);
        
        // 添加文本
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("微软雅黑", Font.BOLD, 32));
        String text = "活动海报";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        g2d.drawString(text, (width - textWidth) / 2, height / 2 + textHeight / 4);
        
        g2d.dispose();
        
        // 保存为临时文件
        File tempFile = File.createTempFile("temp_", ".jpg");
        tempFile.deleteOnExit();
        ImageIO.write(image, "jpg", tempFile);
        
        return tempFile;
    }

    /**
     * 下载图片到临时文件
     * 
     * @param imageUrl 图片URL
     * @return 临时文件
     * @throws IOException 下载失败时抛出异常
     */
    private File downloadImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IOException("图片URL不能为空");
        }
        
        log.info("开始下载图片，URL: {}", imageUrl);
        
        // 创建临时文件
        File tempFile = File.createTempFile("cover_", ".jpg");
        tempFile.deleteOnExit();
        
        // 处理不同的URL协议
        if (imageUrl.startsWith("cloud://")) {
            // 处理微信云存储URL
            log.info("检测到cloud协议URL，尝试从FileService获取真实URL");
            try {
                // 这里应替换为实际获取云存储真实URL的逻辑
                // 如果项目中有处理cloud://URL的服务，可以调用该服务
                // 如果没有，则需要从其他地方获取真实URL，或者使用默认图片
                
                // 临时方案：直接使用系统中的一个默认图片
                // 这里假设存在静态资源目录下的默认图片，实际情况可能需要调整
                return new File("src/main/resources/static/images/default_activity.jpg");
            } catch (Exception e) {
                log.error("处理cloud协议URL失败: {}", e.getMessage());
                throw new IOException("无法处理cloud协议URL: " + e.getMessage());
            }
        } else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            // 标准HTTP/HTTPS协议处理
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("下载图片失败，HTTP状态码: " + connection.getResponseCode());
            }
            
            // 保存图片到临时文件
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = connection.getInputStream().read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            
            return tempFile;
        } else {
            // 其他协议或本地文件路径
            log.info("未识别的URL协议，尝试作为本地文件路径处理");
            File file = new File(imageUrl);
            if (file.exists() && file.isFile()) {
                return file;
            }
            
            throw new IOException("不支持的URL协议或文件不存在: " + imageUrl);
        }
    }
    
    /**
     * 创建活动海报
     * 
     * @param coverImageFile 封面图文件
     * @param qrcodeFile 小程序码文件
     * @param activity 活动信息
     * @return 海报文件
     * @throws IOException 创建失败时抛出异常
     */
    private File createPoster(File coverImageFile, File qrcodeFile, Activity activity) throws IOException {
        // 读取封面图
        BufferedImage coverImage = ImageIO.read(coverImageFile);
        
        // 读取小程序码
        BufferedImage qrcodeImage = ImageIO.read(qrcodeFile);
        
        // 海报尺寸：750x1334 (标准手机屏幕比例)
        int posterWidth = 750;
        int posterHeight = 1334;
        
        // 创建海报画布
        BufferedImage poster = new BufferedImage(posterWidth, posterHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = poster.createGraphics();
        
        // 设置背景色为白色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, posterWidth, posterHeight);
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 绘制封面图（居中，宽度为海报宽度的90%）
        int coverWidth = (int) (posterWidth * 0.9);
        int coverHeight = (int) (coverWidth * 0.6); // 保持封面图比例
        int coverX = (posterWidth - coverWidth) / 2;
        int coverY = 50; // 顶部留白
        
        g2d.drawImage(coverImage, coverX, coverY, coverWidth, coverHeight, null);
        
        // 设置标题字体
        Font titleFont = new Font("微软雅黑", Font.BOLD, 36);
        g2d.setFont(titleFont);
        g2d.setColor(new Color(51, 51, 51)); // 深灰色
        
        // 绘制标题（居中）
        String title = activity.getTitle();
        FontMetrics titleMetrics = g2d.getFontMetrics(titleFont);
        int titleWidth = titleMetrics.stringWidth(title);
        int titleX = (posterWidth - titleWidth) / 2;
        int titleY = coverY + coverHeight + 60;
        
        g2d.drawString(title, titleX, titleY);
        
        // 设置信息字体
        Font infoFont = new Font("微软雅黑", Font.PLAIN, 24);
        g2d.setFont(infoFont);
        g2d.setColor(new Color(102, 102, 102)); // 中灰色
        
        // 绘制时间信息
        String timeInfo = "活动时间：" + activity.getStartTime().format(DATE_TIME_FORMATTER) + 
                          " - " + activity.getEndTime().format(DATE_TIME_FORMATTER);
        int timeY = titleY + 50;
        g2d.drawString(timeInfo, 50, timeY);
        
        // 绘制地点信息
        String locationInfo = "集合地点：" + activity.getActivityPosition();
        int locationY = timeY + 40;
        g2d.drawString(locationInfo, 50, locationY);
        
        // 绘制价格信息
        String priceInfo = "活动价格：¥" + activity.getNormalPrice();
        int priceY = locationY + 40;
        g2d.drawString(priceInfo, 50, priceY);
        
        // 绘制小程序码（底部居中）
        int qrcodeSize = 200;
        int qrcodeX = (posterWidth - qrcodeSize) / 2;
        int qrcodeY = priceY + 60;
        
        g2d.drawImage(qrcodeImage, qrcodeX, qrcodeY, qrcodeSize, qrcodeSize, null);
        
        // 绘制提示文字
        Font tipFont = new Font("微软雅黑", Font.PLAIN, 20);
        g2d.setFont(tipFont);
        g2d.setColor(new Color(153, 153, 153)); // 浅灰色
        
        String tipText = "长按识别小程序码，查看活动详情";
        FontMetrics tipMetrics = g2d.getFontMetrics(tipFont);
        int tipWidth = tipMetrics.stringWidth(tipText);
        int tipX = (posterWidth - tipWidth) / 2;
        int tipY = qrcodeY + qrcodeSize + 30;
        
        g2d.drawString(tipText, tipX, tipY);
        
        // 释放资源
        g2d.dispose();
        
        // 保存海报到临时文件
        File posterFile = File.createTempFile("poster_", ".jpg");
        posterFile.deleteOnExit();
        
        ImageIO.write(poster, "jpg", posterFile);
        
        return posterFile;
    }
}