package com.ys.PressureTest.utils;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtils {
    private static Toast toast;

    /** 
     * 解决Toast重复弹出 长时间不消失的问题 
     * @param context 
     * @param message 
     */  
    public static void showShortToast(Context context, String message){
        if (toast==null){
            toast = Toast.makeText(context,message,Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 10);//让Toast显示在中间
        }else {
            toast.setText(message);
        }
        toast.show();//设置新的消息提示
     }

    public static void showLongToast(Context context, String message){
        if (toast==null){
            toast = Toast.makeText(context,message,Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 10);//让Toast显示在中间
        }else {
            toast.setText(message);
        }
        toast.show();//设置新的消息提示
    }


} 