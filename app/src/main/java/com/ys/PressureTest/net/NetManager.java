package com.ys.PressureTest.net;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.ys.PressureTest.R;

import java.util.List;

/**
 * Created by Administrator on 2018/7/24.
 */

public class NetManager {

    private WifiManager wifiManager;
    private Context context;
    private static NetManager networkManager;
    private List<WifiConfiguration> mConfigList;

    public NetManager(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public static NetManager getIntance(Context context) {
        if (networkManager == null) {
            networkManager = new NetManager(context);
        }
        return networkManager;
    }

     boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

     void openWifi() {
        if (wifiManager != null && !wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
    }

    void closeWifi() {
        if (wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(false);
    }

     WifiInfo getConnectionInfo() {
        return wifiManager.getConnectionInfo();
    }

    public void getConfiguration() {
        mConfigList = wifiManager.getConfiguredNetworks();// 得到配置好的网络信息
    }

     boolean connectWifi(int wifiId) {
        for (int i = 0; i < mConfigList.size(); i++) {
            WifiConfiguration wifi = mConfigList.get(i);
            if (wifi.networkId == wifiId) {
                while (!(wifiManager.enableNetwork(wifiId, true))) {// 激活该Id，建立连接
                    System.out.println("ConnectWifi>>:"
                            + String.valueOf(mConfigList.get(wifiId).status));// status:0--已经连接，1--不可连接，2--可以连接
                }
                wifiManager.saveConfiguration();
                return true;
            }
        }
        return false;
    }

     void startScan() {
        wifiManager.startScan();
    }

     List<ScanResult> getScanResults() {
        return wifiManager.getScanResults();
    }

     int addWifiConfig(List<ScanResult> wifiList, String ssid,
                                    String pwd, String capabilities) {
        int wifiId = -1;
        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult wifi = wifiList.get(i);
            if (wifi.SSID.equals(ssid)) {
                WifiConfiguration wifiCong = new WifiConfiguration();
                wifiCong.SSID = "\"" + wifi.SSID + "\"";// \"转义字符，代表"
                if (TextUtils.isEmpty(pwd)) {
                    wifiCong.allowedKeyManagement
                            .set(WifiConfiguration.KeyMgmt.NONE);
                } else {
                    wifiCong.preSharedKey = "\"" + pwd + "\"";// WPA-PSK密码
                }
                wifiCong.hiddenSSID = false;
                wifiCong.status = WifiConfiguration.Status.ENABLED;
                wifiId = wifiManager.addNetwork(wifiCong);// 将配置好的特定WIFI密码信息添加,添加完成后默认是不激活状态，成功返回ID，否则为-1
                if (wifiId != -1) {
                    return wifiId;
                }
            }
        }
        return wifiId;
    }

    boolean judgeConnected(ScanResult result) {
        WifiInfo info = getConnectionInfo();
        if (TextUtils.isEmpty(info.getBSSID())
                || TextUtils.isEmpty(result.BSSID)) {
            return false;
        }

        if (isEquals(info.getBSSID(), result.BSSID)
                && isEquals(info.getSSID().replaceAll("\"", ""),
                result.SSID)) {
            return true;
        }
        return false;
    }

    private boolean isEquals(String actual, String expected) {
        return actual == expected || (actual == null ? expected == null : actual.equals(expected));
    }

    private String getWifiStatus(Context context, String str) {
        String temp = "";
        if (str.contains("WPA") && str.contains("WPA2")) {
            temp =context.getString(R.string.st_Through_the_WPA_WPA2_protection);
        } else if (!str.contains("WPA") && str.contains("WPA2")) {
            temp = context.getString(R.string.st_Through_the_connected_protection_wpa2);
        } else if (str.contains("WPA") && !str.contains("WPA2")) {
            temp = context.getString(R.string.st_Through_the_connected_protection_wpa);
        } else if (str.contains("WEP")) {
            temp = context.getString(R.string.st_Through_the_connected_protection_wep);
        } else if (str.equals("[ESS]") || str.equals("[WPS][ESS]")) {
            temp = context.getString(R.string.st_open);
        }
        return temp;
    }

}
