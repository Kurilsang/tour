package com.tour.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @Author Abin
 */
@Getter
public class ServiceException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    public ServiceException(String message) {
        this.code = 500;
        this.message = message;
    }

    public ServiceException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}