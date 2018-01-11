package com.example.cs_android.baidumap.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.example.cs_android.baidumap.R;

import java.util.List;

public class LocationActivity extends AppCompatActivity {

    public LocationClient mLocationClient = null;
    private TextView mTvBbdLocation;
    private CheckBox mCb1, mCb2, mCb3, mCb4, mCb5;
    private StringBuilder sb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
        mLocationClient.registerLocationListener(new MyBDAbstractLocationListener()); //注册监听函数
        setContentView(R.layout.activity_location);
        mTvBbdLocation = (TextView) findViewById(R.id.tv_bdLocation);
        mCb1 = (CheckBox) findViewById(R.id.cb1);
        mCb2 = (CheckBox) findViewById(R.id.cb2);
        mCb3 = (CheckBox) findViewById(R.id.cb3);
        mCb4 = (CheckBox) findViewById(R.id.cb4);
        mCb5 = (CheckBox) findViewById(R.id.cb5);

    }

    public void start(View view) {
        LocationClientOption mOption = new LocationClientOption();

        /**
         * 默认高精度，设置定位模式
         * LocationMode.Hight_Accuracy 高精度定位模式：这种定位模式下，会同时使用
         网络定位（Wi-Fi和基站定位）和GPS定位，优先返回最高精度的定位结果；
         但是在室内gps无信号，只会返回网络定位结果；
         室外如果gps收不到信号，也只会返回网络定位结果。
         * LocationMode.Battery_Saving 低功耗定位模式：这种定位模式下，不会使用GPS，只会使用网络定位。
         * LocationMode.Device_Sensors 仅用设备定位模式：这种定位模式下，
         不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位
         */
        mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        /**
         * 默认是true，设置是否使用gps定位
         * 如果设置为false，即使mOption.setLocationMode(LocationMode.Hight_Accuracy)也不会gps定位
         */
        mOption.setOpenGps(true);

        /**
         * 默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
         * 目前国内主要有以下三种坐标系：
         1. wgs84：目前广泛使用的GPS全球卫星定位系统使用的标准坐标系；
         2. gcj02：经过国测局加密的坐标；
         3. bd09：为百度坐标系，其中bd09ll表示百度经纬度坐标，bd09mc表示百度墨卡托米制坐标；
         * 在国内获得的坐标系类型可以是：国测局坐标、百度墨卡托坐标 和 百度经纬度坐标。
         在海外地区，只能获得WGS84坐标。请在使用过程中注意选择坐标。
         */
        mOption.setCoorType("bd09ll");

        /**
         * 默认0，即仅定位一次；设置间隔需大于等于1000ms，表示周期性定位
         * 如果不在AndroidManifest.xml声明百度指定的Service，周期性请求无法正常工作
         * 这里需要注意的是：如果是室外gps定位，不用访问服务器，设置的间隔是3秒，那么就是3秒返回一次位置
         如果是WiFi基站定位，需要访问服务器，这个时候每次网络请求时间差异很大，设置的间隔是3秒，
         只能大概保证3秒左右会返回就一次位置，有时某次定位可能会5秒才返回
         */
        mOption.setScanSpan(0);

        /**
         * 默认false，设置是否需要地址信息
         * 返回省、市、区、街道等地址信息，这个api用处很大，
         很多新闻类app会根据定位返回的市区信息推送用户所在市的新闻
         */
        mOption.setIsNeedAddress(true);

        /**
         * 默认false，设置是否需要位置语义化结果
         * 可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
         */
        mOption.setIsNeedLocationDescribe(true);

        /**
         * 默认false,设置是否需要设备方向传感器的方向结果
         * 一般在室外gps定位时，返回的位置信息是带有方向的，但是有时候gps返回的位置也不带方向，
         这个时候可以获取设备方向传感器的方向
         * wifi基站定位的位置信息是不带方向的，如果需要可以获取设备方向传感器的方向
         */
        mOption.setNeedDeviceDirect(false);

        /**
         * 默认false，设置是否当gps有效时按照设定的周期频率输出GPS结果
         * 室外gps有效时，周期性1秒返回一次位置信息，其实就是设置了
         locationManager.requestLocationUpdates中的minTime参数为1000ms，1秒回调一个gps位置
         * 如果设置了mOption.setScanSpan(3000)，那minTime就是3000ms了，3秒回调一个gps位置
         */
        mOption.setLocationNotify(false);

        /**
         * 默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
         * 如果你已经拿到了你要的位置信息，不需要再定位了，不杀死留着干嘛
         */
        mOption.setIgnoreKillProcess(true);

        /**
         * 默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
         * POI就是获取到的位置附近的一些商场、饭店、银行等信息
         */
        mOption.setIsNeedLocationPoiList(true);

        /**
         * 默认false，设置是否收集CRASH信息，默认收集
         */
        mOption.SetIgnoreCacheException(false);

        /**
         * 默认false，设置定位时是否需要海拔高度信息，默认不需要，除基础定位版本都可用
         */
        mOption.setIsNeedAltitude(false);

        mLocationClient.setLocOption(mOption);//设置定位参数

        mLocationClient.start();//发起定位
    }


    private class MyBDAbstractLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (null != bdLocation && bdLocation.getLocType() != BDLocation.TypeServerError) {
                sb = new StringBuilder();
                if (mCb1.isChecked()) {
                    //获取经纬度等基本信息
                    getCb1(bdLocation);
                }
                if (mCb2.isChecked()) {
                    //地址信息
                    getCb2(bdLocation);
                }
                if (mCb3.isChecked()) {
                    //位置描述信息
                    getCb3(bdLocation);
                }
                if (mCb4.isChecked()) {
                    //判断定位类型,获取不同信息
                    getCb4(bdLocation);
                }
                if (mCb5.isChecked()){
                    //周边POI信息
                    getCb5(bdLocation);
                }

                mTvBbdLocation.setText(sb.toString());
            }

        }
    }

    private void getCb5(BDLocation bdLocation) {
        Toast.makeText(this, "获取周边", Toast.LENGTH_SHORT).show();
        List<Poi> poiList = bdLocation.getPoiList();
        if (poiList.size()>0&&poiList!=null){
            for (int i = 0; i < poiList.size(); i++) {
                sb.append("周边"+i+":").append(poiList.get(i).getName()).append("\r\n");
            }
        }
    }

    private void getCb4(BDLocation bdLocation) {
        sb.append("判断定位类型,获取不同信息:").append("\r\n");
        sb.append("--------------------").append("\r\n");
        if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
            sb.append("速度 单位：km/h").append(bdLocation.getSpeed()).append("\r\n");
            sb.append("卫星数目").append(bdLocation.getSatelliteNumber()).append("\r\n");
            sb.append("海拔高度 单位：米").append(bdLocation.getAltitude()).append("\r\n");
            sb.append("gps质量判断").append(bdLocation.getGpsAccuracyStatus()).append("\r\n");
            Toast.makeText(LocationActivity.this, "gps定位成功", Toast.LENGTH_SHORT).show();

        } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
            // 运营商信息
            if (bdLocation.hasAltitude()) {// *****如果有*****
                sb.append("海拔高度:").append(bdLocation.getAltitude()).append("\r\n");
            }
            sb.append("运营商信息:").append(bdLocation.getOperators()).append("\r\n");
            Toast.makeText(LocationActivity.this, "网络定位成功", Toast.LENGTH_SHORT).show();

        } else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
            Toast.makeText(LocationActivity.this, "离线定位成功，离线定位结果也是有效的",
                    Toast.LENGTH_SHORT).show();

        } else if (bdLocation.getLocType() == BDLocation.TypeServerError) {
            Toast.makeText(LocationActivity.this, "服务端网络定位失败，可以反馈IMEI号和" +
                    "大体定位时间到loc-bugs@baidu.com，会有人追查原因", Toast.LENGTH_SHORT).show();

        } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkException) {
            Toast.makeText(LocationActivity.this, "网络不通导致定位失败，请检查网络是否通畅",
                    Toast.LENGTH_SHORT).show();

        } else if (bdLocation.getLocType() == BDLocation.TypeCriteriaException) {

            Toast.makeText(LocationActivity.this, "法获取有效定位依据导致定位失败，一般是" +
                    "由于手机的原因，处于飞行模式下一般会造成这种结可以反馈IMEI号和" +
                    " 果,可以试着重启手机", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCb3(BDLocation bdLocation) {
        sb.append("位置描述信息:").append("\r\n");
        sb.append("--------------------").append("\r\n");
        sb.append("获取位置描述信息").append(bdLocation.getLocationDescribe()).append("\r\n");
        sb.append("--------------------").append("\r\n");
    }

    private void getCb2(BDLocation bdLocation) {
        sb.append("地址信息:").append("\r\n");
        sb.append("--------------------").append("\r\n");
        sb.append("获取详细地址信息：").append(bdLocation.getAddrStr()).append("\r\n");
        sb.append("获取国家").append(bdLocation.getCountry()).append("\r\n");
        sb.append("获取省份").append(bdLocation.getProvince()).append("\r\n");
        sb.append("获取城市").append(bdLocation.getCity()).append("\r\n");
        sb.append("获取区县").append(bdLocation.getDistrict()).append("\r\n");
        sb.append("获取街道信息").append(bdLocation.getStreet()).append("\r\n");
        sb.append("--------------------").append("\r\n");
    }

    private void getCb1(BDLocation bdLocation) {
        sb.append("获取经纬度等基本信息:").append("\r\n");
        sb.append("--------------------").append("\r\n");
        sb.append("纬度:").append(bdLocation.getLatitude()).append("\r\n");
        sb.append("经度:").append(bdLocation.getLongitude()).append("\r\n");
        sb.append("定位方向:").append(bdLocation.getDirection()).append("\r\n");
        sb.append("定位精度:").append(bdLocation.getRadius()).append("\r\n");
        sb.append("定位坐标类型:").append(bdLocation.getCoorType()).append("\r\n");
        sb.append("定位类型、定位错误返回码:").append(bdLocation.getLocType()).append("\r\n");
        sb.append("对应的定位类型说明：").append(bdLocation.getLocTypeDescription()).append("\r\n");
        sb.append("获取经纬度服务器时间：").append(bdLocation.getTime()).append("\r\n");
         //判断用户是在室内，还是在室外1：室内，0：室外，这个判断不一定是100%准确的
        int userIndoorState = bdLocation.getUserIndoorState();
        if (userIndoorState==0){
            sb.append("判断用户是在室内：").append("室外").append("\r\n");
        }else if (userIndoorState==1){
            sb.append("判断用户是在室内：").append("室内").append("\r\n");
        }
        sb.append("--------------------").append("\r\n");
    }
}
