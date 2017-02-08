package com.moliying.mly_tracklinedemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final int PLAYBACK_OVER = 1;
    private static final String TAG = "MainActivity";
    private MapView mMapView;
    private BaiduMap baiduMap;
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;
    private boolean flag = true;
    private double currentLat;//当前的纬度
    private double currentLng;//当前的经度
    private String currentAddr;//当前位置的地址信息
    private DatabaseAdapter dbAdapter;
    private int currentTrackLineID;//当前创建的线路 ID
    private boolean isTracking = false;//添加线路跟踪模拟线程的标记
    private GeoCoder geoCoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.bmapView);
        dbAdapter = new DatabaseAdapter(this);
        initBaiduMap();
        System.out.println("onCreate.");
    }

    //初始化地图参数
    private void initBaiduMap() {
        baiduMap = mMapView.getMap();
        baiduMap.setMyLocationEnabled(true);//打开定位图层
        mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
        myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener); // 注册监听函数

        //相关参数配置
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
        mLocationClient.setLocOption(option);
        mLocationClient.start();// 启动SDK定位
        mLocationClient.requestLocation();// 发起定位请求

        //用于转换地理编码的监听器
        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有检索到结果
                } else {
                    // 获取地理编码结果
                    // System.out.println(result.getAddress());
                    currentAddr = result.getAddress();
                    //更新线路的结束位置
                    dbAdapter.updateEndLoc(currentAddr, currentTrackLineID);
                }
            }

            @Override
            public void onGetGeoCodeResult(GeoCodeResult arg0) {

            }
        });
    }

    //百度地图定位的事件回调监听
    private class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.i(TAG, "onReceiveLocation: "+bdLocation.getAddrStr());
            if(bdLocation!=null && flag){
                flag = false;
                currentLat = bdLocation.getLatitude();//当前的纬度
                currentLng = bdLocation.getLongitude();//当前的经度
                currentAddr = bdLocation.getAddrStr();//当前位置的地址

                //构造我的当前位置信息
                MyLocationData.Builder builder = new MyLocationData.Builder();
                builder.latitude(bdLocation.getLatitude());// 设置纬度
                builder.longitude(bdLocation.getLongitude());// 设置经度
                builder.accuracy(bdLocation.getRadius());// 设置精度（半径）
                builder.direction(bdLocation.getDirection());// 设置方向
                builder.speed(bdLocation.getSpeed());// 设置速度
                MyLocationData locationData = builder.build();
                //把我的位置信息设置到地图上
                baiduMap.setMyLocationData(locationData);
                //配置我的位置
                LatLng latlng = new LatLng(currentLat, currentLng);
                //设置我的位置的配置信息: 模式:跟随模式,是否要显示方向,图标
                baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                        MyLocationConfiguration.LocationMode.FOLLOWING,
                        true, null));
                // 设置我的位置为地图的中心点(缩放级别为 3-20)
                baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(
                        latlng, 18));
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //功能菜单项
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mylocation:
                mylocation();//我的位置
                break;
            case R.id.start_track:
                startTrack();//开始跟踪
                break;
            case R.id.end_track:
                endTrack();//结束跟踪
                break;
            case R.id.track_back:
                trackBack();//跟踪回放
                break;

            default:
                break;
        }
        return true;
    }

    /**
     * 跟踪回放
     */
    AlertDialog dialog = null;
    private void trackBack() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("跟踪路线列表");
        View view = getLayoutInflater().inflate(
                R.layout.track_line_playback_dialog, null);
        ListView playbackListView = (ListView) view
                .findViewById(R.id.listView1_play_back);

        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        //把数据库中所有的线路查出来
        ArrayList<Track> tracks = dbAdapter.getTracks();
        HashMap<String, String> map = null;
        Track t = null;
        int size = tracks.size();
        for (int i = 0; i < size; i++) {
            map = new HashMap<String, String>();
            t = tracks.get(i);
            map.put("id", String.valueOf(t.getId()));
            map.put("trackName_createDate",
                    t.getTrack_name() + "--" + t.getCreate_date());
            map.put("startEndLoc",
                    "从[" + t.getStart_loc() + "]到[" + t.getEnd_loc() + "]");
            data.add(map);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.playback_item, new String[] { "id",
                "trackName_createDate", "startEndLoc" }, new int[] {
                R.id.textView1_id, R.id.textView2_trackname_createdate,
                R.id.textView3_startEndLoc });
        playbackListView.setAdapter(adapter);
        playbackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TextView tv_id = (TextView) view.findViewById(R.id.textView1_id);
                int _id = Integer.parseInt(tv_id.getText().toString());
                baiduMap.clear();
                new Thread(new TrackPlaybackThread(_id)).start();
                dialog.dismiss();
            }
        });
        builder.setView(view);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    //结束踪踪
    private void endTrack() {
        isTracking = false;// 结束线程
        Toast.makeText(MainActivity.this, "跟踪结束...", Toast.LENGTH_SHORT).show();
        //转换地理编码,把最后的一个经纬度转换成地址
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(
                currentLat, currentLng)));
    }

    //用于存储两个相邻的经纬度点，再画线
    private ArrayList<LatLng> list = new ArrayList<LatLng>();
    //创建一条线路跟踪
    private void createTrack(String trackName) {
        //String trackName_temp = trackName;
        Track track = new Track();
        track.setTrack_name(trackName);
        track.setCreate_date(DateUtils.toDate(new Date()));
        track.setStart_loc(currentAddr);

        currentTrackLineID = dbAdapter.addTrack(track);
        dbAdapter.addTrackDetail(currentTrackLineID, currentLat, currentLng);
        baiduMap.clear();
//        addOverlay();
        list.add(new LatLng(currentLat, currentLng));
        isTracking = true;//线程模拟的标记
        System.out.println(list);
        new Thread(new TrackThread()).start();
    }
    //开始跟踪功能
    private void startTrack() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("线路跟踪");
        builder.setCancelable(true);
        final View view = getLayoutInflater().inflate(
                R.layout.add_track_line_dialog, null);
        builder.setView(view);
        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText et_track_name = (EditText) view
                        .findViewById(R.id.editText1_track_name);
                String trackName = et_track_name.getText().toString();
                System.out.println(trackName);
                createTrack(trackName);//创建线路跟踪
                Toast.makeText(MainActivity.this, "跟踪开始...", Toast.LENGTH_SHORT)
                        .show();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 模拟跟踪的线程
     *
     * @author Administrator
     *
     */
    class TrackThread implements Runnable {

        @Override
        public void run() {
            while (isTracking) {
                getLocation();
                dbAdapter.addTrackDetail(currentTrackLineID, currentLat,
                        currentLng);
//                addOverlay();
                list.add(new LatLng(currentLat, currentLng));
                drawLine();
                System.out.println("drawLine");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //在两个点之间画线
    private void drawLine() {
        OverlayOptions lineOptions = new PolylineOptions().points(list).color(
                0xFFFF0000);
        baiduMap.addOverlay(lineOptions);
        list.remove(0);
    }

    /**
     * 跟踪回放的线程
     */
    class TrackPlaybackThread implements Runnable{
        private int id;
        public TrackPlaybackThread(int id){
            this.id = id;
        }
        @Override
        public void run() {
            //查询id 对应的路线的所有坐标点
            ArrayList<TrackDetail> trackDetails = dbAdapter.getTrackDetails(id);
            TrackDetail td = null;
            list.clear();
            currentLat = trackDetails.get(0).getLat();
            currentLng = trackDetails.get(0).getLng();
            list.add(new LatLng(currentLat, currentLng));
//            addOverlay();
            int size = trackDetails.size();
            for (int i = 1; i < size; i++) {
                td = trackDetails.get(i);
                currentLat = td.getLat();
                currentLng = td.getLng();
                list.add(new LatLng(currentLat, currentLng));
//                addOverlay();
                drawLine();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            handler.sendEmptyMessage(PLAYBACK_OVER);

            //在子线程中访问 UI 组件时，使用以下方法把一个runnable对象加入到 UI线程中执行
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "sss", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case PLAYBACK_OVER:
                    Toast.makeText(MainActivity.this, "回放结束", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        };
    };


    /**
     * 模拟位置
     */
    private void getLocation() {
        currentLat = currentLat + Math.random() / 1000;
        currentLng = currentLng + Math.random() / 1000;
    }

    //我的位置功能
    private void mylocation() {
        Toast.makeText(MainActivity.this, "正在定位中...", Toast.LENGTH_SHORT)
                .show();
        flag = true;
        baiduMap.clear();//清除地图上自定义的图层
        baiduMap.setMyLocationEnabled(true);//启用我的位置图层
        mLocationClient.requestLocation();// 发起定位请求
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
}
