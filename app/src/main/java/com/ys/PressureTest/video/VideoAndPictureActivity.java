package com.ys.PressureTest.video;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceView;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ys.PressureTest.Constant;
import com.ys.PressureTest.R;
import com.ys.PressureTest.log.TestLogPrinter;
import com.ys.PressureTest.utils.CpuManager;
import com.ys.PressureTest.utils.LedController;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class VideoAndPictureActivity extends AppCompatActivity {

    MyMediaPlayer mMediaPlayer;
    SurfaceView surfaceView;
    AutoViewPager mAutoViewPager;
    TextView agingSysInfo;
    TextView agingSysInfo1;
    Chronometer chronometer;
    LedController controller;
    static final int FLAG_SYSTEM_READ = 1001;
    static final int FLAG_AGING_LOG = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        setVideo();
        setPicture();
        setSysInfo();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        chronometer.stop();
        chronometer = null;
        catTimer.cancel();
        catTimer = null;
        logTimer.cancel();
        logTimer = null;
        controller.release();
    }

    long startTs;

    @Override
    protected void onResume() {
        super.onResume();
        mMediaPlayer.start();
        startTs = System.currentTimeMillis();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mMediaPlayer.stop();
    }

    private void setSysInfo() {
        agingSysInfo = findViewById(R.id.aging_system_info);
        chronometer = findViewById(R.id.time_chronometer);
        agingSysInfo1 = findViewById(R.id.aging_system_info1);
        catTimer = new Timer();
        logTimer = new Timer();
        setAgingTime();
        logTimer.schedule(agingLogTask, 0, TimeUnit.MINUTES.toMillis(5));
        controller = new LedController();
        controller.start();
        chronometer.start();
    }

    int refreshRate;
    Timer catTimer, logTimer;

    private void setAgingTime() {
        Display display = getWindowManager().getDefaultDisplay();
        refreshRate = (int) display.getRefreshRate();
        StringBuffer buffer = new StringBuffer();
        buffer.append("FPS：" + refreshRate);
        buffer.append("\nDDR使用率：");
        buffer.append("\nCPU使用率：");
        agingSysInfo.setText(buffer.toString());
        catTimer.schedule(new CatTask(), 0, 3 * 1000);
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == FLAG_SYSTEM_READ) {
                agingSysInfo.setText(msg.obj.toString());
            }
            return true;
        }
    });


    TimerTask agingLogTask = new TimerTask() {
        @Override
        public void run() {
            TestLogPrinter.getInstance(VideoAndPictureActivity.this).agingLog("",startTs, System.currentTimeMillis(),"视频图片轮播时长", Constant.DAO_VIDEO_TEST);
            handler.sendEmptyMessage(FLAG_AGING_LOG);
        }
    };


    class CatTask extends TimerTask {

        @Override
        public void run() {
            if (handler == null) return;
            StringBuffer buffer = new StringBuffer();
            buffer.append("FPS：" + refreshRate);
            buffer.append("\nDDR使用率：" + CpuManager.getMemoryUsageRate());
            buffer.append("\nCPU使用率：" + CpuManager.getCPURateDesc());
            Message message = handler.obtainMessage();
            message.what = FLAG_SYSTEM_READ;
            message.obj = buffer;
            handler.sendMessage(message);
        }
    }

    private void setPicture() {
        mAutoViewPager = findViewById(R.id.viewdio_viewpager);
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(R.mipmap.img1);
        integers.add(R.mipmap.img2);
        integers.add(R.mipmap.img3);
        integers.add(R.mipmap.img4);
        integers.add(R.mipmap.img5);
        mAutoViewPager.setAdapter(new BaseViewPagerAdapter<Integer>(this, integers) {
            @Override
            public void loadImage(ImageView view, int position, Integer integer) {
                Glide.with(VideoAndPictureActivity.this).load(integer).into(view);
            }
        });
        mAutoViewPager.start();
    }

    private void setVideo() {
        surfaceView = findViewById(R.id.video_aging_suface);
        mMediaPlayer = new MyMediaPlayer(this, surfaceView.getHolder(), MyMediaPlayer.DEFAULT_PLAY_FILE_NAME);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (controller != null) {
            controller.reset();
        }
    }
}
