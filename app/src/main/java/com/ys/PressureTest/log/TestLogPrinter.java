package com.ys.PressureTest.log;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.ys.PressureTest.Constant;
import com.ys.PressureTest.utils.FileUtils;
import com.ys.PressureTest.utils.ModelUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/14.
 */

public class TestLogPrinter {
    private LinkedHashMap<String, String> testInfoMap;
    private static TestLogPrinter instance;
    private Context mContext;
    private SQLiteDao sqLiteDao;

    public static TestLogPrinter getInstance(Context context) {
        if (instance == null) {
            synchronized (TestLogPrinter.class) {
                if (instance == null) {
                    instance = new TestLogPrinter(context);
                }
            }
        }
        return instance;
    }

    private TestLogPrinter(Context context) {
        this.mContext = context;
        sqLiteDao = SQLiteDao.getInstance(context);
        testInfoMap = new LinkedHashMap<String, String>();
        testInfoMap.put("日期：", tsFormat(System.currentTimeMillis(), "yyyy年MM月dd日\n"));
        testInfoMap.put("芯片型号：", ModelUtils.getRKModel());
        testInfoMap.put("DDR：", ModelUtils.getRamMemory());
        testInfoMap.put("EMMC：", ModelUtils.getRealSizeOfNand());
        testInfoMap.put("固件版本号：", ModelUtils.getSystemModelInfo() + "\n");
    }

    public synchronized void functionlLog() {
        StringBuffer buffer = new StringBuffer("UTF-8");
        Iterator<Map.Entry<String, String>> iterator = testInfoMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            buffer.append(next.getKey());
            buffer.append(next.getValue());
            buffer.append("\n");
        }
//        ThreadManager.getNormalPool().execute(new LogTask(buffer.toString()));
        sqLiteDao.updateOrder(Constant.DAO_BASIC_INFO,buffer.toString());
    }

    public void agingLog(String content,long startTs, long endTs, String sort, int dao) {
        ThreadManager.getNormalPool().execute(new AgingTask(content,startTs, endTs,sort,dao));
    }

    class AgingTask extends Thread {
        long startTs;
        long endTs;
        String systemInfo;
        String sort;
        int dao;

        public AgingTask(String content,long startTs, long endTs,String sort,int dao) {
            this.startTs = startTs;
            this.endTs = endTs;
            this.systemInfo = content;
            this.sort = sort;
        }

        @Override
        public void run() {
            super.run();
            String startTime = tsFormat(startTs, "yyyy-MM-dd HH:mm");
            String endTime = tsFormat(endTs, "yyyy-MM-dd HH:mm");
            String time = "";
            long ts = endTs - startTs;
            int minute = (int) (ts / (60 * 1000));
            int hour = 0;
            if (minute >= 60) {
                hour = minute / 60;
                minute = minute % 60;
                int day = 0;
                if (hour >= 24) {
                    day = hour / 24;
                    hour = hour % 24;
                    time = day + "天" + hour + "小时" + minute + "分钟";
                } else {
                    time = hour + "小时" + minute + "分钟";
                }
            } else {
                time = minute + "分钟";
            }

            StringBuffer buffer = new StringBuffer();
            buffer.append(systemInfo + "\n");
            buffer.append(sort + ": " + time + " ");
            buffer.append("开始时间：" + startTime + " ");
            buffer.append("结束时间：" + endTime + "\n\n"
                    +"---------------------------------------------------------------------------------------------------");
            SQLiteDao.getInstance(mContext).updateOrder(dao,buffer.toString());
        }
    }

    private static String tsFormat(long ts, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date date = new Date();
        date.setTime(ts);
        String t1 = dateFormat.format(date);
        return t1;
    }
}
