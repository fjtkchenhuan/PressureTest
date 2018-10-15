package com.ys.PressureTest.autotime;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import com.ys.PressureTest.Constant;
import com.ys.PressureTest.log.SQLiteDao;
import com.ys.PressureTest.utils.LedController;
import com.ys.PressureTest.utils.PowerOnOffUtils;
import com.ys.PressureTest.utils.ToastUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018/7/6.
 */

public class AutoSyncNetTimeService extends Service{
    private static final String TAG = "AutoSyncNetTimeService";
    private static long systemOnTime;
    private static long firstEnterServiceTime;
    private int count = 11;
    private LedController controller;

    @Override
    public void onCreate() {
        super.onCreate();
        controller = new LedController();
//        handler.post(CheckTime);
        new Thread(){
            @Override
            public void run() {
                super.run();
                if (getNetTime() > 0)
                    firstEnterServiceTime = getNetTime();
                Log.d(TAG,"第一次进入服务的时间 = " + getDate(firstEnterServiceTime) + "毫秒值 = " + firstEnterServiceTime);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (firstEnterServiceTime > 0)
                            new GetNetTimeTask().execute();
                        else {
                            String s = "未获取到网络时间，请确认网络连接正常";
                            controller.start();
                            SQLiteDao.getInstance(AutoSyncNetTimeService.this).insertDate(s + "\n");
                            showDialog();
                        }

                    }
                });

            }
        }.start();

    }

    private void showDialog() {
        AlertDialog dialog = new AlertDialog.Builder(AutoSyncNetTimeService.this.getApplicationContext())
                .setMessage("未获取到网络时间，请确认网络连接正常")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        controller.release();
                    }
                }).create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }


    private Runnable reboot = new Runnable() {
        @Override
        public void run() {
//            CommandUtils.execCommandSu("reboot");
            PowerOnOffUtils.reboot(AutoSyncNetTimeService.this);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        systemOnTime = intent.getLongExtra("currentTime",0);
        Log.d(TAG,"开机时间 = " + getDate(systemOnTime));
        return super.onStartCommand(intent, flags, startId);
    }

    class GetNetTimeTask extends AsyncTask<Void,Void,Long> {

        @Override
        protected Long doInBackground(Void... voids) {
            return getNetTime();
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            Message message = handler.obtainMessage();
            message.obj = aLong;
            handler.sendMessage(message);
        }
    }


    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            long netTime = (long) msg.obj;
            long syncTakeTime = System.currentTimeMillis() - firstEnterServiceTime;
            Log.d(TAG,"syncTakeTime = " + syncTakeTime);
            SharedPreferences sharedPreferences = getSharedPreferences(Constant.SP_AUTO_SYNC_TIME,0);
            //对比网络时间和当前时间，在1min以内默认同步时间成功
            if (Math.abs((netTime - System.currentTimeMillis())) < 60 * 1000) {
                ToastUtils.showLongToast(AutoSyncNetTimeService.this,"同步网络时间成功！！！" );
                 syncTakeTime = System.currentTimeMillis() - firstEnterServiceTime;
                String s;
                if (syncTakeTime > 1000)
                    s = syncTakeTime/1000 + "s";
                else
                    s = syncTakeTime + "ms";
                //获取sp中存储的自动确定网络时间的次数和日志
                StringBuilder lastLogs = new StringBuilder(sharedPreferences.getString(Constant.SP_SYNC_TIME_LOGS,""));
                int syncCounts = sharedPreferences.getInt(Constant.SP_COUNT_SYNC_TIME,0);
                //记录本次的日志
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("自动确定网络时间---本次记录的时间：" + getDate(System.currentTimeMillis()) + " ");
                stringBuilder.append("开始测试时间：" + getDate(firstEnterServiceTime) + " ");
                stringBuilder.append("同步网络时间所花费的时间：" + s);
                lastLogs.append(stringBuilder.toString() + "\n");
                Log.d(TAG,"sync net time = " + lastLogs.toString());
                //将数据存到sp和数据库中
                sharedPreferences.edit().putString(Constant.SP_SYNC_TIME_LOGS,lastLogs.toString()).apply();
                sharedPreferences.edit().putInt(Constant.SP_COUNT_SYNC_TIME,syncCounts + 1).apply();
                SQLiteDao.getInstance(AutoSyncNetTimeService.this).updateOrder(Constant.DAO_AUTO_SYNC_NET_TIME,lastLogs.toString());
                SQLiteDao.getInstance(AutoSyncNetTimeService.this).updateOrder(Constant.DAO_AUTO_SYNC_NET_TIME_COUNTS,"同步网络时间测试的次数="+
                        sharedPreferences.getInt(Constant.SP_COUNT_SYNC_TIME,0) + "\n\n"
                        +"---------------------------------------------------------------------------------------------------");

                handler.postDelayed(nextTest,TimeUnit.SECONDS.toMillis(4));
                //准备下一次的测试

            } else if (syncTakeTime > 60 * 1000 && firstEnterServiceTime > 0) {
                String s = "1分钟以内时间未同步成功\n";
                SQLiteDao.getInstance(AutoSyncNetTimeService.this).insertDate(s);
                controller.start();
                handler.removeCallbacksAndMessages(null);
            } else {
                SystemClock.sleep(1000);
                new GetNetTimeTask().execute();
            }
            return true;
        }
    });

    private Runnable nextTest = new Runnable() {
        @Override
        public void run() {
            updateTime(AutoSyncNetTimeService.this, PowerOnOffUtils.getTimeMills(2000,1,1,8,0));
            ToastUtils.showLongToast(AutoSyncNetTimeService.this,"同步时间成功，更新时间到2000.1.1 8:00 10秒后再次重启" );
            handler.postDelayed(CountDown,TimeUnit.SECONDS.toMillis(4));
        }
    };

    private Runnable CountDown = new Runnable() {
        @Override
        public void run() {
            count --;
            ToastUtils.showShortToast(AutoSyncNetTimeService.this,"同步网络时间成功，即将重启，倒计时" + count + "秒");
            handler.postDelayed(CountDown,TimeUnit.SECONDS.toMillis(1));
            if (count == 0)
                handler.post(reboot);
        }
    };

    private void updateTime(Context context, long ts) {
        Intent intent = new Intent();
        intent.setAction("com.ys.update_time");
        intent.putExtra("current_time", ts);
        if (context == null) return;
        context.sendBroadcast(intent);
    }

    private static String getDate(long s) {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateformat.format(s);
    }

    private long getNetTime() {
        URL url = null;//取得资源对象
        try {
            url = new URL("http://www.baidu.com");
            URLConnection uc = url.openConnection();//生成连接对象
            uc.connect(); //发出连接
            return uc.getDate(); //取得网站日期时间
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        handler.removeCallbacks(CountDown);
        handler.removeCallbacks(nextTest);
        controller.release();
    }
}
