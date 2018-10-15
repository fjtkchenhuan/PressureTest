package com.ys.PressureTest.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
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
import com.ys.PressureTest.utils.LedController;
import com.ys.PressureTest.utils.NetUtils;
import com.ys.PressureTest.utils.PowerOnOffUtils;
import com.ys.PressureTest.utils.TimeUtils;
import com.ys.PressureTest.utils.ToastUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

public class EthernetActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "EthernetActivity";
    private Handler handler;
    private TextView testText;
    private Button stopTest;
    private ListView listView;
    private SharedPreferences netSharedPreferences;
    private SharedPreferences powerSharedPreferences;
    private int count = 16;
    private int openCount;
    private long openEthTime;
    int[] names = new int[]{R.string.reboot_mode,R.string.switch_mode};
    private BaseUsedAdapter adapter;
    private static boolean isRebootRunnable;
    private static boolean isSwitchRunnable;
    private Context context;
    private LedController controller;
    private boolean allowToPost = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ethernet);
        context = EthernetActivity.this;
        handler = new Handler(new Callback(this));
        netSharedPreferences = getSharedPreferences(Constant.SP_NET_TEST,0);
        powerSharedPreferences = getSharedPreferences(Constant.SP_POWER_ON_OFF,0);
        controller = new LedController();

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
                    netSharedPreferences.edit().putInt(Constant.SP_ETHERNET_REBOOT_MODE,1).apply();
                    handler.post(sendReboot);
                    isRebootRunnable = true;
                }else if (position == 1) {
                    netSharedPreferences.edit().putInt(Constant.SP_ETHERNET_REBOOT_MODE,2).apply();
                    NetUtils.openOrCloseEth(context,true);
                    handler.post(sendNotReboot);
                    isSwitchRunnable = true;
                }
            }
        });
        stopTest.setOnClickListener(this);

        int ethMode = netSharedPreferences.getInt(Constant.SP_ETHERNET_REBOOT_MODE,0);
        Log.d(TAG,"ethMode = " + ethMode + ",isRebootRunnable = " + isRebootRunnable + ",isSwitchRunnable = " + isSwitchRunnable);
        if (ethMode == 1 && !isRebootRunnable) {
            handler.post(sendReboot);
            isRebootRunnable = true;
        }else if (ethMode == 2 && !isSwitchRunnable) {
            handler.post(sendNotReboot);
            isSwitchRunnable = true;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stop_test:
                Log.d(TAG,"stop");
                stopTest();
                break;
                default:
                    break;
        }
    }

    private void stopTest() {
        netSharedPreferences.edit().putInt(Constant.SP_ETHERNET_REBOOT_MODE,0).apply();
        handler.removeCallbacksAndMessages(null);
        isSwitchRunnable = false;
        isRebootRunnable = false;
        allowToPost = false;
    }

    private Runnable sendNotReboot = new Runnable() {
        @Override
        public void run() {
            int count = netSharedPreferences.getInt(Constant.SP_ETHERNET_NOT_REBOOT_COUNT,1);
            StringBuffer content = new StringBuffer(netSharedPreferences.getString(Constant.SP_ETHERNET_NOT_REBOOT_CONTENT,""));
            boolean ping = NetUtils.ping();
            int type = NetUtils.getNetWorkType(context);
            Log.d(TAG,"ping = " + ping + ",type = " + type);
            if (allowToPost) {
                if (openEthTime == 0) {
                    NetUtils.openOrCloseEth(context,false);
                    Log.d(TAG,"第一次进入测试");
                    openCount = 16;
                    handler.postDelayed(OpenCountDown,TimeUnit.SECONDS.toMillis(2));
                } else if (ping && type == 9) {
                    String s;
                    long l = System.currentTimeMillis() - openEthTime;
                    if (l > 1000)
                        s = l/1000 + "s";
                    else
                        s = l + "ms";
                    String s1 = " 本次以太网联网所花时间 =" + s;
                    if (l < 60 * 1000) {
                        content.append("以太网开关模式---当前时间："+TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") + " 以太网测试次数：" + count);
                        content.append(s1 + "\n");
                        netSharedPreferences.edit().putInt(Constant.SP_ETHERNET_NOT_REBOOT_COUNT,count + 1).apply();
                        netSharedPreferences.edit().putString(Constant.SP_ETHERNET_NOT_REBOOT_CONTENT,content.toString()).apply();
                        SQLiteDao.getInstance(context).updateOrder(Constant.DAO_ETH_NOT_REBOOT,content.toString());
                    }

                    testText.setText("开关模式，当前以太网开关关闭，测试次数：" + count);
                    ToastUtils.showLongToast(context,"以太网网络已连接，即将关闭开关，进入下次测试");
                    openCount = 16;
                    Log.d(TAG,"5s后关闭以太网");
                    handler.postDelayed(OpenCountDown,TimeUnit.SECONDS.toMillis(5));
                }else {
                    if ((System.currentTimeMillis() - openEthTime) > 60 * 1000) {
                        Log.d(TAG,"以太网无网络，测试中止");
                        handler.removeCallbacks(sendNotReboot);
                        content.append("以太网开关模式---当前时间："+TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") + " 以太网没有网络，测试中止!!! \n");
                        netSharedPreferences.edit().putString(Constant.SP_ETHERNET_NOT_REBOOT_CONTENT,content.toString()).apply();
                        SQLiteDao.getInstance(context).updateOrder(Constant.DAO_ETH_NOT_REBOOT,content.toString());

                        testText.setTextColor(getResources().getColor(R.color.colorAccent));
                        testText.setText("以太网没有网络，停止开关模式测试！！！" );
                        stopTest();
                        controller.start();
                    }else {
                        handler.postDelayed(sendNotReboot,TimeUnit.SECONDS.toMillis(1));
                        testText.setText("开关模式，当前以太网开关打开，正在连接网络.......");
                    }
               }
            }

        }
    };

    private Runnable sendReboot = new Runnable() {
        @Override
        public void run() {
            long powerOnTime = powerSharedPreferences.getLong(Constant.SP_POWER_ON_TIME,0);
            int count = netSharedPreferences.getInt(Constant.SP_ETHERNET_REBOOT_COUNT,1);
            StringBuffer content = new StringBuffer(netSharedPreferences.getString(Constant.SP_ETHERNET_REBOOT_CONTENT,""));
            int type = NetUtils.getNetWorkType(context);
            if (NetUtils.ping() && type == 9) {
                String s;
                long l = System.currentTimeMillis() - powerOnTime;
                StringBuffer buffer = new StringBuffer();
                Log.d(TAG,"lll = " + l);
                if (l >= 1000)
                    s = l/1000 + "s";
                else
                    s = l + "ms";
                if (l < 60 * 1000) {
                    String s1 = " 本次以太网联网所花时间 =" + s;
                    buffer.append("以太网重启模式---当前时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
                    buffer.append(" 开机时间：" + TimeUtils.tsFormat(powerOnTime,"yyyy-MM-dd HH:mm:ss"));
                    buffer.append(" 关机时间：" + TimeUtils.tsFormat(powerSharedPreferences.getLong(Constant.SP_POWER_OFF_TIME,0),"yyyy-MM-dd HH:mm:ss"));
                    buffer.append(" 以太网重启测试次数：" + count);
                    buffer.append(s1);
                    content.append(buffer.toString() + "\n");
                    netSharedPreferences.edit().putInt(Constant.SP_ETHERNET_REBOOT_COUNT,count+1).apply();
                    netSharedPreferences.edit().putString(Constant.SP_ETHERNET_REBOOT_CONTENT,content.toString()).apply();
                    SQLiteDao.getInstance(context).updateOrder(Constant.DAO_ETH_REBOOT,content.toString());
                }
                testText.setText(buffer.toString());
                handler.postDelayed(CountDown, TimeUnit.SECONDS.toMillis(1));
            } else {
                if (System.currentTimeMillis() - powerOnTime > 60 * 1000) {
                    netSharedPreferences.edit().putString(Constant.SP_ETHERNET_REBOOT_CONTENT,content.toString()).apply();
                    SQLiteDao.getInstance(context).updateOrder(Constant.DAO_ETH_REBOOT,content.append(
                            "以太网重启模式---当前时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") +
                            " 以太网没有网络，停止重启测试!!! \n").toString());
                    testText.setTextColor(getResources().getColor(R.color.colorAccent));
                    testText.setText("以太网没有网络，停止重启测试");
                    stopTest();
                    controller.start();
                } else {
                    testText.setText("重启模式，正在连接网络......");
                    handler.postDelayed(sendReboot,TimeUnit.SECONDS.toMillis(1));
                }
            }
        }
    };

    private Runnable CountDown = new Runnable() {
        @Override
        public void run() {
            count --;
            ToastUtils.showShortToast(context,"以太网重启测试，即将重启，倒计时" + count + "秒");
            handler.postDelayed(CountDown,TimeUnit.SECONDS.toMillis(1));
            if (count == 0)
                PowerOnOffUtils.reboot(context);
        }
    };

    private Runnable OpenCountDown = new Runnable() {
        @Override
        public void run() {
            NetUtils.openOrCloseEth(context,false);
            openCount --;
            ToastUtils.showShortToast(context,openCount + "秒后打开以太网开关，进入下一次测试");
            handler.postDelayed(OpenCountDown,TimeUnit.SECONDS.toMillis(1));
            if (openCount <= 1) {
                Log.d(TAG,"OpenCountDown 打开以太网");
                handler.post(sendNotReboot);
                NetUtils.openOrCloseEth(context,true);
                openEthTime = System.currentTimeMillis();
                handler.removeCallbacks(OpenCountDown);
                return;
            }

        }
    };

    private static class Callback implements Handler.Callback {
        WeakReference<EthernetActivity> reference;

        private Callback(EthernetActivity presenter) {
            reference = new WeakReference<EthernetActivity>(presenter);
        }

        @Override
        public boolean handleMessage(Message msg) {
            EthernetActivity presenter = reference.get();

            return !(presenter == null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.release();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (controller != null) {
            controller.reset();
        }
    }
}
