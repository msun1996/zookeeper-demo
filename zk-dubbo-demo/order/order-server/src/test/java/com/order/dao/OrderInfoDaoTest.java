package com.order.dao;

import com.order.domain.OrderInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderInfoDaoTest {

    @Autowired
    OrderInfoDao orderInfoDao;

    @Test
    public void saveOrderInfo() {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderId(2);
        orderInfo.setProductId(1);
        orderInfo.setProductName("a");
        orderInfo.setProductQuantity(1);
        orderInfoDao.saveOrderInfo(orderInfo);
    }


}