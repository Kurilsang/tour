package com.tour.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tour.common.Result;
import com.tour.dto.AdminRefundAcceptDTO;
import com.tour.dto.RefundRejectDTO;
import com.tour.query.RefundApplyQuery;
import com.tour.service.RefundApplyService;
import com.tour.vo.RefundApplyVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员退款控制器
 *
 * @Author Kuril
 */
@Api(tags = "管理员退款接口", description = "管理员处理退款申请相关接口")
@RestController
@RequestMapping("/api/admin/refund")
@Slf4j
public class AdminRefundController {

    private final RefundApplyService refundApplyService;

    @Autowired
    public AdminRefundController(RefundApplyService refundApplyService) {
        this.refundApplyService = refundApplyService;
    }

    /**
     * 加载所有退款申请信息
     *
     * @param refundApplyQuery 查询条件
     * @return 退款申请列表
     */
    @ApiOperation(value = "加载退款申请列表", notes = "管理员根据条件查询所有退款申请列表")
    @ApiResponses({
            @ApiResponse(code = 200, message = "查询成功"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/loadDataList")
    public Result loadDataList(@RequestBody RefundApplyQuery refundApplyQuery) {
        log.info("管理员退款申请loadDataList，参数：{}", refundApplyQuery);
        
        // 查询退款申请列表
        IPage<RefundApplyVO> refundApplies = refundApplyService.loadDataList(refundApplyQuery);
        return Result.success(refundApplies);
    }
    
    /**
     * 拒绝退款请求
     *
     * @param rejectDTO 拒绝退款参数
     * @return 处理结果
     */
    @ApiOperation(value = "拒绝退款请求", notes = "管理员拒绝用户的退款申请")
    @ApiResponses({
            @ApiResponse(code = 200, message = "拒绝成功"),
            @ApiResponse(code = 400, message = "参数错误或退款申请状态不正确"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/refuse")
    public Result refuse(@RequestBody RefundRejectDTO rejectDTO) {
        log.info("管理员拒绝退款请求：{}", rejectDTO);
        
        // 拒绝退款申请
        boolean result = refundApplyService.refuseRefundApply(rejectDTO.getId(), rejectDTO.getAdminRemark());
        return Result.success(result);
    }

    /**
     * 接受退款申请，并执行退款操作
     *
     * @param acceptDTO 接受退款申请DTO
     * @return 操作结果
     */
    @ApiOperation(value = "接受退款申请", notes = "管理员接受退款申请，并执行退款操作，可选指定退款金额")
    @ApiResponses({
            @ApiResponse(code = 200, message = "处理成功", response = Result.class),
            @ApiResponse(code = 400, message = "参数错误或申请状态错误"),
            @ApiResponse(code = 401, message = "未登录或token已过期"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/accept")
    public Result acceptRefund(@RequestBody AdminRefundAcceptDTO acceptDTO) {
        log.info("接受退款申请，参数：{}", acceptDTO);
        
        if (acceptDTO.getId() == null) {
            return Result.error("退款申请ID不能为空");
        }
        
        boolean result;
        
        // 检查是否指定了退款金额
        if (acceptDTO.getSpecifiedAmount() != null) {
            // 使用指定金额处理退款
            result = refundApplyService.acceptRefundApply(
                    acceptDTO.getId(), 
                    acceptDTO.getAdminRemark(),
                    acceptDTO.getSpecifiedAmount()
            );
        } else {
            // 使用原来的方法
            result = refundApplyService.acceptRefundApply(
                    acceptDTO.getId(),
                    acceptDTO.getAdminRemark()
            );
        }
        
        if (result) {
            return Result.success("退款申请处理成功");
        } else {
            return Result.error("退款申请处理失败");
        }
    }
} 