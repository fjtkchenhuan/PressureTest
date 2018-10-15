package com.ys.PressureTest;

/**
 * Created by Administrator on 2018/7/3.
 */

public class Constant {

    public static final int DAO_BASIC_INFO = 0;
    public static final int DAO_REBOOT = 1;
    public static final int DAO_NEXT_POWER_ON_OFF_TIME = 2;
    public static final int DAO_LAST_POWER_ON_OFF_TIME = 3;
    public static final int DAO_POWERONOFF_COUNTS = 4;
    public static final int DAO_AUTO_SYNC_NET_TIME = 5;
    public static final int DAO_AUTO_SYNC_NET_TIME_COUNTS = 6;
    public static final int DAO_VIDEO_TEST = 7;
    public static final int DAO_ETH_REBOOT = 8;
    public static final int DAO_ETH_NOT_REBOOT = 9;
    public static final int DAO_WIFI_REBOOT = 10;
    public static final int DAO_WIFI_NOT_REBOOT = 11;
    public static final int DAO_MOB_REBOOT = 12;
    public static final int DAO_MOB_NOT_REBOOT = 13;
    public static final int DAO_MOB_LONG_TIME = 14;

    public static final String SP_REBOOT = "Reboot";
    public static final String SP_REBOOT_DATA = "rebootData";

    //保存最近一次的定时开关机数据
    public static final String SP_POWER_ON_OFF = "PowerOnOffDatas";
    public static final String SP_POWER_DATA = "powerOn";
    public static final String SP_LAST_POWERONOFF_TIME = "lastTime";
    public static final String SP_POWERONOFF_COUNTS = "powerOnOffCounts";
    public static final String SP_POWERONOFF_MODE = "powerOnOffMode";
    public static final String SP_POWER_OFF_TIME = "powerOffTime";
    public static final String SP_POWER_ON_TIME = "powerOnTime";

    //测试自动确定网络时间是否打开
    public static final String SP_AUTO_SYNC_TIME = "AutoSyncNetTime";
    public static final String SP_BOOLEAN_SYNC_TIME = "IsSyncTime";
    public static final String SP_SYNC_TIME_LOGS = "SyncNetTimeLogs";
    public static final String SP_COUNT_SYNC_TIME = "SyncNetTimeCounts";

    public static final String LAST_POWER_ON_TIME = "last_powerOnTime";
    public static final String LAST_POWER_OFF_TIME = "last_powerOffTime";

    public static final String SP_NET_TEST = "net_test";
    public static final String SP_ETHERNET_REBOOT_COUNT = "ethernet_reboot_count";
    public static final String SP_ETHERNET_REBOOT_CONTENT = "ethernet_reboot_content";
    public static final String SP_ETHERNET_REBOOT_MODE = "ethernet_is_reboot_mode";
    public static final String SP_ETHERNET_NOT_REBOOT_COUNT = "ethernet_not_reboot_count";
    public static final String SP_ETHERNET_NOT_REBOOT_CONTENT = "ethernet_not_reboot_content";
    public static final String SP_WIFI_REBOOT_COUNT = "wifi_reboot_count";
    public static final String SP_WIFI_REBOOT_CONTENT = "wifi_reboot_content";
    public static final String SP_WIFI_REBOOT_MODE = "wifi_is_reboot_mode";
    public static final String SP_WIFI_NOT_REBOOT_COUNT = "wifi_not_reboot_count";
    public static final String SP_WIFI_NOT_REBOOT_CONTENT = "wifi_not_reboot_content";
    public static final String SP_MOBILE_REBOOT_COUNT = "mobile_reboot_count";
    public static final String SP_MOBILE_REBOOT_CONTENT = "mobile_reboot_content";
    public static final String SP_MOBILE_REBOOT_MODE = "mobile_is_reboot_mode";
    public static final String SP_MOBILE_NOT_REBOOT_COUNT = "mobile_not_reboot_count";
    public static final String SP_MOBILE_NOT_REBOOT_CONTENT = "mobile_not_reboot_content";

}
