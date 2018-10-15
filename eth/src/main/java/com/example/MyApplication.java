package com.example;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class MyApplication extends Application {
    public static String USER_INFO = "com.example";
    private static SharedPreferences mSharedPreferences;
    private static MyApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mSharedPreferences = getSharedPreferences(USER_INFO, 0);
    }
    public static MyApplication getInstance() {
        return instance;
    }
    public void saveData(String key, Object data) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        try {
            Log.i("SharedPreferences", "设置的tag =" + key + "   //date = " + data);
            if (data instanceof Integer) {
                editor.putInt(key, (Integer) data);
            } else if (data instanceof Boolean) {
                editor.putBoolean(key, (Boolean) data);
            } else if (data instanceof String) {
                editor.putString(key, (String) data);
            } else if (data instanceof Float) {
                editor.putFloat(key, (Float) data);
            } else if (data instanceof Long) {
                editor.putLong(key, (Long) data);
            }
        } catch (Exception e) {
            Log.i("SharedPreferences", "获取的的tag =" + key + "   //date = " + e.toString());
        }
        editor.commit();
    }

    public Object getData(String key, Object defaultObject) {
        try {
            Log.i("SharedPreferences", "获取的的tag =" + key + "   //date = " + defaultObject.toString());
            if (defaultObject instanceof String) {
                return mSharedPreferences.getString(key, (String) defaultObject);
            } else if (defaultObject instanceof Integer) {
                return mSharedPreferences.getInt(key, (Integer) defaultObject);
            } else if (defaultObject instanceof Boolean) {
                return mSharedPreferences.getBoolean(key, (Boolean) defaultObject);
            } else if (defaultObject instanceof Float) {
                return mSharedPreferences.getFloat(key, (Float) defaultObject);
            } else if (defaultObject instanceof Long) {
                return mSharedPreferences.getLong(key, (Long) defaultObject);
            }
        } catch (Exception e) {
            Log.i("SharedPreferences", "获取的的tag =" + key + "   //date = " + e.toString());
            return null;
        }
        return null;
    }
}
