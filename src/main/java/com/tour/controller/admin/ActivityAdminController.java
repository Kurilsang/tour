package com.tour.controller.admin;

import com.tour.common.Result;
import com.tour.service.ActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 活动管理员控制器
 *
 * @Author Abin
 */
@Api(tags = "活动管理员接口", description = "活动管理员专用接口")
@RestController
@RequestMapping("/api/admin/activity")
public class ActivityAdminController {

  @Autowired
  private ActivityService activityService;

  /**
   * 结束活动
   *
   * @param activityId 活动ID
   * @return 处理成功返回true，失败返回false
   */
  @Operation(summary = "结束活动", description = "将活动相关订单设为已完成，活动状态设为已结束")
  @ApiResponses({
      @ApiResponse(code = 200, message = "处理成功", response = Result.class),
      @ApiResponse(code = 400, message = "参数错误"),
      @ApiResponse(code = 500, message = "服务器内部错误")
  })
  @PostMapping("/completeActivity")
  public Result<Boolean> completeActivity(
      @RequestParam @Parameter(description = "活动ID", required = true) Long activityId) {
    boolean result = activityService.completeActivity(activityId);
    return Result.success(result);
  }
}