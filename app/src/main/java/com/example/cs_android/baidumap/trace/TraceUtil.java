package com.example.cs_android.baidumap.trace;

import android.graphics.Color;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.trace.model.SortType;
import com.example.cs_android.baidumap.R;

import java.util.List;

/**
 * Created by cs_android on 2018/1/15.
 */

public class TraceUtil {
    private Overlay polylineOverlay;
    private BaiduMap mBaiduMap;
    private MapStatus mapStatus;

    /**
     * 绘制历史轨迹
     */
    public void drawHistoryTrack(BaiduMap baiduMap,List<LatLng> points, SortType sortType) {
        mBaiduMap=baiduMap;
        // 绘制新覆盖物前，清空之前的覆盖物
        baiduMap.clear();
        if (points == null || points.size() == 0) {
            if (null != polylineOverlay) {
                polylineOverlay.remove();
                polylineOverlay = null;
            }
            return;
        }

        if (points.size() == 1) {
            OverlayOptions startOptions = new MarkerOptions().position(points.get(0)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_start))
                    .zIndex(9).draggable(true);
            mBaiduMap.addOverlay(startOptions);
            animateMapStatus(points.get(0), 18.0f);
            return;
        }


        LatLng startPoint;
        LatLng endPoint;
        if (sortType == SortType.asc) {
            startPoint = points.get(0);
            endPoint = points.get(points.size() - 1);
        } else {
            startPoint = points.get(points.size() - 1);
            endPoint = points.get(0);
        }

        // 添加起点图标
        OverlayOptions startOptions = new MarkerOptions()
                .position(startPoint).icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_start))
                .zIndex(9).draggable(true);
        // 添加终点图标
        OverlayOptions endOptions = new MarkerOptions().position(endPoint)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_end)).zIndex(9).draggable(true);

        // 添加路线（轨迹）
        OverlayOptions polylineOptions = new PolylineOptions().width(10)
                .color(Color.BLUE).points(points);

        mBaiduMap.addOverlay(startOptions);
        mBaiduMap.addOverlay(endOptions);
        polylineOverlay = mBaiduMap.addOverlay(polylineOptions);

        OverlayOptions markerOptions =
                new MarkerOptions().flat(true).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_point))
                        .position(points.get(points.size() - 1))
                        .rotate((float) getAngle(points.get(0), points.get(1)));
        mBaiduMap.addOverlay(markerOptions);

        animateMapStatus(points);
    }

    public void animateMapStatus(LatLng point, float zoom) {
        MapStatus.Builder builder = new MapStatus.Builder();
        mapStatus = builder.target(point).zoom(zoom).build();
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
    }

    public void animateMapStatus(List<LatLng> points) {
        if (null == points || points.isEmpty()) {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        MapStatusUpdate msUpdate = MapStatusUpdateFactory.newLatLngBounds(builder.build());
        mBaiduMap.animateMapStatus(msUpdate);
    }

    /**
     * 根据两点算取图标转的角度
     */
    public double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        return 180 * (radio / Math.PI) + deltAngle - 90;
    }

    /**
     * 算斜率
     */
    public static double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        return (toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude);
    }

    /**
     * 将轨迹坐标对象转换为地图坐标对象
     *
     * @param traceLatLng
     * @return
     */
    public static LatLng convertTrace2Map(com.baidu.trace.model.LatLng traceLatLng) {
        return new LatLng(traceLatLng.latitude, traceLatLng.longitude);
    }

    public static boolean isZeroPoint(double latitude, double longitude) {
        return isEqualToZero(latitude) && isEqualToZero(longitude);
    }

    /**
     * 校验double数值是否为0
     *
     * @param value
     * @return
     */
    public static boolean isEqualToZero(double value) {
        return Math.abs(value - 0.0) < 0.01 ? true : false;
    }
}
