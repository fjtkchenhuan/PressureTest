package com.ys.PressureTest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Administrator on 2018/7/30.
 */

public class CountDownReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.CountdownBegin")) {
            Intent intent1 = new Intent("android.intent.ClearOnOffTime");
            intent1.setPackage("com.adtv");
            context.sendBroadcast(intent1);
        }

    }
}
