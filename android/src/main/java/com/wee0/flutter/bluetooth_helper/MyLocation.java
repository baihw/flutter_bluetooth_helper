package com.wee0.flutter.bluetooth_helper;

import androidx.annotation.NonNull;

/**
 * 位置信息对象
 */
public final class MyLocation {

    // 经度
    private double longitude;
    // 纬度
    private double latitude;

    // 地址信息
    private String address;

    /**
     * 设置经度
     *
     * @param longitude 经度
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return 获取经度
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * 设置纬度
     *
     * @param latitude 纬度
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return 获取纬度
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * 设置地址信息
     *
     * @param address 地址信息
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return 地址信息
     */
    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        StringBuilder _builder = new StringBuilder();
        _builder.append("{\"type\":\"").append(MyLocation.class.getSimpleName()).append("\"");
        _builder.append(",\"longitude\":").append(longitude);
        _builder.append(",\"latitude\":").append(latitude);
        _builder.append(",\"address\":\"").append(address).append("\"");
        _builder.append("}");
        return _builder.toString();
    }
}
