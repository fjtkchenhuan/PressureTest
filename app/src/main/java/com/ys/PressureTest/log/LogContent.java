package com.ys.PressureTest.log;

/**
 * Created by Administrator on 2018/7/3.
 */

public class LogContent {
    public int id;
    public String content;
    public String name;

    public LogContent (){}
    public LogContent (int id ,String name,String content) {
        this.id = id;
        this.content = content;
        this.name = name;
    }
}
