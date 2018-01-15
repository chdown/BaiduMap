package com.example.cs_android.baidumap.activity;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.example.cs_android.baidumap.R;
import com.example.cs_android.baidumap.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * POI搜索页
 */

public class PoiActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mEtCity;
    private AutoCompleteTextView mActvSearchkey;
    private Button mBtnSearch, mBtnSearchNearby, mBtnSearchBound;
    private PoiSearch mPoiSearch;
    private BaiduMap mBaiduMap;
    private List<LatLng> latLngs = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_poi);
        TextureMapView mMapView = (TextureMapView) findViewById(R.id.texture_map);
        mBaiduMap = mMapView.getMap();

        initDate();

        //创建POI检索实例
        mPoiSearch = PoiSearch.newInstance();
        //创建POI检索监听者；

        //设置POI检索监听者；
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
    }

    private void initDate() {
        mEtCity = (EditText) findViewById(R.id.city);

        mActvSearchkey = (AutoCompleteTextView) findViewById(R.id.searchkey);
        mBtnSearch = (Button) findViewById(R.id.search);
        mBtnSearchNearby = (Button) findViewById(R.id.searchNearby);
        mBtnSearchBound = (Button) findViewById(R.id.searchBound);

        mBtnSearch.setOnClickListener(this);
        mBtnSearchNearby.setOnClickListener(this);
        mBtnSearchBound.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search:
                //发起检索请求；
                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city(mEtCity.getText().toString().trim())
                        .keyword(mActvSearchkey.getText().toString().trim())
                        .pageNum(0));
                break;
            case R.id.searchNearby:
                //发起检索请求；
                mPoiSearch.searchNearby((new PoiNearbySearchOption())
                        .location(new LatLng(39.915599,116.403694))
                        .sortType(PoiSortType.distance_from_near_to_far)
                        .keyword(mActvSearchkey.getText().toString().trim())
                        .radius(2000)
                        .pageNum(0)
                        );
                break;
            case R.id.searchBound:
                LatLng southwest = new LatLng( 39.92235, 116.380338 );
                LatLng northeast = new LatLng( 39.947246, 116.414977);
                LatLngBounds searchbound = new LatLngBounds.Builder()
                        .include(southwest)
                        .include(northeast)
                        .build();
                mPoiSearch.searchInBound(new PoiBoundSearchOption()
                        .bound(searchbound)
                        .keyword(mActvSearchkey.getText().toString().trim()));
                break;
        }
    }

    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {

        public void onGetPoiResult(PoiResult result) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                //详情检索失败
                // result.error请参考SearchResult.ERRORNO
                Toast.makeText(PoiActivity.this, "未搜索到POI数据", Toast.LENGTH_SHORT).show();
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                //获取POI检索结果
                Toast.makeText(PoiActivity.this, "已搜索到POI数据", Toast.LENGTH_SHORT).show();
                mBaiduMap.clear();
                List<PoiInfo> allPoi = result.getAllPoi();
                for (int i = 0; i < allPoi.size(); i++) {
                    Resources res = getResources();
                    Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.icon_gcoding);
                    Bitmap bitmap1 = ImageUtils.drawTextToCenter(PoiActivity.this, bitmap, "" + i, 20, Color.BLACK);

                    OverlayOptions options = new MarkerOptions()
                            .position(result.getAllPoi().get(i).location)
                            .title(result.getAllPoi().get(i).name + ":" + result.getAllPoi().get(i).address)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmap1));
                    mBaiduMap.addOverlay(options);


                }
                mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Toast.makeText(PoiActivity.this, marker.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

                // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
                String strInfo = "在";
                for (CityInfo cityInfo : result.getSuggestCityList()) {
                    strInfo += cityInfo.city;
                    strInfo += ",";
                }
                strInfo += "找到结果";
                Toast.makeText(PoiActivity.this, strInfo, Toast.LENGTH_LONG)
                        .show();
            }
        }

        public void onGetPoiDetailResult(PoiDetailResult result) {
            //获取Place详情页检索结果
            if (result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(PoiActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(PoiActivity.this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT)
                        .show();
            }
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

    @Override
    protected void onDestroy() {
        //释放POI检索实例；
        mPoiSearch.destroy();
        super.onDestroy();
    }
}
