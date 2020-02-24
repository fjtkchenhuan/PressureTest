package com.ys.PressureTest.poweronoff;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ys.PressureTest.Constant;
import com.ys.PressureTest.R;
import com.ys.PressureTest.utils.TimeUtils;

public class AutoPowerOnOffActivity extends AppCompatActivity {

    private TextView textCount;
    private TextView powerTime;
    private Button stop;
    private SharedPreferences powerOnOffSp;
    private static final long MINUTES = 60 * 1000;
    private static final long POWER_ON_FF_TIME_INTERVAL = 4 * MINUTES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_power_on_off);

        powerOnOffSp = getSharedPreferences(Constant.SP_POWER_ON_OFF,0);

        textCount = findViewById(R.id.count);
        powerTime = findViewById(R.id.power_on_off_time);
        stop = findViewById(R.id.stop);

        initView();
    }

    private void initView() {
        int[][] times = setPowerOnOff(this);
        if (times[0] == null || times[1] == null) return;
        String dateOn = "开机时间：" + times[0][0] + "-" + makeupZero(times[0][1]) + "-" + makeupZero(times[0][2])
                + " " + makeupZero(times[0][3]) + ":" + makeupZero(times[0][4]);
        String dateoff = "关机时间：" + times[1][0] + "-" + makeupZero(times[1][1]) + "-" + makeupZero(times[1][2])
                + " " + makeupZero(times[1][3]) + ":" + makeupZero(times[1][4]);
        int count = powerOnOffSp.getInt(Constant.SP_AUTO_POWERONOFF_COUNTS , 0);
        textCount.setText("定时开关机测试次数：" + count);
        powerTime.setText(dateOn + "\n" + dateoff);

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.intent.ClearOnOffTime");
                intent.setPackage("com.adtv");
                sendBroadcast(intent);
                powerOnOffSp.edit().putInt(Constant.SP_AUTO_POWERONOFF_COUNTS , 0).apply();
                textCount.setText("定时开关机测试次数：" + 0);
                Toast.makeText(AutoPowerOnOffActivity.this,"停止自动测试定时开关机",Toast.LENGTH_LONG).show();
            }
        });
    }

    private String makeupZero(int value) {
        return value < 10 ? "0" + value : "" + value;
    }

    private int[][] setPowerOnOff(Context context) {
        int[] powerOnTime = new int[5];
        int[] powerOffTime = new int[5];
        timeParse(powerOnTime, powerOffTime);
        Intent intent = new Intent("android.intent.action.setpoweronoff");
        intent.putExtra("timeon", powerOnTime);
        intent.putExtra("timeoff", powerOffTime);
        intent.putExtra("enable", true); //使能开关机功能，设为 false,则为关闭
        intent.setPackage("com.adtv");
        context.sendBroadcast(intent);
        return new int[][]{powerOnTime, powerOffTime};
    }

    private static void timeParse(int[] powerOnTime, int[] powerOffTime) {
        long l = System.currentTimeMillis();
        long twoMLater = l + MINUTES;
        long fourMLater = twoMLater + POWER_ON_FF_TIME_INTERVAL;
        String two = TimeUtils.tsFormat(twoMLater, "yyyy-MM-dd-HH-mm");
        String four = TimeUtils.tsFormat(fourMLater, "yyyy-MM-dd-HH-mm");
        String[] twos = two.split("-");
        String[] fours = four.split("-");
        for (int i = 0; i < twos.length; i++) {
            powerOffTime[i] = Integer.parseInt(twos[i]);
        }

        for (int i = 0; i < fours.length; i++) {
            powerOnTime[i] = Integer.parseInt(fours[i]);
        }
    }
}
