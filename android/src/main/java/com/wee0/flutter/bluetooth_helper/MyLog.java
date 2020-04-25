package com.wee0.flutter.bluetooth_helper;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志统一处理
 */
final class MyLog {

    private static final String _LOG_CATALOG = "wee0.BluetoothHelper";
    private static final Pattern _DEF_PARAMS_PATTERN = Pattern.compile("\\{\\}");
    // 是否输出调试信息
    private static boolean _isDebug = false;

    /**
     * 开启调试信息
     */
    static void enableDebug() {
        _isDebug = true;
    }

    /**
     * 输出调试信息
     *
     * @param msg    消息内容
     * @param params 消息参数
     */
    static void debug(String msg, Object... params) {
        if (_isDebug)
            Log.d(_LOG_CATALOG, _formatMsg(msg, params));
    }

    /**
     * 输出提示信息
     *
     * @param msg    消息内容
     * @param params 消息参数
     */
    static void info(String msg, Object... params) {
        Log.i(_LOG_CATALOG, _formatMsg(msg, params));
    }

    /**
     * 输出警告信息
     *
     * @param msg    消息内容
     * @param params 消息参数
     */
    static void warn(String msg, Object... params) {
        Log.w(_LOG_CATALOG, _formatMsg(msg, params));
    }

    /**
     * 格式化日志消息
     *
     * @param msg    消息内容
     * @param params 消息内容参数
     * @return 格式化后的消息
     */
    private static String _formatMsg(String msg, Object... params) {
        if (null == msg || null == params || 1 > params.length)
            return msg;

        Matcher _matcher = _DEF_PARAMS_PATTERN.matcher(msg);
        boolean _isMatched = _matcher.find();
        if (!_isMatched) {
            return msg;
        }

        final int _parLen = params.length;
        int _i = 0;
        StringBuffer _sb = new StringBuffer();
        while (_isMatched) {
            if (_i >= _parLen)
                break;
            Object _valObj = params[_i];
            String _valString = "NULL";
            if (null != _valObj) {
                if (_valObj.getClass().isArray()) {
                    StringBuilder _arrBuilder = new StringBuilder(64);
                    _arrBuilder.append("[");
                    for (int _j = 0, _jLen = Array.getLength(_valObj); _j < _jLen; _j++) {
                        _arrBuilder.append(Array.get(_valObj, _j)).append(',');
                    }
                    if (',' == _arrBuilder.charAt(_arrBuilder.length() - 1))
                        _arrBuilder.deleteCharAt(_arrBuilder.length() - 1);
                    _arrBuilder.append("]");
                    _valString = _arrBuilder.toString();
                } else {
                    _valString = _valObj.toString();
                }
                if (-1 < _valString.indexOf("$"))
                    _valString = _valString.replaceAll("\\$", "\\\\\\$");
            }
            _matcher.appendReplacement(_sb, _valString);
            _isMatched = _matcher.find();
            _i++;
        }
        _matcher.appendTail(_sb);
        return _sb.toString();
    }


//    static void print(BluetoothGatt gatt) {
//        StringBuilder _builder = new StringBuilder();
//        BluetoothDevice _device = gatt.getDevice();
//        _builder.append(",device: ").append(_device);
//        _builder.append(",deviceName: ").append(_device.getName());
//        _builder.append(",deviceType: ").append(_device.getType());
//        _builder.append(",deviceAddress: ").append(_device.getAddress());
//        _builder.append(",deviceBondState: ").append(_device.getBondState());
//        Log.d(_LOG_CATALOG, _builder.toString());
//    }
//
//    static void print(BluetoothGattService gattService) {
//        StringBuilder _builder = new StringBuilder();
//        _builder.append("BluetoothGattService uuid: ").append(gattService.getUuid());
//        _builder.append(",type: ").append(gattService.getType());
//        _builder.append(",isPrimary: ").append(BluetoothGattService.SERVICE_TYPE_PRIMARY == gattService.getType());
//        _builder.append(",instanceId: ").append(gattService.getInstanceId());
//        _builder.append(",instance: ").append(gattService);
//        Log.d(_LOG_CATALOG, _builder.toString());
//    }
//
//    static void print(BluetoothGattCharacteristic characteristic) {
//        StringBuilder _builder = new StringBuilder();
//        _builder.append("BluetoothGattCharacteristic uuid: ").append(characteristic.getUuid());
//        _builder.append(",value: ").append(characteristic.getValue());
//        _builder.append(",describeContents: ").append(characteristic.describeContents());
//        _builder.append(",properties: ").append(characteristic.getProperties());
//        _builder.append(",permissions: ").append(characteristic.getPermissions());
//        _builder.append(",descriptors: ").append(characteristic.getDescriptors());
//        _builder.append(",instanceId: ").append(characteristic.getInstanceId());
//        _builder.append(",instance: ").append(characteristic);
//        Log.d(_LOG_CATALOG, _builder.toString());
//    }
//
//    static void print(BluetoothAdapter adapter) {
//        StringBuilder _builder = new StringBuilder();
//        _builder.append("name:").append(adapter.getName());
//        _builder.append(",state:").append(adapter.getState());
//        _builder.append(",isDiscovering:").append(adapter.isDiscovering());
//        _builder.append(",address:").append(adapter.getAddress());
//        _builder.append(",bondedDevices:").append(adapter.getBondedDevices());
//        Log.d(_LOG_CATALOG, _builder.toString());
//    }
}
