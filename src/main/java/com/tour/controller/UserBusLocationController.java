package com.tour.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.common.Result;
import com.tour.common.util.UserContext;
import com.tour.model.ActivityOrder;
import com.tour.query.ActivityOrderQuery;
import com.tour.service.impl.ActivityOrderServiceImpl;
import com.tour.service.impl.ActivityServiceImpl;
import com.tour.service.impl.BusLocationServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户活动订单接口
 *
 * @Author Kuril
 */
@Api(tags = "上车点用户接口", description = "上车点用户接口")
@RestController
@RequestMapping("/api/busLocation")
public class UserBusLocationController {


    private final BusLocationServiceImpl busLocationService;

    public UserBusLocationController( BusLocationServiceImpl busLocationService) {
        this.busLocationService = busLocationService;
    }

    /**
     * 加载所有上车点信息（仅选择框）
     *
     * @return 所有上车点信息（仅选择框）
     */
    @Operation(summary = "加载所有上车点信息", description = "加载所有上车点信息（仅用于选择框加载）")
    @ApiResponses({
            @ApiResponse(code = 200, message = "加载成功", response = Result.class),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/loadDataList")
    public Result loadDataList() {


        return Result.success(busLocationService.selectAllData());
    }



}