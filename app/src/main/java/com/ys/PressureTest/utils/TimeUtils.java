package com.ys.PressureTest.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2017/12/14.
 */

public class TimeUtils {

    public static String tsFormat(long ts, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date date = new Date();
        date.setTime(ts);
        String t1 = dateFormat.format(date);
        return t1;
    }

    public static long getTimeMills(int year, int month, int day, int hour, int minute,int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, minute,second);
        return calendar.getTimeInMillis();
    }


    public static String getCurrentTime(String format){
        return tsFormat(System.currentTimeMillis(), format);
    }
}
