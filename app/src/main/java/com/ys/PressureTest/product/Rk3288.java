package com.ys.PressureTest.product;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2018/4/13.
 */

public class Rk3288 extends RK {
    static final String RTC_PATH = "/sys/devices/ff650000.i2c/i2c-0/0-0051/rtc/rtc0/time";
    static final String[] LED_PATH = new String[]{"/sys/devices/misc_power_en.22/green_led", "/sys/devices/misc_power_en.23/green_led"};
    public final static Rk3288 INSTANCE = new Rk3288();
    private Rk3288(){}
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
        return "/sys/devices/misc_power_en.23/usb_3g";
    }

    @Override
    public void takeBrightness(Context context) {
        context.startActivity(new Intent("android.intent.action.SHOW_BRIGHTNESS_DIALOG"));
    }
}
