package com.ys.PressureTest.product;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2018/4/13.
 */

public class Rk3368 extends RK {
    static final String RTC_PATH = "/sys/devices/ff150000.i2c/i2c-3/3-0051/rtc/rtc0/time";
    static final String[] LED_PATH = new String[]{"/sys/devices/misc_power_en.22/green_led", "/sys/devices/misc_power_en.23/green_led"};
    public static final Rk3368 INSTANCE =  new Rk3368();
    private Rk3368(){}
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
        return "/sys/devices/misc_power_en.22/usb_3g";
    }

    @Override
    public void takeBrightness(Context context) {
        context.startActivity(new Intent("android.intent.action.SHOW_BRIGHTNESS_DIALOG"));
    }
}
