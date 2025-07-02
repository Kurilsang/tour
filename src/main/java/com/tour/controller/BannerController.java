package com.tour.controller;

import com.tour.common.Result;
import com.tour.service.BannerService;
import com.tour.vo.BannerVO;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author Abin
 * @Description 轮播图控制器
 * @DateTime 2025/5/9 13:55
 */
@Slf4j
@Api(tags = "轮播图", description = "轮播图相关接口")
@RestController
@RequestMapping("/api/banners")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    /**
     * 获取轮播图列表
     */
    @ApiOperation(value = "获取轮播图列表", notes = "获取系统轮播图列表，按排序号升序排列")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/list")
    public Result<List<BannerVO>> getBannerList() {
        // 查询轮播图列表
        List<BannerVO> banners = bannerService.getBannerList();
        return Result.success(banners);
    }
} 