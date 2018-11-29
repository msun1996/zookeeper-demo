package com.product.enums;

import lombok.Getter;

/**
 * author: mSun
 * date: 2018/11/27
 */
@Getter
public enum  ResultEnum {
    PRODUCT_STOCK_ERROR(1, "库存有误"),
    ;

    private Integer code;

    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
