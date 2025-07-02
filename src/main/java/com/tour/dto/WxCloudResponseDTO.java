package com.tour.dto;

import lombok.Data;

/**
 * 微信云托管响应DTO
 */
@Data
public class WxCloudResponseDTO {
    /**
     * 错误码
     */
    private Integer errcode;
    
    /**
     * 错误信息
     */
    private String errmsg;
    
    /**
     * 创建成功响应
     */
    public static WxCloudResponseDTO success() {
        WxCloudResponseDTO response = new WxCloudResponseDTO();
        response.setErrcode(0);
        response.setErrmsg("success");
        return response;
    }
    
    /**
     * 创建失败响应
     * 
     * @param errmsg 错误信息
     */
    public static WxCloudResponseDTO error(String errmsg) {
        WxCloudResponseDTO response = new WxCloudResponseDTO();
        response.setErrcode(1);
        response.setErrmsg(errmsg);
        return response;
    }
} 