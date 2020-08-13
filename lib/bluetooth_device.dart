import 'dart:async';
import 'dart:convert';
import 'bluetooth_helper.dart';

/// 当前蓝牙相关事件回调通知
typedef void EventCallback(BluetoothEvent event);

/// 蓝牙设备信息
class BluetoothDevice {
  // 设备标识
  String _deviceId;

  // 设备名称
  String _deviceName;

  // 事件回调通知函数
  EventCallback _eventCallback;

  // 连接状态
  int _deviceState = BluetoothEventDeviceStateChange.STATE_DISCONNECTED;

  // ignore: cancel_subscriptions
  StreamSubscription _subscription;

  BluetoothDevice.create(String deviceId, String deviceName) {
    this._deviceId = deviceId;
    this._deviceName = deviceName;
  }

  BluetoothDevice.fromMap(Map data) {
    this._deviceId = data["deviceId"];
    this._deviceName = data["deviceName"];
  }

  BluetoothDevice.fromJsonString(String data) {
    Map _data = json.decode(data);
    this._deviceId = _data["deviceId"];
    this._deviceName = _data["deviceName"];
  }

  String toJsonString() {
    return json.encode(this);
  }

  Map toJson() {
    return {"deviceId": this._deviceId, "deviceName": this._deviceName};
  }

  @override
  String toString() {
    return "BluetoothDevice{name:$_deviceName, id:$_deviceId}";
  }

  /// 设备标识
  String get deviceId => this._deviceId;

  /// 设备名称
  String get deviceName => this._deviceName;

  /// 设置事件回调处理函数
  set eventCallback(EventCallback eventCallback) =>
      this._eventCallback = eventCallback;

  /// 设备状态
  int get deviceState => this._deviceState;

  /// 是否已连接
  bool get isConnected =>
      BluetoothEventDeviceStateChange.STATE_CONNECTED == this._deviceState;

  /// 是否正在建立连接。。。
  bool get isConnecting =>
      BluetoothEventDeviceStateChange.STATE_CONNECTING == this._deviceState;

  /// 是否已经断开连接.
  bool get isDisconnected =>
      BluetoothEventDeviceStateChange.STATE_DISCONNECTED == this._deviceState;

  /// 蓝牙事件处理
  void _onEventHandle(BluetoothEvent event) {
    if (event.type != BluetoothEventStateChange.TYPE &&
        this._deviceId != event.deviceId) {
      BluetoothHelper.debug("device $_deviceName ignore event: $event");
      return;
    }
    BluetoothHelper.debug("device $_deviceName onEventHandle: $event");
    if (null != this._eventCallback) this._eventCallback(event);
    if (event is BluetoothEventDeviceStateChange &&
        BluetoothEventDeviceStateChange.STATE_DISCONNECTED == event.state) {
      this.disconnect();
      return;
    }
  }

  // 读取数据返回结果数据
  Stream<List<int>> get _readResultStream async* {
    yield* BluetoothHelper.me.events
        .where((_event) =>
            _event.type == BluetoothEventReadResult.TYPE &&
            this._deviceId == _event.deviceId)
        .cast<BluetoothEventReadResult>()
        .map((_event) => _event.data);
  }

  /// 建立连接
  Future<bool> connect([int timeout = 3]) async {
    BluetoothHelper.debug(
        "connect to id:${this._deviceId}, name:${this.deviceName}, state:${this._deviceState}");
    if (BluetoothEventDeviceStateChange.STATE_CONNECTED == this._deviceState) {
      BluetoothHelper.debug("already connected!");
      return true;
    }
    if (BluetoothEventDeviceStateChange.STATE_CONNECTING == this._deviceState) {
      BluetoothHelper.debug("already connecting!");
      return false;
    }
    if (BluetoothHelper.me.isWaitingScan) {
      BluetoothHelper.debug("waiting scan!");
      return false;
    }
    this._deviceState = BluetoothEventDeviceStateChange.STATE_CONNECTING;
    try {
      bool _connectResult =
          await BluetoothHelper.me.connect(this._deviceId, timeout);
      BluetoothHelper.debug("bluetooth_device connect result: $_connectResult");
      if (_connectResult) {
        this._deviceState = BluetoothEventDeviceStateChange.STATE_CONNECTED;
//      this._failCount = 0;
        if (null == this._subscription)
          this._subscription = BluetoothHelper.me.events.listen(_onEventHandle);
      } else {
//      if (this._failCount++ > _failCountDefThreshold && !BluetoothHelper.me.isWaitingScan) BluetoothHelper.me.waitingScan();
        this.disconnect();
      }
      return _connectResult;
    } catch (_err) {
      BluetoothHelper.debug("bluetooth_device connect error: $_err");
      this.disconnect();
      return false;
    }
  }

  /// 发现所有服务特征码
  Future<List> discoverCharacteristics([int timeout = 3]) async {
    return BluetoothHelper.me.discoverCharacteristics(this._deviceId, timeout);
  }

  /// 设置特征监听
  Future<bool> setCharacteristicNotification(String characteristicId,
      [bool enable = true]) async {
    return BluetoothHelper.me.setCharacteristicNotification(
        this._deviceId, characteristicId, enable);
  }

  /// 特征数据读取
  Future<List<int>> characteristicRead(String characteristicId) async {
    bool _readResult = await BluetoothHelper.me
        .characteristicRead(this._deviceId, characteristicId);
    if (!_readResult) {
      BluetoothHelper.debug("read error!");
      return null;
    }
    return await _readResultStream.first;
  }

  /// 特征数据写入
  Future<bool> characteristicWrite(
      String characteristicId, List<int> data) async {
    bool _write = await BluetoothHelper.me
        .characteristicWrite(this._deviceId, characteristicId, data);
    if (!_write) {
      BluetoothHelper.debug("write error!");
      return false;
    }
    BluetoothEventWriteResult _writeResult = await BluetoothHelper.me.events
        .where((_event) =>
            _event.type == BluetoothEventWriteResult.TYPE &&
            this._deviceId == _event.deviceId)
        .first;
    BluetoothHelper.debug("writeResult: $_writeResult");
    return _writeResult.isOk;
  }

  /// 断开连接
  Future<bool> disconnect() async {
    if (BluetoothEventDeviceStateChange.STATE_DISCONNECTED == this._deviceState)
      return false;
    BluetoothHelper.debug("disconnect...");
    this._deviceState = BluetoothEventDeviceStateChange.STATE_DISCONNECTED;
    if (null != this._subscription) {
      this._subscription.cancel();
      this._subscription = null;
    }
    return BluetoothHelper.me.disconnect(this.deviceId);
  }
}
