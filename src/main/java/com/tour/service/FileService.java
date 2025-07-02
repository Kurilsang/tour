package com.tour.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @Author Abin
 * @Description 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传文件
     * @param file 文件
     * @param type 文件类型目录
     * @param openid 用户openid，可以为空
     * @return 包含文件URL信息的Map
     */
    Map<String, String> uploadFile(MultipartFile file, String type, String openid) throws IOException;
    
    /**
     * 上传文件(File类型)
     * @param file 文件(File类型)
     * @param type 文件类型目录
     * @param openid 用户openid，可以为空
     * @return 包含文件URL信息的Map
     */
    Map<String, String> uploadFile(File file, String type, String openid) throws IOException;
    
    /**
     * 本地上传文件
     * @param file 文件
     * @param type 文件类型目录
     * @return 包含文件URL信息的Map
     */
    Map<String, String> uploadFileLocal(MultipartFile file, String type) throws IOException;
    
    /**
     * 本地上传文件(File类型)
     * @param file 文件(File类型)
     * @param type 文件类型目录
     * @return 包含文件URL信息的Map
     */
    Map<String, String> uploadFileLocal(File file, String type) throws IOException;
} 