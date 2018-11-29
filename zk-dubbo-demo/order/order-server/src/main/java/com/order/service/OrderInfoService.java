package com.order.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.order.dao.OrderInfoDao;
import com.order.domain.OrderInfo;
import com.product.api.dubbo.ProductService;
import com.product.common.DecreaseStockInput;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @author: mSun
 * @date: 2018/11/29
 */
@Service
public class OrderInfoService {

    @Reference(
            version= "${product.service.version}",
            application = "${dubbo.application.id}",
            registry = "${dubbo.registry.id}",
            timeout = 10000
    )
    private ProductService productService;

    @Autowired
    OrderInfoDao orderInfoDao;

    /**
     * 创建订单
     * @param orderInfo
     * @return
     */
    public OrderInfo createOrderInfo(OrderInfo orderInfo) {

        Random random = new Random();
        Integer orderId = random.nextInt(900000)+100000;
        orderInfo.setOrderId(orderId);

        DecreaseStockInput decreaseStockInput = new DecreaseStockInput();
        BeanUtils.copyProperties(orderInfo, decreaseStockInput);
        System.out.println(decreaseStockInput);
        // 扣除库存
        productService.decreaseStock(decreaseStockInput);
        // 保存订单
        orderInfoDao.saveOrderInfo(orderInfo);
        return orderInfo;
    }

}
