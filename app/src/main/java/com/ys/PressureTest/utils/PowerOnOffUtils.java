package com.ys.PressureTest.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by Administrator on 2018/7/5.
 */

public class PowerOnOffUtils {
    private static final String TAG = "PowerOnOffUtils";

    public static boolean isArrayHavePositiveNum(long[] subTime) {
        for (long l : subTime){
            if (l > 0) {
                return true;
            }
        }
        return false;
    }

    public static long getPowerOffSubTime(String txt) {
        String s = txt.replace(" ","").replace(".","").
                replace(":","").replace("-","").trim();
        int[] PowerOff = new int[5];
        PowerOff[0] = Integer.parseInt(s.substring(0,4));
        PowerOff[1] = Integer.parseInt(s.substring(4,6));
        PowerOff[2] = Integer.parseInt(s.substring(6,8));
        PowerOff[3] = Integer.parseInt(s.substring(8,10));
        PowerOff[4] = Integer.parseInt(s.substring(10,12));
        long powerOffTime = getTimeMills(PowerOff[0],PowerOff[1],PowerOff[2],PowerOff[3],PowerOff[4]);
        return powerOffTime - System.currentTimeMillis();
    }

    public static int[] getPowerOffTime(String txt) {
        String s = txt.replace(" ","").replace(".","").
                replace(":","").replace("-","").trim();
        int[] PowerOff = new int[5];
        PowerOff[0] = Integer.parseInt(s.substring(0,4));
        PowerOff[1] = Integer.parseInt(s.substring(4,6));
        PowerOff[2] = Integer.parseInt(s.substring(6,8));
        PowerOff[3] = Integer.parseInt(s.substring(8,10));
        PowerOff[4] = Integer.parseInt(s.substring(10,12));

        Log.d(TAG, "关机时间 = " + Arrays.toString(PowerOff));
        return PowerOff;
    }

    public static int[] getPowerOnTime(String txt) {
        String s = txt.replace(" ","").replace(".","").
                replace(":","").replace("-","").trim();
        int[] PowerOn = new int[5];
        PowerOn[0] = Integer.parseInt(s.substring(12,16));
        PowerOn[1] = Integer.parseInt(s.substring(16,18));
        PowerOn[2] = Integer.parseInt(s.substring(18,20));
        PowerOn[3] = Integer.parseInt(s.substring(20,22));
        PowerOn[4] = Integer.parseInt(s.substring(22,24));

        Log.d(TAG, "开机时间 = " + Arrays.toString(PowerOn));
        return PowerOn;
    }

    public static long getTimeMills(int year,int month,int date,int hour,int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month-1);
        calendar.set(Calendar.DAY_OF_MONTH,date);
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,0);
        return calendar.getTimeInMillis();
    }

    public static void reboot(Context context) {
        Intent intent = new Intent("android.intent.action.reboot");
        context.sendBroadcast(intent);
    }
}
