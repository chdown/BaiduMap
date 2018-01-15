package com.example.cs_android.baidumap.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.SuggestAddrInfo;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.cs_android.baidumap.R;
import com.example.cs_android.baidumap.utils.BikingRouteOverlay;
import com.example.cs_android.baidumap.utils.DrivingRouteOverlay;
import com.example.cs_android.baidumap.utils.TransitRouteOverlay;
import com.example.cs_android.baidumap.utils.WalkingRouteOverlay;

/**
 * 路线规划页
 */

public class RouteActivity extends AppCompatActivity {

    private BaiduMap mBaiduMap;
    private CheckBox mCb1, mCb2, mCb3, mCb4, mCb5;
    private LocationClient mLocationClient;
    private RoutePlanSearch mSearch;
    private EditText mEtEnd, mEtCityName, mEtStart;
    private LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
        mLocationClient.registerLocationListener(new MyBDAbstractLocationListener()); //注册监听函数
        setContentView(R.layout.activity_route);
        TextureMapView mMapView = (TextureMapView) findViewById(R.id.texture_map);
        mBaiduMap = mMapView.getMap();

        initDate();
    }


    public void start(View view) {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);
        mLocationClient.start();//开启定位

        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(listener);

        PlanNode stNode = PlanNode.withCityNameAndPlaceName(mEtCityName.getText().toString().trim(),
                mEtStart.getText().toString().trim());
        PlanNode enNode = PlanNode.withCityNameAndPlaceName(mEtCityName.getText().toString().trim(),
                mEtEnd.getText().toString().trim());

        //步行
        if (mCb1.isChecked()) {
            mSearch.walkingSearch((new WalkingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        }
        //自行车
        if (mCb2.isChecked()) {
            //通过设ridingType，可以区分普通自行车，和电动车线路
            //ridingType(int ridingType)
            mSearch.bikingSearch((new BikingRoutePlanOption())
                    .from(stNode)
                    .to(enNode)
                    .ridingType(0));
        }
        //电动车
        if (mCb3.isChecked()) {
            //通过设ridingType，可以区分普通自行车，和电动车线路
            //ridingType(int ridingType)
            mSearch.bikingSearch((new BikingRoutePlanOption())
                    .from(stNode)
                    .to(enNode)
                    .ridingType(1));
        }
        //驾车
        if (mCb4.isChecked()) {
            mSearch.drivingSearch((new DrivingRoutePlanOption())
                    .from(stNode)
                    .to(enNode));
        }
        //公交
        if (mCb5.isChecked()) {
            mSearch.transitSearch(
                    new TransitRoutePlanOption()
                            .from(stNode)
                            .to(enNode));
        }

    }

    OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
        //获取步行线路规划结果
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(RouteActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                mLocationClient.stop();
            }
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
                Toast.makeText(RouteActivity.this, "你好，请检查起点或终点信息，建议输入起点为："
                        + suggestAddrInfo.getSuggestStartNode() + "建议输入起点为：" +
                        suggestAddrInfo.getSuggestEndNode(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                if (result.getRouteLines().size() >= 1) {
                    MyWalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    overlay.setData(result.getRouteLines().get(0));
                    overlay.addToMap();
                    overlay.zoomToSpan();
                } else {
                    Log.d("route result", "结果数<0");
                    return;
                }
            }
        }

        //获取综合公共交通线路规划结果
        @Override
        public void onGetTransitRouteResult(TransitRouteResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(RouteActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                mLocationClient.stop();
            }
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
                Toast.makeText(RouteActivity.this, "你好，请检查起点或终点信息，建议输入起点为："
                        + suggestAddrInfo.getSuggestStartNode() + "建议输入起点为：" +
                        suggestAddrInfo.getSuggestEndNode(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                if (result.getRouteLines().size() >= 1) {
                    MyTransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    overlay.setData(result.getRouteLines().get(0));
                    overlay.addToMap();
                    overlay.zoomToSpan();
                } else {
                    Log.d("route result", "结果数<0");
                    return;
                }
            }
        }

        //获取**跨城**综合公共交通线路规划结果
        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
//            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
//                Toast.makeText(RouteActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
//                mLocationClient.stop();
//            }
//            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
//                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
//            SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
//            Toast.makeText(RouteActivity.this, "你好，请检查起点或终点信息，建议输入起点为："
//                    + suggestAddrInfo.getSuggestStartNode() + "建议输入起点为：" +
//                    suggestAddrInfo.getSuggestEndNode(), Toast.LENGTH_SHORT).show();
//            return;
//            }
//            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
//                if (result.getRouteLines().size() >= 1) {
//                    MyTransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaiduMap);
//                    mBaiduMap.setOnMarkerClickListener(overlay);
//                    overlay.setData(result.getRouteLines().get(0));
//                    overlay.addToMap();
//                    overlay.zoomToSpan();
//                } else {
//                    Log.d("route result", "结果数<0");
//                    return;
//                }
//            }
        }

        //获取驾车线路规划结果
        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(RouteActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                mLocationClient.stop();
            }
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
                Toast.makeText(RouteActivity.this, "你好，请检查起点或终点信息，建议输入起点为："
                        + suggestAddrInfo.getSuggestStartNode() + "建议输入起点为：" +
                        suggestAddrInfo.getSuggestEndNode(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                if (result.getRouteLines().size() >= 1) {
                    MyDrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    overlay.setData(result.getRouteLines().get(0));
                    overlay.addToMap();
                    overlay.zoomToSpan();
                } else {
                    Log.d("route result", "结果数<0");
                    return;
                }
            }
        }

        //室内路线规划结果
        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
        }

        //获取普通骑行路规划结果
        @Override
        public void onGetBikingRouteResult(BikingRouteResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(RouteActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                mLocationClient.stop();
            }
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                SuggestAddrInfo suggestAddrInfo = result.getSuggestAddrInfo();
                Toast.makeText(RouteActivity.this, "你好，请检查起点或终点信息，建议输入起点为："
                        + suggestAddrInfo.getSuggestStartNode() + "建议输入起点为：" +
                        suggestAddrInfo.getSuggestEndNode(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                if (result.getRouteLines().size() >= 1) {
                    MyBikingRouteOverlay overlay = new MyBikingRouteOverlay(mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    overlay.setData(result.getRouteLines().get(0));
                    overlay.addToMap();
                    overlay.zoomToSpan();
                } else {
                    Log.d("route result", "结果数<0");
                    return;
                }
            }
        }
    };

    private void initDate() {
        mCb1 = (CheckBox) findViewById(R.id.cb1);
        mCb2 = (CheckBox) findViewById(R.id.cb2);
        mCb3 = (CheckBox) findViewById(R.id.cb3);
        mCb4 = (CheckBox) findViewById(R.id.cb4);
        mCb5 = (CheckBox) findViewById(R.id.cb5);
        mEtStart = (EditText) findViewById(R.id.et_start);
        mEtEnd = (EditText) findViewById(R.id.et_end);
        mEtCityName = (EditText) findViewById(R.id.et_city_name);
    }

    boolean useDefaultIcon = false;//使用默认ICON

    //驾车
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {
        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    //步行
    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {
        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    //自行车
    private class MyBikingRouteOverlay extends BikingRouteOverlay {
        public MyBikingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    //公交
    private class MyTransitRouteOverlay extends TransitRouteOverlay {
        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }


    //定位监听
    private class MyBDAbstractLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
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
