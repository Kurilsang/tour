/**
 * @Author Abin
 * @Description 轮播图管理控制器
 */
package com.tour.controller.admin;

import com.tour.common.Result;
import com.tour.common.constant.ErrorCode;
import com.tour.common.util.UserContext;
import com.tour.dto.BannerDTO;
import com.tour.service.BannerService;
import com.tour.vo.BannerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图管理控制器
 * 仅管理员可访问
 */
@Api(tags = "轮播图管理接口", description = "管理员操作轮播图的接口")
@Slf4j
@RestController
@RequestMapping("/api/admin/banner")
public class BannerAdminController {

    @Autowired
    private BannerService bannerService;

    /**
     * 获取轮播图列表
     */
    @ApiOperation(value = "获取轮播图列表", notes = "管理员获取轮播图列表")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功"),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 403, message = "没有管理员权限"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/list")
    public Result<List<BannerVO>> getBannerList() {
        List<BannerVO> banners = bannerService.getBannerList();
        return Result.success(banners);
    }

    /**
     * 更新轮播图列表
     */
    @ApiOperation(value = "更新轮播图列表", notes = "管理员更新轮播图列表，会清空之前的轮播图后重新创建")
    @ApiResponses({
            @ApiResponse(code = 200, message = "更新成功"),
            @ApiResponse(code = 400, message = "参数错误"),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 403, message = "没有管理员权限"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/update")
    public Result<Boolean> updateBannerList(@RequestBody List<BannerDTO> bannerList) {
        if (bannerList == null) {
            return Result.error(ErrorCode.PARAM_ERROR, "轮播图列表不能为空");
        }
        
        // 获取当前操作者的openid
        String operatorOpenid = UserContext.getOpenId();
        
        // 调用服务更新轮播图列表
        boolean success = bannerService.updateBannerList(bannerList, operatorOpenid);
        
        return Result.success(success);
    }
} 