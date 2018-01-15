package com.example.cs_android.baidumap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.example.cs_android.baidumap.R;

import java.util.Random;

/**
 * 地图显示页
 */

public class MapActivity extends AppCompatActivity {
    private TextureMapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private boolean is_First_locate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
        mLocationClient.registerLocationListener(new MyBDAbstractLocationListener()); //注册监听函数
        setContentView(R.layout.activity_map);
        mMapView = (TextureMapView) findViewById(R.id.texture_map);
        mBaiduMap = mMapView.getMap();
    }

    //按钮的单击事件
    public void onclick(View view) {
        switch (view.getId()) {
            //切换地图类型
            case R.id.btn_change_map:
                Random random = new Random();
                int i = random.nextInt(3);
                switch (i) {
                    case 0:
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL); //切换地图类型
                        break;
                    case 1:
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE); //切换地图类型
                        break;
                    case 2:
                        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NONE); //切换地图类型
                        break;
                }
                break;
            //定位当前位置
            case R.id.btn_location_map:
                LocationClientOption option = new LocationClientOption();
                option.setOpenGps(true); // 打开gps
                option.setCoorType("bd09ll"); // 设置坐标类型
                option.setScanSpan(1000);
                mLocationClient.setLocOption(option);
                mLocationClient.start();//开启定位
                /**
                 * 对定位的图标进行配置，需要MyLocationConfiguration实例，这个类是用设置定位图标的显示方式的
                 */
//                MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
//                mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
//                        mCurrentMode, true, null));
                mBaiduMap.setMyLocationEnabled(true);//开启定位图层
                break;
            //关闭定位图层
            case R.id.btn_close_location_map:
                mBaiduMap.setMyLocationEnabled(false);
                break;
            //离线地图
            case R.id.btn_download_map:
                startActivity(new Intent(MapActivity.this, OffLineActivity.class));
                break;
        }
    }


    //开启定位的监听器
    private class MyBDAbstractLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null || mMapView == null) {
                return;
            }

            if (is_First_locate) {
                is_First_locate = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }

            MyLocationData locationData = new MyLocationData.Builder()
                    //设置精度圈
                    .accuracy(bdLocation.getRadius())
                    //设置方向
                    .direction(bdLocation.getDirection())
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(locationData);

        }
    }
}
