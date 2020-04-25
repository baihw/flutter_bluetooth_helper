package com.wee0.flutter.bluetooth_helper;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;

import java.io.IOException;
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

    MyLocationManager() {
        locationManager = (LocationManager) PlatformHelper.me().getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * @return 位置是否开启
     */
    public boolean isEnabled() {
        if (PlatformHelper.sdkGE19()) {
            try {
                int _locationMode = Settings.Secure.getInt(PlatformHelper.me().getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE);
                return Settings.Secure.LOCATION_MODE_OFF != _locationMode;
            } catch (Settings.SettingNotFoundException e) {
                return false;
            }
        } else {
            String _locationProviders = Settings.Secure.getString(PlatformHelper.me().getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            MyLog.debug("_locationProviders: {}", _locationProviders);
            return null != _locationProviders && 0 != _locationProviders.trim().length();
        }
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
        if (!isEnabled()) throw new IllegalStateException("location is disable.");

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
