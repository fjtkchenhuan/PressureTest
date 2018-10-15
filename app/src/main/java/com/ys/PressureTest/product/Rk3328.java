package com.ys.PressureTest.product;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2018/4/13.
 */

public class Rk3328 extends RK {
    static final String RTC_PATH = "/sys/devices/ff160000.i2c/i2c-1/1-0051/rtc/rtc0/time";
    static final String[] LED_PATH = new String[]{"/sys/devices/misc_power_en.3/led"};
    public final static Rk3328 INSTANCE = new Rk3328();
    private Rk3328(){}
    @Override
    public String getRtcPath() {
        return RTC_PATH;
    }

    @Override
    public String getLedPath() {
        return filterPath(LED_PATH);
    }

    @Override
    public String get4GPath() {
        return "";
    }

    @Override
    public void takeBrightness(Context context) {
        context.startActivity(new Intent("android.intent.action.SHOW_BRIGHTNESS_DIALOG"));
    }
}
