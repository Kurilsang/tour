package com.tour.service.impl;

import com.tour.common.util.COSUtil;
import com.tour.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Author Abin
 * @Description 文件服务实现类
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private COSUtil cosUtil;
    
    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.domain}")
    private String domain;
    
    @Value("${spring.profiles.active}")
    private String activeProfile;

    /**
     * 上传文件，根据环境选择上传方式
     * @param file 文件
     * @param type 文件类型目录
     * @param openid 用户openid，可以为空
     * @return 包含文件URL信息的Map
     */
    @Override
    public Map<String, String> uploadFile(MultipartFile file, String type, String openid) throws IOException {
        // 根据环境选择上传方式
        if ("prod".equals(activeProfile)) {
            log.info("生产环境：使用COS对象存储上传文件");
            return uploadFileToCOS(file, type, openid);
        } else {
            log.info("开发环境：使用本地存储上传文件");
            return uploadFileLocal(file, type);
        }
    }
    
    /**
     * 上传文件(File类型)，根据环境选择上传方式
     * @param file 文件(File类型)
     * @param type 文件类型目录
     * @param openid 用户openid，可以为空
     * @return 包含文件URL信息的Map
     */
    @Override
    public Map<String, String> uploadFile(File file, String type, String openid) throws IOException {
        // 根据环境选择上传方式
        if ("prod".equals(activeProfile)) {
            log.info("生产环境：使用COS对象存储上传File类型文件");
            return uploadFileToCOS(file, type, openid);
        } else {
            log.info("开发环境：使用本地存储上传File类型文件");
            return uploadFileLocal(file, type);
        }
    }
    
    /**
     * 上传文件到COS对象存储
     * @param file 文件
     * @param type 文件类型目录
     * @param openid 用户openid，可以为空
     * @return 包含文件URL信息的Map
     */
    private Map<String, String> uploadFileToCOS(MultipartFile file, String type, String openid) throws IOException {
        if (type == null || type.isEmpty()) {
            type = "temp";
        }
        
        // 如果openid为null，设置为空字符串（管理端上传）
        if (openid == null) {
            openid = "";
        }
        
        // 上传文件到对象存储
        String fileUrl = cosUtil.uploadFile(file, openid, type);
        
        // 文件名
        String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        
        // 返回结果
        Map<String, String> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("filename", filename);
        
        log.info("文件上传到COS成功：type={}, filename={}, url={}", type, filename, fileUrl);
        return result;
    }
    /**
     * 上传文件到COS对象存储
     * @param file 文件
     * @param type 文件类型目录
     * @param openid 用户openid，可以为空
     * @return 包含文件URL信息的Map
     */
    private Map<String, String> uploadFileToCOS(File file, String type, String openid) throws IOException {
        if (type == null || type.isEmpty()) {
            type = "temp";
        }

        // 如果openid为null，设置为空字符串（管理端上传）
        if (openid == null) {
            openid = "";
        }

        // 上传文件到对象存储
        String fileUrl = cosUtil.uploadFile(file, openid, type);

        // 文件名
        String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

        // 返回结果
        Map<String, String> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("filename", filename);

        log.info("文件上传到COS成功：type={}, filename={}, url={}", type, filename, fileUrl);
        return result;
    }
    /**
     * 本地上传文件
     * @param file 文件
     * @param type 文件类型目录
     * @return 包含文件URL信息的Map
     */
    @Override
    public Map<String, String> uploadFileLocal(MultipartFile file, String type) throws IOException {
        if (type == null || type.isEmpty()) {
            type = "temp";
        }
        
        // 1. 生成文件存储路径
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = String.format("/%s/%s", type, datePath);
        String absolutePath = uploadPath + relativePath;

        // 2. 确保目录存在
        Path dirPath = Paths.get(absolutePath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 3. 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString().replaceAll("-", "") + extension;

        // 4. 保存文件
        String filePath = absolutePath + File.separator + filename;
        File destFile = new File(filePath);
        file.transferTo(destFile);

        // 5. 生成访问URL
        String fileUrl = domain + relativePath + "/" + filename;

        // 6. 返回结果
        Map<String, String> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("filename", filename);

        log.info("文件上传到本地成功：type={}, filename={}, url={}", type, filename, fileUrl);
        return result;
    }
    
    /**
     * 本地上传文件(File类型)
     * @param file 文件(File类型)
     * @param type 文件类型目录
     * @return 包含文件URL信息的Map
     */
    @Override
    public Map<String, String> uploadFileLocal(File file, String type) throws IOException {
        if (type == null || type.isEmpty()) {
            type = "temp";
        }
        
        // 1. 生成文件存储路径
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = String.format("/%s/%s", type, datePath);
        String absolutePath = uploadPath + relativePath;

        // 2. 确保目录存在
        Path dirPath = Paths.get(absolutePath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 3. 生成唯一文件名
        String originalFilename = file.getName();
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString().replaceAll("-", "") + extension;

        // 4. 保存文件
        String filePath = absolutePath + File.separator + filename;
        File destFile = new File(filePath);
        Files.copy(file.toPath(), destFile.toPath());

        // 5. 生成访问URL
        String fileUrl = domain + relativePath + "/" + filename;

        // 6. 返回结果
        Map<String, String> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("filename", filename);

        log.info("文件(File类型)上传到本地成功：type={}, filename={}, url={}", type, filename, fileUrl);
        return result;
    }

} 