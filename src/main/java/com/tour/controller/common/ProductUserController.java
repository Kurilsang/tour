package com.tour.controller.common;

import com.tour.common.Result;
import com.tour.dto.ProductDTO;
import com.tour.enums.ProductStatusEnum;
import com.tour.model.Product;
import com.tour.query.ProductQuery;
import com.tour.service.ProductService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通用的（包括未登录游客）的Product接口
 *
 * @Author Kuril
 */
@Api(tags = "通用产品接口", description = "通用的（包括未登录游客）的活动相关接口")
@RestController
@RequestMapping("/api/common/product")
public class ProductUserController {

    @Autowired
    private ProductService productService;

    /**
     * 加载所有产品内容
     *
     * @return 返回所有产品列表
     */
    @Operation(summary = "根据条件加载所有产品内容", description = "根据条件获取所有产品的列表信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "加载成功", response = Result.class),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/loadDataList")
    public Result loadDataList(@RequestBody ProductQuery productQuery) {

//      用户只能看到上架的
        productQuery.setStatus(ProductStatusEnum.ON.getCode());

        return Result.success(productService.findByPage(productQuery));
    }
    /**
     * 根据ID获取对应产品信息
     *
     * @param id product的id
     */
    @Operation(summary = "根据ID获取对应产品信息", description = "通过产品ID获取该产品的详细信息")
    @ApiResponses({
            @ApiResponse(code = 200, message = "获取成功", response = Result.class),
            @ApiResponse(code = 400, message = "产品ID不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/getProductById/{id}")
    public Result getProduct(@PathVariable @Parameter(description = "产品ID", required = true) Long id) {

        return Result.success(productService.findProductById(id));
    }

}