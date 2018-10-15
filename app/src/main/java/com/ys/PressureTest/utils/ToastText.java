/*
 * modify for auto power onff ,static class settingpreferences
 * @author James
 * @email lujian430@hotmail.com
 * @data 2015-5-13
*/
package com.ys.PressureTest.utils;


import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

//import android.util.Log;


public class ToastText {
    private static Context mContext = null;
    private static Toast mToast = null;
	
    public static void initToast(Context context) {
	mContext = context;

    }
 
    public static void show(int resId) {
		
	if(null == mToast){
		mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
		mToast.setGravity(Gravity.CENTER, 0, 0);
	}

        show(mContext.getText(resId));
    }
 
    public static void show(CharSequence s) {

	if(null == mToast){
		mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
		mToast.setGravity(Gravity.CENTER, 0, 0);
	}
		
        mToast.setText(s);
        mToast.show();
    }
 
    public static void cancel() {
		if(null != mToast){
			mToast.cancel();
			mToast = null;
		}
    }
}

