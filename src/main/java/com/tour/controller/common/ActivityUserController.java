package com.tour.controller.common;

import com.tour.common.Result;
import com.tour.common.util.UserContext;
import com.tour.dto.ActivityDTO;
import com.tour.query.ActivityQuery;
import com.tour.service.ActivityService;
import com.tour.service.impl.ActivityOrderServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

/**
 * 通用的（包括未登录游客）的activity接口
 *
 * @Author Kuril
 */
@Api(tags = "通用活动接口", description = "通用的（包括未登录游客）的活动相关接口")
@RestController
@RequestMapping("/api/common/activity")
public class ActivityUserController {
  private final ActivityService activityService;
  private final ActivityOrderServiceImpl activityOrderService;

  public ActivityUserController(@Qualifier("activityService") ActivityService activityService, ActivityOrderServiceImpl activityOrderService) {
    this.activityService = activityService;
    this.activityOrderService = activityOrderService;
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
    return Result.success(activityService.UserFindListByPage(activityQuery));
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
   * 首页展示活动列表
   *
   * @param activityQuery 查询条件
   * @return 返回已发布和进行中的活动列表
   * @Author Abin
   */
  @Operation(summary = "首页展示活动列表", description = "获取已发布和进行中的活动列表")
  @ApiResponses({
      @ApiResponse(code = 200, message = "获取成功", response = Result.class),
      @ApiResponse(code = 500, message = "服务器内部错误")
  })
  @PostMapping("/loadAvailableActivities")
  public Result loadAvailableActivities(@RequestBody ActivityQuery activityQuery) {
    return Result.success(activityService.findAvailableActivities(activityQuery));
  }

  /**
   * 查询已结束的活动列表
   *
   * @param activityQuery 查询条件
   * @return 返回已结束的活动列表
   * @Author Abin
   */
  @Operation(summary = "查询已结束的活动列表", description = "获取状态为已结束的活动列表，默认按创建时间降序排序")
  @ApiResponses({
      @ApiResponse(code = 200, message = "获取成功", response = Result.class),
      @ApiResponse(code = 500, message = "服务器内部错误")
  })
  @PostMapping("/loadFinishedActivities")
  public Result loadFinishedActivities(@RequestBody ActivityQuery activityQuery) {
    return Result.success(activityService.findFinishedActivities(activityQuery));
  }


  /**
   * 生成活动小程序码
   *
   * @param activityId 活动ID
   * @param page 小程序页面路径
   * @return 小程序码URL
   */
  @Operation(summary = "生成活动小程序码",
          description = "根据活动ID生成带有场景值的小程序码")
  @ApiResponses({
          @ApiResponse(code = 200, message = "生成成功", response = Result.class),
          @ApiResponse(code = 500, message = "服务器内部错误")
  })
  @GetMapping("/{activityId}/minicode")
  public Result generateActivityWxaCode(
          @PathVariable Long activityId,
          @RequestParam(required = false) @Parameter(description = "小程序页面路径") String page) {

    // 获取当前用户openid
    String openid = UserContext.getOpenId();

    // 调用服务生成小程序码
    String wxaCodeUrl = activityService.generateActivityWxaCode(activityId, page, openid);


    return Result.success(wxaCodeUrl);
  }
  
  /**
   * 生成活动海报
   *
   * @param activityId 活动ID
   * @param page 小程序页面路径
   * @return 海报URL
   */
  @Operation(summary = "生成活动海报",
          description = "根据活动ID生成包含活动信息和小程序码的海报")
  @ApiResponses({
          @ApiResponse(code = 200, message = "生成成功", response = Result.class),
          @ApiResponse(code = 500, message = "服务器内部错误")
  })
  @GetMapping("/{activityId}/poster")
  public Result generatePoster(
          @PathVariable Long activityId,
          @RequestParam(required = false) @Parameter(description = "小程序页面路径") String page) {

    // 获取当前用户openid
    String openid = UserContext.getOpenId();

    // 调用服务生成海报
    String posterUrl = activityService.generateActivityPoster(activityId, page, openid);

    return Result.success(posterUrl);
  }
}