package com.example.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressLint({"SimpleDateFormat"})
public class SimpleDateUtil {
    public static String format(String paramString) {
        long l = Long.parseLong(paramString);
        return new SimpleDateFormat("yyyy/MM/dd/ HH:mm:ss").format(Long.valueOf(l));
    }

    public static long formatBig(long paramLong) {
        return Long.parseLong(new SimpleDateFormat("yyyyMMddHHmmss").format(Long.valueOf(paramLong)));
    }

    public static String formatLong(long paramLong) {
        String str = new SimpleDateFormat("yyyy:MM:dd/HH:mm/ss").format(Long.valueOf(paramLong));
        return str.substring(str.indexOf("/") + 1, str.lastIndexOf("/"));
    }

    public static String formatLongCurrent(long paramLong) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(paramLong)).substring(0, 10);
    }

    public static String formatLongData(long paramLong) {
        String str = new SimpleDateFormat("yyyyMMdd/HH:mm:ss").format(Long.valueOf(paramLong));
        return str.substring(0, str.indexOf("/"));
    }

    public static String formatLongTime(long paramLong) {
        String str = new SimpleDateFormat("yyyyMMdd/HH:mm:ss").format(Long.valueOf(paramLong));
        return str.substring(str.indexOf("/") + 1);
    }

    public static String formatMessage(long paramLong) {
        return new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss").format(Long.valueOf(paramLong));
    }

    public static String formatStringData(String paramString) {
        long l = Long.parseLong(paramString);
        paramString = new SimpleDateFormat("yyyyMMdd/HH:mm:ss").format(Long.valueOf(l));
        return paramString.substring(0, paramString.indexOf("/"));
    }

    public static String formatTime(long paramLong) {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(Long.valueOf(paramLong));
    }

    public static String formatTime(String paramString) {
        long l = Long.parseLong(paramString);
        paramString = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss").format(Long.valueOf(l));
        return paramString.substring(paramString.lastIndexOf("/") + 1, paramString.lastIndexOf(":"));
    }

    public static String getCurrentDateLogin() {
        long l = System.currentTimeMillis();
        return new SimpleDateFormat("yyyy-MM-dd").format(Long.valueOf(l));
    }

    public static String getCurrentDateTime() {
        long l = System.currentTimeMillis();
        String str = new SimpleDateFormat("yyyy/MM/dd/ HH:mm:ss").format(Long.valueOf(l));
        return str.substring(str.indexOf("/") + 1, str.lastIndexOf(":"));
    }

    public static String getDate() {
        long l = System.currentTimeMillis();
        String str = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss").format(Long.valueOf(l));
        return str;
    }

    public static int getHour() {
        long l = System.currentTimeMillis();
        return Integer.valueOf(new SimpleDateFormat("HH").format(Long.valueOf(l))).intValue();
    }

    public static int getMim() {
        long l = System.currentTimeMillis();
        return Integer.valueOf(new SimpleDateFormat("mm").format(Long.valueOf(l))).intValue();
    }

    public static String getTime() {
        long l = System.currentTimeMillis();
        String str = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss").format(Long.valueOf(l));
        return str;
    }

    public static String getWeek() {
        Date localDate = new Date();
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.setTime(localDate);
        int j = localCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        int i = j;
        if (j < 0) {
            i = 0;
        }
        return new String[]{"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"}[i];
    }
}
