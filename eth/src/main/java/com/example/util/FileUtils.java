package com.example.util;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.config.AppConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

public class FileUtils {
    public String SDPATH;

    public String getSDPATH() {
        return SDPATH;
    }

    public void setSDPATH(String sDPATH) {
        SDPATH = sDPATH;
    }

    public FileUtils() {
        // 得到当前外部存储设备的目录
        // SDCARD
//        SDPATH = Environment.getExternalStorageDirectory() + "/";
    }

    // 在SD卡上创建文件
    public File creatSDfile(String fileName) throws IOException {
        File file = new File(SDPATH + fileName);
        file.createNewFile();
        return file;
    }

    // 在SD卡上创建目录
    public File creatSDDir(String dirName) {
        File dir = new File(SDPATH + dirName);
        dir.mkdir();
        return dir;
    }

    // 判断SD卡上的文件是否存在
    public boolean isFileExist(String fileName) {
        File file = new File(SDPATH + fileName);
        return file.exists();
    }

    // 把InputStream里的数据写入到SD卡中去
    public File writeToSDFromIuput(String path, String fileName,
                                   InputStream input) {
        File file = null;
        OutputStream output = null;
        try {
            creatSDDir(path);
            file = creatSDfile(path + fileName);
            output = new FileOutputStream(file);
            byte buffer[] = new byte[4 * 1024];
            while ((input.read(buffer)) != -1) {
                output.write(buffer);
            }
            output.flush();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (Exception e2) {
                // TODO: handle exception
                e2.printStackTrace();
            }
        }
        return file;
    }

    public void writeToSDFromStr(String path, String fileName, String str) {
        File file = null;
        FileOutputStream fos = null;
        try {
            file = new File(path, fileName);
            fos = new FileOutputStream(file);

//          fos.write(str.getBytes());
//          fos.write("\r\n".getBytes());
//          fos.write("I am lilu".getBytes());
//          fos.close();
            PrintWriter pw = new PrintWriter(fos, true);
            pw.println(str);
            pw.close();
            Log.i("TAG", "====保存成功====:");
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public void SaveToSD(String name, String content) throws Exception {
        //获取外部设备
        //Environment.getExternalStorageDirectory() 获取SD的路径
        File file = new File(Environment.getExternalStorageDirectory() + "/clent", name);
        FileOutputStream outStream = new FileOutputStream(file);
        //写入文件
        outStream.write(content.getBytes());
        outStream.close();
    }


    /**
     * 此方法为android程序写入sd文件文件，用到了android-annotation的支持库@*
     *
     * @param buffer   写入文件的内容
     * @param folder   保存文件的文件夹名称,如log；可为null，默认保存在sd卡根目录
     * @param fileName 文件名称，默认app_log.txt
     * @param append   是否追加写入，true为追加写入，false为重写文件
     * @param autoLine 针对追加模式，true为增加时换行，false为增加时不换行
     */
    public synchronized static void writeFileToSDCard(@NonNull final byte[] buffer, @Nullable final String folder,
                                                      @Nullable final String fileName, final boolean append, final boolean autoLine) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean sdCardExist = Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED);
                String folderPath = "";
                if (sdCardExist) {
                    //TextUtils为android自带的帮助类
                    if (TextUtils.isEmpty(folder)) {
                        //如果folder为空，则直接保存在sd卡的根目录
                        folderPath = Environment.getExternalStorageDirectory()
                                + File.separator;
                    } else {
                        folderPath = Environment.getExternalStorageDirectory()
                                + File.separator + folder + File.separator;
                    }
                } else {
                    return;
                }
                File fileDir = new File(folderPath);
                if (!fileDir.exists()) {
                    if (!fileDir.mkdirs()) {
                        return;
                    }
                }
                File file;
                //判断文件名是否为空
                if (TextUtils.isEmpty(fileName)) {
                    file = new File(folderPath + "app_log.txt");
                } else {
                    file = new File(folderPath + fileName);
                }
                RandomAccessFile raf = null;
                FileOutputStream out = null;
                try {
                    if (append) {
                        //如果为追加则在原来的基础上继续写文件
                        raf = new RandomAccessFile(file, "rw");
                        raf.seek(file.length());
                        raf.write(buffer);
                        if (autoLine) {
                            raf.write("\n".getBytes());
                        }
                    } else {
                        //重写文件，覆盖掉原来的数据
                        out = new FileOutputStream(file);
                        out.write(buffer);
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (raf != null) {
                            raf.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static byte[] strToByteArray(String str) {
        if (str == null) {
            return null;
        }
        byte[] byteArray = str.getBytes();
        return byteArray;
    }


    public static void method1(String filePath, String desc,boolean falg) {
        Log.i("wrote", "==========" + desc);
        FileWriter fw = null;
        try {
            // 如果文件存在，则追加内容；如果文件不存在，则创建文件
            File fileSave = new File(filePath);
            if (fileSave.exists()) {
                fileSave.delete();
            }
            fileSave.createNewFile();
            fw = new FileWriter(fileSave, true);
//            PrintWriter pw = new PrintWriter(fw);
            fw.write(desc);
            fw.close();
//            pw.println(desc);
//            pw.flush();
//            fw.flush();
//            pw.close();
//            fw.close();
            Log.i("wrote", "=====success=====");
        } catch (Exception e) {
            Log.i("wrote", "=====failed=====" + e.toString());
            e.printStackTrace();
        }
    }
    /**
     * 追加文件：使用FileWriter
     *
     * @param fileName
     * @param content
     */
    public static void method(String fileName, String content,boolean falg) {
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, falg);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void createFileIfNotExciet() {
        String filePath = AppConfig.SAVE_LOG_PATH;
        File fileSave = new File(filePath);
        if (!fileSave.exists()) {
            fileSave.mkdirs();
        }

    }

}