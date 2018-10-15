package com.example.clenttest;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.example.util.SharedPreferenceutil;

public class ModeSelectionActivity extends Activity implements View.OnClickListener {
    private static final String tag = "TAG";
    Button btn_reboot, btn_onreboot;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);
        btn_reboot = findViewById(R.id.btn_reboot);
        btn_onreboot = findViewById(R.id.btn_onreboot);
        btn_onreboot.setOnClickListener(this);
        btn_reboot.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reboot:
                SharedPreferenceutil.setSelect(false);
                finish();
                break;
            case R.id.btn_onreboot:
                SharedPreferenceutil.setSelect(true);
                finish();
                break;
        }
    }
}

