package com.product.domain;

import lombok.Data;

/**
 * 商品信息
 * author: mSun
 * date: 2018/11/27
 */
@Data
public class ProductInfo {

    // 编号
    private Integer productId;

    // 名字
    private String productName;

    // 库存
    private Integer productStock;

}
