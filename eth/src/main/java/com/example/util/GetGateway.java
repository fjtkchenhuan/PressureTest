package com.example.util;

import java.lang.reflect.Method;

public class GetGateway {
    String nullIpInfo;
    public static String getValueFromProp(String key) {
        String value = "";
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", new Class<?>[]{String.class});
            value = (String) getMethod.invoke(classType, new Object[]{key});
        } catch (Exception e) {
        }
        return value;
    }


    public String getIPAddress() {
        String tempIpInfo;
        String iface = "eth0";
        tempIpInfo = getValueFromProp("dhcp." + iface + ".ipaddress");
        if ((tempIpInfo != null) && (!tempIpInfo.equals(""))) {
        } else {
            tempIpInfo = nullIpInfo;
        }
        return tempIpInfo;
    }

    public String getMask() {
        String tempIpInfo;
        String iface = "eth0";
        tempIpInfo = getValueFromProp("dhcp." + iface + ".mask");
        if ((tempIpInfo != null) && (!tempIpInfo.equals(""))) {
        } else {
            tempIpInfo = nullIpInfo;
        }
        return tempIpInfo;
    }

    public String getGateway() {
        String tempIpInfo;
        String iface = "eth0";
        tempIpInfo = getValueFromProp("dhcp." + iface + ".gateway");
        if ((tempIpInfo != null) && (!tempIpInfo.equals(""))) {
        } else {
            tempIpInfo = nullIpInfo;
        }
        return tempIpInfo;
    }

    public String getDns1() {
        String tempIpInfo;
        String iface = "eth0";
        tempIpInfo = getValueFromProp("dhcp." + iface + ".dns1");
        if ((tempIpInfo != null) && (!tempIpInfo.equals(""))) {
        } else {
            tempIpInfo = nullIpInfo;
        }
        return tempIpInfo;
    }

    public String getDns2() {
        String tempIpInfo;
        String iface = "eth0";
        tempIpInfo = getValueFromProp("dhcp." + iface + ".dns2");
        if ((tempIpInfo != null) && (!tempIpInfo.equals(""))) {
        } else {
            tempIpInfo = nullIpInfo;
        }
        return tempIpInfo;
    }

}
