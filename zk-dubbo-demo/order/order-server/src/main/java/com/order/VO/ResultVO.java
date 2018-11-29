package com.order.VO;

import lombok.Data;

/**
 * 响应结果
 * @author: mSun
 * @date: 2018/11/29
 */
@Data
public class ResultVO<T> {
    // 响应码
    private Integer code;

    // 信息
    private String message;

    // 具体数据
    private T data;
}
