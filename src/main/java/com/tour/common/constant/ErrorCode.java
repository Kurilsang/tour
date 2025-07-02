package com.tour.common.constant;

/**
 * 错误码常量
 *
 * @Author Abin
 */
public interface ErrorCode {
    /**
     * 成功
     */
    int SUCCESS = 0;

    /**
     * 未授权
     */
    int UNAUTHORIZED = 401;

    /**
     * 禁止访问
     */
    int FORBIDDEN = 403;

    /**
     * 系统错误
     */
    int SYSTEM_ERROR = 500;

    /**
     * 参数错误
     */
    int PARAM_ERROR = 400;

    /**
     * 业务错误
     */
    int BIZ_ERROR = 501;

    /**
     * 未知错误
     */
    int UNKNOWN_ERROR = 999;
    
    /**
     * 资源不存在
     */
    int RESOURCE_NOT_FOUND = 404;

    /**
     * 内容不合规
     */
    int CONTENT_RISKY = 4001;
}