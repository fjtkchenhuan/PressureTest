package com.ys.PressureTest.product;


import com.ys.PressureTest.utils.ModelUtils;

/**
 * Created by Administrator on 2018/4/13.
 */

public class RkFactory {

    public static RK getRK() {
        String product = ModelUtils.getRKModel();
        if (product.contains("rk3328")) {
            return Rk3328.INSTANCE;
        }else if (product.contains("rk3399")) {
            return Rk3399.INSTANCE;
        }else if (product.contains("rk3368")) {
            return Rk3368.INSTANCE;
        } else if (product.contains("rk3288")) {
            return Rk3288.INSTANCE;
        } else if (product.contains("rk3128")) {
            return Rk3128.INSTANCE;
        }
        return Rk3368.INSTANCE;
    }
}
