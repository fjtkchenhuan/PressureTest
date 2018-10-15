package com.ys.PressureTest.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ys.PressureTest.BaseUsedAdapter;
import com.ys.PressureTest.Constant;
import com.ys.PressureTest.R;
import com.ys.PressureTest.log.SQLiteDao;
import com.ys.PressureTest.log.TestLogPrinter;
import com.ys.PressureTest.product.RkFactory;
import com.ys.PressureTest.utils.CommandUtils;
import com.ys.PressureTest.utils.LedController;
import com.ys.PressureTest.utils.NetUtils;
import com.ys.PressureTest.utils.PowerOnOffUtils;
import com.ys.PressureTest.utils.TimeUtils;
import com.ys.PressureTest.utils.ToastUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

public class MobileNetActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MobileNetActivity";
    private Handler handler;
    private TextView testText;
    private Button stopTest;
    private ListView listView;
    private SharedPreferences netSharedPreferences;
    private SharedPreferences powerSharedPreferences;
    private int count = 16;
    private int openCount;
    private long openMobileTime;
    int[] names = new int[]{R.string.long_running,R.string.reboot_mode,R.string.reset_mode};
    private BaseUsedAdapter adapter;
    private static boolean isRebootRunnable;
    private static boolean isResetRunnable;
    private static boolean isLongRunnable;
    private Context context;
    private HandlerThread mBackThread;
    private Handler mBackHandler;
    private long longTimeBegin;
    private static final int MSG_LONG_RUNNING = 0;
    private static final int MSG_REBOOT_MODE = 1;
    private static final int MSG_SWITCH_MODE = 2;
    private LedController controller;

    //  /sys/devices/misc_power_en.23/usb_3g   3288
    //  /sys/devices/misc_power_en.22/usb_3g   3368
    //  /sys/devices/misc_power_en.19/usb_3g   3128

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_net);
        context = MobileNetActivity.this;
        handler = new Handler(new Callback(this));
        netSharedPreferences = getSharedPreferences(Constant.SP_NET_TEST,0);
        powerSharedPreferences = getSharedPreferences(Constant.SP_POWER_ON_OFF,0);
        controller = new LedController();

        mBackThread = new HandlerThread("mybackthread");
        mBackThread.start();
        mBackHandler = new Handler(mBackThread.getLooper());

        stopTest = findViewById(R.id.stop_test);
        testText = findViewById(R.id.test_text);
        listView = findViewById(R.id.listView);
        adapter = new BaseUsedAdapter(names);
        listView.setAdapter(adapter);

        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adapter.selected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.selected(position);
                if (position == 0) {
                    mBackHandler.postDelayed(LongTimeRunning,30 * 1000);
                    longTimeBegin = System.currentTimeMillis();
                    testText.setText("长时间运行模式，马上开始.......");
                    isLongRunnable = true;
                }else if (position == 1) {
                    netSharedPreferences.edit().putInt(Constant.SP_MOBILE_REBOOT_MODE,1).apply();
                    testText.setText("重启模式，马上开始.......");
                    mBackHandler.postDelayed(RebootMode,10 * 1000);
                    isRebootRunnable = true;
                }else if (position == 2) {
                    netSharedPreferences.edit().putInt(Constant.SP_MOBILE_REBOOT_MODE,2).apply();
                    mBackHandler.postDelayed(SwitchMode,10 * 1000);
                    testText.setText("复位模式，马上开始.......");
                    isResetRunnable = true;
                }
            }
        });
        stopTest.setOnClickListener(this);

        int ethMode = netSharedPreferences.getInt(Constant.SP_MOBILE_REBOOT_MODE,0);
        Log.d(TAG,"ethMode = " + ethMode + ",isRebootRunnable = " + isRebootRunnable + ",isResetRunnable = " + isResetRunnable);
        if (ethMode == 1 && !isRebootRunnable) {
            mBackHandler.post(RebootMode);
            isRebootRunnable = true;
        }else if (ethMode == 2 && !isResetRunnable) {
            mBackHandler.post(SwitchMode);
            isResetRunnable = true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stop_test:
                stopTest();
                break;
            default:
                break;
        }
    }

    private void stopTest() {
        netSharedPreferences.edit().putInt(Constant.SP_MOBILE_REBOOT_MODE,0).apply();
        handler.removeCallbacksAndMessages(null);
        mBackHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
        isResetRunnable = false;
        isRebootRunnable = false;
        if (isLongRunnable) {
            isLongRunnable = false;
            TestLogPrinter.getInstance(context).agingLog("\n4G长时间运行测试",longTimeBegin,System.currentTimeMillis(),
                    "联网时间",Constant.DAO_MOB_LONG_TIME);
        }
    }

    private Runnable LongTimeRunning = new Runnable() {
        @Override
        public void run() {
            Message message = new Message();
            boolean flag = isNetOn();
            message.obj = flag;
            message.what = MSG_LONG_RUNNING;
            mHandler.sendMessage(message);
        }
    };

    private Runnable RebootMode = new Runnable() {
        @Override
        public void run() {
            Message message = new Message();
            boolean flag = isNetOn();
            message.obj = flag;
            message.what = MSG_REBOOT_MODE;
            mHandler.sendMessage(message);
        }
    };

    private Runnable SwitchMode = new Runnable() {
        @Override
        public void run() {
            Message message = new Message();
            boolean flag = isNetOn();
            message.obj = flag;
            message.what = MSG_SWITCH_MODE;
            mHandler.sendMessage(message);
        }
    };

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // 处理UI更新
            if (msg.what == MSG_LONG_RUNNING) {
                boolean flag = (boolean) msg.obj;
                if (flag) {
                    testText.setText("4G网络正常连接，正在测试长时间运行......");
                    mBackHandler.postDelayed(LongTimeRunning, 30 * 1000);
                } else {
                    testText.setText("长时间运行测试，4G网络连接异常！！！");
                    testText.setTextColor(getResources().getColor(R.color.colorAccent));
                    controller.start();
                    TestLogPrinter.getInstance(context).agingLog("\n4G长时间运行测试", longTimeBegin, System.currentTimeMillis(),
                            "联网时间", Constant.DAO_MOB_LONG_TIME);
                }
            } else if (msg.what == MSG_REBOOT_MODE) {
                boolean flag = (boolean) msg.obj;
                if (flag) {
                    long powerOnTime = powerSharedPreferences.getLong(Constant.SP_POWER_ON_TIME, 0);
                    int count = netSharedPreferences.getInt(Constant.SP_MOBILE_REBOOT_COUNT, 1);
                    StringBuffer content = new StringBuffer(netSharedPreferences.getString(Constant.SP_MOBILE_REBOOT_CONTENT, ""));

                    String s;
                    long l = System.currentTimeMillis() - powerOnTime;
                    StringBuffer buffer = new StringBuffer();
                    if (l >= 1000)
                        s = l / 1000 + "s";
                    else
                        s = l + "ms";
                    if (l < 180 * 1000) {
                        String s1 = " 本次4G联网所花时间 =" + s;
                        buffer.append("4G重启模式---当前时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
                        buffer.append(" 开机时间：" + TimeUtils.tsFormat(powerOnTime, "yyyy-MM-dd HH:mm:ss"));
                        buffer.append(" 关机时间：" + TimeUtils.tsFormat(powerSharedPreferences.getLong(Constant.SP_POWER_OFF_TIME, 0), "yyyy-MM-dd HH:mm:ss"));
                        buffer.append(" 4G重启测试次数：" + count);
                        buffer.append(s1);
                        content.append(buffer.toString() + "\n");
                        netSharedPreferences.edit().putInt(Constant.SP_MOBILE_REBOOT_COUNT, count + 1).apply();
                        netSharedPreferences.edit().putString(Constant.SP_MOBILE_REBOOT_CONTENT, content.toString()).apply();
                        SQLiteDao.getInstance(context).updateOrder(Constant.DAO_MOB_REBOOT, content.toString());
                    }
                    testText.setText(buffer.toString());
                    handler.postDelayed(CountDown, TimeUnit.SECONDS.toMillis(1));
                } else {
                    long powerOnTime = powerSharedPreferences.getLong(Constant.SP_POWER_ON_TIME, 0);
                    if (System.currentTimeMillis() - powerOnTime > 180 * 1000) {
                        StringBuffer content = new StringBuffer(netSharedPreferences.getString(Constant.SP_MOBILE_REBOOT_CONTENT, ""));
                        netSharedPreferences.edit().putString(Constant.SP_MOBILE_REBOOT_CONTENT, content.toString()).apply();
                        SQLiteDao.getInstance(context).updateOrder(Constant.DAO_MOB_REBOOT, content.append(
                                "4G重启模式---当前时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") +
                                        " 4G没有网络，停止重启测试!!! \n").toString());
                        testText.setTextColor(getResources().getColor(R.color.colorAccent));
                        testText.setText("4G没有网络，停止重启测试");
                        stopTest();
                        controller.start();
                    } else {
                        testText.setText("重启模式，正在连接网络......");
                        mBackHandler.postDelayed(RebootMode, 3000);
                    }
                }
            } else if (msg.what == MSG_SWITCH_MODE) {
                boolean flag = (boolean) msg.obj;
                int count = netSharedPreferences.getInt(Constant.SP_MOBILE_NOT_REBOOT_COUNT, 0);
                StringBuffer content = new StringBuffer(netSharedPreferences.getString(Constant.SP_MOBILE_NOT_REBOOT_CONTENT, ""));
                if (openMobileTime == 0) {
                    Log.d(TAG, "第一次进入测试");
                    handler.postDelayed(CloseMobile, 10 * 1000);
                    testText.setText("第一次进行复位测试，即将关闭复位角");
//                    openCount = 11;
//                    handler.postDelayed(OpenCountDown, TimeUnit.SECONDS.toMillis(2));
                } else if (flag) {
                    String s;
                    long l = System.currentTimeMillis() - openMobileTime;
                    if (l > 1000)
                        s = l / 1000 + "s";
                    else
                        s = l + "ms";
                    String s1 = " 本次4G联网所花时间 =" + s;
                    if (l < 180 * 1000) {
                        content.append("4G复位模式---当前时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") + " 4G测试次数：" + count);
                        content.append(s1 + "\n");
                        netSharedPreferences.edit().putInt(Constant.SP_MOBILE_NOT_REBOOT_COUNT, count + 1).apply();
                        netSharedPreferences.edit().putString(Constant.SP_MOBILE_NOT_REBOOT_CONTENT, content.toString()).apply();
                        SQLiteDao.getInstance(context).updateOrder(Constant.DAO_MOB_NOT_REBOOT, content.toString());
                    }
                    testText.setText("复位模式，当前4G已连网，测试次数：" + count);
                    handler.postDelayed(CloseMobile, 10 * 1000);

                } else {
                    if ((System.currentTimeMillis() - openMobileTime) > 180 * 1000) {
                        Log.d(TAG, "4G无网络，测试中止");
                        mBackHandler.removeCallbacks(SwitchMode);
                        content.append("4G复位模式---当前时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") + " 4G没有网络，测试中止!!! \n");
                        netSharedPreferences.edit().putString(Constant.SP_MOBILE_NOT_REBOOT_CONTENT, content.toString()).apply();
                        SQLiteDao.getInstance(context).updateOrder(Constant.DAO_MOB_NOT_REBOOT, content.toString());

                        testText.setTextColor(getResources().getColor(R.color.colorAccent));
                        testText.setText("4G没有网络，停止测试！！！");
                        stopTest();
                        controller.start();
                    } else {
                        mBackHandler.postDelayed(SwitchMode, 3000);
                        testText.setText("复位模式，当前4G复位角值为1，正在连接网络.......");
                    }
                }
            }
            return true;
        }
    });

//    private Runnable sendNotReboot = new Runnable() {
//        @Override
//        public void run() {
//            int count = netSharedPreferences.getInt(Constant.SP_MOBILE_NOT_REBOOT_COUNT,0);
//            StringBuffer content = new StringBuffer(netSharedPreferences.getString(Constant.SP_MOBILE_NOT_REBOOT_CONTENT,""));
//            if (openMobileTime == 0) {
//                Log.d(TAG,"第一次进入测试");
//                openCount = 11;
//                handler.postDelayed(OpenCountDown, TimeUnit.SECONDS.toMillis(2));
//            } else if (isNetOn()) {
//                String s;
//                long l = System.currentTimeMillis() - openMobileTime;
//                if (l > 1000)
//                    s = l/1000 + "s";
//                else
//                    s = l + "ms";
//                String s1 = " 本次4G联网所花时间 =" + s;
//                if (l < 180 * 1000) {
//                    content.append("4G复位模式---当前时间："+ TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") + " 4G测试次数：" + count);
//                    content.append(s1 + "\n");
//                    netSharedPreferences.edit().putInt(Constant.SP_MOBILE_NOT_REBOOT_COUNT,count + 1).apply();
//                    netSharedPreferences.edit().putString(Constant.SP_MOBILE_NOT_REBOOT_CONTENT,content.toString()).apply();
//                    SQLiteDao.getInstance(context).updateOrder(Constant.DAO_MOB_NOT_REBOOT,content.toString());
//                }
//                testText.setText("复位模式，当前4G已连网，测试次数：" + count);
//                handler.postDelayed(CloseMobile,10*1000);
//
//            }else {
//                if ((System.currentTimeMillis() - openMobileTime) > 180 * 1000) {
//                    Log.d(TAG,"4G无网络，测试中止");
//                    handler.removeCallbacks(sendNotReboot);
//                    content.append("4G复位模式---当前时间："+TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") + "  4G没有网络，测试中止!!! \n");
//                    netSharedPreferences.edit().putString(Constant.SP_MOBILE_NOT_REBOOT_CONTENT,content.toString()).apply();
//                    SQLiteDao.getInstance(context).updateOrder(Constant.DAO_MOB_NOT_REBOOT,content.toString());
//
//                    testText.setTextColor(getResources().getColor(R.color.colorAccent));
//                    testText.setText("4G没有网络，停止测试！！！" );
//                    stopTest();
//                }else {
//                    handler.postDelayed(sendNotReboot,TimeUnit.SECONDS.toMillis(3));
//                    testText.setText("复位模式，当前4G复位角值为1，正在连接网络.......");
//                }
//            }
//
//        }
//    };

    private Runnable CloseMobile = new Runnable() {
        @Override
        public void run() {
            closeMobile();
            Log.d(TAG,"关闭4G");
            testText.setText("复位模式，当前4G复位角写入值0");
            openCount = 31;
            handler.postDelayed(OpenCountDown,TimeUnit.SECONDS.toMillis(2));
        }
    };

//    private Runnable sendReboot = new Runnable() {
//        @Override
//        public void run() {
//            if (isNetOn()) {
//                long powerOnTime = powerSharedPreferences.getLong(Constant.SP_POWER_ON_TIME,0);
//                int count = netSharedPreferences.getInt(Constant.SP_MOBILE_REBOOT_COUNT,1);
//                StringBuffer content = new StringBuffer(netSharedPreferences.getString(Constant.SP_MOBILE_REBOOT_CONTENT,""));
//
//                String s;
//                long l = System.currentTimeMillis() - powerOnTime;
//                StringBuffer buffer = new StringBuffer();
//                if (l >= 1000)
//                    s = l/1000 + "s";
//                else
//                    s = l + "ms";
//                if (l < 180 * 1000) {
//                    String s1 = " 本次4G联网所花时间 =" + s;
//                    buffer.append("4G重启模式---当前时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
//                    buffer.append(" 开机时间：" + TimeUtils.tsFormat(powerOnTime,"yyyy-MM-dd HH:mm:ss"));
//                    buffer.append(" 关机时间：" + TimeUtils.tsFormat(powerSharedPreferences.getLong(Constant.SP_POWER_OFF_TIME,0),"yyyy-MM-dd HH:mm:ss"));
//                    buffer.append(" 4G重启测试次数：" + count);
//                    buffer.append(s1);
//                    content.append(buffer.toString() + "\n");
//                    netSharedPreferences.edit().putInt(Constant.SP_MOBILE_REBOOT_COUNT,count+1).apply();
//                    netSharedPreferences.edit().putString(Constant.SP_MOBILE_REBOOT_CONTENT,content.toString()).apply();
//                    SQLiteDao.getInstance(context).updateOrder(Constant.DAO_MOB_REBOOT,content.toString());
//                }
//
//                testText.setText(buffer.toString());
//                handler.postDelayed(CountDown, TimeUnit.SECONDS.toMillis(1));
//
//            } else {
//                long powerOnTime = powerSharedPreferences.getLong(Constant.SP_POWER_ON_TIME,0);
//                if (System.currentTimeMillis() - powerOnTime > 180 * 1000) {
//                    StringBuffer content = new StringBuffer(netSharedPreferences.getString(Constant.SP_MOBILE_REBOOT_CONTENT,""));
//                    netSharedPreferences.edit().putString(Constant.SP_MOBILE_REBOOT_CONTENT,content.toString()).apply();
//                    SQLiteDao.getInstance(context).updateOrder(Constant.DAO_MOB_REBOOT,content.append(
//                            "4G重启模式---当前时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") +
//                                    "4G没有网络，停止重启测试!!! \n").toString());
//                    testText.setTextColor(getResources().getColor(R.color.colorAccent));
//                    testText.setText("4G没有网络，停止重启测试");
//                    stopTest();
//                } else {
//                    testText.setText("重启模式，正在连接网络......");
//                    handler.postDelayed(sendReboot,TimeUnit.SECONDS.toMillis(3));
//                }
//            }
//        }
//    };

    private Runnable CountDown = new Runnable() {
        @Override
        public void run() {
            count --;
            ToastUtils.showShortToast(context,"4G网络测试，即将重启，倒计时" + count + "秒");
            handler.postDelayed(CountDown,TimeUnit.SECONDS.toMillis(1));
            if (count == 0)
                PowerOnOffUtils.reboot(context);
        }
    };

    private Runnable OpenCountDown = new Runnable() {
        @Override
        public void run() {
            openCount --;
            ToastUtils.showShortToast(context,openCount + "秒后写入4G复位角为1，进入下一次测试");
            handler.postDelayed(OpenCountDown,TimeUnit.SECONDS.toMillis(1));
            if (openCount <= 1) {
                Log.d(TAG,"OpenCountDown 打开4G");
                openMobile();
                mBackHandler.postDelayed(SwitchMode,2000);
                openMobileTime = System.currentTimeMillis();
                handler.removeCallbacks(OpenCountDown);
                return;
            }

        }
    };

    private static class Callback implements Handler.Callback {
        WeakReference<MobileNetActivity> reference;

        private Callback(MobileNetActivity presenter) {
            reference = new WeakReference<MobileNetActivity>(presenter);
        }

        @Override
        public boolean handleMessage(Message msg) {
            MobileNetActivity presenter = reference.get();
            return !(presenter == null);
        }
    }

    private void openMobile() {
        try {
            CommandUtils.writeIOFile("1",RkFactory.getRK().get4GPath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeMobile() {
        try {
            CommandUtils.writeIOFile("0",RkFactory.getRK().get4GPath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isNetOn() {
        boolean ping = NetUtils.ping();
        int type = NetUtils.getNetWorkType(context);
        Log.d(TAG,"ping = " + ping + ",type = " + type);
        return ping && type == 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.release();
        TestLogPrinter.getInstance(context).agingLog("\n4G长时间运行测试",longTimeBegin,System.currentTimeMillis(),
                "联网时间",Constant.DAO_MOB_LONG_TIME);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (controller != null) {
            controller.reset();
        }
    }
}
