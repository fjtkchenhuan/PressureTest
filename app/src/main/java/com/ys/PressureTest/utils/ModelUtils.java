package com.ys.PressureTest.utils;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by RYX on 2017/8/30.
 */

public class ModelUtils {
    public static final String TAG = "Veriosn";

    public final static String SYSTEM_VERSION_INFO = "ro.build.description";

    /**
     * 获取固件版本型号信息
     *
     * @return
     */
    public static String getSystemModelInfo() {
        return CommandUtils.getValueFromProp(SYSTEM_VERSION_INFO);
    }

    /**
     * 获取瑞芯微芯片型号
     *
     * @return
     */
    public static String getRKModel() {
        String intern = Build.PRODUCT.intern();
        if (intern.contains("312x"))
            intern = "rk3128";
        return intern;
    }

    /**
     * 获取固件版本号
     *
     * @return
     */
    public static String getFirmwareVersion() {
        String bv = Build.VERSION.INCREMENTAL;
        String reg = "[1-9]\\d{3}(((0[13578]|1[02])([0-2]\\d|3[01]))|((0[469]|11)([0-2]\\d|30))|(02([01]\\d|2[0-8])))";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(bv);
        while (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    /**
     * .获取CPU型号
     *
     * @return
     */
    public static String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取CPU核心数
     *
     * @return
     */
    public static int getNumCores() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            Log.d(TAG, "CPU Count: " + files.length);
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Print exception
            Log.d(TAG, "CPU Count: Failed.");
            e.printStackTrace();
            //Default to return 1 core
            return 1;
        }
    }

    /**
     * 获取CPU最大频率
     *
     * @return
     */
    public static String getMinCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }

    /**
     * RAM内存大小
     *
     * @return
     */
    public static String getRamMemory() {
        String path = "/proc/meminfo";// 系统内存信息文件
        String memorySize = "";
        try {
            FileReader localFileReader = new FileReader(path);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader);
            String readLine = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
            // 对读取到的字符串进行ASCII值的匹配，获取到需要的内存空间的大小
            char[] charArray = readLine.toCharArray();
            // 通过StingBuffer将可用空间的 总大小串起来
            StringBuffer sb = new StringBuffer();
            for (char c : charArray) {
                if (c >= '0' && c <= '9') {
                    sb.append(c);
                }
            }
            long kb = Long.parseLong(sb.toString());
            long mb = kb / 1024;
            long gb = mb / 1024;
            if (mb % 1024 > 0) {
                gb += 1;
            }
            memorySize = gb + "G";
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return memorySize;
    }

    /**
     * 得到内置存储空间的总容量
     *
     * @return
     */
    public static String getRealSizeOfNand() {
        String path = Environment.getExternalStorageDirectory().getPath();
        String size = "0G";
        if (readBlockSize(path, 0) / (1024 * 1024) < 3) {
            size = "4G";
        } else if (readBlockSize(path, 0) / (1024 * 1024) >= 3 && readBlockSize(path, 0) / (1024 * 1024) < 7) {
            size = "8G";
        } else if (readBlockSize(path, 0) / (1024 * 1024) >= 7 && readBlockSize(path, 0) / (1024 * 1024) < 15) {
            size = "16G";
        } else if (readBlockSize(path, 0) / (1024 * 1024) >= 15 && readBlockSize(path, 0) / (1024 * 1024) < 31) {
            size = "32G";
        } else if (readBlockSize(path, 0) / (1024 * 1024) >= 31 && readBlockSize(path, 0) / (1024 * 1024) < 63) {
            size = "64G";
        } else if (readBlockSize(path, 0) / (1024 * 1024) >= 63 && readBlockSize(path, 0) / (1024 * 1024) < 127) {
            size = "128G";
        } else {
            size = "8G";
        }

        return size;
    }

    // 获取存储空间相关
    private static long readBlockSize(String path, int flag) {
        StatFs sf = new StatFs(path);
        long blockSize = sf.getBlockSize();
        long blockCount = sf.getBlockCount();
        long availCount = sf.getAvailableBlocks();
        if (flag == 0) { // sum
            return blockSize * blockCount / 1024;
        } else if (flag == 1) { // avail
            return blockSize * availCount / 1024;
        } else {
            return (blockSize * blockCount / 1024) - (blockSize * availCount / 1024);
        }
    }
}
