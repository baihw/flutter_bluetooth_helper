/// 蓝牙特征信息
class BluetoothCharacteristic {
  // 唯一标识
  String _id;

  // 设备唯一标识
  String _deviceId;

  BluetoothCharacteristic(this._deviceId, this._id);

  /// 唯一标识
  String get id => _id;

  /// 设备唯一标识
  String get deviceId => _deviceId;
}
