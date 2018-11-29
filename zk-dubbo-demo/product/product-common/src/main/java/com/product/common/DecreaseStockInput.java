package com.product.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 扣库存输入参数
 * author: mSun
 * date: 2018/11/27
 */
@Data
public class DecreaseStockInput  implements Serializable {

    // 商品Id
    private Integer productId;

    // 商品数量
    private Integer productQuantity;

    public DecreaseStockInput() {
    }

    public DecreaseStockInput(Integer productId, Integer productQuantity) {
        this.productId = productId;
        this.productQuantity = productQuantity;
    }

}
