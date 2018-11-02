package com.ys.PressureTest.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.ys.PressureTest.log.LogContent;
import com.ys.PressureTest.log.SQLiteDao;
import com.ys.PressureTest.product.Rk3328;
import com.ys.PressureTest.product.Rk3399;
import com.ys.PressureTest.product.RkFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * adb shell am start -n com.yishengkj.testtools/MainActivity
 * Created by Administrator on 2017/12/14.
 */

public class FileUtils {

    private static final String TAG = "FileUtils";

    public static void CopyAssets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            File file = new File(newPath);
            if (fileNames.length > 0) {// 如果是目录
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    CopyAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {// 如果是文件
                if (file.exists()) return;
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveToSDCard(String fileName, String content) {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)){
            Log.e("saveToSDCard","没有权限");
            return;
        }
        File file = new File(Environment.getExternalStorageDirectory(), fileName);
        if (file.exists()) {
            file.delete();
            file = new File(Environment.getExternalStorageDirectory(), fileName);
        }
        writeFile(file, content);
    }

    /**
     * 这里定义的是一个文件保存的方法，写入到文件中，所以是输出流
     **/
    public static void save(String filename, String filecontent) {
        FileOutputStream   output = null;
        try {
            output = new FileOutputStream(filename,true);
            output.write(filecontent.getBytes());  //将String字符串以字节流的形式写入到输出流中
            output.close();         //关闭输出流
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 这里定义的是文件读取的方法
     */
    public static String read(String filename)  {
        //打开文件输入流
        FileInputStream input = null;
        try {
            input = new FileInputStream(filename);
            byte[] temp = new byte[1024];
            StringBuilder sb = new StringBuilder("");
            int len = 0;
            //读取文件内容:
            while ((len = input.read(temp)) > 0) {
                sb.append(new String(temp, 0, len));
            }
            input.close();
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void writeFile(File file, String content) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(content.getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String readFile(File file) {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String readline = "";
            while ((readline = br.readLine()) != null) {
                sb.append(readline);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    /**
     * 获取该路径下第一个文件名称
     *
     * @param extSDPath
     * @return
     */
    public static String getFirstFileNameWithPath(String extSDPath) {
        String fileName = extSDPath;
        if (extSDPath != null) {
            File file = new File(extSDPath);
            if (file.exists()) {
                fileName = getFileName(file);
            }
        }
        return fileName;
    }

    public static String getFileName(File dir) {
        File[] fileArray = dir.listFiles();
        String filname = dir.getAbsolutePath();
        if (fileArray == null) return filname;
        for (File f : fileArray) {
            if (f.isFile()) {
                filname = f.getAbsolutePath();
                break;
            } else {
                getFileName(f);
            }
        }
        return filname;
    }

    public static List<String> getTxtList(String filePath, Context context) {
        List newList = new ArrayList<String>();
        try {
            File file = new File(filePath);
            int count = 0;//初始化 key值
            if (file.isFile() && file.exists()) {//文件存在
                InputStreamReader isr = new InputStreamReader(new FileInputStream(file),"utf-8");
                BufferedReader br = new BufferedReader(isr);

                String lineTxt = null;
                while ((lineTxt = br.readLine()) != null) {
                    if (!"".equals(lineTxt)) {
                        String reds = lineTxt.split("\\+")[0];  //java 正则表达式
                        newList.add(count, reds);
                        count++;
                    }
                }
                isr.close();
                br.close();
            }else {
                ToastUtils.showShortToast(context,"can not find file");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newList;
    }

    public static void saveDaoToSD(Context context) {
        List<LogContent> logContents = SQLiteDao.getInstance(context).getAllDate();

        if (logContents == null)
            saveToSDCard("YSStressTest.txt","记录全部清空");
        else {
            StringBuilder stringBuffer = new StringBuilder();
            for (LogContent logContent : logContents) {
                stringBuffer.append(logContent.content + "\n");
                saveToSDCard("YSStressTest.txt",stringBuffer.toString());
            }
        }
    }

    public static void getLogs(final String path) {
        File file = new File(path);
        file.setExecutable(true);
        file.setReadable(true);//设置可读权限
        file.setWritable(true);//设置可写权限
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file != null && file.exists()) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    if ((RkFactory.getRK() instanceof Rk3328) || (RkFactory.getRK() instanceof Rk3399))
                        CommandUtils.exect("logcat -v time  > " + path);
                    else
                        CommandUtils.do_exec("logcat -v time  > " + path);
                }
            });
            t.start();
        }
    }

    public static void stopLog() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                CommandUtils.do_exec("busybox1.11 killall logcat");
            }
        });
        t.start();
    }

    public static void getKmsgLog(String path) {
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        Process process = null;
        String lineText = null;
        List<String> txtLists = new ArrayList<>();
        try {
            process = Runtime.getRuntime().exec("dmesg");
            reader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(reader);
            while ((lineText = bufferedReader.readLine()) != null) {
                txtLists.add(lineText);
            }
            StringBuffer buffer = new StringBuffer();
            for (String s : txtLists) {
                buffer.append(s + "\n");
            }
            saveToSDCard(path,buffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}
