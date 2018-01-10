package com.example.cs_android.baidumap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.cs_android.baidumap.R;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.app_description));
        requestPermissions();
    }

    private void requestPermissions() {

    }

    public void onclick(View v) {
        switch (v.getId()) {
            case R.id.btn_location:
                //启动定位页面
                startCls(LocationActivity.class);
                break;
        }
    }

    public void startCls(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }
}
