package com.tour.common;

import lombok.Data;

/**
 * 统一响应结果类
 *
 * @Author Abin
 */
@Data
public class Result<T> {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 成功返回结果
     *
     * @param data 返回数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 失败返回结果
     *
     * @param message 错误信息
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败返回结果
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
} 