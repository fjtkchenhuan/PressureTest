package com.ys.PressureTest.product;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2018/4/13.
 */

public class Rk3399 extends RK {
    static final String RTC_PATH = "/sys/devices/platform/ff120000.i2c/i2c-2/2-0051/rtc/rtc0/time";
    static final String[] LED_PATH = new String[]{"/sys/devices/platform/misc_power_en/red_led"};
    public static final Rk3399 INSTANCE = new Rk3399();
    private Rk3399(){}
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
