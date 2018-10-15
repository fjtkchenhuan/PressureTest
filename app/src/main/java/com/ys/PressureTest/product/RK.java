package com.ys.PressureTest.product;

import android.content.Context;

import java.io.File;

/**
 * Created by Administrator on 2018/4/14.
 */

public abstract class RK {
    /**
     * 获取RTC信息文件路径，用于测试RTC是否正常
     *
     * @return
     */
    public abstract String getRtcPath();

    /**
     * 获取LED灯信息文件路径，控制led等亮灭
     *
     * @return
     */
    public abstract String getLedPath();

    /**
     * 获取4G复位io口路径
     * @return
     */
    public abstract String get4GPath();

    /**
     * 调用亮度调节功能
     *
     * @param context
     */
    public abstract void takeBrightness(Context context);

    /**
     * 过滤文件路径
     *
     * @param filePaths
     * @return
     */
    protected String filterPath(String[] filePaths) {
        if (filePaths == null) return null;
        String filterPath = null;
        for (String path : filePaths) {
            if (new File(path).exists()) {
                filterPath = path;
                break;
            }
        }
        return filterPath;
    }
}
