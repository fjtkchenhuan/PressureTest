package com.ys.PressureTest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.ys.PressureTest.Constant;
import com.ys.PressureTest.autotime.AutoSyncNetTimeService;
import com.ys.PressureTest.log.SQLiteDao;
import com.ys.PressureTest.net.EthernetActivity;
import com.ys.PressureTest.net.MobileNetActivity;
import com.ys.PressureTest.net.WifiActivity;
import com.ys.PressureTest.poweronoff.AutoPowerOnOffActivity;
import com.ys.PressureTest.reboot.RebootActivity;
import com.ys.PressureTest.utils.CommandUtils;
import com.ys.PressureTest.utils.FileUtils;
import com.ys.PressureTest.utils.PowerOnOffUtils;
import com.ys.PressureTest.utils.TimeUtils;
import com.ys.PressureTest.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018/7/3.
 */

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private Handler handler;
    private Context mContext;
    private SharedPreferences powerOnOffSp;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        handler = new Handler();
        powerOnOffSp = context.getSharedPreferences(Constant.SP_POWER_ON_OFF,0);
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG,"onReceive");
            //保留开机时间
            powerOnOffSp.edit().putLong(Constant.SP_POWER_ON_TIME,System.currentTimeMillis()).apply();

            //重启压力测试
            startRebootTest(context);

            //在日志中存执行过的定时开关机数据
            savePowerOnOffTime(context);

            //开启自动更新网络时间
            startSyncNetTime();

            //获取U盘里的定时开关机数据，对比当前时间，将最近的一次开关机时间设置到系统
            if (powerOnOffSp.getInt(Constant.SP_POWERONOFF_MODE,0) == 1) {
                setPowerOnOffTime(context);
                long lastPowerOnTime = powerOnOffSp.getLong(Constant.SP_REBOOT_POWERONTIME,System.currentTimeMillis() - 60 * 1000);
                if (Math.abs(System.currentTimeMillis() - lastPowerOnTime) > 2 * 24 *60 *60 *1000)
                    FileUtils.getKmsgLog(Environment.getExternalStorageDirectory() + "/KernelLog.txt");
            } else if (powerOnOffSp.getInt(Constant.SP_POWERONOFF_MODE,0) == 3) {
                //自动测试定时开关机
                startActivity(context, AutoPowerOnOffActivity.class);
                int count = powerOnOffSp.getInt(Constant.SP_AUTO_POWERONOFF_COUNTS , 0);
                powerOnOffSp.edit().putInt(Constant.SP_AUTO_POWERONOFF_COUNTS , count + 1).apply();
            }

            //以太网测试，重启
            if (context.getSharedPreferences(Constant.SP_NET_TEST,0).getInt(Constant.SP_ETHERNET_REBOOT_MODE,0) == 1) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(mContext, EthernetActivity.class);
                    }
                });
            }

            //wifi测试，重启
            if (context.getSharedPreferences(Constant.SP_NET_TEST,0).getInt(Constant.SP_WIFI_REBOOT_MODE,0) == 1) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(mContext, WifiActivity.class);
                    }
                });
            }

            //4g测试，重启
            if (context.getSharedPreferences(Constant.SP_NET_TEST,0).getInt(Constant.SP_MOBILE_REBOOT_MODE,0) == 1) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(mContext, MobileNetActivity.class);
                        Log.d(TAG,"4G测试重启");
                    }
                });
            }

            //保存数据库里的日志到sd卡中
//            FileUtils.saveDaoToSD(context);
        }
    }

    private void startSyncNetTime() {
        handler.postDelayed(StartSyncNetTime, TimeUnit.SECONDS.toMillis(5));
    }

    private Runnable StartSyncNetTime = new Runnable() {
        @Override
        public void run() {
            if (mContext.getSharedPreferences(Constant.SP_AUTO_SYNC_TIME,0).getBoolean(Constant.SP_BOOLEAN_SYNC_TIME,false)) {
                Intent intent = new Intent(mContext, AutoSyncNetTimeService.class);
                intent.putExtra("currentTime", System.currentTimeMillis());
                mContext.startService(intent);
            }
        }
    };

    private void setPowerOnOffTime(Context context) {
        //将当前开机时间存起来
        powerOnOffSp.edit().putLong(Constant.SP_REBOOT_POWERONTIME,System.currentTimeMillis()).apply();
        Set<String> powerOn = powerOnOffSp.getStringSet(Constant.SP_POWER_DATA,new HashSet<String>(0));

        List<String> powerOnOffList = new ArrayList<>();
        for (String poweron : powerOn){
            Collections.addAll(powerOnOffList,poweron);
        }

        long[] subTime = new long[powerOnOffList.size()];
        for (int i = 0; i < powerOnOffList.size(); i++) {
            subTime[i] = PowerOnOffUtils.getPowerOffSubTime(powerOnOffList.get(i));
        }
        int index0 = 0;
        int[] powerOnTime;
        int[] powerOffTime;
        if (PowerOnOffUtils.isArrayHavePositiveNum(subTime)) {
            for (int i = 0; i < subTime.length; i++) {
                if (subTime[i] > 0) {
                    index0 = i;
                    break;
                }
            }

            long min = subTime[index0];
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

             powerOnTime = PowerOnOffUtils.getPowerOnTime(powerOnOffList.get(index));
             powerOffTime = PowerOnOffUtils.getPowerOffTime(powerOnOffList.get(index));

            Intent intent = new Intent("android.intent.action.setpoweronoff");
            intent.putExtra("timeon", powerOnTime);
            intent.putExtra("timeoff", powerOffTime);
            intent.putExtra("enable", true);
            context.sendBroadcast(intent);
            ToastUtils.showShortToast(context,"成功设置一组模式1的开关机时间");

            SQLiteDao.getInstance(context).updateOrder(Constant.DAO_NEXT_POWER_ON_OFF_TIME,"下次开关机记录时间："
                            + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss")
                    +"  下次的开机时间=" + Arrays.toString(powerOnTime)
                    +"，下次的关机时间=" + Arrays.toString(powerOffTime) + "\n");
        }else {
            ToastUtils.showShortToast(context,"设置的时间均不符合要求");
            SQLiteDao.getInstance(context).updateOrder(Constant.DAO_NEXT_POWER_ON_OFF_TIME,"");
        }
    }


    private void savePowerOnOffTime(Context context) {
        String spPowerOn = powerOnOffSp.getString(Constant.LAST_POWER_ON_TIME,"0");
        String spPowerOff = powerOnOffSp.getString(Constant.LAST_POWER_OFF_TIME,"");
        String powerOnTime = CommandUtils.getValueFromProp("persist.sys.powerontimeper");
        String powerOffTime = CommandUtils.getValueFromProp("persist.sys.powerofftimeper");

        StringBuilder lastTimes = new StringBuilder(powerOnOffSp.getString(Constant.SP_LAST_POWERONOFF_TIME,""));

        Log.d(TAG,"spPowerOn="+spPowerOn+",powerOnTime="+powerOnTime);
        if ((!spPowerOn.equals(powerOnTime) && !spPowerOff.equals(powerOffTime)) &&
                (!powerOnTime.equals("000000000000") && !powerOffTime.equals("000000000000")) ) {
            powerOnOffSp.edit().putString(Constant.LAST_POWER_ON_TIME,powerOnTime).apply();
            powerOnOffSp.edit().putString(Constant.LAST_POWER_OFF_TIME,powerOffTime).apply();
            StringBuffer buffer = new StringBuffer();
            buffer.append("定时开关机---本次记录的时间: "+ TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") + " ");
            buffer.append("开机时间：" + powerOnTime + " ");
            buffer.append("关机时间：" + powerOffTime);
            lastTimes.append(buffer + "\n");
            powerOnOffSp.edit().putString(Constant.SP_LAST_POWERONOFF_TIME,lastTimes.toString()).apply();
            SQLiteDao.getInstance(context).updateOrder(Constant.DAO_LAST_POWER_ON_OFF_TIME,lastTimes.toString());
        }

            Log.d(TAG,"powerOffTime = " + powerOffTime);
        if (powerOffTime.length() == 12 && !powerOffTime.equals("000000000000")) {
            int year = Integer.parseInt(powerOffTime.substring(0, 4));
            int month = Integer.parseInt(powerOffTime.substring(4, 6));
            int date = Integer.parseInt(powerOffTime.substring(6, 8));
            int hour = Integer.parseInt(powerOffTime.substring(8, 10));
            int minute = Integer.parseInt(powerOffTime.substring(10));
            long pressurePowerOffTime = powerOnOffSp.getLong(Constant.SP_POWER_OFF_TIME, 0);
            Log.d(TAG, "pressurePowerOffTime = " + TimeUtils.tsFormat(pressurePowerOffTime, "yyyy-MM-dd HH:mm:ss"));

            if (Math.abs(pressurePowerOffTime - TimeUtils.getTimeMills(year, month, date, hour, minute, 0)) < 90 * 1000) {
                int count = powerOnOffSp.getInt(Constant.SP_POWERONOFF_COUNTS, 0);
                Log.d(TAG, "count = " + count);
                powerOnOffSp.edit().putInt(Constant.SP_POWERONOFF_COUNTS, count + 1).apply();
                Log.d(TAG, "定时开关机测试的次数");
                Log.d(TAG, "count ====" + powerOnOffSp.getInt(Constant.SP_POWERONOFF_COUNTS, 0));
                SQLiteDao.getInstance(context).updateOrder(Constant.DAO_POWERONOFF_COUNTS, "定时开关机测试的次数=" +
                        powerOnOffSp.getInt(Constant.SP_POWERONOFF_COUNTS, 0) + "\n\n"
                        + "---------------------------------------------------------------------------------------------------");
            }
        }
    }

    private void startRebootTest(Context context) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences("state", 0);
        int rebootFlag = mSharedPreferences.getInt("reboot_flag", 0);
        if (rebootFlag == 1) {
            Intent pintent = new Intent(context, RebootActivity.class);
            pintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(pintent);
        }
    }

    private void startActivity(Context context,Class clazz) {
        Intent intent = new Intent(context,clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }



}
