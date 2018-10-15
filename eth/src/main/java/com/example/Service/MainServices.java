package com.example.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.example.util.FileUtils;
import com.example.util.GetLogThread;
import com.example.util.SharedPreferenceutil;

import java.util.List;

import static com.example.util.Mac.getMacAddress;
import static com.example.util.DataUtil.getPowerSharedInfo;

public class MainServices extends Service{
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        getItem(getApplicationContext());
    }
    private void getItem(Context context) {
        PackageManager pckMan = context.getPackageManager();
        List<PackageInfo> packageInfo = pckMan.getInstalledPackages(0);
        for (PackageInfo pInfo:packageInfo){
            if (pInfo.packageName.equals("com.adtv")){
                String mac = "固件版本："+Build.VERSION.INCREMENTAL.trim().trim()+"\r\n"
                        +"定时开关机版本："+String.valueOf("版本代码:"+pInfo.versionCode)+"版本名称:"+pInfo.versionName
                        +"\r\n"+"开关机时间："+getPowerSharedInfo().toString()+"\r\n";
                Log.i("loadPhoneStatus","MainServices="+mac);
                writeToSd(mac);
            }
        }
        if (!"".equals(getMacAddress())){
            writeToSd("mac地址：" + getMacAddress() + "\r\n");
        }
        Runnable getLogThread = new GetLogThread();
        Thread thread = new Thread(getLogThread);
        thread.start();
    }
    public static void writeToSd(String writeDesc) {
        FileUtils.method("/sdcard/information/"+SharedPreferenceutil.getFileName(), writeDesc, true);
    }

}
