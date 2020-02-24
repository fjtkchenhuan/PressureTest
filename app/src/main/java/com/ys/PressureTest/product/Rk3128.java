package com.ys.PressureTest.product;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2018/4/13.
 */

public class Rk3128 extends RK {
    static final String[] LED_PATH = new String[]{"/sys/devices/misc_power_en.19/out8", "/sys/devices/misc_power_en.18/out8"};
    static final String RTC_PATH = "/sys/devices/20072000.i2c/i2c-0/0-0051/rtc/rtc0/time";
    public static final Rk3128 INSTANCE = new Rk3128();

    private Rk3128() {
    }

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
        return "/sys/devices/misc_power_en.19/usb_3g";
    }

    @Override
    public void takeBrightness(Context context) {
        Intent intent = new Intent("com.ys.show_brightness_dialog");
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.setPackage("com.ys.ys_receiver");
        context.sendBroadcast(intent);
    }
}
