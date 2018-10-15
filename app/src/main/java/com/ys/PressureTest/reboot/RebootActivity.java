package com.ys.PressureTest.reboot;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ys.PressureTest.Constant;
import com.ys.PressureTest.R;
import com.ys.PressureTest.log.SQLiteDao;
import com.ys.PressureTest.log.TestLogPrinter;
import com.ys.PressureTest.utils.CommandUtils;
import com.ys.PressureTest.utils.PowerOnOffUtils;
import com.ys.PressureTest.utils.TimeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class RebootActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String LOG_TAG = "RebootActivity";
    private final static int MSG_REBOOT = 0;
    private final static int MSG_REBOOT_COUNTDOWN = 1;
    private final static int MSG_REBOOT_STARTCOUNT = 2;

    private final int DELAY_TIME = 5000;// ms
    private final int REBOOT_OFF = 0;
    private final int REBOOT_ON = 1;

    private SharedPreferences mSharedPreferences;

    private TextView mCountTV;
    private TextView mCountdownTV;
    private TextView mMaxTV;
    private Button mStartButton;
    private Button mStopButton;
    private Button mExitBtn;
    private Button mSettingButton;
    private Button mClearButton;

    private PowerManager.WakeLock mWakeLock;
    private int mState;
    private int mCount;
    private int mCountDownTime;
    private int mMaxTimes; // max times to reboot
    private boolean mIsCheckSD = false;
    private boolean mFT = false;
    private String mSdState = null;
    private String RebootMode = null;
    private StringBuffer stringBuffer;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reboot);

        sharedPreferences = getSharedPreferences(Constant.SP_REBOOT,0);
        mSharedPreferences = getSharedPreferences("state", 0);
        mState = mSharedPreferences.getInt("reboot_flag", 0);
        mCount = mSharedPreferences.getInt("reboot_count", 0);
        mMaxTimes = mSharedPreferences.getInt("reboot_max", 0);
        mIsCheckSD = mSharedPreferences.getBoolean("check_sd", false);
        ((KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE)).newKeyguardLock("TestReboot").disableKeyguard();
        mWakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.FULL_WAKE_LOCK, "RebootTest");
        mWakeLock.acquire();
        // init resource
        initRes();
        stringBuffer = new StringBuffer(sharedPreferences.getString(Constant.SP_REBOOT_DATA,""));

        if (mState == REBOOT_ON) {
            stringBuffer.append("重启测试记录的时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") +
                    "  实际测试次数：" + mCount + ",设置次数：" + mMaxTimes +"\n");
            sharedPreferences.edit().putString(Constant.SP_REBOOT_DATA,stringBuffer.toString()).apply();
            SQLiteDao.getInstance(this).updateOrder(Constant.DAO_REBOOT,sharedPreferences.getString(Constant.SP_REBOOT_DATA,""));
            if (mMaxTimes != 0 && mMaxTimes <= mCount) {
                mState = REBOOT_OFF;
                saveSharedPreferences(mState, 0);
                saveMaxTimes(0);
                updateBtnState();
                mCountTV.setText(mCountTV.getText()+" TEST FINISH!");
//                TestLogPrinter.getInstance(this).setRebootFinishLog();
                stringBuffer.append("重启测试记录的时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") +
                        "  实际测试次数：" + mCount + ",设置次数：" + mMaxTimes + "。重启测试完成 \n\n"
                        +"---------------------------------------------------------------------------------------------------");
                sharedPreferences.edit().putString(Constant.SP_REBOOT_DATA,stringBuffer.toString()).apply();
                SQLiteDao.getInstance(this).updateOrder(Constant.DAO_REBOOT,sharedPreferences.getString(Constant.SP_REBOOT_DATA,""));
            }else if(isRebootError()/*false*/){
                mState = REBOOT_OFF;
                saveSharedPreferences(mState, 0);
                saveMaxTimes(0);
                updateBtnState();
                mCountTV.setText(mCountTV.getText()+" Test fail for error!");
//                TestLogPrinter.getInstance(this).setRebootErrorLog();
                stringBuffer.append("重启测试记录的时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") +
                        "  实际测试次数：" + mCount + ",设置次数：" + mMaxTimes + "。重启出错\n\n"
                        +"---------------------------------------------------------------------------------------------------");
                sharedPreferences.edit().putString(Constant.SP_REBOOT_DATA,stringBuffer.toString()).apply();
                SQLiteDao.getInstance(this).updateOrder(Constant.DAO_REBOOT,sharedPreferences.getString(Constant.SP_REBOOT_DATA,""));
            }else {
                mCountDownTime = DELAY_TIME / 1000;
                mHandler.sendEmptyMessage(MSG_REBOOT_STARTCOUNT);
            }
        }
//        TestLogPrinter.getInstance(this).setRebootLog(mCount,mMaxTimes);
    }

    private void initRes() {
        mCountTV = (TextView) findViewById(R.id.count_tv);
        mCountTV.setText(getString(R.string.have_test_time) + mCount);
        mMaxTV = (TextView) findViewById(R.id.maxtime_tv);
        if (mMaxTimes == 0) {
            mMaxTV.setText(getString(R.string.set_test_time)
                    + getString(R.string.not_setting));
        } else {
            mMaxTV.setText(getString(R.string.set_test_time) + mMaxTimes);
        }

        mStartButton = (Button) findViewById(R.id.start_btn);
        mStartButton.setOnClickListener(this);

        mStopButton = (Button) findViewById(R.id.stop_btn);
        mStopButton.setOnClickListener(this);

        mExitBtn = (Button) findViewById(R.id.exit_btn);
        mExitBtn.setOnClickListener(this);

        mSettingButton = (Button) findViewById(R.id.setting_btn);
        mSettingButton.setOnClickListener(this);

        mClearButton = (Button) findViewById(R.id.clear_btn);
        mClearButton.setOnClickListener(this);

        updateBtnState();
        mCountdownTV = (TextView) findViewById(R.id.countdown_tv);
    }

    private void reboot() {
        // save state
        saveSharedPreferences(mState, mCount + 1);

//        Intent reboot = new Intent(Intent.ACTION_REBOOT);
//        reboot.putExtra("nowait", 1);
//        reboot.putExtra("interval", 1);
//        reboot.putExtra("window", 0);
//        sendBroadcast(reboot);

//		PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		pManager.reboot("重启");
//		System.out.println("execute cmd--> reboot\n" + "重启");
//        CommandUtils.execCommandSu("reboot");
        PowerOnOffUtils.reboot(this);
    }

    private void saveSharedPreferences(int flag, int count) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt("reboot_flag", flag);
        edit.putInt("reboot_count", count);
        edit.putBoolean("check_sd", mIsCheckSD);
        edit.apply();
    }

    private void saveMaxTimes(int max) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt("reboot_max", max);
        edit.apply();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REBOOT:
                    if (mState == 1)
                        reboot();
                    break;

                case MSG_REBOOT_COUNTDOWN:
                    if (mState == 0)
                        return;
                    if (mCountDownTime != 0) {
                        mCountdownTV.setText(getString(R.string.reboot_countdown)
                                + mCountDownTime);
                        mCountdownTV.setVisibility(View.VISIBLE);
                        mCountDownTime--;
                        sendEmptyMessageDelayed(MSG_REBOOT_COUNTDOWN, 1000);
                    } else {
                        if(isSystemError()){
                            mState = REBOOT_OFF;
                            saveSharedPreferences(mState, 0);
                            saveMaxTimes(0);
                            updateBtnState();
                            mCountTV.setText(mCountTV.getText()+" Test fail for error!");
//                            TestLogPrinter.getInstance(RebootActivity.this).setRebootErrorLog();
                            stringBuffer.append("重启测试记录的时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss")+
                                    "  实际测试次数：" + mCount + ",设置次数：" + mMaxTimes + "。重启出错\n\n"
                                    +"---------------------------------------------------------------------------------------------------");
                            sharedPreferences.edit().putString(Constant.SP_REBOOT_DATA,stringBuffer.toString()).apply();
                            SQLiteDao.getInstance(RebootActivity.this).updateOrder(Constant.DAO_REBOOT,sharedPreferences.getString(Constant.SP_REBOOT_DATA,""));
                        }else{
                            mCountdownTV.setText(getString(R.string.reboot_countdown)
                                    + mCountDownTime);
                            mCountdownTV.setVisibility(View.VISIBLE);
                            sendEmptyMessage(MSG_REBOOT);
                        }
                    }
                    break;
                case MSG_REBOOT_STARTCOUNT:
                    sendEmptyMessage(MSG_REBOOT_COUNTDOWN);
                    break;

                default:
                    break;
            }
        };
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_btn:
                onStartClick();
                break;
            case R.id.stop_btn:
                onStopClick();
                break;
            case R.id.exit_btn:
                finish();
                break;
            case R.id.setting_btn:
                onSettingClick();
                break;
            case R.id.clear_btn:
                onClearSetting();
                break;
            default:
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //       stopTest();
        mWakeLock.release();
    }

    private void onStartClick() {
        mFT = true;
        new AlertDialog.Builder(RebootActivity.this)
                .setTitle(R.string.reboot_dialog_title)
                .setMessage(R.string.reboot_dialog_msg)
                .setPositiveButton(R.string.dialog_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mState = REBOOT_ON;
                                mCountDownTime = DELAY_TIME / 1000; // ms->s
                                updateBtnState();
                                mHandler.sendEmptyMessage(MSG_REBOOT_STARTCOUNT);
                            }
                        })
                .setNegativeButton(R.string.dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                            }
                        }).show();
    }

    private void onStopClick() {
        mHandler.removeMessages(MSG_REBOOT);
        mCountdownTV.setVisibility(View.INVISIBLE);
        mState = REBOOT_OFF;
        updateBtnState();
        mIsCheckSD = false;
        saveSharedPreferences(mState, 0);

    }

    private void onSettingClick() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_setting)
                .setView(editText)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!editText.getText().toString().trim().equals("")) {
                            mMaxTimes = Integer.valueOf(editText.getText().toString());
                            saveMaxTimes(mMaxTimes);
                            mMaxTV.setText(getString(R.string.set_test_time)+mMaxTimes);
                            mCount = 0;
                            saveSharedPreferences(mState,0);
                            mCountTV.setText(getString(R.string.have_test_time)+mCount);
//                            TestLogPrinter.getInstance(RebootActivity.this).setRebootLog(mCount,mMaxTimes);
//                            SQLiteDao.getInstance(RebootActivity.this).updateOrder(Constant.DAO_REBOOT,"重启测试记录的时间：" + TimeUtils.getCurrentTime("yyyy-MM-dd HH:mm:ss") +
//                                    "  实际测试次数：" + mCount + ",设置次数：" + mMaxTimes + "\n\n"
//                                    +"---------------------------------------------------------------------------------------------------");
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void onClearSetting() {
        mMaxTimes = 0;
        mCount = 0;
        saveMaxTimes(mMaxTimes);
        saveSharedPreferences(mState,0);
        mMaxTV.setText(getString(R.string.set_test_time) + getString(R.string.not_setting));
        mCountTV.setText(getString(R.string.have_test_time) + getString(R.string.not_setting));
    }

    private void SavedRebootMode() {
        Log.d(LOG_TAG,"SavedRebootMode");
        Process process = null;
        String filePath = "mnt/internal_sd/boot_mode.txt";
        File file1 = new File(filePath);
        if (file1.isFile() && file1.exists()) {
            file1.delete();
        }
        try {
            getBootMode();
        } catch (Exception e) {
            Log.e(LOG_TAG, "getBootMode fail!!!");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    Log.d(LOG_TAG,lineTxt);
                    int p1 = lineTxt.indexOf("(");
                    int p2 = lineTxt.indexOf(")");
                    RebootMode = lineTxt.substring(p1+1, p2);
                    Toast.makeText(this,RebootMode,Toast.LENGTH_LONG ).show();
                    Log.d(LOG_TAG,"RebootMode=" + RebootMode);
                }
                Log.d(LOG_TAG,"RebootMode=" + RebootMode);
                read.close();
            }else{
                Log.e(LOG_TAG,"not find the mnt/internal_sd/boot_mode.txt");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG,"read error!!");
            e.printStackTrace();
        }
    }
    private boolean isRebootError(){
        Log.d(LOG_TAG,"isRebootError");
//        SavedRebootMode();
        if(RebootMode!=null){
            if(Integer.valueOf(RebootMode) == 7){
                Dialog dialog = new AlertDialog.Builder(
                        this)
                        .setTitle("重启测试异常")
                        .setMessage("检测到本次为panic重启，详情请看last_log")
                        .setPositiveButton("确定",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int whichButton) {
                                dialog.cancel();
                            }
                        }).setNegativeButton("取消",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int whichButton) {
                                dialog.cancel();
                            }}).create();
                dialog.show();
                return true;

            }else if(Integer.valueOf(RebootMode) == 8){
                Dialog dialog = new AlertDialog.Builder(
                        this)
                        .setTitle("重启测试异常")
                        .setMessage("检测到本次为watchdog重启，详情请看last_log")
                        .setPositiveButton("确定",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int whichButton) {
                                dialog.cancel();
                            }}).setNegativeButton("取消",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int whichButton) {
                                dialog.cancel();
                            }}).create();
                dialog.show();
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean isSystemError(){
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        Process process = null;
        String lineText = null;
        if(false){
            try{
                process = Runtime.getRuntime().exec("logcat -d");
                reader = new InputStreamReader(process.getInputStream());
                bufferedReader = new BufferedReader(reader);
                while((lineText = bufferedReader.readLine()) != null){
                    Log.d(LOG_TAG,"-------------->>lineTxt:"+lineText);
                    if(lineText.indexOf("Force finishing activity")!=-1||lineText.indexOf("backtrace:")!=-1){
                        Log.d("--hjc","------lineTxt:"+lineText);
                        Dialog dialog = new AlertDialog.Builder(
                                this)
                                .setTitle("重启测试异常")
                                .setMessage("检测到系统异常，详情请看logcat")
                                .setPositiveButton("确定",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int whichButton) {
                                        dialog.cancel();
                                    }
                                }).setNegativeButton("取消",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int whichButton) {
                                        dialog.cancel();
                                    }}).create();
                        dialog.show();
                        reader.close();
                        bufferedReader.close();
                        return true;
                    }
                }
                reader.close();
                bufferedReader.close();
                return false;
            } catch (Exception e) {
                Log.e(LOG_TAG,"process Runtime error!!");
                e.printStackTrace();
            }
        }
        return false;

    }

    private void updateBtnState() {
        mStartButton.setEnabled(mState == REBOOT_OFF);
        mClearButton.setEnabled(mState == REBOOT_OFF);
        mSettingButton.setEnabled(mState == REBOOT_OFF);
        mStopButton.setEnabled(mState == REBOOT_ON);
    }

    public static void getBootMode() throws IOException {
            CommandUtils.setValueToProp("ctl.start", "getbootmode");
            String mount_rt_add;
            while (true) {
                mount_rt_add = CommandUtils.getValueFromProp("init.svc.getbootmode");
                Log.d(LOG_TAG,"mount_rt  " + mount_rt_add);
                if (mount_rt_add != null && mount_rt_add.equals("stopped")) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    Log.e(LOG_TAG, "Exception: " + ex.getMessage());
                }
            }

    }

}
