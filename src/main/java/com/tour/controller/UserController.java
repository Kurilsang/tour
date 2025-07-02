package com.tour.controller;

import cn.hutool.core.bean.BeanUtil;
import com.tour.common.Result;
import com.tour.dto.UserUpdateDTO;
import com.tour.dto.TravelerDTO;
import com.tour.model.User;
import com.tour.model.Traveler;
import com.tour.service.IUserService;
import com.tour.service.ITravelerService;
import com.tour.common.util.UserContext;
import com.tour.vo.UserVO;
import com.tour.vo.TravelerVO;
import io.swagger.annotations.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户控制器
 *
 * @Author Abin
 */
@Api(tags = "用户管理接口", description = "用户信息的查询与修改")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ITravelerService travelerService;

    /**
     * 获取用户信息
     */
    @ApiOperation(value = "获取当前用户信息", notes = "根据token获取当前登录用户的详细信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "获取成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        // 从上下文获取当前用户的openid
        String openid = UserContext.getOpenId();
        User user = userService.getUserByOpenid(openid);
        
        // 转换为VO对象
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新当前用户信息", description = "更新当前登录用户的昵称和头像")
    @ApiResponses({
        @ApiResponse(code = 200, message = "更新成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PutMapping("/info")
    public Result<Boolean> updateUserInfo(
            @RequestBody @Parameter(description = "用户更新信息", required = true, schema = @Schema(implementation = UserUpdateDTO.class)) UserUpdateDTO userUpdateDTO) {
        // 从上下文获取当前用户的openid，确保只能更新自己的信息
        String openid = UserContext.getOpenId();
        
        // 构建用户对象
        User user = new User();
        user.setOpenid(openid);
        user.setNickname(userUpdateDTO.getNickname());
        user.setAvatar(userUpdateDTO.getAvatar());
        
        return Result.success(userService.updateUser(user));
    }

    /**
     * 查询当前用户的所有出行人
     */
    @Operation(summary = "查询出行人列表", description = "获取当前用户添加的所有出行人信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/travelers")
    public Result<List<TravelerVO>> listTravelers() {
        // 从上下文获取当前用户的openid
        String openid = UserContext.getOpenId();
        
        // 查询该用户的所有出行人
        List<Traveler> travelers = travelerService.getTravelersByOpenid(openid);
        
        // 转换为VO列表
        List<TravelerVO> travelerVOs = travelers.stream()
            .map(traveler -> {
                TravelerVO vo = new TravelerVO();
                BeanUtil.copyProperties(traveler, vo);
                return vo;
            })
            .collect(Collectors.toList());
        
        return Result.success(travelerVOs);
    }

    /**
     * 添加出行人
     */
    @Operation(summary = "添加出行人", description = "为当前用户添加新的出行人信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "添加成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/travelers")
    public Result<Boolean> addTraveler(
            @RequestBody @Parameter(description = "出行人信息", required = true) TravelerDTO travelerDTO) {
        // 从上下文获取当前用户的openid
        String openid = UserContext.getOpenId();
        
        // 构建出行人对象
        Traveler traveler = new Traveler();
        BeanUtil.copyProperties(travelerDTO, traveler);
        traveler.setOpenid(openid);
        
        // 保存出行人信息
        boolean success = travelerService.saveTraveler(traveler);
        return Result.success(success);
    }

    /**
     * 删除出行人
     */
    @Operation(summary = "删除出行人", description = "删除指定ID的出行人信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "删除成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 403, message = "无权删除该出行人"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @DeleteMapping("/travelers/{id}")
    public Result<Boolean> deleteTraveler(
            @PathVariable @Parameter(description = "出行人ID", required = true) String id) {
        // 从上下文获取当前用户的openid
        String openid = UserContext.getOpenId();
        // 删除出行人信息
        boolean success = travelerService.deleteTravelerByIdAndOpenid(id, openid);
        return Result.success(success);
    }
} 