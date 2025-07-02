package com.tour.common.util;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Author Abin
 * @Description 微信云托管对象存储工具类
 */
@Slf4j
@Component
public class COSUtil {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${cos.bucket}")
    private String bucket;
    
    @Value("${cos.region}")
    private String region;

    /**
     * 获取临时密钥
     * @return 临时密钥信息
     */
    private Map<String, String> getTemporaryKey() {
        try {
            String url = "http://api.weixin.qq.com/_/cos/getauth";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>("{}", headers);
            
            Map<String, String> response = restTemplate.postForObject(url, entity, Map.class);
            return response;
        } catch (Exception e) {
            log.error("获取临时密钥失败", e);
            throw new RuntimeException("获取COS临时密钥失败", e);
        }
    }
    
    /**
     * 获取文件元数据
     * @param openid 用户openid，管理端为空字符串
     * @param path 文件路径
     * @return 文件元数据
     */
    private String getFileMetadata(String openid, String path) {
        try {
            String url = "http://api.weixin.qq.com/_/cos/metaid/encode";
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("openid", openid);
            requestBody.put("bucket", bucket);
            requestBody.put("paths", Collections.singletonList(path));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            if (response != null && response.get("errcode").equals(0)) {
                Map<String, Object> respdata = (Map<String, Object>) response.get("respdata");
                return ((java.util.ArrayList<String>) respdata.get("x_cos_meta_field_strs")).get(0);
            }
            throw new RuntimeException("获取文件元数据失败: " + response);
        } catch (Exception e) {
            log.error("获取文件元数据失败", e);
            throw new RuntimeException("获取文件元数据失败", e);
        }
    }
    
    /**
     * 上传文件到COS
     * @param file 文件
     * @param openid 用户openid，管理端为空字符串
     * @param dir 存储目录
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String openid, String dir) throws IOException {
        // 临时文件
        File localFile = File.createTempFile("temp_", file.getOriginalFilename());
        file.transferTo(localFile);
        
        try {
            // 获取临时密钥
            Map<String, String> tempKey = getTemporaryKey();
            
            // 初始化COS客户端
            BasicSessionCredentials credentials = new BasicSessionCredentials(
                    tempKey.get("TmpSecretId"),
                    tempKey.get("TmpSecretKey"),
                    tempKey.get("Token"));
            
            ClientConfig clientConfig = new ClientConfig(new Region(region));
            COSClient cosClient = new COSClient(credentials, clientConfig);
            
            // 构建文件路径
            String extension = "";
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + extension;
            String key = dir + "/" + fileName;
            
            // 获取文件元数据
            String metaId = getFileMetadata(openid, key);
            
            // 设置元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata("fileid", metaId);
            
            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, localFile);
            putObjectRequest.setMetadata(metadata);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            
            // 关闭客户端
            cosClient.shutdown();
            
            // 返回访问链接格式: https://[存储桶名称].tcb.qcloud.la/[文件路径]
            return "https://" + bucket + ".tcb.qcloud.la/" + key;
        } catch (Exception e) {
            log.error("上传文件到COS失败", e);
            throw new RuntimeException("上传文件到COS失败:" + e.getMessage(), e);
        } finally {
            // 删除临时文件
            if (localFile.exists()) {
                Files.delete(localFile.toPath());
            }
        }
    }

    /**
     * 上传File对象到COS
     * @param file File对象
     * @param openid 用户openid，管理端为空字符串
     * @param dir 存储目录
     * @return 文件访问URL
     */
    public String uploadFile(File file, String openid, String dir) throws IOException {
        try {
            // 获取临时密钥
            Map<String, String> tempKey = getTemporaryKey();
            
            // 初始化COS客户端
            BasicSessionCredentials credentials = new BasicSessionCredentials(
                    tempKey.get("TmpSecretId"),
                    tempKey.get("TmpSecretKey"),
                    tempKey.get("Token"));
            
            ClientConfig clientConfig = new ClientConfig(new Region(region));
            COSClient cosClient = new COSClient(credentials, clientConfig);
            
            // 构建文件路径
            String fileName = file.getName();
            String extension = "";
            if (fileName.contains(".")) {
                extension = fileName.substring(fileName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString().replaceAll("-", "") + extension;
            String key = dir + "/" + newFileName;
            
            // 获取文件元数据
            String metaId = getFileMetadata(openid, key);
            
            // 设置元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata("fileid", metaId);
            
            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, file);
            putObjectRequest.setMetadata(metadata);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            
            // 关闭客户端
            cosClient.shutdown();
            
            // 返回访问链接格式: https://[存储桶名称].tcb.qcloud.la/[文件路径]
            return "https://" + bucket + ".tcb.qcloud.la/" + key;
        } catch (Exception e) {
            log.error("上传文件到COS失败", e);
            throw new RuntimeException("上传文件到COS失败:" + e.getMessage(), e);
        }
    }
} 