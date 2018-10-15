package com.ys.PressureTest.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ys.PressureTest.Constant;
import com.ys.PressureTest.PermissionActivity;
import com.ys.PressureTest.R;
import com.ys.PressureTest.log.SQLiteDao;
import com.ys.PressureTest.utils.LedController;
import com.ys.PressureTest.utils.NetUtils;
import com.ys.PressureTest.utils.PowerOnOffUtils;
import com.ys.PressureTest.utils.TimeUtils;
import com.ys.PressureTest.utils.ToastUtils;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WifiActivity extends PermissionActivity implements View.OnClickListener{

    private static final String TAG = "WifiActivity";
    WifiAdapter mWifiAdapter;
    ListView wifiList;
    ImageView wifiSwitch;
    TextView failText;
    private List<ScanResult> scanResults;
    private NetManager netManager;
    private Handler handler;
    private SharedPreferences sharedPreferences;
    private SharedPreferences powerSharedPreferences;
    private static boolean isRebootRunnable;
    private static boolean isSwitchRunnable;
    private LedController controller;
    private boolean allowToPost = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        wifiList = findViewById(R.id.list_wifi);
        wifiSwitch = findViewById(R.id.wlan_switch_iv);
        failText = findViewById(R.id.text_failed);
        handler = new Handler(new Callback(this));
        findViewById(R.id.wifi_reboot).setOnClickListener(this);
        findViewById(R.id.wifi_not_reboot).setOnClickListener(this);
        findViewById(R.id.stop_test).setOnClickListener(this);

        netManager = NetManager.getIntance(this);
        sharedPreferences = getSharedPreferences(Constant.SP_NET_TEST,0);
        powerSharedPreferences = getSharedPreferences(Constant.SP_POWER_ON_OFF,0);
        controller = new LedController();

        setWifi();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.setPriority(1000);
        registerReceiver(wifiReceiver, intentFilter);

        int wifiMode = sharedPreferences.getInt(Constant.SP_WIFI_REBOOT_MODE,0);
        Log.d(TAG,"wifiMode = " + wifiMode + ",isRebootRunnable = " + isRebootRunnable + ",isSwitchRunnable = " + isSwitchRunnable);
        if (wifiMode == 1 && !isRebootRunnable) {
            handler.post(WifiRebootRunnable);
        }else if (wifiMode == 2 && !isSwitchRunnable)
            handler.post(WifiSwitchRunnable);
    }


    private void setWifi() {
//        mWifiAdapter = new WifiAdapter(this);
//        wifiList.setAdapter(mWifiAdapter);
//        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                showPasswordDialog(position);
//            }
//        });

        boolean bool = netManager.isWifiEnabled();
        wifiSwitch.setImageResource(bool ? R.drawable.controlbutton_open : R.drawable.controlbutton_close);
        wifiSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wlanSwitch();
            }
        });
    }

    private void wlanSwitch() {
        Log.i("dc", "wlanSwitch");
        //初始状态
        boolean bool = netManager.isWifiEnabled();
        bool = !bool;
        wifiSwitch.setImageResource(bool ? R.drawable.controlbutton_open : R.drawable.controlbutton_close);
        if (bool)
            netManager.openWifi();
         else
            netManager.closeWifi();
    }

    private void showPasswordDialog(int position) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        View view = View.inflate(this,R.layout.wifi_password_dialog,null);
        alertDialog.setView(view);
        alertDialog.setCancelable(true);
        final EditText password = view.findViewById(R.id.et_pwd);
        CheckBox showPassword = view.findViewById(R.id.cb_show);
        final Button connect = view.findViewById(R.id.bt_connect);
        password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.requestFocus();
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                connect.setEnabled(password.getText().toString().trim().length() >= 8);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                //光标后置
                Editable etext = password.getText();
                Selection.setSelection(etext, etext.length());
            }
        });

        final AlertDialog dialog = alertDialog.create();
        dialog.show();
        final ScanResult scanResult = scanResults.get(position);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scanResult != null) {
                    int netId = netManager.addWifiConfig(scanResults,scanResult.SSID,password.getText().toString(),scanResult.capabilities);
                    if (netId != -1) {
                        netManager.getConfiguration();
                        netManager.connectWifi(netId);
                    } else
                        ToastUtils.showShortToast(WifiActivity.this,"网络连接错误");
                }
                dialog.dismiss();
            }
        });

    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (netManager != null)
                    netManager.startScan();
                return true;
            }
        });

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        };

        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG,"action = " + intent.getAction());
            if ((WifiManager.SCAN_RESULTS_AVAILABLE_ACTION).equals(intent.getAction())) {
                scanResults = netManager.getScanResults();
//                mWifiAdapter.update(scanResults);
                handler.postDelayed(runnable, 1000);
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (state) {
                    case WifiManager.WIFI_STATE_DISABLED:
//                        mWifiAdapter.clear();
                        wifiSwitch.setImageResource(R.drawable.controlbutton_close);
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        netManager.startScan();
                        wifiSwitch.setImageResource(R.drawable.controlbutton_open);
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        break;
                }

            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                SupplicantState state = (SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
//                if (mWifiAdapter != null && state != null) {
//                    mWifiAdapter.updateWifiState(WifiInfo.getDetailedStateOf(state).ordinal());
//                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

//                if (mWifiAdapter != null && info != null) {
//                    mWifiAdapter.updateWifiState(info.getDetailedState().ordinal());
//                }

            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wifiReceiver);
        controller.release();
    }

    @Override
    public void onClick(View v) {
        boolean netIsAvailable = isWifiNetOn();
        switch (v.getId()) {
            case R.id.wifi_reboot:
                sharedPreferences.edit().putInt(Constant.SP_WIFI_REBOOT_MODE,1).apply();
                if (!netIsAvailable)
                    ToastUtils.showShortToast(this,"请先确认wifi已联网");
                else
                    handler.postDelayed(WifiRebootRunnable,1000);
                break;
            case R.id.wifi_not_reboot:
                sharedPreferences.edit().putInt(Constant.SP_WIFI_REBOOT_MODE,2).apply();
                if (!netIsAvailable)
                    ToastUtils.showShortToast(this,"请先确认wifi已联网");
                else
                    handler.postDelayed(WifiSwitchRunnable,1000);
                break;
            case R.id.stop_test:
                Log.d(TAG,"stop");
               stopTest();
                break;
                default:
                    break;
        }
    }

    private void stopTest() {
        sharedPreferences.edit().putInt(Constant.SP_WIFI_REBOOT_MODE,0).apply();
        handler.removeCallbacksAndMessages(null);
        isRebootRunnable = false;
        isSwitchRunnable = false;
        allowToPost = false;
    }

    private Runnable WifiRebootRunnable = new Runnable() {
        @Override
        public void run() {
            isRebootRunnable = true;
            long powerOnTime = powerSharedPreferences.getLong(Constant.SP_POWER_ON_TIME,0);
            int count = sharedPreferences.getInt(Constant.SP_WIFI_REBOOT_COUNT,1);
            StringBuffer content = new StringBuffer(sharedPreferences.getString(Constant.SP_WIFI_REBOOT_CONTENT,""));
            if (isWifiNetOn()) {
                String s;
                long l = System.currentTimeMillis() - powerOnTime;
                StringBuffer buffer = new StringBuffer();
                Log.d(TAG,"lll = " + l);
                if (l >= 1000)
                    s = l/1000 + "s";
                else
                    s = l + "ms";
                if (l < 120 * 1000) {
                    String s1 = " 本次wifi联网所花时间 =" + s;
                    buffer.append("Wifi重启模式---当前时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
                    buffer.append(" 开机时间：" + TimeUtils.tsFormat(powerOnTime,"yyyy-MM-dd HH:mm:ss"));
                    buffer.append(" 关机时间：" + TimeUtils.tsFormat(powerSharedPreferences.getLong(Constant.SP_POWER_OFF_TIME,0),"yyyy-MM-dd HH:mm:ss"));
                    buffer.append(" wifi重启测试次数：" + count);
                    buffer.append(s1);
                    content.append(buffer.toString() + "\n");
                    sharedPreferences.edit().putInt(Constant.SP_WIFI_REBOOT_COUNT,count+1).apply();
                    sharedPreferences.edit().putString(Constant.SP_WIFI_REBOOT_CONTENT,content.toString()).apply();
                    SQLiteDao.getInstance(WifiActivity.this).updateOrder(Constant.DAO_WIFI_REBOOT,content.toString());
                }
                handler.postDelayed(CountDown, TimeUnit.SECONDS.toMillis(1));
            } else {
                if (System.currentTimeMillis() - powerOnTime > 120 * 1000) {
                    SQLiteDao.getInstance(WifiActivity.this).updateOrder(Constant.DAO_WIFI_REBOOT,content.append(
                            "Wifi重启模式---当前时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") + " wifi没有网络，停止重启测试!!! \n").toString());
                    sharedPreferences.edit().putString(Constant.SP_WIFI_REBOOT_CONTENT,content.toString()).apply();
                    stopTest();
                    controller.start();
                    failText.setVisibility(View.VISIBLE);
                    netManager.closeWifi();
                } else {
                    handler.postDelayed(WifiRebootRunnable,TimeUnit.SECONDS.toMillis(3));
                }
            }
        }
    };

    int rebootCount = 16;
    private Runnable CountDown = new Runnable() {
        @Override
        public void run() {
            rebootCount --;
            ToastUtils.showShortToast(WifiActivity.this,"Wifi重启测试，即将重启，倒计时" + rebootCount + "秒");
            handler.postDelayed(CountDown,TimeUnit.SECONDS.toMillis(1));
            if (rebootCount == 0)
                PowerOnOffUtils.reboot(WifiActivity.this);
        }
    };

    private Runnable WifiSwitchRunnable = new Runnable() {
        @Override
        public void run() {
            isSwitchRunnable = true;
            int count = sharedPreferences.getInt(Constant.SP_WIFI_NOT_REBOOT_COUNT,1);
            StringBuffer content = new StringBuffer(sharedPreferences.getString(Constant.SP_WIFI_NOT_REBOOT_CONTENT,""));
            boolean isNetOn = isWifiNetOn();

            if (allowToPost) {
                if (openWifiSwitchTime == 0) {
                    Log.d(TAG,"第一次进入测试");
                    netManager.closeWifi();
                    openCount = 16;
                    handler.postDelayed(WifiSwitchCount,2000);
                } else {
                    Log.d(TAG,"isNetOn = " + isNetOn);
                    if (isNetOn) {
                        String s;
                        long l = System.currentTimeMillis() - openWifiSwitchTime;
                        if (l > 1000)
                            s = l/1000 + "s";
                        else
                            s = l + "ms";
                        String s1 = " 本次wifi联网所花时间 =" + s;
                        if (l < 120 * 1000) {
                            content.append("Wifi开关模式---当前时间："+ TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
                            content.append(" wifi控制开关测试次数：" + count);
                            content.append(s1);
                            content.append("\n");
                            sharedPreferences.edit().putInt(Constant.SP_WIFI_NOT_REBOOT_COUNT,count + 1).apply();
                            sharedPreferences.edit().putString(Constant.SP_WIFI_NOT_REBOOT_CONTENT,content.toString()).apply();
                            SQLiteDao.getInstance(WifiActivity.this).updateOrder(Constant.DAO_WIFI_NOT_REBOOT,content.toString());
                        }

                        Log.d(TAG,"网络已连接，关闭wifi");
                        ToastUtils.showShortToast(WifiActivity.this,"网络已连接，即将关闭wifi开关");
                        openCount = 16;
                        handler.postDelayed(WifiSwitchCount,5000);
                    }else {
                        ToastUtils.showShortToast(WifiActivity.this,"wifi开关已打开，正在连接网络.......");
                        if ((System.currentTimeMillis() - openWifiSwitchTime) > 120 * 1000) {
                            Log.d(TAG,"wifi无网络，测试中止");
                            content.append("Wifi开关模式---当前时间："+ TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") + " wifi未连接，开关模式测试中止!!! \n");
                            SQLiteDao.getInstance(WifiActivity.this).updateOrder(Constant.DAO_WIFI_NOT_REBOOT,content.toString());
                            sharedPreferences.edit().putString(Constant.SP_WIFI_NOT_REBOOT_CONTENT,content.toString()).apply();
                            stopTest();
                            controller.start();
                            failText.setVisibility(View.VISIBLE);
                            netManager.closeWifi();
                        }else {
                            handler.postDelayed(WifiSwitchRunnable, TimeUnit.SECONDS.toMillis(3));
                        }
                    }
                }
            }

        }
    };

    private int openCount;
    private long openWifiSwitchTime;
    private Runnable WifiSwitchCount = new Runnable() {
        @Override
        public void run() {
            netManager.closeWifi();
            openCount--;
            ToastUtils.showShortToast(WifiActivity.this, openCount + "秒后打开Wifi开关");
            handler.postDelayed(WifiSwitchCount,1000);
            if (openCount <= 1) {
                Log.d(TAG,"打开wifi");
                netManager.openWifi();
                openWifiSwitchTime = System.currentTimeMillis();
                handler.removeCallbacks(WifiSwitchCount);
                handler.post(WifiSwitchRunnable);
            }

        }
    };

    private static class Callback implements Handler.Callback {
        WeakReference<WifiActivity> reference;

        private Callback(WifiActivity presenter) {
            reference = new WeakReference<WifiActivity>(presenter);
        }

        @Override
        public boolean handleMessage(Message msg) {
            WifiActivity presenter = reference.get();

            return !(presenter == null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (controller != null) {
            controller.reset();
        }
    }

    private boolean isWifiNetOn() {
        return NetUtils.ping() && NetUtils.getNetWorkType(this) == 1 && netManager.isWifiEnabled();
    }
}
