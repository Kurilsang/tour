package com.tour.controller;

import com.tour.common.Result;
import com.tour.common.exception.ServiceException;
import com.tour.dto.BusLocationDTO;
import com.tour.model.BusLocation;
import com.tour.query.BusLocationQuery;
import com.tour.service.BannerService;
import com.tour.service.impl.BusLocationServiceImpl;
import com.tour.vo.BannerVO;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author Kuril
 * @Description 上车点控制器
 * @DateTime 2025/5/13 9:32
 */
@Slf4j
@Api(tags = "上车点管理员接口", description = "上车点管理员接口")
@RestController
@RequestMapping("/api/admin/busLocation")
public class BusLocationController {


    private final BusLocationServiceImpl busLocationService;

    public BusLocationController(BusLocationServiceImpl busLocationService) {
        this.busLocationService = busLocationService;
    }

    /**
     * 添加新上车点
     */
    @ApiOperation(value = "添加新上车点", notes = "添加新上车点")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/addBusLocation")
    public Result addBusLocation(@RequestBody BusLocationDTO busLocationDTO) {
        busLocationService.addBusLocation(busLocationDTO);
        return Result.success(null);
    }
    /**
     * 编辑上车点
     */
    @ApiOperation(value = "编辑上车点", notes = "编辑上车点")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/editBusLocation/{id}")
    public Result editBusLocation(@RequestBody BusLocationDTO busLocationDTO,@PathVariable Long id) {
        busLocationService.editBusLocation(busLocationDTO,id);
        return Result.success(null);
    }
    /**
     * 批量删除上车点
     */
    @ApiOperation(value = "批量删除上车点", notes = "批量删除上车点")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })

    @DeleteMapping("/deleteBusLocations/{ids}")
    public Result deleteBusLocations(@PathVariable List<Long> ids) {
        if(ids.isEmpty())
        {
            throw new ServiceException("id不能为空");
        }
        busLocationService.deleteBusLocations(ids);
        return Result.success(null);
    }

    /**
     * 根据条件批量查出上车点信息
     */
    @ApiOperation(value = "根据条件批量查出上车点信息", notes = "根据条件批量查出上车点信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/findBusLocationsByPage")
    public Result findBusLocationsByPage(@RequestBody BusLocationQuery busLocationQuery) {
        return Result.success(busLocationService.findBusLocationsByPage(busLocationQuery));
    }

    /**
     * 根据id查出对应上车点信息
     */
    @ApiOperation(value = "根据id查出对应上车点信息", notes = "根据id查出对应上车点信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/findBusLocationById/{id}")
    public Result findBusLocationById(@PathVariable Long id) {
        return Result.success(busLocationService.findBusLocationById(id));
    }


}