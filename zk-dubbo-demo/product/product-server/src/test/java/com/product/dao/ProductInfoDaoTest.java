package com.product.dao;

import com.product.domain.ProductInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductInfoDaoTest {

    @Autowired
    ProductInfoDao productInfoDao;

    @Test
    public void getByProductId() {
        ProductInfo productInfo = productInfoDao.getByProductId(1);
        System.out.println(productInfo);
    }

    @Test
    public void updateProduct() {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setProductId(1);
        productInfo.setProductStock(10);
        productInfoDao.updateProduct(productInfo);
    }
}