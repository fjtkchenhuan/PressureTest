package com.example.util;

import com.example.MyApplication;

public class SharedPreferenceutil {

    public static void setSelect(boolean falg){
        MyApplication.getInstance().saveData("falg",falg);
    }
    public static boolean getSelect(){
        return (boolean) MyApplication.getInstance().getData("falg",true);
    }

    public static void setGateway(String falg){
        MyApplication.getInstance().saveData("gateway",falg);
    }
    public static String getGateway(){
        return (String) MyApplication.getInstance().getData("gateway","");
    }
    public static void setFrequency(int size){
        MyApplication.getInstance().saveData("size",size);
    }
    public static int getFrequency(){
        return (int) MyApplication.getInstance().getData("size",0);
    }


    public static void setFileName(String fileName){
        MyApplication.getInstance().saveData("fileName",fileName);
    }
    public static String getFileName(){
        return (String) MyApplication.getInstance().getData("fileName","");
    }
    public static void setData(String data){
        MyApplication.getInstance().saveData("data",data);
    }
    public static String getData(){
        return (String) MyApplication.getInstance().getData("data","");
    }
    public static void setMac(String mac){
        MyApplication.getInstance().saveData("mac",mac);
    }
    public static String getMac(){
        return (String) MyApplication.getInstance().getData("mac","");
    }
}
