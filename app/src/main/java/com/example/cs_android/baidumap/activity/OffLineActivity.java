package com.example.cs_android.baidumap.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.example.cs_android.baidumap.R;

import java.util.ArrayList;

/**
 * 离线地图下载使用页面
 */

public class OffLineActivity extends AppCompatActivity implements MKOfflineMapListener {

    private MKOfflineMap mOffline;
    private EditText mEtCityName;
    int cityid;
    private EditText cityNameView;
    private ArrayList<MKOLUpdateElement> localMapList;
    private LocalMapAdapter lAdapter;
    private LinearLayout cl;
    private LinearLayout lm;
    private TextView ratio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_off_line);
        mOffline = new MKOfflineMap();
        mOffline.init(this);

        mEtCityName = (EditText) findViewById(R.id.city);
        cl = (LinearLayout) findViewById(R.id.citylist_layout);
        lm = (LinearLayout) findViewById(R.id.localmap_layout);

        initView();
    }

    private void initView() {
        cityNameView = (EditText) findViewById(R.id.city);

        ListView hotCityList = (ListView) findViewById(R.id.hotcitylist);
        ArrayList<String> hotCities = new ArrayList<String>();
        final ArrayList<String> hotCityNames = new ArrayList<String>();
        // 获取热闹城市列表
        ArrayList<MKOLSearchRecord> records1 = mOffline.getHotCityList();
        if (records1 != null) {
            for (MKOLSearchRecord r : records1) {
                //V4.5.0起，保证数据不溢出，使用long型保存数据包大小结果
                hotCities.add(r.cityName + "(" + r.cityID + ")" + "   --"
                        + this.formatDataSize(r.dataSize));
                hotCityNames.add(r.cityName);
            }
        }
        ListAdapter hAdapter = (ListAdapter) new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, hotCities);
        hotCityList.setAdapter(hAdapter);
        hotCityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cityNameView.setText(hotCityNames.get(i));
                ArrayList<MKOLSearchRecord> records = mOffline.searchCity(hotCityNames.get(i));
                if (records == null || records.size() != 1) {
                    return;
                }
                cityid = records.get(0).cityID;
                Toast.makeText(OffLineActivity.this, "获取城市ID成功：" + cityid + "点击开始进行瞎子啊", Toast.LENGTH_SHORT).show();
            }
        });
        ListView allCityList = (ListView) findViewById(R.id.allcitylist);
        // 获取所有支持离线地图的城市
        ArrayList<String> allCities = new ArrayList<String>();
        final ArrayList<String> allCityNames = new ArrayList<String>();
        ArrayList<MKOLSearchRecord> records2 = mOffline.getOfflineCityList();
        if (records1 != null) {
            for (MKOLSearchRecord r : records2) {
                //V4.5.0起，保证数据不溢出，使用long型保存数据包大小结果
                allCities.add(r.cityName + "(" + r.cityID + ")" + "   --"
                        + this.formatDataSize(r.dataSize));
                allCityNames.add(r.cityName);
            }
        }
        ListAdapter aAdapter = (ListAdapter) new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, allCities);
        allCityList.setAdapter(aAdapter);
        allCityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cityNameView.setText(allCityNames.get(i));
                ArrayList<MKOLSearchRecord> records = mOffline.searchCity(allCityNames.get(i));
                if (records == null || records.size() != 1) {
                    return;
                }
                cityid = records.get(0).cityID;
                Toast.makeText(OffLineActivity.this, "获取城市ID成功：" + cityid + "点击开始进行瞎子啊", Toast.LENGTH_SHORT).show();
            }
        });
        LinearLayout cl = (LinearLayout) findViewById(R.id.citylist_layout);
        LinearLayout lm = (LinearLayout) findViewById(R.id.localmap_layout);
        lm.setVisibility(View.GONE);
        cl.setVisibility(View.VISIBLE);

        // 获取已下过的离线地图信息
        localMapList = mOffline.getAllUpdateInfo();
        if (localMapList == null) {
            localMapList = new ArrayList<MKOLUpdateElement>();
        }

        ListView localMapListView = (ListView) findViewById(R.id.localmaplist);
        lAdapter = new LocalMapAdapter();
        localMapListView.setAdapter(lAdapter);

    }

    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                MKOLUpdateElement update = mOffline.getUpdateInfo(state);
                // 处理下载进度更新提示
                if (update != null) {
                    ratio.setText(String.format("%s : %d%%", update.cityName, update.ratio));
                    updateView();
                }
            }
            break;
            case MKOfflineMap.TYPE_NEW_OFFLINE:
                // 有新离线地图安装
                Log.d("OfflineDemo", String.format("add offlinemap num:%d", state));
                break;
            case MKOfflineMap.TYPE_VER_UPDATE:
                // 版本更新提示
                // MKOLUpdateElement e = mOffline.getUpdateInfo(state);

                break;
            default:
                break;
        }
    }

    /**
     * 离线地图管理列表适配器
     */
    public class LocalMapAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return localMapList.size();
        }

        @Override
        public Object getItem(int index) {
            return localMapList.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }

        @Override
        public View getView(int index, View view, ViewGroup arg2) {
            MKOLUpdateElement e = (MKOLUpdateElement) getItem(index);
            view = View.inflate(OffLineActivity.this, R.layout.offline_localmap_list, null);
            initViewItem(view, e);
            return view;
        }

        void initViewItem(View view, final MKOLUpdateElement e) {
            Button remove = view.findViewById(R.id.remove);
            TextView title = view.findViewById(R.id.title);
            TextView update = view.findViewById(R.id.update);
            ratio = view.findViewById(R.id.ratio);
            ratio.setText(e.ratio + "%");
            title.setText(e.cityName);
            if (e.update) {
                update.setText("可更新");
            } else {
                update.setText("最新");
            }
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mOffline.remove(e.cityID);
                    updateView();
                }
            });
        }

    }

    /**
     * 更新状态显示
     */
    public void updateView() {
        localMapList = mOffline.getAllUpdateInfo();
        if (localMapList == null) {
            localMapList = new ArrayList<MKOLUpdateElement>();
        }
        lAdapter.notifyDataSetChanged();
    }


    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.search:
                ArrayList<MKOLSearchRecord> records = mOffline.searchCity(mEtCityName.getText().toString());
                if (records == null || records.size() != 1) {
                    return;
                }
                Toast.makeText(this, "搜索成功，城市ID为：" + String.valueOf(records.get(0).cityID + "可以点击下载按钮进行下载"), Toast.LENGTH_SHORT).show();
                cityid = records.get(0).cityID;
                break;
            case R.id.start:
                mOffline.start(cityid);
                Toast.makeText(this, "开始下载离线地图. cityid: " + cityid, Toast.LENGTH_SHORT)
                        .show();
                updateView();
                break;
            case R.id.stop:
                mOffline.pause(cityid);
                updateView();
                break;
            case R.id.del:
                mOffline.remove(cityid);
                updateView();
                break;
            case R.id.clButton:
                lm.setVisibility(View.GONE);
                cl.setVisibility(View.VISIBLE);
                break;
            case R.id.localButton:
                lm.setVisibility(View.VISIBLE);
                cl.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * V4.5.0起，保证数据不溢出，使用long型保存数据包大小结果
     */
    public String formatDataSize(long size) {
        String ret = "";
        if (size < (1024 * 1024)) {
            ret = String.format("%dK", size / 1024);
        } else {
            ret = String.format("%.1fM", size / (1024 * 1024.0));
        }
        return ret;
    }

    @Override
    protected void onPause() {
        MKOLUpdateElement temp = mOffline.getUpdateInfo(cityid);
        if (temp != null && temp.status == MKOLUpdateElement.DOWNLOADING) {
            mOffline.pause(cityid);
        }
        super.onPause();
    }
}
