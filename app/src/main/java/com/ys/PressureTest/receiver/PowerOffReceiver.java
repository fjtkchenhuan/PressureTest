package com.ys.PressureTest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.ys.PressureTest.Constant;

/**
 * Created by Administrator on 2018/7/13.
 */

public class PowerOffReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.SP_POWER_ON_OFF,0);
        if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
            sharedPreferences.edit().putLong(Constant.SP_POWER_OFF_TIME,System.currentTimeMillis()).apply();

        }
    }
}
