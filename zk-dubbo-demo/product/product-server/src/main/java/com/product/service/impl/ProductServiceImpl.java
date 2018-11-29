package com.product.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.product.api.dubbo.ProductService;
import com.product.common.DecreaseStockInput;
import com.product.common.ProductInfoOutput;
import com.product.dao.ProductInfoDao;
import com.product.domain.ProductInfo;
import com.product.enums.ResultEnum;
import com.product.exception.ProductException;
import com.product.lock.DistributedLockByCurator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 继承实现Dubbo定义类
 * author: mSun
 * date: 2018/11/27
 */
@Service(
        version = "${product.service.version}",
        application = "${dubbo.application.id}",
        protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}"
)
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductInfoDao productInfoDao;

    @Autowired
    private DistributedLockByCurator distributedLockByCurator;

    @Override
    public ProductInfoOutput getByProductId(Integer productId) {
        ProductInfo productInfo = productInfoDao.getByProductId(productId);
        ProductInfoOutput productInfoOutput = new ProductInfoOutput();
        BeanUtils.copyProperties(productInfo, productInfoOutput);
        return productInfoOutput;
    }

    @Override
    @Transactional
    public void decreaseStock(DecreaseStockInput decreaseStockInput) {

        distributedLockByCurator.acquireDistributedLock("decreaseStock");

        // 查询库存
        ProductInfo productInfo = productInfoDao.getByProductId(decreaseStockInput.getProductId());

        // 判断商品库存是否够
        Integer quantity = productInfo.getProductStock() - decreaseStockInput.getProductQuantity();
        if (quantity < 0) {
            distributedLockByCurator.releaseDistributedLock("decreaseStock");
            throw new ProductException(ResultEnum.PRODUCT_STOCK_ERROR);
        }

        // 模拟下高并发，增加延迟进行观察
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 更新库存
        productInfo.setProductStock(quantity);
        productInfoDao.updateProduct(productInfo);
        distributedLockByCurator.releaseDistributedLock("decreaseStock");
    }
}
