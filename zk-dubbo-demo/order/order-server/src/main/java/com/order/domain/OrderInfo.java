package com.order.domain;

import lombok.Data;

@Data
public class OrderInfo {

    // 订单Id
    private Integer orderId;

    // 商品Id
    private Integer productId;

    // 商品名
    private String productName;

    // 商品数量
    private Integer productQuantity;

}
