/**
 * @Author Kuril
 * @Description 评论状态枚举
 */
package com.tour.enums;

public enum CommentStatusEnum {
    /**
     * 待审核
     */
    PENDING(1, "待审核"),
    
    /**
     * 可见
     */
    VISIBLE(2, "可见"),
    
    /**
     * 不可见
     */
    INVISIBLE(3, "不可见");

    /**
     * 状态代码
     */
    private final Integer code;
    
    /**
     * 状态名称
     */
    private final String name;

    CommentStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }
} 