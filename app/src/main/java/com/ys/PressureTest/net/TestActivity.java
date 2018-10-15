package com.ys.PressureTest.net;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ys.PressureTest.R;
import com.ys.PressureTest.utils.NetUtils;
import com.ys.PressureTest.utils.ToastUtils;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";
    private HandlerThread mBackThread;
    private Handler mBackHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mBackThread = new HandlerThread("mybackthread");
        mBackThread.start();
        mBackHandler = new Handler(mBackThread.getLooper());
        mBackHandler.post(check);
    }

    private Runnable check = new Runnable() {
        @Override
        public void run() {
            Message message = new Message();
            boolean flag = isNetOn();
            message.obj = flag;
            message.what = 0;
            mHandler.sendMessage(message);
        }
    };

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 处理UI更新
            if (msg.what == 0) {
                boolean flag = (boolean) msg.obj;
                if (flag) {
                    ToastUtils.showShortToast(TestActivity.this,"已连网");
                } else {
                    ToastUtils.showShortToast(TestActivity.this,"未连网");
                    mBackHandler.postDelayed(check,2000);
                }
            }
        }
    };


    private boolean isNetOn() {
        boolean ping = NetUtils.ping();
        int type = NetUtils.getNetWorkType(this);
        Log.d(TAG,"ping = " + ping + ",type = " + type);
        return ping && type == 0;
    }


}
