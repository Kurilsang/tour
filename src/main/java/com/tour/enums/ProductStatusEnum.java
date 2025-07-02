package com.tour.enums;

public enum ProductStatusEnum {
    /**
     * 下架
     */
    OFF(1, "下架"),

    /**
     * 上架
     */
    ON(2, "上架");

    /**
     * 状态代码
     */
    private final Integer code;

    /**
     * 状态名称
     */
    private final String name;

    ProductStatusEnum(Integer code, String name) {
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
