package com.tour.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.common.Result;
import com.tour.common.exception.ServiceException;
import com.tour.dao.CommentMapper;
import com.tour.dto.ActivityDTO;
import com.tour.enums.CommentStatusEnum;
import com.tour.model.Activity;
import com.tour.model.Comment;
import com.tour.query.ActivityQuery;
import com.tour.service.ActivityService;
import com.tour.service.impl.ActivityServiceImpl;
import com.tour.vo.SignUpListVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 活动管理员接口
 *
 * @Author Kuril
 */
@Api(tags = "活动管理员接口", description = "活动管理员接口")
@RestController
@RequestMapping("/api/admin/activity")
public class ActivityController {


    private final ActivityServiceImpl activityService;
    
    @Autowired
    private CommentMapper commentMapper;

    public ActivityController(ActivityServiceImpl activityService) {
        this.activityService = activityService;
    }

    /**
     * 加载所有活动内容
     *
     * @return 返回所有活动列表
     */
    @Operation(summary = "根据条件加载所有活动内容", description = "根据条件获取所有活动的列表信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "加载成功", response = Result.class),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/loadDataList")
    public Result loadDataList(@RequestBody ActivityQuery activityQuery) {
        IPage<Activity> pageResult = activityService.findListByPage(activityQuery);
        
        // 为每个活动添加是否有待审核评论的标志
        List<Activity> records = pageResult.getRecords();
        records.forEach(activity -> {
            // 查询该活动是否有待审核的评论
            LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Comment::getActivityId, activity.getId())
                       .eq(Comment::getStatus, CommentStatusEnum.PENDING.getCode());
            long count = commentMapper.selectCount(queryWrapper);
            
            // 将结果添加到活动对象中
            activity.setHasPendingComments(count > 0);
        });
        
        return Result.success(pageResult);
    }

    /**
     * 根据ID获取对应活动信息
     *
     * @param id activity的id
     */
    @Operation(summary = "根据ID获取对应活动信息", description = "通过活动ID获取该活动的详细信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 400, message = "活动ID不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/getActivityById/{id}")
    public Result getActivity(@PathVariable @Parameter(description = "活动ID", required = true) Long id) {
        return Result.success(activityService.selectActivityById(id));
    }

    /**
     * 添加活动及详情内容接口
     *
     * @param activityDTO 具体需要前端参数详见该类
     * @return 添加活动成功返回null
     */
    @Operation(summary = "添加活动及详情内容", description = "添加新的活动及其详细信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "添加成功", response = Result.class),
            @ApiResponse(code = 400, message = "活动信息不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/addActivity")
    public Result addActivity(@RequestBody @Parameter(description = "活动信息", required = true, schema = @Schema(implementation = ActivityDTO.class)) ActivityDTO activityDTO) {
        Long activityId = activityService.addActivity(activityDTO);
        // 返回活动ID，方便前端查询详情
        return Result.success(activityId);
    }

    /**
     * 根据ID删除活动及详情内容接口
     *
     * @param id activity的id
     * @return 删除活动成功返回null
     */
    @Operation(summary = "根据ID删除活动及详情内容", description = "通过活动ID删除该活动及其详细信息，只有已关闭或已结束的活动才能删除")
    @ApiResponses({
            @ApiResponse(code = 200, message = "删除成功", response = Result.class),
            @ApiResponse(code = 400, message = "活动ID不能为空或活动状态不允许删除"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/deleteActivityById/{id}")
    public Result deleteActivityById(@PathVariable @Parameter(description = "活动ID", required = true) int id) {
        try {
            activityService.deleteActivityById(id);
            return Result.success(null);
        } catch (ServiceException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除活动失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID更新活动及详情内容接口
     *
     * @param id         activity的id
     * @param activityDTO 具体需要前端参数详见该类
     * @return 修改活动成功返回null
     */
    @Operation(summary = "根据ID更新活动及详情内容", description = "通过活动ID更新该活动及其详细信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "更新成功", response = Result.class),
            @ApiResponse(code = 400, message = "活动ID和活动信息不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/updateActivityById/{id}")
    public Result updateActivityById(@PathVariable @Parameter(description = "活动ID", required = true) Long id,
                                     @RequestBody @Parameter(description = "活动信息", required = true, schema = @Schema(implementation = ActivityDTO.class)) ActivityDTO activityDTO) {
        activityService.updateActivityById(id, activityDTO);
        return Result.success(null);
    }


    /**
     * 根据ID更新活动及详情内容接口
     *
     * @param id         activity的id
     * @param status 要切换的状态 具体需要前端参数详见该类
     * @return 修改活动成功返回null
     */
    @Operation(summary = "根据ID更新活动状态", description = "根据ID更新活动状态")
    @ApiResponses({
            @ApiResponse(code = 200, message = "更新成功", response = Result.class),
            @ApiResponse(code = 400, message = "活动ID和状态值不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/switchActivityStatusById/{id}/{status}")
    public Result switchActivityStatusById(@PathVariable @Parameter(description = "活动ID", required = true) Long id,
                                     @PathVariable @Parameter(description = "活动ID", required = true) Integer status ) {
        activityService.switchActivityStatusById(id, status);
        return Result.success(null);
    }

//    /**
//     * 完成活动报名处理
//     *
//     * @param signUpListVOList 报名列表数据
//     * @return 处理成功返回true，失败返回false
//     * @Author Kuril
//     */
//    @Operation(summary = "结束活动", description = "将活动报名列表中的订单设为已完成，删除报名数据，并将活动设为已关闭，回滚库存（不重置销量）")
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "处理成功", response = Result.class),
//            @ApiResponse(code = 400, message = "参数错误"),
//            @ApiResponse(code = 500, message = "服务器内部错误")
//    })
//    @PostMapping("/completeActivitySignUp")
//    public Result completeActivitySignUp(@RequestBody @Parameter(description = "报名列表数据", required = true) List<SignUpListVO> signUpListVOList) {
//        boolean result = activityService.completeActivitySignUp(signUpListVOList);
//        return Result.success(result);
//    }

    /**
     * 获取可用于轮播图的活动列表（已发布和进行中）
     *
     * @param activityQuery 查询条件
     * @return 返回已发布和进行中的活动列表
     * @Author Abin
     */
    @Operation(summary = "获取可用于轮播图的活动列表", description = "获取处于已发布或进行中状态的活动列表")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/loadAvailableActivities")
    public Result loadAvailableActivities(@RequestBody ActivityQuery activityQuery) {
        return Result.success(activityService.findAvailableActivities(activityQuery));
    }
//
//    /**
//     * 批量删除活动及详情内容接口
//     *
//     * @param idsStr 活动ID字符串，格式为"1,2,3"
//     * @return 删除结果，包含成功删除的数量
//     * @Author Kuril
//     */
//    @Operation(summary = "批量删除活动及详情内容", description = "通过活动ID列表批量删除活动及其详细信息，只有已关闭或已结束的活动才能删除")
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "删除成功", response = Result.class),
//            @ApiResponse(code = 400, message = "活动ID格式错误"),
//            @ApiResponse(code = 500, message = "服务器内部错误")
//    })
//    @GetMapping("/deleteActivitiesByIds/{idsStr}")
//    public Result deleteActivitiesByIds(@PathVariable @Parameter(description = "活动ID字符串，格式为1,2,3", required = true) String idsStr) {
//        try {
//            // 将逗号分隔的ID字符串转换为Long类型的List
//            List<Long> ids = Arrays.stream(idsStr.split(","))
//                    .map(String::trim)
//                    .filter(s -> !s.isEmpty())
//                    .map(Long::parseLong)
//                    .collect(Collectors.toList());
//
//            if (ids.isEmpty()) {
//                return Result.error("未提供有效的活动ID");
//            }
//
//            int successCount = activityService.deleteActivitiesByIds(ids);
//            return Result.success(successCount);
//        } catch (NumberFormatException e) {
//            return Result.error("活动ID格式错误，请提供有效的数字ID");
//        } catch (Exception e) {
//            return Result.error("批量删除活动失败: " + e.getMessage());
//        }
//    }
}