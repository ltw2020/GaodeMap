 package com.example.lbstest;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.NaviPara;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextWatcher, RouteSearch.OnRouteSearchListener,
        AMap.OnMapClickListener, AMap.OnMarkerClickListener,View.OnClickListener {

    public static double mLatitude;
    public static double mLongitude;
    //侧滑菜单
    public DrawerLayout drawerLayout;
    //地图
    MapView mMapView = null;
    AMap aMap = null;
    //声明AMapLocationClient类对象（定位）
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象，用来设置发起定位的模式和相关参数。
    public AMapLocationClientOption mLocationOption = null;
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;
    //侧滑菜单按钮
    ImageView imageView;
    //四种规划路线
    Button btn1;
    Button btn2;
    Button btn3;
    Button btn4;
    //到这去
    Button go_btn;
    //规划路线
    private TextView content;
    private static final String TAG = "MainAty";
    //
    private EditText edit;
    private ListView lv;
    private SearchAdapter mAdapter;
    //光标
    public InputMethodManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        //四种路线
        findViewById(R.id.route_bus).setOnClickListener(this);
        findViewById(R.id.route_driver).setOnClickListener(this);
        findViewById(R.id.route_walk).setOnClickListener(this);
        findViewById(R.id.route_ride).setOnClickListener(this);
        content = (TextView) this.findViewById(R.id.route_content);
        //默认公交
        //setRouteCarListener(1);

        go_btn=findViewById(R.id.go);
        go_btn.setOnClickListener(this);

        manager=(InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        //地图模式
        btn1=findViewById(R.id.satellite);
        btn1.setOnClickListener(this);
        btn2=findViewById(R.id.night);
        btn2.setOnClickListener(this);
        btn3=findViewById(R.id.navigation);
        btn3.setOnClickListener(this);
        btn4=findViewById(R.id.common);
        btn4.setOnClickListener(this);

        //初始化amap
        if (aMap == null) {
            aMap = mMapView.getMap();//得到地图
        }

        lv = findViewById(R.id.search_list);
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //定位大头针
                LatLng latLng = new LatLng(AddressBean.latitude,AddressBean.longitude);
                final Marker marker = aMap.addMarker(new MarkerOptions().position(latLng));
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(AddressBean.latitude,AddressBean.longitude)));

                TextView textView = view.findViewById(R.id.item_title);
                edit.setText(textView.getText().toString());
                edit.setSelection(textView.getText().toString().length());//将光标移至文字末尾

                /*String sth = "总共有："+parent.getCount()+"行数据"
                        +"\n该item里的Name值："+textView.getText().toString()
                        +"\n当前点击第："+position+"行"+AddressBean.latitude;
                Toast.makeText(MainActivity.this, sth,Toast.LENGTH_SHORT).show();*/
            }
        });

        //小蓝点
        setUpMap();
        //初始化定位
        location();

        drawerLayout= findViewById(R.id.drawerLayout);
        //给按钮添加一个监听器
        imageView = findViewById(R.id.top_view_left_iv);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //打开侧滑菜单
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        //搜索框
        initView();
    }

    private void location() {
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(true);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
        //不自动返回
    }


    private void initView() {
        edit = (EditText) findViewById(R.id.search_edit);
        edit.setImeOptions(EditorInfo.IME_ACTION_SEARCH);//回车键搜索
        edit.setInputType(EditorInfo.TYPE_CLASS_TEXT);//输入为普通文本
        lv = (ListView) findViewById(R.id.search_list);
        edit.addTextChangedListener(this);
        mAdapter = new SearchAdapter(this);
        lv.setAdapter(mAdapter);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);//软键盘不自动弹出
        //回车键
        edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    lv.setVisibility(View.GONE);
                    if (manager != null)
                        manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    drawerLayout.openDrawer(GravityCompat.START);
                    return true;
                }
                return false;
            }
        });
    }

    private void setUpMap() {
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.strokeWidth(2);//设置定位蓝点精度圈的边框宽度的方法。
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.arrows));
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        //点击地图定位
        aMap.setOnMapClickListener(this);
    }


    //AMapLocationListener接口只有onLocationChanged方法可以实现，用于接收异步返回的定位结果，回调参数是AMapLocation。
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    aMapLocation.getLatitude();//获取纬度
                    mLatitude= aMapLocation.getLatitude();
                    mLongitude=aMapLocation.getLongitude();//获取经度
                    aMapLocation.getAccuracy();//获取精度信息
                    aMapLocation.getCountry();//国家信息
                    aMapLocation.getProvince();//省信息
                    aMapLocation.getCity();//城市信息
                    aMapLocation.getDistrict();//城区信息
                    aMapLocation.getStreet();//街道信息
                    aMapLocation.getStreetNum();//街道门牌号信息
                    aMapLocation.getCityCode();//城市编码
                    aMapLocation.getAdCode();//地区编码
                    aMapLocation.getAoiName();//获取当前定位点的AOI信息
                    aMapLocation.getBuildingId();//获取当前室内定位的建筑物Id
                    aMapLocation.getFloor();//获取当前室内定位的楼层
                    aMapLocation.getGpsAccuracyStatus();//获取GPS的当前状态
                    //获取定位时间
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(aMapLocation.getTime());
                    df.format(date);
                    //
                    if (isFirstLoc) {
                        //只显示一次
                        aMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(mLatitude,mLongitude)));
                        StringBuffer buffer = new StringBuffer();
                        buffer.append(aMapLocation.getCountry() + "" + aMapLocation.getProvince() + "" + aMapLocation.getCity() + "" + aMapLocation.getProvince() + "" + aMapLocation.getDistrict() + "" + aMapLocation.getStreet() + "" + aMapLocation.getStreetNum());
                        Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                        isFirstLoc = false;
                    }
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();//销毁定位客户端，同时销毁本地定位服务。
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
        mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

            InputTask.getInstance(this, mAdapter).onSearch(s.toString(), "");
            edit.setCompoundDrawables(null,null,null,null);

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        aMap.clear();
        MarkerOptions otMarkerOptions = new MarkerOptions();
        otMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.locate));
        otMarkerOptions.position(latLng);
        aMap.addMarker(otMarkerOptions);
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        Toast.makeText(MainActivity.this,latLng.toString(),Toast.LENGTH_SHORT).show();
    }

    /**
     * 点击事件
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.satellite:
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 设置卫星地图模式，aMap是地图控制器对象。
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.night:
                aMap.setMapType(AMap.MAP_TYPE_NIGHT);// 设置夜景地图模式，aMap是地图控制器对象。
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.navigation:
                aMap.setMapType(AMap.MAP_TYPE_NAVI);// 设置导航地图模式，aMap是地图控制器对象。
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.common:
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 设置普通地图模式，aMap是地图控制器对象。
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.route_bus:
                setRouteCarListener(1);
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.route_driver:
                setRouteCarListener(0);
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.route_walk:
                setRouteCarListener(2);
                drawerLayout.closeDrawer(GravityCompat.START);

                break;
            case R.id.route_ride:
                setRouteCarListener(3);
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.go:
                lv.setVisibility(View.GONE);
                if (manager != null)
                    manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                drawerLayout.openDrawer(GravityCompat.START);
                break;
        }
    }

    //规划路线进度
    private ProgressDialog dialog;
    //type 0 驾车 1 公交 2 步行 3 骑行
    private void setRouteCarListener(int type) {
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        //清除防止重复显示
        aMap.clear();
        if (dialog == null) {
            dialog = new ProgressDialog(this);
        }
        dialog.setMessage("正在规划路线，请稍后...");
        dialog.show();

        RouteSearch routeSearch = new RouteSearch(this);
        //起始点
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(new LatLonPoint(mLatitude, mLongitude),
                new LatLonPoint(AddressBean.latitude, AddressBean.longitude));
        //驾车：第一个参数表示fromAndTo包含路径规划的起点和终点，drivingMode表示驾车模式(支持20种模式)
        //第三个参数表示途经点（最多支持16个），第四个参数表示避让区域（最多支持32个），第五个参数表示避让道路
        //模式链接：http://lbs.amap.com/api/android-navi-sdk/guide/route-plan/drive-route-plan
        //此处返回结果会躲避拥堵，路程较短，尽量缩短时间
        if (type == 0) {
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo,10, null, null, "");
            routeSearch.calculateDriveRouteAsyn(query);
        } else if (type == 1) {
            //公交：fromAndTo包含路径规划的起点和终点，RouteSearch.BusLeaseWalk表示公交查询模式
            //第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算,1表示计算
            RouteSearch.BusRouteQuery query1 = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BusLeaseWalk, null, 1);
            routeSearch.calculateBusRouteAsyn(query1);
        } else if (type == 2) {
            //步行：SDK提供两种模式：RouteSearch.WALK_DEFAULT 和 RouteSearch.WALK_MULTI_PATH（注意：过时）
            RouteSearch.WalkRouteQuery query2 = new RouteSearch.WalkRouteQuery(fromAndTo);
            routeSearch.calculateWalkRouteAsyn(query2);
        } else if (type == 3) {
            //骑行：（默认推荐路线及最快路线综合模式，可以接二参同上）
            RouteSearch.RideRouteQuery query3 = new RouteSearch.RideRouteQuery(fromAndTo);
            routeSearch.calculateRideRouteAsyn(query3);
        }
        routeSearch.setRouteSearchListener(this);
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
        dialog.dismiss();
        if (i == 1000) {
            BusPath busPath = busRouteResult.getPaths().get(0);
            setBusRoute(busPath, busRouteResult.getStartPos(), busRouteResult.getTargetPos());
            float distance = busPath.getDistance() / 1000;
            long duration = busPath.getDuration() / 60;
            //需步行距离
            float walkDistance = busPath.getWalkDistance() / 1000;
            //行车的距离
            float busDistance = busPath.getBusDistance() / 1000;
            //成本、费用（其中walkDistance+busDistance=distance 行车+步行=总距离）
            float cost = busPath.getCost();
            content.setText("\n距离/公里：" + distance + "\n时间/分：" + duration + "\n步行距离/公里：" + walkDistance
                    + "\n行车距离/公里：" + busDistance + "\n成本、费用：" + cost);
        } else {
            Log.e(TAG, "onBusRouteSearched: 路线规划失败");
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        dialog.dismiss();
        if (i == 1000) {
            DrivePath drivePath = driveRouteResult.getPaths().get(0);
            setDrivingRoute(drivePath, driveRouteResult.getStartPos(), driveRouteResult.getTargetPos());
            //策略
            String strategy = drivePath.getStrategy();
            //总的交通信号灯数
            int clights = drivePath.getTotalTrafficlights();
            float distance = drivePath.getDistance() / 1000;
            long duration = drivePath.getDuration() / 60;
            content.setText("策略：" + strategy + "\n总的交通信号灯数/个：" + clights +
                    "\n距离/公里：" + distance + "\n时间/分：" + duration);
        } else {
            Log.e(TAG, "onDriveRouteSearched: 路线规划失败");
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        dialog.dismiss();
        if (i == 1000) {
            WalkPath walkPath = walkRouteResult.getPaths().get(0);
            setWalkRoute(walkPath, walkRouteResult.getStartPos(), walkRouteResult.getTargetPos());
            float distance = walkPath.getDistance() / 1000;
            long duration = walkPath.getDuration() / 60;
            content.setText("\n距离/公里：" + distance + "\n时间/分：" + duration);
        } else {
            Log.e(TAG, "onWalkRouteSearched: 路线规划失败");
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
        dialog.dismiss();
        if (i == 1000) {
            RidePath ridePath = rideRouteResult.getPaths().get(0);
            setRideRoute(ridePath, rideRouteResult.getStartPos(), rideRouteResult.getTargetPos());
            float distance = ridePath.getDistance() / 1000;
            long duration = ridePath.getDuration() / 60;
            content.setText("\n距离/公里：" + distance + "\n时间/分：" + duration);
        } else {
            Log.e(TAG, "onRideRouteSearched: 路线规划失败");
        }
    }

    //驾车规划路线
    private void setDrivingRoute(DrivePath drivePath, LatLonPoint start, LatLonPoint end) {
        DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(this, aMap, drivePath, start, end);
        drivingRouteOverlay.setNodeIconVisibility(true);//设置节点（转弯）marker是否显示
        drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true。
        drivingRouteOverlay.removeFromMap();//去掉DriveLineOverlay上的线段和标记。
        drivingRouteOverlay.addToMap(); //添加驾车路线添加到地图上显示。
        drivingRouteOverlay.zoomToSpan();//移动镜头到当前的视角。
        drivingRouteOverlay.setRouteWidth(1);//设置路线的宽度
    }

    //公交规划路线
    private void setBusRoute(BusPath busPath, LatLonPoint start, LatLonPoint end) {
        BusRouteOverlay busRouteOverlay = new BusRouteOverlay(this, aMap, busPath, start, end);
        busRouteOverlay.removeFromMap();//去掉DriveLineOverlay上的线段和标记。
        busRouteOverlay.addToMap(); //添加驾车路线添加到地图上显示。
        busRouteOverlay.zoomToSpan();//移动镜头到当前的视角。
        busRouteOverlay.setNodeIconVisibility(true);//是否显示路段节点图标
    }

    /**
     * 步行规划线路
     */
    private void setWalkRoute(WalkPath walkPath, LatLonPoint start, LatLonPoint end) {
        WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this, aMap, walkPath, start, end);
        walkRouteOverlay.removeFromMap();
        walkRouteOverlay.addToMap();
        walkRouteOverlay.zoomToSpan();
    }

    /**
     * 骑行规划线路
     */
    private void setRideRoute(RidePath ridePath, LatLonPoint start, LatLonPoint end) {
        RideRouteOverlay rideRouteOverlay = new RideRouteOverlay(this, aMap, ridePath, start, end);
        rideRouteOverlay.removeFromMap();
        rideRouteOverlay.addToMap();
        rideRouteOverlay.zoomToSpan();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}












