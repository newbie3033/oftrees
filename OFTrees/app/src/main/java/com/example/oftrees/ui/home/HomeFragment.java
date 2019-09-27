package com.example.oftrees.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.example.oftrees.CollectData;
import com.example.oftrees.MainActivity;
import com.example.oftrees.R;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int GREEN=0;
    private static final int RED=1;
    private static final int YELLOW=2;


    private HomeViewModel homeViewModel;

    private LocationClient mLocationClient;
    private TextView positionText;
    private MapView mapView;
    private BaiduMap baiduMap;
    private UiSettings mUiSettings;
    private boolean isFirstLocate=true;
    BaiduMap.OnMarkerClickListener listener;

    private double currentLat,currentLng;
    private StringBuilder currentPosition = new StringBuilder();
    private String currentPositionInfos;
    private OkHttpClient okHttpClient;

    private JSONObject _Response;
    private JSONArray _ResponseData;
    private boolean _ResponseSuccess=false;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);

        mLocationClient=new LocationClient(getActivity().getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        SDKInitializer.initialize(getActivity().getApplicationContext());
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mapView= rootView.findViewById(R.id.home_bmapView);
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        //实例化UiSettings类对象,设置百度地图ui
        mUiSettings = baiduMap.getUiSettings();
        //通过设置enable为true或false 选择是否显示指南针
        mUiSettings.setCompassEnabled(true);
        mapView.setZoomControlsPosition(new Point(0,0));

        positionText=(TextView) rootView.findViewById(R.id.home_text);


        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                positionText.setText(s);
            }
        });

        //获得许可
        List<String>permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[]permissions=permissionList.toArray(new String[permissionList.size()]);
            HomeFragment.this.requestPermissions(permissions,1);
        }else{
            requestLocation();
        }
        

        //设置marker点击事件
        BaiduMap.OnMarkerClickListener listener = new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng currentmarker=marker.getPosition();

                Intent intent=new Intent(getActivity(),InfoShowActivity.class);
                intent.putExtra("positionLat",currentmarker.latitude);
                intent.putExtra("positionLng",currentmarker.longitude);
                startActivity(intent);
                return true;
            }
        };
        baiduMap.setOnMarkerClickListener(listener);

        okHttpClient=new OkHttpClient();



        return rootView;
    }

    private void navigateTo(BDLocation location){
        if(isFirstLocate){
            LatLng ll=new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update=MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate=false;
        }
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData=locationBuilder.build();
        baiduMap.setMyLocationData(locationData);

    }

    //开始地理定位
    private  void requestLocation(){
        initLocation();
        mLocationClient.start();
    }
    //初始化位置
    private void initLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setOpenGps(true);
        option.setScanSpan(3000);
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        mLocationClient.setLocOption(option);
    }

    public  class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {

            if(location.getLocType()==BDLocation.TypeGpsLocation||location.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo(location);
            }

            if(currentPosition!=null){
                currentPosition.delete(0,currentPosition.length());
            }
            //存储当前的位置信息
            currentLat=location.getLatitude();
            currentLng=location.getLongitude();
            currentPositionInfos=location.getCountry()+location.getProvince()
                    +location.getCity()
                    +location.getDistrict()
                    +location.getStreet();

            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
            currentPosition.append("经线：").append(location.getLongitude()).append("\n");
            currentPosition.append("国家：").append(location.getCountry()).append("\n");
            currentPosition.append("省：").append(location.getProvince()).append("\n");
            currentPosition.append("市：").append(location.getCity()).append("\n");
            currentPosition.append("区：").append(location.getDistrict()).append("\n");
            currentPosition.append("街道：").append(location.getStreet()).append("\n");
            currentPosition.append("定位方式：");
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("网络");
//                Toast.makeText(getActivity(),"请打开GPS并到空旷的地方获得更精准的定位！",Toast.LENGTH_SHORT).show();
            }
            positionText.setText(currentPosition);
        }
    }

    //刷新地图
    public void refreshMap(){
        requestLocation();
        if(baiduMap.getLocationData()!=null)
        {
            //移动地图定位和中心
            MyLocationData locationData=baiduMap.getLocationData();
            LatLng ll=new LatLng(locationData.latitude,locationData.longitude);
            MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update=MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);

            //更新marker
            removeMarker();
            addMarker(ll.latitude,ll.longitude,GREEN);

            //考虑点聚合
        }
    }

    //显示定位信息
    public void showPositionInfo(){

        if(positionText.getVisibility()==View.GONE){
            positionText.setVisibility(View.VISIBLE);
            mapView.setVisibility(View.GONE);

//            Log.d("HomeFragment", "showPositionInfo:yes ");
        }
        else{
            positionText.setVisibility(View.GONE);
            mapView.setVisibility(View.VISIBLE);

//            Log.d("HomeFragment", "showPositionInfo: no");
        }


    }

    //添加marker
    public void addMarker(double latitude,double longitude,int clr){
        //定义Maker坐标点
        LatLng point = new LatLng(latitude, longitude);
        //构建Marker图标
        BitmapDescriptor bitmap;
        switch (clr){
            case 0:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.location_green);
                break;
            case 1:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.location_red);
                break;
            case 2:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker_bule);
                break;
                default:
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.marker96);
                    break;
        }

        //构建MarkerOption，用于在地图上添加Marker
        MarkerOptions ooA = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //设置掉下动画
        ooA.animateType(MarkerOptions.MarkerAnimateType.jump);

        OverlayOptions option=ooA;

        //在地图上添加Marker，并显示
        baiduMap.addOverlay(option);


    }

    //移除marker
    public void removeMarker(){
        baiduMap.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Log.d(TAG, "onRequestPermissionsResult: 必须同意所有权限才能使用本程序");
                            Toast.makeText(getActivity(),"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(getActivity(),"发生未知错误",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
                default:
        }
    }

    private void _getTrees(){
        JSONObject obj=new JSONObject();
        try {
            obj.put("type","DATA_GET_TREES");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonStr=obj.toString();
        RequestBody body=RequestBody.create(MediaType.parse("application/json;charset:utf-8"),jsonStr);
        Request.Builder request=new Request.Builder()
                .url("http://39.106.89.21/Server/MyMgisServer.php")
                .post(body);
        Call call=okHttpClient.newCall(request.build());
        call.enqueue(callback);

    }

    private Callback callback=new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String str=new String(response.body().bytes(),"utf-8");
            //解析收到的字符串：树的位置信息
            Log.d(TAG, "onResponse: "+str);
            try {
                _Response=new JSONObject(str);
                _ResponseData=_Response.getJSONArray("data");
                _ResponseSuccess=_Response.getBoolean("success");
//                Log.d(TAG, "onResponse: data:"+_ResponseData.length()+_ResponseData.getJSONObject(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (_ResponseSuccess)
            {
                //显示
                Message message=new Message();
                //0:显示树
                message.what=0;
                handler.sendMessage(message);
            }else {
//                Toast.makeText(getActivity(),"暂无树木!",Toast.LENGTH_SHORT).show();
            }

        }
    };

    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            //更新UI
            JSONObject tree;
            switch (msg.what){
                //显示树
                case 0:
                    for(int i=0;i<_ResponseData.length();i++)
                    {
                        try {
                            tree=_ResponseData.getJSONObject(i);
                            addMarker(
                                    Double.parseDouble(tree.getString("t_positionlat")),
                                    Double.parseDouble(tree.getString("t_positionlng")),
                                    RED);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }


            }
        }
    };



    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        final FloatingActionMenu fab = (FloatingActionMenu)getView().findViewById(R.id.fab);

        fab.setClosedOnTouchOutside(false);

        //设置悬浮按钮的绑定事件
        FloatingActionButton fB_setCenter=getActivity().findViewById(R.id.fButton_setCenter);
        fB_setCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开出新的子线程，连接服务器获取树木点

                _getTrees();
                //刷新地图
                refreshMap();
            }
        });
        FloatingActionButton fB_showInfo=getActivity().findViewById(R.id.fButton_showInfo);
        fB_showInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPositionInfo();
            }
        });


        final FloatingActionMenu floatingActionMenu1=getActivity().findViewById(R.id.fButton_changeMap);

        FloatingActionButton fB_mapNormal=getActivity().findViewById(R.id.fButton_mapNormal);
        fB_mapNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换为普通地图
                baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                //切换按键图标
                floatingActionMenu1.close(true);

            }
        });
        FloatingActionButton fB_mapSate=getActivity().findViewById(R.id.fButton_mapSate);
        fB_mapSate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换为卫星地图
                baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                //切换按键图标
                floatingActionMenu1.close(true);
            }
        });

        FloatingActionButton fB_collectData=getActivity().findViewById(R.id.fButton_collectData);

        //根据用户权限，隐藏相关功能
        boolean authority=getActivity().getIntent().getBooleanExtra("authority",false);
        Log.d(TAG, "onResume: 1111111"+authority);
        if(fB_collectData!=null){
            if(!authority) {
                //隐藏采集数据功能
                fab.removeMenuButton(fB_collectData);
            }
            else{
                fB_collectData.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //调用采集数据的活动
                        Intent intent=new Intent(getActivity(), CollectData.class);
                        intent.putExtra("positionLat",currentLat);
                        intent.putExtra("positionLng",currentLng);
                        intent.putExtra("infos",currentPositionInfos);
                        startActivityForResult(intent,1);
                    }
                });
            }
        }


        //显示已记录的点（由RecordsFragment跳转
        if(getArguments()!=null)
        {
            ArrayList<String> positions=new ArrayList<>();
            positions=getArguments().getStringArrayList("positions");
            String t,tlt,tlg;
            for(int i=0;i<positions.size();i++){
                t=positions.get(i);
                tlt=t.substring(0,t.indexOf(","));
                tlg=t.substring(t.indexOf(",")+1);
                double plat=Double.parseDouble(tlt);
                double plng=Double.parseDouble(tlg);
                addMarker(plat,plng,2);
            }
        }


    }
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

}