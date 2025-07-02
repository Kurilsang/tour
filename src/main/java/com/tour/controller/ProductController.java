package com.tour.controller;

import com.tour.common.Result;
import com.tour.dto.ProductDTO;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品管理员接口
 *
 * @Author Kuril
 */
@Api(tags = "产品管理员接口", description = "产品管理员接口")
@RestController
@RequestMapping("/api/admin/product")
public class ProductController {

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

    /**
     * 添加产品及详情内容接口
     *
     * @param productDTO 具体需要前端参数详见该类
     * @return 添加产品成功返回null
     */
    @Operation(summary = "添加产品及详情内容", description = "添加新的产品")
    @ApiResponses({
            @ApiResponse(code = 200, message = "添加成功", response = Result.class),
            @ApiResponse(code = 400, message = "产品信息不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/addProduct")
    public Result addProduct(@RequestBody
                             @Parameter(description = "产品信息", required = true,
                                     schema = @Schema(implementation = ProductDTO.class))
                                 ProductDTO productDTO) {
        productService.addProduct(productDTO);

        return Result.success(null);
    }

    /**
     * 根据ID删除产品及详情内容接口
     *
     * @param id product的id
     * @return 删除产品成功返回null
     */
    @Operation(summary = "根据ID删除产品及详情内容", description = "通过产品ID删除该产品")
    @ApiResponses({
            @ApiResponse(code = 200, message = "删除成功", response = Result.class),
            @ApiResponse(code = 400, message = "产品ID不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/deleteProductById/{id}")
    public Result deleteProductById(@PathVariable
                                    @Parameter(description = "产品ID", required = true)
                                    Long id) {

        productService.deleteProductById(id);

        return Result.success(null);
    }
    
    /**
     * 批量删除产品及详情内容接口
     *
     * @param ids 产品ID列表，格式为"1,2,3"
     * @return 批量删除成功返回null
     */
    @Operation(summary = "批量删除产品及详情内容", description = "通过产品ID列表批量删除产品")
    @ApiResponses({
            @ApiResponse(code = 200, message = "批量删除成功", response = Result.class),
            @ApiResponse(code = 400, message = "产品ID列表不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @GetMapping("/batchDeleteProducts/{ids}")
    public Result batchDeleteProducts(@PathVariable
                                     @Parameter(description = "产品ID列表，格式为'1,2,3'", required = true)
                                     String ids) {
        
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        
        productService.batchDeleteProducts(idList);
        
        return Result.success(null);
    }
    
    /**
     * 切换产品状态接口
     *
     * @param id 产品ID
     * @param status 目标状态(1-下架 2-上架)
     * @return 状态切换成功返回null
     */
    @Operation(summary = "切换产品状态", description = "切换产品的上架/下架状态")
    @ApiResponses({
            @ApiResponse(code = 200, message = "状态切换成功", response = Result.class),
            @ApiResponse(code = 400, message = "产品ID或状态参数错误"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PutMapping("/toggleStatus/{id}/{status}")
    public Result toggleProductStatus(
            @PathVariable @Parameter(description = "产品ID", required = true) Long id,
            @PathVariable @Parameter(description = "目标状态(1-下架 2-上架)", required = true) Integer status) {
        
        productService.toggleProductStatus(id, status);

        return Result.success(null);
    }

    /**
     * 根据ID更新产品及详情内容接口
     *
     * @param id         product的id
     * @param productDTO 具体需要前端参数详见该类
     * @return 修改产品成功返回null
     */
    @Operation(summary = "根据ID更新产品及详情内容", description = "通过产品ID更新该产品")
    @ApiResponses({
            @ApiResponse(code = 200, message = "更新成功", response = Result.class),
            @ApiResponse(code = 400, message = "产品ID和产品信息不能为空"),
            @ApiResponse(code = 500, message = "服务器内部错误")
    })
    @PostMapping("/updateProductById/{id}")
    public Result updateProductById(@PathVariable
                                    @Parameter(description = "产品ID", required = true)
                                    Long id,
                                    @RequestBody
                                    @Parameter(description = "产品信息", required = true,
                                            schema = @Schema(implementation = ProductDTO.class))
                                    ProductDTO productDTO) {

        productService.updateProduct(id,productDTO);

        return Result.success(null);
    }
}