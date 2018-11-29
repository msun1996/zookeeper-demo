package com.order.dao;

import com.order.domain.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 订单信息
 * @author: mSun
 * @date: 2018/11/29
 */
@Mapper
@Repository
public interface OrderInfoDao {

    void saveOrderInfo(OrderInfo orderInfo);

}