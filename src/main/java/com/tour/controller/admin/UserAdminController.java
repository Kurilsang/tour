/**
 * @Author Abin
 * @Description 用户管理控制器
 */
package com.tour.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.common.Result;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.UserContext;
import com.tour.dto.UserRoleDTO;
import com.tour.enums.RoleEnum;
import com.tour.model.User;
import com.tour.query.UserQuery;
import com.tour.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 用户管理控制器
 * 专门处理管理员对用户的操作
 */
@Api(tags = "用户管理接口", description = "管理员操作用户的接口")
@Slf4j
@RestController
@RequestMapping("/api/admin/user")
public class UserAdminController {

    @Autowired
    private IUserService userService;

    /**
     * 管理员查询用户列表
     */
    @Operation(summary = "查询用户列表", description = "管理员查询用户列表，支持按昵称和openid搜索，按角色和注册时间筛选")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功", response = Result.class),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 403, message = "没有管理员权限"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/list")
    public Result<IPage<User>> queryUserList(
            @RequestParam(required = false) @Parameter(description = "搜索关键词，可匹配昵称或openid") String keyword,
            @RequestParam(required = false) @Parameter(description = "用户角色") String role,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "注册开始时间") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Parameter(description = "注册结束时间") Date endTime,
            @RequestParam(required = false, defaultValue = "desc") @Parameter(description = "排序方向，desc降序，asc升序") String sortDirection,
            @RequestParam(required = false, defaultValue = "1") @Parameter(description = "页码") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") @Parameter(description = "每页记录数") Integer pageSize) {
        
        // 构建查询参数
        UserQuery query = new UserQuery();
        query.setKeyword(keyword);
        query.setRole(role);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setSortDirection(sortDirection);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        
        // 调用服务执行查询
        IPage<User> page = userService.queryUserList(query);
        return Result.success(page);
    }

    /**
     * 超级管理员调整用户角色
     */
    @Operation(summary = "调整用户角色", description = "超级管理员调整用户角色，不能对其他超级管理员降级")
    @ApiResponses({
        @ApiResponse(code = 200, message = "调整成功", response = Result.class),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 403, message = "没有超级管理员权限"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PutMapping("/role")
    public Result<Boolean> updateUserRole(
            @RequestBody @Parameter(description = "用户角色信息", required = true, 
                schema = @Schema(implementation = UserRoleDTO.class)) UserRoleDTO roleDTO) {
        // 参数校验
        if (roleDTO == null || roleDTO.getOpenid() == null || roleDTO.getRole() == null) {
            return Result.error(ErrorCode.PARAM_ERROR, "参数不完整");
        }
        
        // 获取当前操作者信息
        String operatorOpenid = UserContext.getOpenId();
        String operatorRole = UserContext.getRole();
        
        // 检查操作者是否是超级管理员
        if (!RoleEnum.SUPER_ADMIN.getCode().equals(operatorRole)) {
            log.warn("用户 {} 尝试调整角色但没有超级管理员权限", operatorOpenid);
            throw new ServiceException(ErrorCode.FORBIDDEN, "调整用户角色需要超级管理员权限");
        }
        
        // 调用服务更新角色
        boolean success = userService.updateUserRole(roleDTO, operatorOpenid);
        
        return Result.success(success);
    }
    
    /**
     * 管理员重置用户资料
     * 将用户的头像和昵称恢复为默认值
     */
    @Operation(summary = "重置用户资料", description = "管理员将用户的头像和昵称重置为默认值")
    @ApiResponses({
        @ApiResponse(code = 200, message = "重置成功", response = Result.class),
        @ApiResponse(code = 400, message = "参数错误"),
        @ApiResponse(code = 401, message = "未登录或token已过期"),
        @ApiResponse(code = 403, message = "没有管理员权限"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PutMapping("/clean")
    public Result<Boolean> cleanUserProfile(
            @RequestParam @Parameter(description = "目标用户openid", required = true) String openid) {
        // 参数校验
        if (openid == null || openid.isEmpty()) {
            return Result.error(ErrorCode.PARAM_ERROR, "目标用户openid不能为空");
        }
        
        // 获取当前操作者的openid
        String operatorOpenid = UserContext.getOpenId();
        
        // 调用服务重置用户资料
        boolean success = userService.resetUserProfile(openid, operatorOpenid);
        
        return Result.success(success);
    }
} 