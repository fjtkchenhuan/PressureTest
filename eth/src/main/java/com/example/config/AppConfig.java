package com.example.config;

import android.os.Environment;

public class AppConfig {
    //打印log信息
    public static final String ISINFORMATION = "information";
    public static final String PINGTEST = "pingtest";
    public  static final String TAG = PINGTEST;

    public static final String BASE_SD_URL = Environment.getExternalStorageDirectory().getPath();

    public static final String SAVE_LOG_PATH = BASE_SD_URL + "/information";

}
