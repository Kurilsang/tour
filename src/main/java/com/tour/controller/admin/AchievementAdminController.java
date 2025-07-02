package com.tour.controller.admin;

import com.tour.common.Result;
import com.tour.common.util.UserContext;
import com.tour.dto.AchievementCreateDTO;
import com.tour.dto.AchievementUpdateDTO;
import com.tour.service.IAchievementService;
import com.tour.vo.AchievementVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author Abin
 * @Description 成就管理员控制器
 */
@Slf4j
@Api(tags = "成就管理后台接口", description = "成就管理员操作接口")
@RestController
@RequestMapping("/api/admin/achievements")
public class AchievementAdminController {

    @Autowired
    private IAchievementService achievementService;

    /**
     * 创建成就
     */
    @ApiOperation(value = "创建成就", notes = "管理员创建新成就")
    @ApiResponses({
            @ApiResponse(code = 200, message = "创建成功", response = Result.class),
            @ApiResponse(code = 400, message = "参数错误"),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/create")
    public Result<AchievementVO> createAchievement(@RequestBody AchievementCreateDTO createDTO) {
        AchievementVO achievement = achievementService.createAchievement(createDTO);
        return Result.success(achievement);
    }

    /**
     * 更新成就
     */
    @ApiOperation(value = "更新成就", notes = "管理员更新成就信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "更新成功", response = Result.class),
            @ApiResponse(code = 400, message = "参数错误"),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 404, message = "成就不存在"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/update")
    public Result<AchievementVO> updateAchievement(@RequestBody AchievementUpdateDTO updateDTO) {
        AchievementVO achievement = achievementService.updateAchievement(updateDTO);
        return Result.success(achievement);
    }

    /**
     * 获取所有成就列表（管理员视图）
     */
    @ApiOperation(value = "获取所有成就列表", notes = "管理员获取所有成就")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/list")
    public Result<List<AchievementVO>> getAllAchievements() {
        List<AchievementVO> achievements = achievementService.getAllAchievementsForAdmin();
        return Result.success(achievements);
    }

    /**
     * 生成小程序码
     *
     * @param id   成就ID
     * @param page 小程序页面路径
     * @return 小程序码URL
     */
    @ApiOperation(value = "生成小程序码", notes = "根据成就ID生成小程序码，用于前端制作分享海报")
    @ApiResponses({
            @ApiResponse(code = 200, message = "生成成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 400, message = "无效的参数"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/{id}/minicode")
    public Result<String> generateMiniCode(@PathVariable("id") Long id,
                                           @RequestParam(value = "page", defaultValue = "") String page) {
        // 从上下文获取用户的openid
        String openid = UserContext.getOpenId();
        String miniCodeUrl = achievementService.generateWxaCode(id, page, openid);
        return Result.success(miniCodeUrl);
    }
}
