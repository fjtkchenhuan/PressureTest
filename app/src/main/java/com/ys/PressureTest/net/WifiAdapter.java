package com.ys.PressureTest.net;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.ys.PressureTest.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2017/12/8.
 */

public class WifiAdapter extends BaseAdapter {
    private List<ScanResult> wifis;
    private NetManager netManager;
    private Context mContext;
    private int state;//wifi连接状态

    public WifiAdapter(Context context) {
        this.mContext = context;
        netManager = NetManager.getIntance(context);
    }

    /**
     * 更新wifi连接状态
     */
    public void updateWifiState(int state) {
        this.state = state;
        if (wifis != null && wifis.size() > 0) {
            notifyDataSetChanged();
        }
    }

    public void update(List<ScanResult> wifis) {
        this.wifis = wifis;
//        Collections.sort(wifis, new Comparator<ScanResult>() {
//            @Override
//            public int compare(ScanResult o1, ScanResult o2) {
//                return Integer.compare(o2.level,o1.level);
//            }
//        });
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return wifis == null ? 0 : wifis.size();
    }

    @Override
    public Object getItem(int position) {
        return wifis.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        ViewHolder holder = null;
        ScanResult scanResult = wifis.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_wifi, null);
            holder.intensity = convertView.findViewById(R.id.item_wifi_intensity);
            holder.name = convertView.findViewById(R.id.item_wifi_name);
            holder.status = convertView.findViewById(R.id.item_wifi_status);
            holder.connectImg = convertView.findViewById(R.id.ic_connect);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (scanResult != null) {
            if (netManager.judgeConnected(scanResult)) {
                if (get(mContext,scanResult.SSID,state).equals(mContext.getResources().getString(R.string.connect_to,scanResult.SSID))) {
                    holder.connectImg.setVisibility(View.VISIBLE);
                    holder.status.setVisibility(View.VISIBLE);
                    holder.status.setText(get(mContext,wifis.get(position).SSID,state));
                    holder.connectImg.setImageResource(R.drawable.check);
//                    holder.status.setTextColor(mContext.getResources().getColor(R.color.wifi_connected));
//                    holder.name.setTextColor(mContext.getResources().getColor(R.color.wifi_connected));
                }
            }else {
                holder.connectImg.setVisibility(View.INVISIBLE);
                holder.status.setVisibility(View.INVISIBLE);
            }
//            else {
//                holder.status.setText(getWifiStatus(mContext, scanResult.capabilities));
//            }
        }
        holder.setName(scanResult);
        holder.setIntensity(scanResult);

        return convertView;
    }

    public void clear() {
        if (wifis != null) {
            wifis.clear();
            notifyDataSetChanged();
        }
    }


    class ViewHolder {
        ImageView intensity;
        TextView name;
        TextView status;
        ImageView connectImg;
        int[] imageId = new int[]{R.mipmap.ic_wifi_signal_1_light, R.mipmap.ic_wifi_signal_2_light, R.mipmap.ic_wifi_signal_3_light, R.mipmap.ic_wifi_signal_4_light};

        private void setName(ScanResult scanResult) {
            if ("".equals(scanResult.SSID))
                name.setText("无名称");
            else
                name.setText(scanResult.SSID);
        }

        private void setIntensity(ScanResult scanResult) {
            int level = WifiManager.calculateSignalLevel(scanResult.level, 4);
            intensity.setImageResource(imageId[level]);
        }
    }

    public static String get(Context context, String ssid, int state) {
        String[] formats = context.getResources().getStringArray((ssid == null)
                ? R.array.wifi_status : R.array.wifi_status_with_ssid);
        if (state >= formats.length || formats[state].length() == 0) {
            return "";
        }
        return String.format(formats[state], ssid);
    }


}
