package com.ys.PressureTest.log;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ys.PressureTest.BaseUsedAdapter;
import com.ys.PressureTest.Constant;
import com.ys.PressureTest.R;
import com.ys.PressureTest.utils.ToastUtils;

import java.util.LinkedHashSet;

public class DeleteLogsActivity extends AppCompatActivity {
    private SQLiteDao sqLiteDao;
    int[] names = new int[]{R.string.last_power_on_off_time_logs, R.string.reboot_log, R.string.auto_sync_net_time,
            R.string.video_test_log,R.string.etherent_reboot_test_log,R.string.wifi_reboot_test_log,
            R.string.mobile_test_log,R.string.delete_all};
    SharedPreferences SPPowerOnOff;
    SharedPreferences SPSyncNetTime;
    SharedPreferences SPNetTest;
    private BaseUsedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_logs);
        sqLiteDao = SQLiteDao.getInstance(this);

        SPPowerOnOff = getSharedPreferences(Constant.SP_POWER_ON_OFF,0);
        SPSyncNetTime = getSharedPreferences(Constant.SP_AUTO_SYNC_TIME,0);
        SPNetTest = getSharedPreferences(Constant.SP_NET_TEST,0);

        ListView listView = findViewById(R.id.delete_log_list);
        adapter = new BaseUsedAdapter(names);
        listView.setAdapter(adapter);
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                adapter.selected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.selected(position);
                switch (position) {
                    case 0:
                        clearPowerOnOffLog();
                        break;
                    case 1:
                        clearRebootLog();
                        break;
                    case 2:
                        clearSyncNetTime();
                        break;
                    case 3:
                        clearVideoTestLog();
                        break;
                    case 4:
                        clearEthTestLog();
                        break;
                    case 5:
                        clearWifiLog();
                        break;
                    case 6:
                        clearMobileLog();
                        break;
                    case 7:
                       clearAllLog();
                        break;
                        default:
                            break;
                }
            }
        });
    }

    private void clearAllLog() {
        sqLiteDao.deleteAllOrder();
        SPSyncNetTime.edit().putInt(Constant.SP_COUNT_SYNC_TIME,0).apply();
        SPSyncNetTime.edit().putString(Constant.SP_SYNC_TIME_LOGS,"").apply();
        SPPowerOnOff.edit().putInt(Constant.SP_POWERONOFF_COUNTS,0).apply();
        SPPowerOnOff.edit().putString(Constant.SP_LAST_POWERONOFF_TIME,"").apply();
        SPNetTest.edit().putString(Constant.SP_ETHERNET_REBOOT_CONTENT,"").apply();
        SPNetTest.edit().putInt(Constant.SP_ETHERNET_REBOOT_COUNT,0).apply();
        SPNetTest.edit().putString(Constant.SP_ETHERNET_NOT_REBOOT_CONTENT,"").apply();
        SPNetTest.edit().putInt(Constant.SP_ETHERNET_NOT_REBOOT_COUNT,0).apply();
        SPNetTest.edit().putString(Constant.SP_WIFI_REBOOT_CONTENT,"").apply();
        SPNetTest.edit().putInt(Constant.SP_WIFI_REBOOT_COUNT,0).apply();
        SPNetTest.edit().putString(Constant.SP_WIFI_NOT_REBOOT_CONTENT,"").apply();
        SPNetTest.edit().putInt(Constant.SP_WIFI_NOT_REBOOT_COUNT,0).apply();
        SPNetTest.edit().putString(Constant.SP_MOBILE_REBOOT_CONTENT,"").apply();
        SPNetTest.edit().putInt(Constant.SP_MOBILE_REBOOT_COUNT,0).apply();
        SPNetTest.edit().putString(Constant.SP_MOBILE_NOT_REBOOT_CONTENT,"").apply();
        SPNetTest.edit().putInt(Constant.SP_MOBILE_NOT_REBOOT_COUNT,0).apply();

        TestLogPrinter.getInstance(DeleteLogsActivity.this).functionlLog();
        sqLiteDao.insertDate(Constant.DAO_NEXT_POWER_ON_OFF_TIME,"");
        sqLiteDao.insertDate(Constant.DAO_REBOOT,"");
        sqLiteDao.insertDate(Constant.DAO_LAST_POWER_ON_OFF_TIME,"");
        sqLiteDao.insertDate(Constant.DAO_POWERONOFF_COUNTS,"");
        sqLiteDao.insertDate(Constant.DAO_AUTO_SYNC_NET_TIME,"");
        sqLiteDao.insertDate(Constant.DAO_AUTO_SYNC_NET_TIME_COUNTS,"");
        sqLiteDao.insertDate(Constant.DAO_VIDEO_TEST,"");
        sqLiteDao.insertDate(Constant.DAO_ETH_REBOOT,"");
        sqLiteDao.insertDate(Constant.DAO_ETH_NOT_REBOOT,"");
        sqLiteDao.insertDate(Constant.DAO_WIFI_REBOOT,"");
        sqLiteDao.insertDate(Constant.DAO_WIFI_NOT_REBOOT,"");
        sqLiteDao.insertDate(Constant.DAO_MOB_REBOOT,"");
        sqLiteDao.insertDate(Constant.DAO_MOB_NOT_REBOOT,"");
        sqLiteDao.insertDate(Constant.DAO_MOB_LONG_TIME,"");
        ToastUtils.showShortToast(DeleteLogsActivity.this,getString(names[7]));
    }

    private void clearPowerOnOffLog() {
        sqLiteDao.deleteOrder(Constant.DAO_NEXT_POWER_ON_OFF_TIME);
        sqLiteDao.insertDate(Constant.DAO_NEXT_POWER_ON_OFF_TIME,"");
        sqLiteDao.deleteOrder(Constant.DAO_LAST_POWER_ON_OFF_TIME);
        sqLiteDao.insertDate(Constant.DAO_LAST_POWER_ON_OFF_TIME,"");
        sqLiteDao.deleteOrder(Constant.DAO_POWERONOFF_COUNTS);
        sqLiteDao.insertDate(Constant.DAO_POWERONOFF_COUNTS,"");
        SPPowerOnOff.edit().putStringSet(Constant.SP_POWER_DATA,new LinkedHashSet<String>()).apply();
        SPPowerOnOff.edit().putString(Constant.SP_LAST_POWERONOFF_TIME,"").apply();
        SPPowerOnOff.edit().putInt(Constant.SP_POWERONOFF_COUNTS,0).apply();
        ToastUtils.showShortToast(DeleteLogsActivity.this,getString(names[0]));
    }

    private void clearRebootLog() {
        sqLiteDao.deleteOrder(Constant.DAO_REBOOT);
        sqLiteDao.insertDate(Constant.DAO_REBOOT,"");
        ToastUtils.showShortToast(DeleteLogsActivity.this,getString(names[1]));
    }

    private void clearSyncNetTime() {
        sqLiteDao.deleteOrder(Constant.DAO_AUTO_SYNC_NET_TIME);
        sqLiteDao.deleteOrder(Constant.DAO_AUTO_SYNC_NET_TIME_COUNTS);
        sqLiteDao.insertDate(Constant.DAO_AUTO_SYNC_NET_TIME,"");
        sqLiteDao.insertDate(Constant.DAO_AUTO_SYNC_NET_TIME_COUNTS,"");
        SPSyncNetTime.edit().putInt(Constant.SP_COUNT_SYNC_TIME,0).apply();
        SPSyncNetTime.edit().putString(Constant.SP_SYNC_TIME_LOGS,"").apply();
        ToastUtils.showShortToast(DeleteLogsActivity.this,getString(names[2]));
    }

    private void clearVideoTestLog() {
        sqLiteDao.deleteOrder(Constant.DAO_VIDEO_TEST);
        sqLiteDao.insertDate(Constant.DAO_VIDEO_TEST,"");
        ToastUtils.showShortToast(DeleteLogsActivity.this,getString(names[3]));
    }


    private void clearEthTestLog() {
        sqLiteDao.deleteOrder(Constant.DAO_ETH_REBOOT);
        sqLiteDao.insertDate(Constant.DAO_ETH_REBOOT,"");
        sqLiteDao.deleteOrder(Constant.DAO_ETH_NOT_REBOOT);
        sqLiteDao.insertDate(Constant.DAO_ETH_NOT_REBOOT,"");
        SPNetTest.edit().putString(Constant.SP_ETHERNET_REBOOT_CONTENT,"").apply();
        SPNetTest.edit().putInt(Constant.SP_ETHERNET_REBOOT_COUNT,0).apply();
        SPNetTest.edit().putString(Constant.SP_ETHERNET_NOT_REBOOT_CONTENT,"").apply();
        SPNetTest.edit().putInt(Constant.SP_ETHERNET_NOT_REBOOT_COUNT,0).apply();
        ToastUtils.showShortToast(DeleteLogsActivity.this,getString(names[4]));
    }

    private void clearWifiLog() {
        sqLiteDao.deleteOrder(Constant.DAO_WIFI_REBOOT);
        sqLiteDao.insertDate(Constant.DAO_WIFI_REBOOT,"");
        sqLiteDao.deleteOrder(Constant.DAO_WIFI_NOT_REBOOT);
        sqLiteDao.insertDate(Constant.DAO_WIFI_NOT_REBOOT,"");
        SPNetTest.edit().putString(Constant.SP_WIFI_REBOOT_CONTENT,"").apply();
        SPNetTest.edit().putInt(Constant.SP_WIFI_REBOOT_COUNT,0).apply();
        SPNetTest.edit().putString(Constant.SP_WIFI_NOT_REBOOT_CONTENT,"").apply();
        SPNetTest.edit().putInt(Constant.SP_WIFI_NOT_REBOOT_COUNT,0).apply();
        ToastUtils.showShortToast(DeleteLogsActivity.this,getString(names[5]));
    }

    private void clearMobileLog() {
        sqLiteDao.deleteOrder(Constant.DAO_MOB_REBOOT);
        sqLiteDao.insertDate(Constant.DAO_MOB_REBOOT,"");
        sqLiteDao.deleteOrder(Constant.DAO_MOB_NOT_REBOOT);
        sqLiteDao.insertDate(Constant.DAO_MOB_NOT_REBOOT,"");
        sqLiteDao.deleteOrder(Constant.DAO_MOB_LONG_TIME);
        sqLiteDao.insertDate(Constant.DAO_MOB_LONG_TIME,"");
        SPNetTest.edit().putString(Constant.SP_MOBILE_REBOOT_CONTENT,"").apply();
        SPNetTest.edit().putInt(Constant.SP_MOBILE_REBOOT_COUNT,0).apply();
        SPNetTest.edit().putString(Constant.SP_MOBILE_NOT_REBOOT_CONTENT,"").apply();
        SPNetTest.edit().putInt(Constant.SP_MOBILE_NOT_REBOOT_COUNT,0).apply();
        ToastUtils.showShortToast(DeleteLogsActivity.this,getString(names[6]));
    }
}
