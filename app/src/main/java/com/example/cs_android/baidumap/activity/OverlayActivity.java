package com.example.cs_android.baidumap.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CheckBox;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.example.cs_android.baidumap.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 绘制maker，面的页
 */

public class OverlayActivity extends AppCompatActivity {


    private BaiduMap mBaiduMap;
    private CheckBox mCb1, mCb2, mCb3, mCb4, mCb5, mCb6,mCb7;
    private List<LatLng> mLatLngs = new ArrayList<LatLng>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_overlay);
        TextureMapView mMapView = (TextureMapView) findViewById(R.id.texture_map);
        mBaiduMap = mMapView.getMap();
        initDate();

        //放大百度地图
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(17f));

        initMapOnclickListener();
    }


    private void initMapOnclickListener() {
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //绘制点
                if (mCb1.isChecked()) {
                    //构建Marker图标
                    BitmapDescriptor bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_gcoding);

                    //构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions()
                            .position(latLng)
                            .icon(bitmap);

                    //在地图上添加Marker，并显示
                    mBaiduMap.addOverlay(option);
                }
                //绘制折线
                if (mCb2.isChecked()) {
                    mLatLngs.add(latLng);

                    if (mLatLngs.size() == 3) {
                        //绘制折线
                        OverlayOptions Polyline = new PolylineOptions().width(10)
                                .color(0xAAFF0000).points(mLatLngs);
                        mBaiduMap.addOverlay(Polyline);
                        mLatLngs.clear();
                    }
                }
                //绘制弧线
                if (mCb3.isChecked()) {
                    mLatLngs.add(latLng);

                    if (mLatLngs.size() == 3) {
                        OverlayOptions Polyline = new ArcOptions().width(10)
                                .color(0xAAFF0000)
                                .points(mLatLngs.get(0), mLatLngs.get(1), mLatLngs.get(2));
                        mBaiduMap.addOverlay(Polyline);
                        mLatLngs.clear();
                    }
                }
                //绘制圆
                if (mCb4.isChecked()) {
                    //设置颜色和透明度，均使用16进制显示，0xAARRGGBB，如 0xAA000000 其中AA是透明度，000000为颜色
                    OverlayOptions ooCircle = new CircleOptions().fillColor(0x000000FF)
                            .center(latLng)
                            .stroke(new Stroke(5, 0xAA000000))
                            .radius(20);
                    mBaiduMap.addOverlay(ooCircle);
                }
                //绘制面-三角形
                if (mCb5.isChecked()) {
                    mLatLngs.add(latLng);

                    if (mLatLngs.size() == 3) {
                        //构建用户绘制多边形的Option对象
                        OverlayOptions polygonOption = new PolygonOptions()
                                .points(mLatLngs)
                                .stroke(new Stroke(5, 0xAA00FF00))
                                .fillColor(0xAAFFFF00);
                        //在地图上添加多边形Option，用于显示
                        mBaiduMap.addOverlay(polygonOption);
                        mLatLngs.clear();
                    }
                }
                //绘制文字
                if (mCb6.isChecked()) {
                                   //构建文字Option对象，用于在地图上添加文字
                    OverlayOptions textOption = new TextOptions()
                            .bgColor(0xAAFFFF00)
                            .fontSize(24)
                            .fontColor(0xFFFF00FF)
                            .text("绘制文字")
                            .rotate(-30)
                            .position(latLng);
                    //在地图上添加该文字对象并显示
                    mBaiduMap.addOverlay(textOption);
                }
                //绘制信息窗口
                if (mCb7.isChecked()){
                    //创建InfoWindow展示的view
                    Button button = new Button(getApplicationContext());
                    button.setBackgroundColor(getColor(R.color.colorPrimary));

                    //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
                    InfoWindow mInfoWindow = new InfoWindow(button, latLng, -47);

                    //显示InfoWindow
                    mBaiduMap.showInfoWindow(mInfoWindow);
                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
    }

    private void initDate() {
        mCb1 = (CheckBox) findViewById(R.id.cb1);
        mCb2 = (CheckBox) findViewById(R.id.cb2);
        mCb3 = (CheckBox) findViewById(R.id.cb3);
        mCb4 = (CheckBox) findViewById(R.id.cb4);
        mCb5 = (CheckBox) findViewById(R.id.cb5);
        mCb6 = (CheckBox) findViewById(R.id.cb6);
        mCb7 = (CheckBox) findViewById(R.id.cb7);
    }
}
