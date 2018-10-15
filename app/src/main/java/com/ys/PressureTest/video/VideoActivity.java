package com.ys.PressureTest.video;

import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.Display;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.VideoView;

import com.ys.PressureTest.Constant;
import com.ys.PressureTest.PermissionActivity;
import com.ys.PressureTest.R;
import com.ys.PressureTest.log.TestLogPrinter;
import com.ys.PressureTest.utils.CpuManager;
import com.ys.PressureTest.utils.LedController;
import com.ys.PressureTest.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class VideoActivity extends PermissionActivity {
    private VideoView videoView;
    private List<String> videoUrls;
    private int index = 0;

    TextView agingSysInfo;
    TextView agingSysInfo1;
    Chronometer chronometer;
    LedController controller;
    int refreshRate;
    Timer catTimer, logTimer;
    long startTs;
    static final int FLAG_SYSTEM_READ = 1001;
    static final int FLAG_AGING_LOG = 1002;
    private StringBuffer buffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video2);

        setSysInfo();
        setVideoInfo();
    }

    private void initFile() {
        String file = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Movies";
        File[] files = new File(file).listFiles();
        videoUrls = new ArrayList<>();

        if (files != null && files.length > 0) {
            for (File file1 : files) {
                String filePath = file1.getAbsolutePath();
                if (isVideo(filePath)) {
                    videoUrls.add(filePath);
                }
            }
        }

        if (videoUrls.size() < 1) {
            ToastUtils.showShortToast(this,"请在内置存储Movies目录下放置视频");
            finish();
        }

    }

    private boolean isVideo(String path) {
        return path.endsWith(".mp4") || path.endsWith(".avi") || path.endsWith(".rmb") || path.endsWith(".rmvb") || path.endsWith(".flv") || path.endsWith(".mkv")
                || path.endsWith(".MP4") || path.endsWith(".AVI") || path.endsWith(".RMB") || path.endsWith(".RMVB") || path.endsWith(".FLV") || path.endsWith(".MKV")
                || path.endsWith(".mov") || path.endsWith(".ts") || path.endsWith(".mpg") || path.endsWith(".vob") || path.endsWith(".wmv") || path.endsWith(".tp")
                || path.endsWith(".MOV") || path.endsWith(".TS") || path.endsWith(".MPG") || path.endsWith(".VOB") || path.endsWith(".WMV") || path.endsWith(".TP")
                || path.endsWith(".m2ts") || path.endsWith(".M2TS") || path.endsWith(".ASF") || path.endsWith(".asf");
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTs = System.currentTimeMillis();
    }

    private void setVideoInfo() {
        initFile();
        videoView = findViewById(R.id.videoView);
        if (videoUrls.size() > 0) {
            videoView.setVideoPath(videoUrls.get(index));
            videoView.start();
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (videoUrls.size() == 1) {
                        videoView.setVideoPath(videoUrls.get(0));
                        videoView.start();
                    } else {
                        index ++;
                        videoView.setVideoPath(videoUrls.get(index));
                        videoView.start();
                        if (index == (videoUrls.size() - 1))
                            index = -1;
                    }

                }
            });
        }
    }

    private void setSysInfo() {
        agingSysInfo = findViewById(R.id.aging_system_info);
        chronometer = findViewById(R.id.time_chronometer);
        agingSysInfo1 = findViewById(R.id.aging_system_info1);
        catTimer = new Timer();
        logTimer = new Timer();
        controller = new LedController();
        setAgingTime();
        logTimer.schedule(agingLogTask, 0, TimeUnit.MINUTES.toMillis(6));
        chronometer.start();
        controller.start();
    }

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

    TimerTask agingLogTask = new TimerTask() {
        @Override
        public void run() {
            if (buffer != null) {
                TestLogPrinter.getInstance(VideoActivity.this).agingLog(buffer.toString(),startTs, System.currentTimeMillis(),"视频轮播时长", Constant.DAO_VIDEO_TEST);
                handler.sendEmptyMessage(FLAG_AGING_LOG);
            }
        }
    };

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == FLAG_SYSTEM_READ) {
                agingSysInfo.setText(msg.obj.toString());
            }
            return true;
        }
    });

    class CatTask extends TimerTask {

        @Override
        public void run() {
            if (handler == null) return;
            buffer = new StringBuffer();
            buffer.append("FPS：" + refreshRate);
            buffer.append("\nDDR使用率：" + CpuManager.getMemoryUsageRate());
            buffer.append("\nCPU使用率：" + CpuManager.getCPURateDesc());
            Message message = handler.obtainMessage();
            message.what = FLAG_SYSTEM_READ;
            message.obj = buffer;
            handler.sendMessage(message);
        }
    }

    @Override
    protected void onDestroy() {
        videoView.stopPlayback();
        chronometer.stop();
        chronometer = null;
        catTimer.cancel();
        catTimer = null;
        logTimer.cancel();
        logTimer = null;
        controller.release();
        TestLogPrinter.getInstance(VideoActivity.this).agingLog(buffer.toString(),startTs, System.currentTimeMillis(),"视频轮播时长",Constant.DAO_VIDEO_TEST);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (controller != null) {
            controller.reset();
        }
    }

}
