package com.example.Receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.example.clenttest.MainActivity;
import com.example.config.AppConfig;
import com.example.util.FileUtils;
import com.example.util.SharedPreferenceutil;
import com.example.util.SimpleDateUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.util.Mac.getMacAddress;

public class StartBootComplete extends BroadcastReceiver {
    private static final String TAG = "BootBroadcastReceiver";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    private static final String ACTION_SHUT_DOWN = "android.intent.action.ACTION_SHUTDOWN";
    private static final String ACTION_RESTART = "android.intent.action.REBOOT";
    private static final String ACTION_TIMERSWITCH = "android.intent.action.setpoweronoff";
    private static final String ACTION_TIMER= "com.signway.PowerOnOff";
    private static final String ACTION_NETWORK = "android.net.ethernet.ETHERNET_STATE_CHANGED";
    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "=============接收到广播====" + action);
        if (action.equals(ACTION_BOOT)){
            Intent intent2 = new Intent(context, MainActivity.class);
// 下面这句话必须加上才能实现开机自动运行app的界面
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent2);
        }
        if (AppConfig.TAG.equals(AppConfig.ISINFORMATION)){
            if (action.equals(ACTION_SHUT_DOWN)) {
                writeToSd("关机时间：" + SimpleDateUtil.getDate() + "\t\n");
//            GetLogThread getLogThread = new GetLogThread();
//            getLogThread.run();
            }
            if (action.equals(ACTION_RESTART)) {
                writeToSd("重启时间：" + SimpleDateUtil.getDate() + "\t\n");
            }
            if (action.equals(ACTION_TIMER)) {
                Bundle bundle = intent.getExtras();
                Log.i(ACTION_TIMER, bundle.getString("group0").toString());
                writeToSd("系统接收到的定时时间：" + bundle.getString("group0").toString() + "\t\n");
            }
            if (action.equals(ACTION_TIMERSWITCH)) {
                int[]  timeon = intent.getIntArrayExtra("timeon");
                int[]  timeoff = intent.getIntArrayExtra("timeoff");
                Log.i(ACTION_TIMERSWITCH, "timeon"+timeon+"timeoff"+timeoff);
                writeToSd("系统接收到的定时开机时间：" + Arrays.toString(timeon) + "\r\n");
                writeToSd("系统接收到的定时关机时间：" + Arrays.toString(timeoff) + "\r\n");
            }
            //监听以太网状态
            if (action.equals(ACTION_NETWORK)) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ConnectivityManager connectivity = (ConnectivityManager) context
                                .getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivity != null) {
                            NetworkInfo info = connectivity.getActiveNetworkInfo();
                            if (info != null && info.isConnected()) {
                                // 当前网络是连接的
                                if (info.getState() == NetworkInfo.State.CONNECTED) {
                                    // 当前所连接的网络可用
                                    Log.i("mac", "mac=" + SharedPreferenceutil.getMac() + "getMacAddress()=" + getMacAddress());
                                    if (!SharedPreferenceutil.getMac().equals(getMacAddress())) {
                                        writeToSd("mac地址：" + getMacAddress() + "\r\n");
                                        SharedPreferenceutil.setMac(getMacAddress());
                                        Log.i("mac", "mac=" + SharedPreferenceutil.getMac());
                                    }
                                }
                            }
                        }
                    }
                }, 1000);//延时1s执行
            }

            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// 监听wifi的打开与关闭，与wifi的连接无关
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                Log.e("TAG", "wifiState:" + wifiState);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        break;
                }
            }
            // 监听wifi的连接状态即是否连上了一个有效无线路由
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    // 获取联网状态的NetWorkInfo对象
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    //获取的State对象则代表着连接成功与否等状态
                    NetworkInfo.State state = networkInfo.getState();
                    //判断网络是否已经连接
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;
                    if (isConnected) {
                        if (!SharedPreferenceutil.getMac().equals(getMacAddress())) {
                            writeToSd("mac地址：" + getMacAddress() + "\r\n");
                            SharedPreferenceutil.setMac(getMacAddress());
                            Log.i("mac", "mac=" + SharedPreferenceutil.getMac());
                        }
                    }
                }
            }
            // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                //获取联网状态的NetworkInfo对象
                NetworkInfo info = intent
                        .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (info != null) {
                    //如果当前的网络连接成功并且网络连接可用
                    if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                        if (info.getType() == ConnectivityManager.TYPE_WIFI
                                || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                            Log.i("TAG", "连上");
                        }
                    } else {
                        Log.i("TAG", "断开");
                    }
                }
            }
        }
    }
    //新建一个File，传入文件夹目录
    public static void setToSd(String path) throws IOException {
        File fileSave = new File(path);
        if (fileSave.exists()) {
            fileSave.delete();
        }
        fileSave.createNewFile();
    }

    public void writeToSd(String writeDesc) {
        FileUtils.method("/sdcard/information/" + SharedPreferenceutil.getFileName(), writeDesc, true);
    }
}
