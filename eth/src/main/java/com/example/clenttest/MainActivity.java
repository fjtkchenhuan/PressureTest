package com.example.clenttest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Device.DeviceHelper;
import com.example.Receiver.StartBootComplete;
import com.example.Service.MainServices;
import com.example.config.AppConfig;
import com.example.util.FileUtils;
import com.example.util.GetGateway;
import com.example.util.IPAddressTest;
import com.example.util.MyToastView;
import com.example.util.SharedPreferenceutil;
import com.example.util.SimpleDateUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    String fileName ;
    String filePath;
    private TextView tv_show,tv_frequency;
    private ScrollView scrollView;
    private Button btn_start, btn_stop, btn_clear, btn_mode,btn_size;
    String lost = "";// 丢包
    String delay = "";// 延迟
    String result = "";
    private static final String tag = "TAG";// Log标志
    String ping, ip;
    int i = SharedPreferenceutil.getFrequency(); ;
    Thread thread = null;
    Process process = null;
    BufferedReader successReader = null;
    BufferedReader errorReader = null;
    DataOutputStream dos = null;
    String data;
    String errorData;
    private String[] light ;
    Handler handler1 = new Handler() {// 创建一个handler对象 ，用于监听子线程发送的消息
        public void handleMessage(Message msg)// 接收消息的方法
        {
            switch (msg.what) {
                case 10:
                    String resultmsg = (String) msg.obj;
                    tv_show.append(resultmsg);
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    writeToSd(resultmsg);
                    Log.i(tag, "====handlerThread====:"
                            + Thread.currentThread().getId());
                    Log.i(tag, "====resultmsg====:" + msg.what);
                    Log.i(tag, "====resultmsg====:" + resultmsg);
                    break;
                case 11:
                    Log.i("=msg.what==", "======================");
                    String showDesc = "丢包率：" + lost + "\t\t" + "平均延时" + delay + "\r\n" + "开机时间:"
                            + DeviceHelper.getBootDate() + "\r\n" + "测试时间" + SimpleDateUtil.getDate() + "\r\n";
                    tv_show.append(showDesc);
                    Log.i("=text=", showDesc);
                    writeToSd(showDesc);
                    restartMethion();
                    break;
                case 12:
                    String error = "丢包率：" + "100%" + "\t\t" + errorData + "\r\n" + "开机时间:"
                            + DeviceHelper.getBootDate() + "\r\n" + "测试时间" + SimpleDateUtil.getDate() + "\r\n";
                    tv_show.append(error);
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    writeToSd(error);
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.i(TAG,"AppConfig.TAG="+AppConfig.TAG+"/"+AppConfig.ISINFORMATION);
        if (AppConfig.TAG.equals(AppConfig.ISINFORMATION)){
            fileName = SimpleDateUtil.getTime();
            SharedPreferenceutil.setFileName(fileName);
            filePath = "/sdcard/information/"+fileName;
            Log.i("SharedPreferenceutil","filePath="+filePath);
            try {
                StartBootComplete.setToSd(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent startservice = new Intent(this, MainServices.class);
            startService(startservice);
        }else {
            setContentView(R.layout.activity_main);
            permission();
            initView();
            String tempIpInfo= IPAddressTest.printIpAddressAndSubnettest();
            if (!TextUtils.isEmpty(tempIpInfo)){
                StringBuilder stringBuilder = new StringBuilder(tempIpInfo);
                ip = stringBuilder.replace(tempIpInfo.length()-1,tempIpInfo.length(),"1").toString().toLowerCase();
                SharedPreferenceutil.setGateway(ip);
            }else {
                ip = SharedPreferenceutil.getGateway();
            }
            String countCmd = " -c " + 10 + " ";
            String sizeCmd = " -s " + 64 + " ";
            String timeCmd = " -i " + 1 + " ";
            String ip_adress = ip;
            Log.i("SharedPreferenceutil", "ip=" + ip);
            ping = "ping" + countCmd + timeCmd + sizeCmd + ip_adress;
            startThread();
        }
    }

    private void permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                List<PermissionItem> permissions = new ArrayList<PermissionItem>();
                permissions.add(
                        new PermissionItem(Manifest.permission.INTERNET, "网络权限", R.drawable.permission_ic_phone));
                permissions.add(
                        new PermissionItem(Manifest.permission.READ_EXTERNAL_STORAGE, "SD卡读取权限", R.drawable.permission_ic_phone));
                permissions.add(
                        new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "SD卡写入权限", R.drawable.permission_ic_phone));
                HiPermission.create(MainActivity.this).title(MainActivity.this.getString(R.string.permission_cus_title)).permissions(
                        permissions).msg(MainActivity.this.getString(R.string.permission_cus_msg)).animStyle(R.style.PermissionAnimModal).style(
                        R.style.PermissionDefaultBlueStyle).checkMutiPermission(new PermissionCallback() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onClose() {
                        Log.i(TAG, "用户拒绝我们的权限");
                    }

                    @Override
                    public void onFinish() {
                        Log.i("main", "onFinish");
                    }

                    @Override
                    public void onDeny(String permission, int position) {
                        MyToastView.getInstance().Toast(MainActivity.this,"您拒绝了" + permission + "权限,请手动打开");
                        Log.i("main", "用户禁止了权限" + permission);
                    }

                    @Override
                    public void onGuarantee(String permission, int position) {
                        Log.i("main", "onGuarantee" + permission);
                    }
                });
            }
    }

    private void initView() {
        tv_show = (TextView) findViewById(R.id.tv_show);
        tv_frequency = (TextView) findViewById(R.id.tv_frequency);
        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        btn_clear = findViewById(R.id.btn_clear);
        btn_mode = findViewById(R.id.btn_mode);
        scrollView = findViewById(R.id.scrollView);
        btn_size = findViewById(R.id.btn_size);
        btn_size.setOnClickListener(this);
        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        btn_mode.setOnClickListener(this);
        if (Build.MODEL.contains("3399")){
            light = new String[]{"/sys/devices/platform/misc_power_en/red_led", "/sys/devices/misc_power_en.23/green_led"};
        }else if (Build.MODEL.contains("rk3368")){
            light = new String[]{"/sys/devices/misc_power_en.22/green_led", "/sys/devices/misc_power_en.23/green_led"};
        }else if (Build.MODEL.contains("MStar Android Tv")){
            light = new String[]{"/sys/devices/misc_power_en.0/green_led", "/sys/devices/misc_power_en.23/green_led"};
        }else if (Build.MODEL.contains("rk3288")){
            light = new String[]{"/sys/devices/misc_power_en.23/green_led","/sys/devices/misc_power_en.22/green_led"};
        }else if (Build.MODEL.contains("rk3128")){
            light = new String[]{"/sys/devices/misc_power_en.19/out8", "/sys/devices/misc_power_en.18/out8"};
        }else if (Build.MODEL.contains("rk3328")){
            light = new String[]{"/sys/devices/misc_power_en.32/led"};
        }else if (Build.MODEL.contains("A83t")){
            light = new String[]{"/sys/devices/misc_power_en.22/green_led"};
        }
        FileUtils.method(light[0], "1", false);
        tv_frequency.setText("重启次数:"+i);
        this.tv_show.setText("开始Ping测试......\r\n");
    }

    private void restartMethion() {
        if (!SharedPreferenceutil.getSelect()) {
            Intent localIntent = new Intent("android.intent.action.REBOOT");
            localIntent.putExtra("nowait", 1);
            localIntent.putExtra("interval", 1);
            localIntent.putExtra("window", 0);
            sendBroadcast(localIntent);
            i++;
            SharedPreferenceutil.setFrequency(i);
        }
    }

    public void writeToSd(String writeDesc) {
        FileUtils.method("/sdcard/Pinglog.txt", writeDesc, true);
    }

    private void startThread() {
        thread = new Thread() {
            public void run() {
                delay = "";
                lost = "";
                while (!interrupted()) {
                    try {
                        Log.i("process", "====ping==" + ping);
                        process = Runtime.getRuntime().exec(ping);
                        Log.i(tag, "====receive====:");
                        InputStream in = process.getInputStream();
                        successReader = new BufferedReader(
                                new InputStreamReader(in));
                        errorReader = new BufferedReader(new InputStreamReader(
                                process.getErrorStream()));
                        String lineStr;
                        while ((lineStr = successReader.readLine()) != null&&!interrupted()) {
                            Log.i(tag, "====receive====:" + lineStr);
                            Message msg = handler1.obtainMessage();
                            msg.obj = lineStr + "\r\n";
                            msg.what = 10;
                            msg.sendToTarget();
                            result = result + lineStr + "\n";
                            if (lineStr.contains("packet loss")) {
                                Log.i(tag, "=====Message=====" + lineStr.toString());
                                int i = lineStr.indexOf("received");
                                int j = lineStr.indexOf("%");
                                Log.i(tag,
                                        "====丢包率====:"
                                                + lineStr.substring(i + 10, j + 1));//
                                lost = lineStr.substring(i + 10, j + 1);
                            }
                            if (lineStr.contains("avg")) {
                                int i = lineStr.indexOf("/", 20);
                                int j = lineStr.indexOf(".", i);
                                Log.i(tag,
                                        "====平均时延:====" + lineStr.substring(i + 1, j));
                                delay = lineStr.substring(i + 1, j);
                                delay = delay + "ms";
                            }
                        }
                        if (process.waitFor() == 0) {
                            Log.i("successReader", "==successReader======");
                            Message msg1 = handler1.obtainMessage();
                            msg1.what = 11;
                            msg1.sendToTarget();
                        } else {
                            Log.i("startThread", "==startThread======");
                            errorData = " PING:传输失败。General failure.";
                            FileUtils.method(light[0], "0", false);
                            sleep(1000);
                            FileUtils.method(light[0], "1", false);
                            Message msg2 = handler1.obtainMessage();
                            msg2.what = 12;
                            msg2.sendToTarget();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("InterruptedException");
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        thread.start();
    }

    @Override
    protected void onDestroy() {
        FileUtils.method(light[0], "1", false);
        super.onDestroy();
        try {
            dos.close();
            successReader.close();
            errorReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        process.destroy();
        System.exit(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                startThread();
                break;
            case R.id.btn_stop:
                this.thread.interrupt();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        thread.interrupt();
                    }
                },1000);
                break;
            case R.id.btn_clear:
                FileUtils.method("/sdcard/aaa.txt", "", false);
                MyToastView.getInstance().Toast(MainActivity.this,"日志清除成功");
                break;
            case R.id.btn_mode:
                Intent intent = new Intent(MainActivity.this, ModeSelectionActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_size:
                SharedPreferenceutil.setFrequency(0);
                tv_frequency.setText("重启次数:"+0);
                MyToastView.getInstance().Toast(MainActivity.this,"次数清零成功");
                break;
        }
    }
}