package com.example.util;

import com.example.Service.MainServices;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by jsjm on 2018/6/25.
 */

public class GetLogThread implements Runnable {

    StringBuffer logContent;

    public GetLogThread() {
        logContent = new StringBuffer();
    }

    @Override
    public void run() {
//        Process pro = null;
////        try {
////            Runtime.getRuntime().exec("logcat -c").waitFor();
////            pro = Runtime.getRuntime().exec("logcat");
////            DataInputStream dis = new DataInputStream(pro.getInputStream());
////            String line = null;
////            while (true) {
////                while ((line = dis.readLine()) != null) {
////                    String temp = logContent.toString();
////                    logContent.delete(0, logContent.length());
////                    logContent.append(line);
////                    logContent.append("\n");
////                    logContent.append(temp);
////                    Log.e("HAHA", "=============获取的log信息==" + logContent.toString());
////                    FileUtils.method("/sdcard/Pinglog.txt", logContent.toString(), true);
////                    Thread.yield();
////                }
////            }
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line + "\n");
            }
            MainServices.writeToSd(String.valueOf(log));
        } catch (IOException e) {
        }

    }
}
