package com.tour.controller;

import com.tour.common.Result;
import com.tour.common.util.UserContext;
import com.tour.service.FileService;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 文件上传控制器
 *
 * @Author Abin
 */
@Api(tags = "文件管理", description = "文件上传相关接口")
@RestController
@RequestMapping("/api/file")
@Slf4j
public class FileController {

    @Autowired
    private FileService fileService;
    
    @Value("${spring.profiles.active}")
    private String activeProfile;

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @param type 文件类型，用于确定存储路径
     * @return 包含文件访问URL的结果
     */
    @Operation(summary = "文件上传", description = "上传文件并返回访问URL，支持通过key参数指定不同的存储目录")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponse(responseCode = "200", description = "", content = @Content(schema = @Schema(implementation = Result.class)))
    @ApiResponse(responseCode = "400", description = "")
    @ApiResponse(responseCode = "500", description = "服务器内部错误")
    @ApiResponses({
            @io.swagger.annotations.ApiResponse(code = 200, message = "上传成功", response = Result.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "请求参数错误"),
            @io.swagger.annotations.ApiResponse(code = 500, message = "服务器内部错误")
    })
    public Result<Map<String, String>> uploadFile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "上传的文件", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "file", required = true) MultipartFile file,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "文件类型，如：avatar、temp等，默认为 temp", required = false)
            @RequestPart(value = "type", required = false) String type) {

        try {
            log.info("当前环境: {}, 接收到文件上传请求: type={}, originalFilename={}",
                    activeProfile, type, file.getOriginalFilename());
                    
            // 获取当前登录用户的openid
            String openid = UserContext.getOpenId();
            
            // 使用文件服务上传
            Map<String, String> result = fileService.uploadFile(file, type, openid);
            
            return Result.success(result);
        } catch (IOException e) {
            log.error("文件上传失败：", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
} 