package com.product.dao;

import com.product.domain.ProductInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 商品信息
 * author: mSun
 * date: 2018/11/27
 */
@Mapper
@Repository
public interface ProductInfoDao {

    // 查询商品信息
    ProductInfo getByProductId(Integer productId);

    // 更新商品信息
    void updateProduct(ProductInfo productInfo);

}
