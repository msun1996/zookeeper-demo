package com.order.utils;

import com.order.VO.ResultVO;

/**
 * @author: mSun
 * @date: 2018/11/29
 */
public class ResultUtil {
    public static ResultVO success(Object object) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(0);
        resultVO.setMessage("成功");
        resultVO.setData(object);
        return resultVO;
    }
}
