package com.wee0.flutter.bluetooth_helper;

/**
 * 自定义异常
 */
final class MyBluetoothException extends RuntimeException {

    /**
     * 暂时未确定下来编码的异常，统一先使用此编码。
     */
    static final String CODE_COMMON = "600";

    /**
     * 蓝牙未开启
     */
    static final String CODE_BLUETOOTH_NOT_ENABLE = "601";

    /**
     * 定位未开启
     */
    static final String CODE_LOCATION_NOT_ENABLE = "602";

    /**
     * 需要先建立连接
     */
    static final String CODE_CONNECT_FIRST = "603";

    // 错误编码
    final String code;

    MyBluetoothException(String code, String msg) {
        super(msg);
        this.code = code;
    }

    MyBluetoothException(String msg) {
        super(msg);
        this.code = CODE_COMMON;
    }

    /**
     * @return 错误编码
     */
    String getCode() {
        return this.code;
    }
}
