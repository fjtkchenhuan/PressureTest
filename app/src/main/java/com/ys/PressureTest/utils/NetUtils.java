package com.ys.PressureTest.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.view.WindowManager;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2018/7/16.
 */

public class NetUtils {
    public static boolean ping() {
        String ip = "www.baidu.com";//// 除非百度挂了，否则用这个应该没问题~
        Process p ;

        try {
            p = Runtime.getRuntime().exec("ping -c 1 -w 100 " + ip);
            int status = p.waitFor();
            if (status == 0) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void openOrCloseEth(Context context,boolean open) {
        Intent intent = new Intent("com.ys.set_eth_enabled");
        intent.putExtra("eth_mode",open);
        context.sendBroadcast(intent);
    }

    public static int getNetWorkType(Context context) {
        int netWorkType = -100;
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            netWorkType = networkInfo.getType();
        }
        return netWorkType;
    }
}
