package com.product.api.dubbo;

import com.product.common.DecreaseStockInput;
import com.product.common.ProductInfoOutput;

/**
 * Product向外提供服务接口(Dubbo)
 * author: mSun
 * date: 2018/11/27
 */
public interface ProductService {

    /**
     * 查库存
     */
    ProductInfoOutput getByProductId(Integer productId);

    /**
     * 扣库存
     */
    void decreaseStock(DecreaseStockInput decreaseStockInput);

}
