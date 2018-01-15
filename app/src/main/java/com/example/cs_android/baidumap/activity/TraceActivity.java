package com.example.cs_android.baidumap.activity;


import android.app.Notification;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.fence.AlarmPoint;
import com.baidu.trace.api.fence.FenceAlarmPushInfo;
import com.baidu.trace.api.fence.MonitoredAction;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;
import com.baidu.trace.model.SortType;
import com.baidu.trace.model.StatusCodes;
import com.example.cs_android.baidumap.R;
import com.example.cs_android.baidumap.trace.CommonUtil;
import com.example.cs_android.baidumap.trace.TraceUtil;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.tag;

/**
 * 轨迹上传显示页
 */

public class TraceActivity extends AppCompatActivity {

    // 轨迹服务ID
    long serviceId = 158468;
    // 设备标识
    String entityName = "myTrace";
    // 是否需要对象存储服务，默认为：false，关闭对象存储服务。
    // 支持随轨迹上传图像等对象数据，若需使用此功能，该参数需设为 true，且需导入bos-android-sdk-1.0.2.jar。
    boolean isNeedObjectStorage = false;
    private TextureMapView mMapView;
    private BaiduMap mBaiduMap;
    private LBSTraceClient mTraceClient;
    private Trace mTrace;
    // 定位周期(单位:秒)
    private int gatherInterval = 5;
    // 打包回传周期(单位:秒)
    private int packInterval = 10;
    private List<LatLng> trackPoints = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_trace);
        mMapView = (TextureMapView) findViewById(R.id.texture_map);
        mBaiduMap = mMapView.getMap();


        mTrace = new Trace(serviceId, entityName, isNeedObjectStorage); // 初始化轨迹服务
        // 初始化轨迹服务客户端
        mTraceClient = new LBSTraceClient(getApplicationContext());

        // 设置定位和打包周期
        mTraceClient.setInterval(gatherInterval, packInterval);

    }

    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_server:
                mTraceClient.startTrace(mTrace, mTraceListener);
                break;
            case R.id.btn_stop_server:
                mTraceClient.stopTrace(mTrace, mTraceListener);
                break;
            case R.id.btn_start_gather:
                mTraceClient.startGather(mTraceListener);
                break;
            case R.id.btn_stop_gather:
                mTraceClient.stopTrace(mTrace, mTraceListener);
                break;
            case R.id.btn_query_trace:
                // 创建历史轨迹请求实例
                HistoryTrackRequest historyTrackRequest = new HistoryTrackRequest(tag, serviceId, entityName);

                //设置轨迹查询起止时间
                // 开始时间(单位：秒)
                long startTime = System.currentTimeMillis() / 1000 - 12 * 60 * 60;
                // 结束时间(单位：秒)
                long endTime = System.currentTimeMillis() / 1000;
                long timeMillis = System.currentTimeMillis();
                // 设置开始时间
                historyTrackRequest.setStartTime(startTime);
                // 设置结束时间
                historyTrackRequest.setEndTime(endTime);

                historyTrackRequest.setPageSize(1000);
                historyTrackRequest.setPageIndex(1);

                // 查询历史轨迹
                mTraceClient.queryHistoryTrack(historyTrackRequest, mHistoryListener);
                break;
        }
    }

    OnTrackListener mHistoryListener = new OnTrackListener() {
        // 历史轨迹回调
        @Override
        public void onHistoryTrackCallback(HistoryTrackResponse response) {
            int total = response.getTotal();
            if (StatusCodes.SUCCESS != response.getStatus()) {
                Toast.makeText(TraceActivity.this, "结果为：" + response.getMessage(), Toast.LENGTH_SHORT).show();
            } else if (0 == total) {
                Toast.makeText(TraceActivity.this, "未查询到历史轨迹", Toast.LENGTH_SHORT).show();
            } else {
                List<TrackPoint> points = response.getTrackPoints();
                if (null != points) {
                    for (TrackPoint trackPoint : points) {
                        if (!TraceUtil.isZeroPoint(trackPoint.getLocation().getLatitude(),
                                trackPoint.getLocation().getLongitude())) {
                            trackPoints.add(TraceUtil.convertTrace2Map(trackPoint.getLocation()));
                        }
                    }
                }
            }
            TraceUtil traceUtil=new TraceUtil();
            traceUtil.drawHistoryTrack(mBaiduMap,trackPoints, SortType.asc);
        }
    };


    OnTraceListener mTraceListener = new OnTraceListener() {

        public int notifyId = 0;

        @Override
        public void onBindServiceCallback(int i, String s) {
            if (i == StatusCodes.SUCCESS) {
                Toast.makeText(TraceActivity.this, "绑定服务成功:" + "\r\n消息编码:" + i +
                        "\r\n消息内容:" + s, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onStartTraceCallback(int i, String s) {
            Toast.makeText(TraceActivity.this, "绑定服务成功:" + "\r\n消息编码:" + i +
                    "\r\n消息内容:" + s, Toast.LENGTH_SHORT).show();
            if (StatusCodes.SUCCESS == i || StatusCodes.START_TRACE_NETWORK_CONNECT_FAILED <= i) {
                Toast.makeText(TraceActivity.this, "绑定服务成功:" + "\r\n消息编码:" + i +
                        "\r\n消息内容:" + s, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onStopTraceCallback(int i, String s) {
            if (StatusCodes.SUCCESS == i || StatusCodes.CACHE_TRACK_NOT_UPLOAD == i) {
                Toast.makeText(TraceActivity.this, "停止服务成功:" + "\r\n消息编码:" + i +
                        "\r\n消息内容:" + s, Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onStartGatherCallback(int i, String s) {
            if (StatusCodes.SUCCESS == i || StatusCodes.GATHER_STARTED == i) {
                Toast.makeText(TraceActivity.this, "开启采集成功:" + "\r\n消息编码:" + i +
                        "\r\n消息内容:" + s, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onStopGatherCallback(int i, String s) {
            if (StatusCodes.SUCCESS == i || StatusCodes.GATHER_STOPPED == i) {
                Toast.makeText(TraceActivity.this, "停止采集成功:" + "\r\n消息编码:" + i +
                        "\r\n消息内容:" + s, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onPushCallback(byte messageType, PushMessage pushMessage) {
            if (messageType < 0x03 || messageType > 0x04) {
                Toast.makeText(TraceActivity.this, pushMessage.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            FenceAlarmPushInfo alarmPushInfo = pushMessage.getFenceAlarmPushInfo();
            if (null == alarmPushInfo) {
                Toast.makeText(TraceActivity.this, "onPushCallback:" + "\r\n状态码:" + messageType +
                        "\r\n消息内容:" + pushMessage, Toast.LENGTH_SHORT).show();
                return;
            }
            StringBuffer alarmInfo = new StringBuffer();
            alarmInfo.append("您于")
                    .append(CommonUtil.getHMS(alarmPushInfo.getCurrentPoint().getLocTime() * 1000))
                    .append(alarmPushInfo.getMonitoredAction() == MonitoredAction.enter ? "进入" : "离开")
                    .append(messageType == 0x03 ? "云端" : "本地")
                    .append("围栏：").append(alarmPushInfo.getFenceName());

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                Notification notification = new Notification.Builder(TraceActivity.this)
                        .setContentTitle("百度鹰眼报警推送")
                        .setContentText(alarmInfo.toString())
                        .setWhen(System.currentTimeMillis()).build();
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(notifyId++, notification);
            }

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

}
