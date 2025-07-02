/**
 * @Author Abin
 * @Description 信息管理控制器
 */
package com.tour.controller.admin;

import com.tour.common.Result;
import com.tour.common.constant.ErrorCode;
import com.tour.common.exception.ServiceException;
import com.tour.common.util.UserContext;
import com.tour.dto.BusinessInfoDTO;
import com.tour.dto.ContractInfoDTO;
import com.tour.dto.InfoDTO;
import com.tour.enums.RoleEnum;
import com.tour.service.IInfoService;
import com.tour.vo.InfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 信息管理控制器
 * 处理管理员设置信息的接口
 * @author Abin
 */
@Api(tags = "信息管理接口", description = "管理员设置系统信息")
@Slf4j
@RestController
@RequestMapping("/api/admin/info")
public class InfoAdminController {

  @Autowired
  private IInfoService infoService;

  /**
   * 设置系统信息
   */
  @Operation(summary = "设置系统信息", description = "管理员设置系统信息，包括商务合作信息等")
  @ApiResponses({
      @ApiResponse(code = 200, message = "设置成功", response = Result.class),
      @ApiResponse(code = 400, message = "参数错误"),
      @ApiResponse(code = 401, message = "未登录或token已过期"),
      @ApiResponse(code = 403, message = "没有管理员权限"),
      @ApiResponse(code = 500, message = "服务器内部错误")
  })
  @PostMapping("/set")
  public Result<Boolean> setInfo(
      @RequestBody @Parameter(description = "信息数据", required = true, schema = @Schema(implementation = InfoDTO.class)) InfoDTO infoDTO) {
    // 参数校验
    if (infoDTO == null || infoDTO.getKey() == null || infoDTO.getValue() == null) {
      return Result.error(ErrorCode.PARAM_ERROR, "参数不完整");
    }

    // 获取当前操作者的openid
    String operatorOpenid = UserContext.getOpenId();

    // 调用服务设置信息
    boolean success = infoService.saveOrUpdateInfo(infoDTO, operatorOpenid);

    return Result.success(success);
  }

  /**
   * 设置商务合作信息
   */
  @Operation(summary = "设置商务合作信息", description = "管理员设置商务合作信息和客服链接")
  @ApiResponses({
      @ApiResponse(code = 200, message = "设置成功", response = Result.class),
      @ApiResponse(code = 400, message = "参数错误"),
      @ApiResponse(code = 401, message = "未登录或token已过期"),
      @ApiResponse(code = 403, message = "没有管理员权限"),
      @ApiResponse(code = 500, message = "服务器内部错误")
  })
  @PostMapping("/business")
  public Result<Boolean> setBusinessInfo(
      @RequestBody @Parameter(description = "商务合作信息", required = true, schema = @Schema(implementation = BusinessInfoDTO.class)) BusinessInfoDTO businessInfoDTO) {
    // 参数校验
    if (businessInfoDTO == null) {
      return Result.error(ErrorCode.PARAM_ERROR, "商务信息不能为空");
    }

    // 获取当前操作者的openid
    String operatorOpenid = UserContext.getOpenId();
    // 调用服务设置商务信息
    boolean success = infoService.saveBusinessInfo(businessInfoDTO, operatorOpenid);
    return Result.success(success);
  }

  /**
   * 根据键名获取信息
   */
  @Operation(summary = "根据键名获取信息", description = "管理员根据指定的键名查询系统信息")
  @ApiResponses({
      @ApiResponse(code = 200, message = "查询成功", response = Result.class),
      @ApiResponse(code = 401, message = "未登录或token已过期"),
      @ApiResponse(code = 403, message = "没有管理员权限"),
      @ApiResponse(code = 404, message = "信息不存在"),
      @ApiResponse(code = 500, message = "服务器内部错误")
  })
  @GetMapping("/get")
  public Result<InfoVO> getInfoByKey(
      @RequestParam @Parameter(description = "键名", required = true) String key) {

    // 查询指定键名的信息
    InfoVO infoVO = infoService.getInfoByKey(key);

    if (infoVO == null) {
      return Result.error(ErrorCode.RESOURCE_NOT_FOUND, "信息不存在");
    }

    return Result.success(infoVO);
  }

  /**
   * 设置合同信息
   */
  @Operation(summary = "设置合同信息", description = "管理员设置合同信息，内容为富文本格式")
  @ApiResponses({
      @ApiResponse(code = 200, message = "设置成功", response = Result.class),
      @ApiResponse(code = 400, message = "参数错误"),
      @ApiResponse(code = 401, message = "未登录或token已过期"),
      @ApiResponse(code = 403, message = "没有管理员权限"),
      @ApiResponse(code = 500, message = "服务器内部错误")
  })
  @PostMapping("/contract")
  public Result<Boolean> setContractInfo(
      @RequestBody @Parameter(description = "合同信息", required = true, schema = @Schema(implementation = ContractInfoDTO.class)) ContractInfoDTO contractInfoDTO) {
    // 参数校验
    if (contractInfoDTO == null || contractInfoDTO.getContent() == null) {
      return Result.error(ErrorCode.PARAM_ERROR, "合同内容不能为空");
    }

    // 获取当前操作者的openid
    String operatorOpenid = UserContext.getOpenId();
    // 调用服务设置合同信息
    boolean success = infoService.saveContractInfo(contractInfoDTO, operatorOpenid);
    return Result.success(success);
  }
}