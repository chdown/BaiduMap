package com.example.cs_android.baidumap.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.fence.AlarmPoint;
import com.baidu.trace.api.fence.CircleFence;
import com.baidu.trace.api.fence.CreateFenceRequest;
import com.baidu.trace.api.fence.CreateFenceResponse;
import com.baidu.trace.api.fence.DeleteFenceResponse;
import com.baidu.trace.api.fence.DistrictFence;
import com.baidu.trace.api.fence.FenceAlarmPushInfo;
import com.baidu.trace.api.fence.FenceInfo;
import com.baidu.trace.api.fence.FenceListRequest;
import com.baidu.trace.api.fence.FenceListResponse;
import com.baidu.trace.api.fence.HistoryAlarmRequest;
import com.baidu.trace.api.fence.HistoryAlarmResponse;
import com.baidu.trace.api.fence.MonitoredAction;
import com.baidu.trace.api.fence.MonitoredStatus;
import com.baidu.trace.api.fence.MonitoredStatusByLocationRequest;
import com.baidu.trace.api.fence.MonitoredStatusByLocationResponse;
import com.baidu.trace.api.fence.MonitoredStatusInfo;
import com.baidu.trace.api.fence.MonitoredStatusRequest;
import com.baidu.trace.api.fence.MonitoredStatusResponse;
import com.baidu.trace.api.fence.OnFenceListener;
import com.baidu.trace.api.fence.PolygonFence;
import com.baidu.trace.api.fence.PolylineFence;
import com.baidu.trace.api.fence.UpdateFenceResponse;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;
import com.example.cs_android.baidumap.R;
import com.example.cs_android.baidumap.trace.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class RailActivity extends AppCompatActivity {

    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private EditText mEtRadius;
    private boolean is_First_locate = true;
    private boolean is_create_rail = true;
    private LBSTraceClient mTraceClient;
    boolean isNeedObjectStorage = false;
    private Trace mTrace;

    private int gatherInterval = 5; // 定位周期(单位:秒)
    private int packInterval = 10; // 打包回传周期(单位:秒)
    private int rail_tag;//标记客户端或服务端围栏
    long serviceId = 158468;  // 轨迹服务ID
    String entityName = "myTrace";   // 设备标识

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
        mLocationClient.registerLocationListener(new MyBDAbstractLocationListener()); //注册监听函数
        setContentView(R.layout.activity_rail);
        TextureMapView mapView = (TextureMapView) findViewById(R.id.texture_map);
        mEtRadius = (EditText) findViewById(R.id.et_radius);
        mBaiduMap = mapView.getMap();

        //初始化轨迹
        mTrace = new Trace(serviceId, entityName, isNeedObjectStorage); // 初始化轨迹服务
        mTraceClient = new LBSTraceClient(getApplicationContext());// 初始化轨迹服务客户端
        mTraceClient.setInterval(gatherInterval, packInterval);

        //定位设置
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);
    }

    public void rail_server(View view) {
        rail_tag = 1;

        initrail();

    }

    private void initrail() {
        mBaiduMap.clear();
        is_create_rail = true;
        mLocationClient.stop();//关闭定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.start();//开启定位
        mLocationClient.start();//开启定位
        mBaiduMap.setMyLocationEnabled(true);
        //开启服务，开启采集
        mTraceClient.startTrace(mTrace, mTraceListener);
        mTraceClient.startGather(mTraceListener);
    }


    public void rail_client(View view) {
        rail_tag = 0;
        initrail();
    }

    OnTraceListener mTraceListener = new OnTraceListener() {


        @Override
        public void onBindServiceCallback(int i, String s) {
        }

        @Override
        public void onStartTraceCallback(int i, String s) {
        }

        @Override
        public void onStopTraceCallback(int i, String s) {
        }

        @Override
        public void onStartGatherCallback(int i, String s) {
        }

        @Override
        public void onStopGatherCallback(int i, String s) {
        }

        @Override
        public void onPushCallback(byte messageType, PushMessage pushMessage) {
            if (messageType < 0x03 || messageType > 0x04) {
                Toast.makeText(RailActivity.this, pushMessage.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            FenceAlarmPushInfo alarmPushInfo = pushMessage.getFenceAlarmPushInfo();
            if (null == alarmPushInfo) {
                Toast.makeText(RailActivity.this, "onPushCallback:" + "\r\n状态码:" + messageType +
                        "\r\n消息内容:" + pushMessage, Toast.LENGTH_SHORT).show();
                return;
            }
            StringBuffer alarmInfo = new StringBuffer();
            alarmInfo.append("您于")
                    .append(CommonUtil.getHMS(alarmPushInfo.getCurrentPoint().getLocTime() * 1000))
                    .append(alarmPushInfo.getMonitoredAction() == MonitoredAction.enter ? "进入" : "离开")
                    .append(messageType == 0x03 ? "云端" : "本地")
                    .append("围栏：").append(alarmPushInfo.getFenceName());

            //获取信息
            Toast.makeText(RailActivity.this, alarmInfo.toString(), Toast.LENGTH_SHORT).show();
            mEtRadius.setText(alarmInfo.toString());

            alarmPushInfo.getFenceId();//获取围栏id
            alarmPushInfo.getMonitoredPerson();//获取监控对象标识
            alarmPushInfo.getFenceName();//获取围栏名称
            alarmPushInfo.getPrePoint();//获取上一个点经度信息
            AlarmPoint alarmPoin = alarmPushInfo.getCurrentPoint();//获取报警点经纬度等信息
            alarmPoin.getCreateTime();//获取此位置上传到服务端时间
            alarmPoin.getLocTime();//获取定位产生的原始时间

        }

        @Override
        public void onInitBOSCallback(int i, String s) {

        }
    };

    //根据坐标查询
    public void location(View view) {
        /**
         * 查询监控对象在指定位置是否在围栏内，以查询服务端为例
         * latLng：位置点
         * fenceIds 服务端围栏编号列表，List<Long>，如传入null，表示查询所有围栏
         */
        int tag = 10;// 请求标识
        List<Long> fenceIds = null; // 围栏编号列表
        com.baidu.trace.model.LatLng location = new com.baidu.trace.model.LatLng(40.0552720000, 116.307655000); // 位置坐标
        CoordType coordType = CoordType.bd09ll;  // 坐标类型
        MonitoredStatusByLocationRequest request = MonitoredStatusByLocationRequest .buildServerRequest(tag, serviceId,
                entityName, fenceIds, location, coordType);
        //发起查询请求
        mTraceClient.queryMonitoredStatusByLocation(request, mFenceListener);

    }

    //查询指定监控对象状态
    public void oneself(View view) {
        /**
         * 查询监控对象是否在围栏内，以查询服务端为例
         * fenceIds：服务端围栏编号列表，List<Long> 传入null指定查询在所有围栏中的信息
         * entityName：监控对象标识
         */
        int tag = 9; // 请求标识
        MonitoredStatusRequest request = MonitoredStatusRequest.buildServerRequest(tag, serviceId,entityName, null);
        //发起查询请求
        mTraceClient.queryMonitoredStatus(request, mFenceListener);

    }

    public void history(View view) {

        /**
         * 以查询服务端为例
         * startTime：开始时间
         * endTime：结束时间
         * fenceIds：服务端围栏编号列表，List<Long>，如传入null，表示查询所有围栏
         */
        int tag = 8;// 请求标识
        long startTime = System.currentTimeMillis() / 1000 - 30 * 60;// 开始时间
        long endTime = System.currentTimeMillis() / 1000; // 结束时间
        CoordType coordType = CoordType.bd09ll;  // 坐标类型
        List<Long> fenceIds = null;
        HistoryAlarmRequest request = HistoryAlarmRequest.buildServerRequest(tag, serviceId, startTime,
                endTime, entityName, fenceIds, coordType);
        //发起查询请求
        mTraceClient.queryFenceHistoryAlarmInfo(request, mFenceListener);

    }

    //查询围栏列表
    public void rail_list(View view) {
        int tag = 7;// 请求标识
        FenceListRequest request = FenceListRequest.buildServerRequest(tag,serviceId, entityName, null,CoordType.bd09ll);
        //发起查询围栏请求
        mTraceClient.queryFenceList(request, mFenceListener);
    }


    private class MyBDAbstractLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //初始化地图
            initMap(bdLocation);
            switch (rail_tag) {
                case 0:
                    if (is_create_rail) {
                        initClient(bdLocation);
                    }
                    break;
                case 1:
                    if (is_create_rail) {
                        initServer(bdLocation);
                        break;
                    }
            }
        }
    }

    private void initMap(BDLocation bdLocation) {
        if (is_First_locate) {
            is_First_locate = false;
            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            //设置缩放中心点；缩放比例；
            builder.target(ll).zoom(18.0f);
            //给地图设置状态
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

    //设置客户端围栏
    private void initClient(BDLocation bdLocation) {
        int tag = 3;// 请求标识
        String fenceName = "local_circle";// 围栏名称
        double radius = Double.parseDouble(mEtRadius.getText().toString().trim());// 围栏半径（单位 : 米）
        int denoise = 200;// 去噪精度
        CoordType coordType = CoordType.bd09ll; // 坐标类型
        com.baidu.trace.model.LatLng center = new com.baidu.trace.model.LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
        // 创建本地圆形围栏请求实例
        CreateFenceRequest localCircleFenceRequest = CreateFenceRequest
                .buildLocalCircleRequest(tag, serviceId, entityName, fenceName, center, radius, denoise, coordType);
        // 创建本地圆形围栏
        mTraceClient.createFence(localCircleFenceRequest, mFenceListener);
        is_create_rail = false;

        //绘制圆
        LatLng llCircle = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
        OverlayOptions ooCircle = new CircleOptions()
                .fillColor(0x000000FF)
                .center(llCircle)
                .stroke(new Stroke(5, 0xAA000000))
                .radius((int) radius);
        mBaiduMap.addOverlay(ooCircle);
    }

    //设置服务端围栏
    private void initServer(BDLocation bdLocation) {
        // 请求标识
        int tag = 11;
        // 围栏名称
        String fenceName = "server_polygon_fence";
        // 多边形顶点集
        List<com.baidu.trace.model.LatLng> vertexes = new ArrayList<>();
        vertexes.add(new com.baidu.trace.model.LatLng(bdLocation.getLatitude() + 0.000500, bdLocation.getLongitude() - 0.000500));
        vertexes.add(new com.baidu.trace.model.LatLng(bdLocation.getLatitude() + 0.000500, bdLocation.getLongitude() + 0.000500));
        vertexes.add(new com.baidu.trace.model.LatLng(bdLocation.getLatitude() - 0.000600, bdLocation.getLongitude() + 0.000600));
        vertexes.add(new com.baidu.trace.model.LatLng(bdLocation.getLatitude() - 0.000600, bdLocation.getLongitude() - 0.000600));
        // 去噪精度
        int denoise = 100;
        // 坐标类型
        CoordType coordType = CoordType.bd09ll;
        // 创建服务端多边形围栏请求实例
        CreateFenceRequest request = CreateFenceRequest.buildServerPolygonRequest(tag,
                serviceId, fenceName, entityName, vertexes, denoise, coordType);
        // 初始化围栏监听器
        //参见客户端围栏
        // 创建服务端多边形围栏
        mTraceClient.createFence(request, mFenceListener);
        is_create_rail = false;

        //绘制多边形
        //定义多边形的五个顶点
        LatLng pt1 = new LatLng(bdLocation.getLatitude() + 0.000500, bdLocation.getLongitude() - 0.000500);
        LatLng pt3 = new LatLng(bdLocation.getLatitude() + 0.000500, bdLocation.getLongitude() + 0.000500);
        LatLng pt4 = new LatLng(bdLocation.getLatitude() - 0.000600, bdLocation.getLongitude() + 0.000600);
        LatLng pt5 = new LatLng(bdLocation.getLatitude() - 0.000600, bdLocation.getLongitude() - 0.000600);
        List<LatLng> pts = new ArrayList<LatLng>();
        pts.add(pt1);
        pts.add(pt3);
        pts.add(pt4);
        pts.add(pt5);
        //构建用户绘制多边形的Option对象
        OverlayOptions polygonOption = new PolygonOptions()
                .points(pts)
                .stroke(new Stroke(5, 0xAA00FF00))
                .fillColor(0x000000FF);

        //在地图上添加多边形Option，用于显示
        mBaiduMap.addOverlay(polygonOption);
    }


    // 初始化围栏监听器
    OnFenceListener mFenceListener = new OnFenceListener() {
        // 创建围栏回调
        @Override
        public void onCreateFenceCallback(CreateFenceResponse response) {
            if ("成功".equals(response.getMessage())) {
                Toast.makeText(RailActivity.this, "创建围栏回调成功", Toast.LENGTH_SHORT).show();
            }
        }

        // 更新围栏回调
        @Override
        public void onUpdateFenceCallback(UpdateFenceResponse response) {
            if ("成功".equals(response.getMessage())) {
                Toast.makeText(RailActivity.this, "更新围栏回调成功", Toast.LENGTH_SHORT).show();
            }
        }

        // 删除围栏回调
        @Override
        public void onDeleteFenceCallback(DeleteFenceResponse response) {
            if ("成功".equals(response.getMessage())) {
                Toast.makeText(RailActivity.this, "删除围栏回调成功", Toast.LENGTH_SHORT).show();
            }
        }

        // 围栏列表回调
        @Override
        public void onFenceListCallback(FenceListResponse response) {
            if ("成功".equals(response.getMessage())) {
                Toast.makeText(RailActivity.this, "围栏列表回调成功", Toast.LENGTH_SHORT).show();
            }
            //获取围栏列表响应结果
            int size = response.getSize();//围栏个数
            List<FenceInfo> fenceInfos = response.getFenceInfos();//获取围栏信息列表
            if (size!=0){
                Toast.makeText(RailActivity.this, "共查询处"+size+"条围栏", Toast.LENGTH_SHORT).show();
            }
            for (FenceInfo fenceInfo : fenceInfos) {
                switch (fenceInfo.getFenceShape()) {//判断围栏形状
                    case circle://圆形
                        CircleFence circleFence = fenceInfo.getCircleFence();
                        circleFence.getFenceId();
                        circleFence.getCenter();
                        circleFence.getRadius();
                        circleFence.getDenoise();//去噪精度
                        circleFence.getMonitoredPerson();//监控设备的唯一标识
                        //...获取圆心和半径就可以在地图上画圆形图层
                        break;
                    case polygon://多边形
                        PolygonFence polygonFence = fenceInfo.getPolygonFence();
                        //获取多边形顶点集合
                        List<com.baidu.trace.model.LatLng> polygonVertexes = polygonFence.getVertexes();
                        //...获取顶点坐标可以在地图上画多边形图层
                        break;
                    case polyline://线形
                        PolylineFence polylineFence = fenceInfo.getPolylineFence();
                        //获取线形顶点集合
                        List<com.baidu.trace.model.LatLng> polylineVertexes = polylineFence.getVertexes();
                        //...
                        break;
                    case district:
                        DistrictFence districtFence = fenceInfo.getDistrictFence();
                        districtFence.getDistrict();//获取行政区名称
                        //...注：行政区围栏并能像多边形一样返回定点集合，行政区范围很大，点很多...，
                        //如果想获取行政区的边界点坐标结合，请使用baidumapapi_search_v4_3_1.jar中DistrictSearch类
                        break;
                }

            }
        }

        // 监控状态回调
        @Override
        public void onMonitoredStatusCallback(MonitoredStatusResponse response) {
            if ("成功".equals(response.getMessage())) {
                Toast.makeText(RailActivity.this, "监控状态回调成功", Toast.LENGTH_SHORT).show();
            }
            //查询监控对象状态响应结果
            List<MonitoredStatusInfo> monitoredStatusInfos = response.getMonitoredStatusInfos();
            for (MonitoredStatusInfo monitoredStatusInfo : monitoredStatusInfos) {
                monitoredStatusInfo.getFenceId();
                MonitoredStatus status = monitoredStatusInfo.getMonitoredStatus();//获取状态
                switch (status) {
                    case in:
                        //监控的设备在围栏内
                        Toast.makeText(RailActivity.this, "监控的设备在围栏内", Toast.LENGTH_SHORT).show();
                        break;
                    case out:
                        //监控的设备在围栏外
                        Toast.makeText(RailActivity.this, "监控的设备在围栏外", Toast.LENGTH_SHORT).show();
                        break;
                    case unknown:
                        //监控的设备状态未知
                        Toast.makeText(RailActivity.this, "监控的设备状态未知", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }

        // 指定位置
        @Override
        public void onMonitoredStatusByLocationCallback(MonitoredStatusByLocationResponse response) {
            if ("成功".equals(response.getMessage())) {
                Toast.makeText(RailActivity.this, "指定位置查询成功", Toast.LENGTH_SHORT).show();
            }
        }

        // 历史报警回调
        @Override
        public void onHistoryAlarmCallback(HistoryAlarmResponse response) {
            if ("成功".equals(response.getMessage())) {
                Toast.makeText(RailActivity.this, "历史报警回调成功", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
