package com.ys.PressureTest.utils;

import android.os.Handler;
import android.os.Message;


import com.ys.PressureTest.product.RkFactory;

import java.io.File;

public class LedController {

    private static final String TAG = "LedController";
    private File green_led = null;
    private boolean enable = false;


    public LedController() {
        String filePath = RkFactory.getRK().getLedPath();
        if (filePath != null) {
            green_led = new File(filePath);
            enable = green_led.exists();
        }
    }

    public void start() {
        if (enable)
            ledHandler.post(ledRunnable);
    }

    public void stop() {
        if (enable)
            ledHandler.removeCallbacks(ledRunnable);
    }

    public void release() {
        stop();
        reset();
        ledHandler = null;
        ledRunnable = null;
    }

    public void reset() {
        if (enable)
            FileUtils.writeFile(green_led, "1");
    }


    Handler ledHandler = new Handler(new Handler.Callback() {
        boolean ledLight = true;

        @Override
        public boolean handleMessage(Message msg) {
            ledLight = !ledLight;
            if (ledLight) {
                FileUtils.writeFile(green_led, "1");
            } else {
                FileUtils.writeFile(green_led, "0");
            }
            ledHandler.postDelayed(ledRunnable, 1000);
            return true;
        }
    });

    Runnable ledRunnable = new Runnable() {
        @Override
        public void run() {
            ledHandler.sendEmptyMessage(0);
        }
    };
}
