package com.ys.PressureTest.video;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;

import com.ys.PressureTest.log.ThreadManager;
import com.ys.PressureTest.utils.FileUtils;

import java.io.IOException;

/**
 * Created by Administrator on 2017/12/9.
 */

public class MyMediaPlayer implements MediaPlayer.OnPreparedListener, SurfaceHolder.Callback {
    public static String DEFAULT_PLAY_FILE_NAME = "test.mp4";
    private MediaPlayer mMediaPlayer;
    private Context context;
    private String filename;
    private boolean isPrepere = false;
    private SurfaceHolder mSurfaceHolder;

    public MyMediaPlayer(Context context, SurfaceHolder holder, String assetsFileName) {
        this.context = context.getApplicationContext();
        filename = assetsFileName;
        holder.addCallback(this);
    }

    private void openAssetVedio() {
        release();
        mMediaPlayer = new MediaPlayer();
        AssetFileDescriptor afd = null;
        try {
            afd = context.getAssets().openFd(filename);
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setOnPreparedListener(this);
            if (Build.VERSION.SDK_INT >= 23)
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        openAssetVedio();

                    }
                });
            afd.close();
            afd = null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (afd != null)
                    afd.close();
                afd = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openVedio(String path) {
        try {
            release();
            Uri uri = Uri.parse(path);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(context, uri);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setOnPreparedListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 4.4播放视频文件
     */
    private void playFilesDir() {
        String path = context.getFilesDir() + "/" + filename;
        ThreadManager.getNormalPool().execute(new MediaCopyTask(context, filename, path, callback));
    }

    Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            openVedio(msg.obj.toString());
            return true;
        }
    };


    public void start() {
        if (isPrepere)
            mMediaPlayer.start();
    }

    public void stop() {
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.pause();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        isPrepere = true;
        mMediaPlayer.start();
    }

    public void setDisplay(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
    }

    public void setLooping(boolean loop) {
        mMediaPlayer.setLooping(loop);
    }

    public void release() {
        isPrepere = false;
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            openAssetVedio();
        } else {
            playFilesDir();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        release();
    }


    class MediaCopyTask implements Runnable {

        Context context;
        String srcFileName;
        String desFilePath;
        Handler handler;

        public MediaCopyTask(Context context, String srcFileName, String desFilePath, Handler.Callback callback) {
            this.context = context.getApplicationContext();
            this.srcFileName = srcFileName;
            this.desFilePath = desFilePath;
            handler = new Handler(callback);
        }

        @Override
        public void run() {
            FileUtils.CopyAssets(context, srcFileName, desFilePath);
            Message message = handler.obtainMessage();
            message.obj = desFilePath;
            handler.sendMessage(message);
        }
    }
}
