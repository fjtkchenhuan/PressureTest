package com.example.util;


import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class DataUtil {

    private static final String TAG = "hahahah";

    public static StringBuilder getPowerSharedInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String path = "/data/data/com.adtv/shared_prefs";
            File file = new File(path);
            Log.e(TAG, "=====" + (file.exists()));
            File[] fileLists = file.listFiles();
            Log.e(TAG, "========获取文件个数====" + fileLists.length);
            if (fileLists.length<=0){
                return stringBuilder;
            }
            for (int i = 0; i < fileLists.length; i++) {
                File fileData = fileLists[i];
                String filePath = fileData.getPath();
                Log.e(TAG, "========获取文件路径====" + filePath);
                readTxtFile(filePath, stringBuilder);
            }
        } catch (Exception e) {
            Log.e(TAG, "=====" + e.toString());
        }
        return stringBuilder;
    }

    public static StringBuilder readTxtFile(String filePath, StringBuilder stringBuilder) {
        try {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = "";
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    stringBuilder.append(lineTxt + "\n");
                }
                read.close();
            } else {
                Log.e(TAG, "找不到指定的文件");
            }
        } catch (Exception e) {
            Log.e(TAG, "读取文件内容出错");
            e.printStackTrace();
        }
        Log.e(TAG, "========获取文件内容====" + stringBuilder.toString());
        return stringBuilder;
    }
}
