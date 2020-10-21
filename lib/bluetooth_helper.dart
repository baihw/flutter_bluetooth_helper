import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';
import 'bluetooth_device.dart';

class BluetoothHelper {
  /// 方法名
  static const String KEY_METHOD = "method";

  /// 参数名
  static const String KEY_ARGS = "args";

  /// 响应代码
  static const String KEY_CODE = "code";

  /// 响应消息
  static const String KEY_MSG = "message";

  /// 响应数据
  static const String KEY_DATA = "data";

  // ignore: close_sinks
  StreamController<BluetoothEvent> _streamController;

  // 是否有任务需要等待执行一次扫描。
  bool _isWaitingScan = false;

  BluetoothHelper._internal() {
    debug("BluetoothHelper init...");
    _streamController = StreamController<BluetoothEvent>.broadcast();

    _basicMessageChannel.setMessageHandler((_msg) {
      debug("handle _msg:$_msg");
      if (null == _msg) return;
      String _methodName = _msg[KEY_METHOD];
      switch (_methodName) {
        case "onStateChange":
          Map _data = _msg[KEY_ARGS];
//          print("stateChange: $_data");
          _streamController.sink.add(BluetoothEventStateChange(_data["state"]));
          break;
        case "onDeviceStateChange":
          Map _data = _msg[KEY_ARGS];
//          print("deviceStateChange: $_data");
          _streamController.sink.add(BluetoothEventDeviceStateChange(
              _data["deviceId"], _data["deviceState"]));
          break;
        case "onCharacteristicNotifyData":
          Map _data = _msg[KEY_ARGS];
//          print("characteristicNotifyData: $_data");
          _streamController.sink.add(BluetoothEventNotifyData(
              _data["deviceId"], _data["characteristicId"], _data["data"]));
          break;
        case "onCharacteristicReadResult":
          Map _data = _msg[KEY_ARGS];
          _streamController.sink.add(BluetoothEventReadResult(
              _data["deviceId"], _data["characteristicId"], _data["data"]));
          break;
        case "onCharacteristicWriteResult":
          Map _data = _msg[KEY_ARGS];
          _streamController.sink.add(BluetoothEventWriteResult(
              _data["deviceId"], _data["characteristicId"], _data["isOk"]));
          break;
        default:
          print("unKnow msg: $_msg");
          break;
      }
      return null;
    });
  }

  factory BluetoothHelper() => _me;

  static final BluetoothHelper _me = new BluetoothHelper._internal();

  static BluetoothHelper get me => _me;

  static const BasicMessageChannel _basicMessageChannel =
      const BasicMessageChannel("bluetooth_helper", StandardMessageCodec());

//  static Map<String, BluetoothDevice> _deviceMap = {};

  /// 释放资源
  void destroy() {
    this._streamController.sink.close().then(
        (_res) => print("BluetoothHelper controller.sink close result:$_res"));
    this
        ._streamController
        .close()
        .then((_res) => print("BluetoothHelper controller close result:$_res"));
  }

  /// 事件流
  Stream<BluetoothEvent> get events => _streamController.stream;

  /// 是否处于等待扫描执行状态。
  bool get isWaitingScan => _isWaitingScan;

  /// 等待扫描任务执行
  void waitingScan() {
    this._isWaitingScan = true;
  }

  /// 保活
  Future<bool> keepAlive() async {
    Map _res = await callMethod("keepAlive");
    bool _val = getResultData(_res);
    return _val;
  }

//  /// 扫描设备，获取扫描结果。
//  Future<List<BluetoothDevice>> _scan(
//      {String deviceName, String deviceId, int timeout = 2}) async {
//    Map _res = await callMethod(
//        "startScan", {"deviceName": deviceName, "deviceId": deviceId});
//    getResultData(_res);
//
//    _res = await Future.delayed(Duration(seconds: timeout), () async {
//      return await callMethod("stopScan");
//    });
//    this._isWaitingScan = false;
//
//    Map _deviceMap = getResultData(_res);
//    if (null == _deviceMap || _deviceMap.isEmpty) return [];
//    List<BluetoothDevice> _devices = _deviceMap.values
//        .where((_item) => null != _item["deviceName"])
//        .map((_item) => BluetoothDevice.fromMap(_item))
//        .toList();
//    return _devices;
//  }

  /// 扫描设备，获取扫描结果。
  Future<List<BluetoothDevice>> scan(
      {String deviceName, String deviceId, int timeout = 2}) async {
    Map _res = await callMethod("startScan",
        {"deviceName": deviceName, "deviceId": deviceId, "timeout": timeout});
    Map _deviceMap = getResultData(_res);
    if (null == _deviceMap || _deviceMap.isEmpty) return [];
    List<BluetoothDevice> _devices = _deviceMap.values
        .where((_item) => null != _item["deviceName"])
        .map((_item) => BluetoothDevice.fromMap(_item))
        .toList();
    return _devices;
  }

  /// 建立连接
  Future<bool> connect(String deviceId, [int timeout = 3]) async {
    Map _res =
        await callMethod("connect", {"deviceId": deviceId, "timeout": timeout});
    bool _val = getResultData(_res);
    return _val;
  }

  /// 获取连接状态
  Future<int> getDeviceState(String deviceId) async {
    Map _res = await callMethod("getDeviceState", {"deviceId": deviceId});
    int _val = getResultData(_res);
    return _val;
  }

  /// 发现所有服务特征码
  Future<List> discoverCharacteristics(String deviceId,
      [int timeout = 3]) async {
    Map _res = await callMethod(
        "discoverServices", {"deviceId": deviceId, "timeout": timeout});
    List _val = getResultData(_res);
    return _val;
  }

  /// 设置特征监听
  Future<bool> setCharacteristicNotification(
      String deviceId, String characteristicId, bool enable) async {
    Map _res = await callMethod("setCharacteristicNotification", {
      "deviceId": deviceId,
      "characteristicId": characteristicId,
      "enable": enable
    });
    bool _val = getResultData(_res);
    return _val;
  }

  /// 特征读取
  Future<bool> characteristicRead(
      String deviceId, String characteristicId) async {
    Map _res = await callMethod("characteristicRead",
        {"deviceId": deviceId, "characteristicId": characteristicId});
    bool _val = getResultData(_res);
    return _val;
  }

  /// 特征写入
  Future<bool> characteristicWrite(
      String deviceId, String characteristicId, List<int> data) async {
    Map _res = await callMethod("characteristicWrite", {
      "deviceId": deviceId,
      "characteristicId": characteristicId,
      "data": Uint8List.fromList(data)
    });
    bool _val = getResultData(_res);
    return _val;
  }

  /// 断开连接
  Future<bool> disconnect(String deviceId) async {
    Map _res = await callMethod("disconnect", {"deviceId": deviceId});
    bool _val = getResultData(_res);
    return _val;
//    final String _result = await _channel.invokeMethod("disconnect", deviceId);
//    return _result;
  }

  /// 开启蓝牙
  static Future<bool> bluetoothEnable() async {
    Map _result = await callMethod("bluetoothEnable");
    bool _isEnable = getResultData(_result);
    return _isEnable;
  }

  /// 关闭蓝牙
  static Future<bool> bluetoothDisable() async {
    Map _result = await callMethod("bluetoothDisable");
    bool _isDisable = getResultData(_result);
    return _isDisable;
  }

  /// 最后一次蓝牙与gps的的状态改变时间，单位：毫秒。
  static Future<Map> get stateLastChangeTime async {
    Map _result = await callMethod("stateLastChangeTime");
    Map _times = getResultData(_result);
    return _times;
  }

  /// 蓝牙是否开启
  static Future<bool> get bluetoothIsEnable async {
    Map _result = await callMethod("bluetoothIsEnable");
    bool _isEnable = getResultData(_result);
    return _isEnable;
  }

  /// 定位是否开启，蓝牙扫描需要开启定位，否则扫描不到结果。
  static Future<bool> get locationIsEnable async {
    Map _result = await callMethod("locationIsEnable", {"requireGps": true});
    bool _isEnable = getResultData(_result);
    return _isEnable;
  }

  /// 调用原生方法
  static Future<dynamic> callMethod(String method, [Object args]) {
    Map<String, Object> _params = {KEY_METHOD: method, KEY_ARGS: args};
    return _basicMessageChannel.send(_params);
  }

  /// 获取返回数据
  static dynamic getResultData(Map result) {
    if (null == result)
      throw PlatformException(code: "600", message: "native result is null!");
    String _resCode = result[KEY_CODE];
    if ("200" != _resCode)
      throw PlatformException(code: _resCode, message: result[KEY_MSG]);
    return result[KEY_DATA];
  }

  static bool _isDebug = false;

  /// 开启调试模式，输出详细的日志信息
  static void enableDebug() {
    _isDebug = true;
    callMethod("debug").then((_res) => print("enableDebug result: $_res"));
  }

  /// 打印调试信息
  static void debug(Object msg) {
    if (_isDebug) print(msg);
  }
}

/// 蓝牙事件
class BluetoothEvent {
  final int _type;
  final String _deviceId;

  BluetoothEvent(this._type, this._deviceId);

  int get type => _type;

  String get deviceId => _deviceId;

  @override
  String toString() {
    return "BluetoothEvent{deviceId:$_deviceId, type:$type}";
  }
}

/// 蓝牙状态改变事件
class BluetoothEventStateChange extends BluetoothEvent {
  static const int TYPE = 0;

  // 关闭
  static const int STATE_OFF = 0;

  // 开启
  static const int STATE_ON = 1;

  // 状态
  final int _state;

  BluetoothEventStateChange(this._state) : super(TYPE, null);

  int get state => _state;

  @override
  String toString() {
    return "BluetoothEventStateChange{state:$state}";
  }
}

/// 设备状态改变事件
class BluetoothEventDeviceStateChange extends BluetoothEvent {
  static const int TYPE = 1;

  // 未连接
  static const int STATE_DISCONNECTED = 0;

  // 正在连接
  static const int STATE_CONNECTING = 1;

  // 已连接
  static const int STATE_CONNECTED = 2;

  // 正在断开
  static const int STATE_DISCONNECTING = 3;

  // 状态
  final int _state;

  BluetoothEventDeviceStateChange(String deviceId, this._state)
      : super(TYPE, deviceId);

  int get state => _state;

  @override
  String toString() {
    return "BluetoothEventDeviceStateChange{deviceId:$_deviceId, state:$state}";
  }
}

/// 接收到监听数据事件
class BluetoothEventNotifyData extends BluetoothEvent {
  static const int TYPE = 2;

  final String _characteristicId;
  final List<int> _data;

  BluetoothEventNotifyData(String deviceId, this._characteristicId, this._data,
      [int type = TYPE])
      : super(type, deviceId);

  /// 特征标识
  String get characteristicId => _characteristicId;

  /// 数据
  List<int> get data => _data;

  @override
  String toString() {
    return "BluetoothEventNotifyData{deviceId:$_deviceId, characteristicId:$_characteristicId, data:$_data}";
  }
}

/// 接收到特征数据读取返回值事件。
class BluetoothEventReadResult extends BluetoothEvent {
  static const int TYPE = 3;

  final String _characteristicId;
  final List<int> _data;

  BluetoothEventReadResult(String deviceId, this._characteristicId, this._data)
      : super(TYPE, deviceId);

  /// 特征标识
  String get characteristicId => _characteristicId;

  /// 数据
  List<int> get data => _data;

  @override
  String toString() {
    return "BluetoothEventReadResult{deviceId:$_deviceId, characteristicId:$_characteristicId, data:$_data}";
  }
}

/// 接收到特征数据写入返回值事件。
class BluetoothEventWriteResult extends BluetoothEvent {
  static const int TYPE = 4;

  final String _characteristicId;
  final bool _isOK;

  BluetoothEventWriteResult(String deviceId, this._characteristicId, this._isOK)
      : super(TYPE, deviceId);

  /// 是否写入成功
  bool get isOk => _isOK;

  @override
  String toString() {
    return "BluetoothEventWriteResult{deviceId:$_deviceId, characteristicId:$_characteristicId, isOk:$_isOK}";
  }
}

/// 蓝牙事件类型
enum BluetoothEventType {
  /// 状态改变
  stateChange,

  /// 接收到数据
  receivedData
}
