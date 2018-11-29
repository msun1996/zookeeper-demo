package com.order.controller;

import com.order.VO.ResultVO;
import com.order.domain.OrderInfo;
import com.order.service.OrderInfoService;
import com.order.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: mSun
 * @date: 2018/11/29
 */
@RestController
public class OrderController {

    @Autowired
    OrderInfoService orderInfoService;

    @PostMapping("/createOrder")
    public ResultVO createOrder(@RequestBody OrderInfo orderInfo) {
        OrderInfo orderInfo1 = orderInfoService.createOrderInfo(orderInfo);
        return ResultUtil.success(orderInfo1);
    }

}
