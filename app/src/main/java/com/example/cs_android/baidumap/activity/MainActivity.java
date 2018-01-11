package com.example.cs_android.baidumap.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.cs_android.baidumap.R;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RxPermissions mRxPermissions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.app_description));
        requestPermissions();
    }

    private void requestPermissions() {
        mRxPermissions = new RxPermissions(MainActivity.this);
        mRxPermissions.requestEach(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            Log.d(TAG, permission.name + " is granted.");
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，弹出提示
                            showPermissions();
                        } else {
                            // 用户拒绝了该权限，弹出提示
                            showPermissions();
                        }
                    }
                });
    }

    public void showPermissions() {
        if (!mRxPermissions.isGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermissionsDialog("此应用需要获取定位地理位置的权限,是否打开应用设置手动授予");
        } else if (!mRxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showPermissionsDialog("此应用需要获取读取手机存储的权限，是否打开应用设置手动授予");
        } else if (!mRxPermissions.isGranted(Manifest.permission.READ_PHONE_STATE)) {
            showPermissionsDialog("此应用需要获取读取手机基本信息的权限，是否打开应用设置手动授予");
        }
    }

    public void showPermissionsDialog(String msg) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("请注意")
                .setMessage(msg)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                        startActivity(intent);
                    }
                }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }).show();
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
