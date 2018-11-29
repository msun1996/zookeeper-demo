package com.product.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品信息输出
 * author: mSun
 * date: 2018/11/27
 */
@Data
public class ProductInfoOutput implements Serializable {

    // 编号
    private Integer productId;

    // 名字
    private String productName;

    // 库存
    private Integer productStock;

}
