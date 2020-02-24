package com.ys.PressureTest;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ys.PressureTest.autotime.AutoSyncNetTimeService;
import com.ys.PressureTest.log.DeleteLogsActivity;
import com.ys.PressureTest.log.SQLiteDao;
import com.ys.PressureTest.log.TestLogPrinter;
import com.ys.PressureTest.net.EthernetActivity;
import com.ys.PressureTest.net.MobileNetActivity;
import com.ys.PressureTest.net.WifiActivity;
import com.ys.PressureTest.reboot.RebootActivity;
import com.ys.PressureTest.utils.FileUtils;
import com.ys.PressureTest.utils.PowerOnOffUtils;
import com.ys.PressureTest.utils.ToastUtils;
import com.ys.PressureTest.video.VideoActivity;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ListView stressList;
    private BaseUsedAdapter stressAdapter;
    private String TAG = "MainActivity";
    private MainHandler handler;
    private static final int REBOOT = 0;
    private int count = 11;
    int[] names = new int[]{R.string.reboot_test,R.string.video_test,R.string.auto_set_time,R.string.ethernet_test,R.string.wifi_test,R.string.mobile_net_test,R.string.print_log};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new MainHandler(this);
        initTableDao();
        stressList = findViewById(R.id.stress_list);
        stressAdapter = new BaseUsedAdapter(names);
        stressList.setAdapter(stressAdapter);
        stressList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                stressAdapter.selected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        stressList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stressAdapter.selected(position);
                if (position == 0) {
                    startActivity(RebootActivity.class);
                } else if (position == 1) {
                    startActivity(VideoActivity.class);
                } else if (position == 2) {
                    changeSystemTime();
                } else if (position == 3) {
                    startActivity(EthernetActivity.class);
                } else if (position == 4) {
                    startActivity(WifiActivity.class);
                } else if (position == 5) {
                    startActivity(MobileNetActivity.class);
                } else if (position == 6) {
                    FileUtils.getLogs(Environment.getExternalStorageDirectory() + "/SystemLog.txt");
                    handler.postDelayed(StopLogging,2000);
                    ToastUtils.showShortToast(MainActivity.this,"日志存储在" + Environment.getExternalStorageDirectory() + "/SystemLog.txt");
                }
            }
        });

        findViewById(R.id.stop_auto_set_time).setOnClickListener(this);
        findViewById(R.id.delete_log).setOnClickListener(this);
        findViewById(R.id.delete_log).setVisibility(View.GONE);

    }

    private Runnable StopLogging = new Runnable() {
        @Override
        public void run() {
            FileUtils.stopLog();
        }
    };

    private void changeSystemTime() {
//        ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).setTime(PowerOnOffUtils.getTimeMills(2000,1,1,8,0));
        getSharedPreferences(Constant.SP_AUTO_SYNC_TIME,0).edit().putBoolean(Constant.SP_BOOLEAN_SYNC_TIME,true).apply();
        updateTime(this,PowerOnOffUtils.getTimeMills(2000,1,1,8,0));
        ToastUtils.showLongToast(this,"系统时间更新到2000.1.1 8:00，10s后重启，开始测试自动确定网络时间功能");
        handler.postDelayed(CountDown,TimeUnit.SECONDS.toMillis(3));
    }

    private Runnable CountDown = new Runnable() {
        @Override
        public void run() {
            count --;
            ToastUtils.showShortToast(MainActivity.this,"同步网络时间，即将重启，倒计时" + count + "秒");
            handler.postDelayed(CountDown,TimeUnit.SECONDS.toMillis(1));
            if (count == 0)
                handler.sendEmptyMessage(REBOOT);

        }
    };

    private static void updateTime(Context context, long ts) {
        Intent intent = new Intent();
        intent.setAction("com.ys.update_time");
        intent.putExtra("current_time", ts);
        intent.setPackage("com.ys.ys_receiver");
        if (context == null) return;
        context.sendBroadcast(intent);
    }

    private void initTableDao() {
        if (!SQLiteDao.getInstance(this).isDataExist()) {
            SQLiteDao.getInstance(this).initTable();
            TestLogPrinter.getInstance(this).functionlLog();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stop_auto_set_time:
                getSharedPreferences(Constant.SP_AUTO_SYNC_TIME,0).edit().putBoolean(Constant.SP_BOOLEAN_SYNC_TIME,false).apply();
                handler.removeMessages(REBOOT);
                stopService(new Intent(MainActivity.this, AutoSyncNetTimeService.class));
                ToastUtils.showShortToast(this,"停止测试自动更新网络时间");
                break;
            case R.id.delete_log:
                startActivity(DeleteLogsActivity.class);
                break;
                default:
                    break;
        }
    }

    private void startActivity(Class clazz) {
        startActivity(new Intent(MainActivity.this,clazz));
    }

    private static class MainHandler extends Handler {
        private final WeakReference<MainActivity> mModule;

        private MainHandler(MainActivity module) {
            super(Looper.getMainLooper());
            mModule = new WeakReference<MainActivity>(module);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity module = mModule.get();
            if (module == null) {
                return;
            }
            switch (msg.what) {
                case REBOOT:
                    PowerOnOffUtils.reboot(module);
                    break;
                    default:
                        break;
            }
        }
    }

}
