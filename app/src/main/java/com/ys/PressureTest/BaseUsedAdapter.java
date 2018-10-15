package com.ys.PressureTest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


/**
 * Created by Administrator on 2018/7/25.
 */

public class BaseUsedAdapter extends BaseAdapter {
    private int[] names;
    private int selectedPosition = 0;

    public BaseUsedAdapter(int[] names) {
        this.names = names;
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return names[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stress_list_item,parent,false);
            holder = new ViewHolder();
            holder.stressNum = convertView.findViewById(R.id.item_menu_icon);
            holder.stressName = convertView.findViewById(R.id.item_menu_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (selectedPosition == position) {
            holder.stressNum.setTextColor(convertView.getContext().getResources().getColor(R.color.main_menu_item_text));
            holder.stressName.setTextColor(convertView.getContext().getResources().getColor(R.color.main_menu_item_text));
        } else {
            holder.stressNum.setTextColor(convertView.getContext().getResources().getColor(R.color.main_menu_item_text1));
            holder.stressName.setTextColor(convertView.getContext().getResources().getColor(R.color.main_menu_item_text1));
        }
        holder.stressNum.setText("" + (position + 1));
        holder.stressName.setText(names[position]);
        return convertView;
    }

     public void selected(int position) {
        if (position == selectedPosition) return;
        selectedPosition = position;
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView stressName;
        TextView stressNum;
    }
}

