/**
 * @Author Abin
 * @Description 信息控制器
 */
package com.tour.controller;

import com.tour.common.Result;
import com.tour.dto.BusinessInfoDTO;
import com.tour.dto.ContractInfoDTO;
import com.tour.service.IInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 信息控制器
 * 处理用户查询信息的接口
 */
@Api(tags = "信息查询接口", description = "用户查询系统信息")
@RestController
@RequestMapping("/api/info")
public class InfoController {

  @Autowired
  private IInfoService infoService;

  /**
   * 获取商务合作信息
   */
  @Operation(summary = "获取商务合作信息", description = "查询系统存储的商务合作信息")
  @ApiResponses({
      @ApiResponse(code = 200, message = "查询成功", response = Result.class),
      @ApiResponse(code = 404, message = "信息不存在"),
      @ApiResponse(code = 500, message = "服务器内部错误")
  })
  @GetMapping("/business")
  public Result<BusinessInfoDTO> getBusinessInfo() {
    // 查询商务信息
    BusinessInfoDTO businessInfoDTO = infoService.getBusinessInfo();
    return Result.success(businessInfoDTO);
  }

  /**
   * 获取合同信息
   */
  @Operation(summary = "获取合同信息", description = "查询系统存储的合同信息，内容为富文本格式")
  @ApiResponses({
      @ApiResponse(code = 200, message = "查询成功", response = Result.class),
      @ApiResponse(code = 404, message = "信息不存在"),
      @ApiResponse(code = 500, message = "服务器内部错误")
  })
  @GetMapping("/contract")
  public Result<ContractInfoDTO> getContractInfo() {
    // 查询合同信息
    ContractInfoDTO contractInfoDTO = infoService.getContractInfo();
    return Result.success(contractInfoDTO);
  }

  /**
   * 根据键名获取信息
   */
  // @Operation(summary = "根据键名获取信息", description = "根据指定的键名查询系统信息")
  // @ApiResponses({
  // @ApiResponse(code = 200, message = "查询成功", response = Result.class),
  // @ApiResponse(code = 404, message = "信息不存在"),
  // @ApiResponse(code = 500, message = "服务器内部错误")
  // })
  // @GetMapping("/get")
  // public Result<InfoVO> getInfoByKey(
  // @RequestParam @Parameter(description = "键名", required = true) String key) {
  // // 查询指定键名的信息
  // InfoVO infoVO = infoService.getInfoByKey(key);

  // if (infoVO == null) {
  // // 如果信息不存在，返回空对象而不是null
  // infoVO = new InfoVO();
  // infoVO.setKey(key);
  // infoVO.setValue("{}");
  // }

  // return Result.success(infoVO);
  // }
}