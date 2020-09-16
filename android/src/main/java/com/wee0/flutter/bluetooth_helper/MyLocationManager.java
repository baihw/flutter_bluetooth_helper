package com.wee0.flutter.bluetooth_helper;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 自定义位置管理
 */
enum MyLocationManager {

    /**
     * 当前对象唯一实例
     */
    me;

    // 位置管理对象
    private final LocationManager locationManager;

    // 最后一次开关时间
    private long lastChangeTime = 0l;

    MyLocationManager() {
        locationManager = (LocationManager) PlatformHelper.me().getActivity().getSystemService(Context.LOCATION_SERVICE);

//        IntentFilter _filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        IntentFilter _filter = new IntentFilter(LocationManager.MODE_CHANGED_ACTION);
        PlatformHelper.me().getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                lastChangeTime = System.currentTimeMillis();
//                final String _action = intent.getAction();
//                if (LocationManager.MODE_CHANGED_ACTION.equals(_action)) {
//                    long _time = System.currentTimeMillis();
//                    boolean _gpsIsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//                    if (_gpsIsEnable)
//                        lastChangeTime = System.currentTimeMillis();
//                    MyLog.debug("gps enable: {}, time: {}", _gpsIsEnable, _time);
//                }
            }
        }, _filter);
    }

    /**
     * @return 最后次状态改变时间
     */
    public long getLastChangeTime(){
        return lastChangeTime;
    }

    /**
     * @return 位置是否开启
     */
    public boolean isEnabled(boolean requireGps) {
        boolean _serviceIsEnabled = false;
        if (PlatformHelper.sdkGE19()) {
            try {
                int _locationMode = Settings.Secure.getInt(PlatformHelper.me().getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE);
                _serviceIsEnabled = Settings.Secure.LOCATION_MODE_OFF != _locationMode;
            } catch (Settings.SettingNotFoundException e) {
                _serviceIsEnabled = false;
            }
        } else {
            String _locationProviders = Settings.Secure.getString(PlatformHelper.me().getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            MyLog.debug("_locationProviders: {}", _locationProviders);
            _serviceIsEnabled = null != _locationProviders && 0 != _locationProviders.trim().length();
        }
        if (!_serviceIsEnabled)
            return false;
        if (requireGps)
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return true;
    }

    /**
     * 获取位置信息
     *
     * @return 位置信息
     */
    public MyLocation getLocation() {
        return _getLocation(false);
    }

    /**
     * 获取位置信息
     *
     * @param parseAddress 是否解析地址
     * @return 位置信息对象
     */
    MyLocation _getLocation(boolean parseAddress) {
        if (!isEnabled(true)) throw new IllegalStateException("location is disable.");

        Criteria _criteria = new Criteria();
        _criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        // 是否需要获取海拔方向数据
        _criteria.setAltitudeRequired(false);
        _criteria.setBearingRequired(false);
        // 是否允许产生资费
        _criteria.setCostAllowed(false);
        // 耗电程度
        _criteria.setPowerRequirement(Criteria.POWER_LOW);
        String _provider = locationManager.getBestProvider(_criteria, true);
        if (null == _provider) {
            List<String> _providers = locationManager.getProviders(true);
            if (_providers.contains(LocationManager.GPS_PROVIDER)) {
                _provider = LocationManager.GPS_PROVIDER;
            } else if (_providers.contains(LocationManager.NETWORK_PROVIDER)) {
                _provider = LocationManager.NETWORK_PROVIDER;
            }
        }
        MyLog.debug("Location Provider is: {}", _provider);

        Location _location = locationManager.getLastKnownLocation(_provider);
        MyLog.debug("location: {}", _location);
        if (null == _location) return null;

        MyLocation _result = new MyLocation();
        _result.setLongitude(_location.getLongitude());
        _result.setLatitude(_location.getLatitude());
        if (parseAddress) {
            Geocoder _geoCoder = new Geocoder(PlatformHelper.me().getApplication(), Locale.getDefault());
            try {
                List<Address> _addresses = _geoCoder.getFromLocation(_location.getLatitude(), _location.getLongitude(), 1);
                if (null != _addresses && !_addresses.isEmpty()) {
                    Address _address = _addresses.get(0);
                    if (null != _address) _result.setAddress(_address.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return _result;
    }

}
