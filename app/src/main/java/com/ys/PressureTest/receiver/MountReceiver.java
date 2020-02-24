package com.ys.PressureTest.receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.ys.PressureTest.Constant;
import com.ys.PressureTest.log.SQLiteDao;
import com.ys.PressureTest.poweronoff.AutoPowerOnOffActivity;
import com.ys.PressureTest.utils.FileUtils;
import com.ys.PressureTest.utils.PowerOnOffUtils;
import com.ys.PressureTest.utils.TimeUtils;
import com.ys.PressureTest.utils.ToastUtils;
import com.ys.PressureTest.video.VideoActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2018/7/4.
 */

public class MountReceiver extends BroadcastReceiver {
    private static final String TAG = "MountReceiver";
    private static final String POWER_ON_OFF_TXT = "StartPowerOnOff.txt";//StartPowerOnOff.txt   StartPowerOnOff.txt
    private static final String VIDEO_PICTURE_TXT = "VideoAndPictureTest.txt";
    private File powerOnOffPath;
    private Context context;
    static final String[] PERMISSION_LIST = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_SETTINGS, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
//        checkPermission();
        if (Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())) {
            String path = intent.getData().getPath();

            if (path != null && !TextUtils.isEmpty(path)) {
                File paths = new File(path);
                doActionWithTestTxt(paths,context);
                Log.d(TAG,"path = " + path);
            }
        }

    }

    private void setPowerOnOffTime(Context context) {
        List<String> txtLists = FileUtils.getTxtList(powerOnOffPath.getPath(),context);
        int length = txtLists.size();

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.SP_POWER_ON_OFF,0);
//        sharedPreferences.edit().putInt(Constant.SP_POWERONOFF_COUNTS,0).apply();
        if (length >= 1) {
            int mode = getPowerMode(txtLists);
            sharedPreferences.edit().putInt(Constant.SP_POWERONOFF_MODE,mode).apply();
            if ( mode == 1) {
                Set<String> powerOnDates = new LinkedHashSet<>();
                for (int i = 1; i < length; i++) {
                    powerOnDates.add(txtLists.get(i));
                }

                sharedPreferences.edit().putStringSet(Constant.SP_POWER_DATA,powerOnDates).apply();

                List<String> powerOnOffList = new ArrayList<>();
                for (String poweron : powerOnDates){
                    Collections.addAll(powerOnOffList,poweron);
                }

                long[] subTime = new long[powerOnOffList.size()];
                for (int i = 0; i < powerOnOffList.size(); i++) {
                    subTime[i] = PowerOnOffUtils.getPowerOffSubTime(powerOnOffList.get(i));
                }
                int index0 = 0;
                if (PowerOnOffUtils.isArrayHavePositiveNum(subTime)) {
                    for (int i = 0; i < subTime.length; i++) {
                        if (subTime[i] > 0) {
                            index0 = i;
                            break;
                        }
                    }
                    long min = subTime[index0];
                    Log.d(TAG,"index0=" + index0 + "min=" + min);
                    int index = 0;
                    for (int i = 0; i < subTime.length; i++ ) {
                        Log.d(TAG,"subTime[" + i + "]" + subTime[i]);
                        if (subTime[i] <0)
                            continue;
                        if (subTime[i] <= min) {
                            min = subTime[i];
                            index = i;
                        }
                    }
                    Log.d(TAG,"index=" + index + "min=" + min);

                    int[] powerOnTime = PowerOnOffUtils.getPowerOnTime(powerOnOffList.get(index));
                    int[] powerOffTime = PowerOnOffUtils.getPowerOffTime(powerOnOffList.get(index));
                    SQLiteDao.getInstance(context).updateOrder(Constant.DAO_NEXT_POWER_ON_OFF_TIME,"下次开关机记录时间："
                            + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss")
                            +"  下次的开机时间=" + Arrays.toString(powerOnTime)
                            +"，下次的关机时间=" + Arrays.toString(powerOffTime) + "\n"
                            +"---------------------------------------------------------------------------------------------------");

                    Intent intent = new Intent("android.intent.action.setpoweronoff");
                    intent.putExtra("timeon", powerOnTime);
                    intent.putExtra("timeoff", powerOffTime);
                    intent.putExtra("enable", true);
                    intent.setPackage("com.adtv");
                    context.sendBroadcast(intent);
                    ToastUtils.showShortToast(context,"成功设置一组模式1的开关机时间");

                    Log.d(TAG,"context.sendBroadcast(intent);11111");
                }else {
                    ToastUtils.showShortToast(context,"设置的时间均不符合要求");
                }

            } else if (mode == 2) {
                int[] weekly = getWeekly(txtLists.get(1));
                int[] powerOnTime = getWeekPowerOnOffTime(txtLists.get(2));
                int[] powerOffTime = getWeekPowerOnOffTime(txtLists.get(3));

                Intent intent = new Intent("android.intent.action.setyspoweronoff");
                intent.putExtra("timeon", powerOnTime);
                intent.putExtra("timeoff", powerOffTime);
                intent.putExtra("wkdays", weekly);
                intent.putExtra("enable", true);
                intent.setPackage("com.adtv");
                context.sendBroadcast(intent);
                ToastUtils.showShortToast(context,"成功设置模式2的开关机时间");
                Log.d(TAG,"context.sendBroadcast(intent);22222");
            } else if (mode == 3) {
                Log.d(TAG,"AutoPowerOnOffActivity");
                Intent intent = new Intent(context, AutoPowerOnOffActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }

    private int getPowerMode(List<String> txtLists) {
        int powerOnOffMode = 0;
        if (txtLists.get(0).equals("mode1"))
            powerOnOffMode = 1;
        else if (txtLists.get(0).equals("mode2"))
            powerOnOffMode = 2;
        else if (txtLists.get(0).equals("mode3"))
            powerOnOffMode = 3;
        Log.d(TAG,"powerOnOffMode = " + powerOnOffMode);
       return powerOnOffMode;
    }

    private void doActionWithTestTxt(File file, Context  context) {
        File[] files = file.listFiles();

        if (files != null && files.length > 0) {
            for (File file1 : files) {
                if (file1.getAbsolutePath().contains(VIDEO_PICTURE_TXT)) {
                    Intent intent1 = new Intent(context, VideoActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent1);
                }else if (file1.getAbsolutePath().contains(POWER_ON_OFF_TXT)) {
                    powerOnOffPath = file1.getAbsoluteFile(); //StartPowerOnOff.txt
                    setPowerOnOffTime(context);
                }

                if (file1.isDirectory())
                    doActionWithTestTxt(file1,context);
            }
        }
    }



    private int[] getWeekly(String txt) {
        int[] weekly = new int[7];
        int length = txt.length();
        String s = txt.substring(length-7,length);
        weekly[0] = Integer.parseInt(s.substring(0,1));
        weekly[1] = Integer.parseInt(s.substring(1,2));
        weekly[2] = Integer.parseInt(s.substring(2,3));
        weekly[3] = Integer.parseInt(s.substring(3,4));
        weekly[4] = Integer.parseInt(s.substring(4,5));
        weekly[5] = Integer.parseInt(s.substring(5,6));
        weekly[6] = Integer.parseInt(s.substring(6));

        Log.d(TAG,"周期 = " + Arrays.toString(weekly));
        return weekly;
    }

    private int[] getWeekPowerOnOffTime(String txt) {
        int power[] = new int[2];
        int length = txt.length();
        String s = txt.substring(length-5,length);
        power[0] = Integer.parseInt(s.substring(0,2));
        power[1] = Integer.parseInt(s.substring(3));
        Log.d(TAG,"时间=" + Arrays.toString(power));
        return power;
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasUnCkeck = false;
            for (int i = 0; i < PERMISSION_LIST.length; i++) {
                if (context.checkSelfPermission(PERMISSION_LIST[i]) != PackageManager.PERMISSION_GRANTED) {
                    hasUnCkeck = true;
                }
            }
            if (hasUnCkeck) {
//                context.requestPermissions(PERMISSION_LIST, 300);
            }
        }
    }
}
