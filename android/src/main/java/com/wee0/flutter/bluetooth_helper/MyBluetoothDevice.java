package com.wee0.flutter.bluetooth_helper;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 蓝牙设备
 */
final class MyBluetoothDevice {

    BluetoothGatt gatt;
    boolean connected = false;
    // 特征对象集合
    Map<String, BluetoothGattCharacteristic> characteristicMap = new HashMap<>(16, 1.0f);

    // 当前回复对象
    IReply currentReply = null;

    final BluetoothDevice device;
    final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            MyLog.debug("onConnectionStateChange status: {}, newState: {}, gatt: {}", status, newState, gatt);
            MyHandler.me().removeCallback(MyHandler.ID_CONNECT_TIMEOUT);
            if (BluetoothProfile.STATE_CONNECTED == newState) {
                connected = true;
                if (null != currentReply) {
                    currentReply.success(true);
                    currentReply = null;
                }
            } else if (BluetoothProfile.STATE_DISCONNECTED == newState) {
                connected = false;
                if (null != currentReply) {
                    currentReply.success(false);
                    currentReply = null;
                }
                // 释放资源
                disconnect();
            }
            MyMethodRouter.me().callOnStateChange(device.getAddress(), newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            MyLog.debug("onServicesDiscovered status: {}, gatt: {}", status, gatt);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                List<BluetoothGattService> _gattServices = gatt.getServices();
                MyLog.debug("_gattServices: {}", _gattServices);
                for (BluetoothGattService _gattService : _gattServices) {
                    MyLog.debug("service: {}", _gattService);
                    List<BluetoothGattCharacteristic> _characteristics = _gattService.getCharacteristics();
                    for (BluetoothGattCharacteristic _characteristic : _characteristics) {
                        MyLog.debug("characteristic: {}", _characteristic);
                        String _uuid = _characteristic.getUuid().toString();
                        characteristicMap.put(_uuid, _characteristic);
                    }
                }
                MyHandler.me().removeCallback(MyHandler.ID_DISCOVER_SERVICES_TIMEOUT);
                List<String> _resultData = new ArrayList<>(characteristicMap.keySet());
                if (null != currentReply) {
                    currentReply.success(_resultData);
                    currentReply = null;
                } else {
                    MyMethodRouter.me().callOnServicesDiscovered(device.getAddress(), _resultData);
                }
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            MyLog.debug("onCharacteristicChanged characteristic: {}, gatt: {}", characteristic, gatt);
            MyMethodRouter.me().callOnCharacteristicNotifyData(device.getAddress(), characteristic.getUuid().toString(), characteristic.getValue());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            MyLog.debug("onCharacteristicRead status: {}, characteristic: {}, gatt: {}", status, characteristic, gatt);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                MyMethodRouter.me().callOnCharacteristicReadResult(device.getAddress(), characteristic.getUuid().toString(), characteristic.getValue());
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            MyLog.debug("onCharacteristicWrite status: {}, characteristic: {}, gatt: {}", status, characteristic, gatt);
            MyMethodRouter.me().callOnCharacteristicWriteResult(device.getAddress(), characteristic.getUuid().toString(), BluetoothGatt.GATT_SUCCESS == status);
            if (BluetoothGatt.GATT_SUCCESS == status) {
                MyLog.debug("write ok: {}", characteristic.getValue());
            } else if (BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH == status) {
                MyLog.debug("write error size too long.");
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            MyLog.debug("onDescriptorWrite status: {}, descriptor: {}, gatt: {}", status, descriptor, gatt);
            super.onDescriptorWrite(gatt, descriptor, status);
        }
    };

    MyBluetoothDevice(BluetoothDevice bluetoothDevice) {
        if (null == bluetoothDevice) throw new IllegalArgumentException("bluetoothDevice can not be null!");
        this.device = bluetoothDevice;
    }

    /**
     * 建立连接
     *
     * @param timeout 超时时间，单位：秒。
     * @param reply   回复对象
     */
    void connect(int timeout, IReply reply) {
//        boolean _isConnected = this._bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).contains(_myDevice.device);
//        MyLog.debug("isConnected:" + _isConnected);
        if (this.connected) {
            MyLog.debug("already connected!");
            return;
        }

        if (null != this.currentReply) {
            MyLog.debug("please wait for another task to complete.");
            return;
        }

        if (!MyBluetoothManager.me().isEnabled()) throw new MyBluetoothException(MyBluetoothException.CODE_BLUETOOTH_NOT_ENABLE, "please turn on bluetooth.");

        if (null != this.gatt) {
            // 可能之前建立连接未成功，也未释放资源，先释放资源，再重新连接。
            this.disconnect();
        }

        this.currentReply = reply;

        if (PlatformHelper.sdkGE23()) {
            this.gatt = this.device.connectGatt(PlatformHelper.me().getActivity(), false, this.gattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            this.gatt = this.device.connectGatt(PlatformHelper.me().getActivity(), false, this.gattCallback);
        }

        MyHandler.me().delayed(MyHandler.ID_CONNECT_TIMEOUT, timeout * 1000, new ICallback() {
            @Override
            public void execute(Object args) {
                if (null != currentReply) {
                    currentReply.success(false);
                    currentReply = null;
                }
                disconnect();
            }
        });
    }

    /**
     * 建立自动连接
     */
    void autoConnect() {

    }

    /**
     * 发现服务
     *
     * @param timeout 超时时间，单位：秒。
     * @param reply   回复对象
     */
    void discoverServices(int timeout, IReply reply) {
        if (null != this.currentReply) {
            MyLog.debug("please wait for another task to complete.");
            return;
        }
        if (!this.connected) {
//            throw new IllegalStateException("please connect first!");
            reply.error(MyBluetoothException.CODE_CONNECT_FIRST, "please connect first!");
            return;
        }
        this.currentReply = reply;
        this.characteristicMap.clear();
        this.gatt.discoverServices();

        MyHandler.me().delayed(MyHandler.ID_DISCOVER_SERVICES_TIMEOUT, timeout * 1000, new ICallback() {
            @Override
            public void execute(Object args) {
                if (null != currentReply) {
                    currentReply.error("timeout");
                    currentReply = null;
                }
            }
        });
    }

    /**
     * 设置特征通知开关
     *
     * @param characteristicId
     * @param enable
     * @return
     */
    boolean characteristicSetNotification(String characteristicId, boolean enable) {
        if (!this.characteristicMap.containsKey(characteristicId)) return false;
        BluetoothGattCharacteristic _characteristic = this.characteristicMap.get(characteristicId);

        boolean _setNotificationResult = this.gatt.setCharacteristicNotification(_characteristic, enable);
        MyLog.debug(characteristicId + " setNotificationResult: {}", _setNotificationResult);
        BluetoothGattDescriptor _gattDescriptor = _characteristic.getDescriptor(BluetoothConstants.descCharacteristicClientConfig);
        if (null != _gattDescriptor) {
            byte[] _value = enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            _gattDescriptor.setValue(_value);
            return gatt.writeDescriptor(_gattDescriptor);
        }
        return false;
    }

    /**
     * 从指定特征读取数据
     *
     * @param characteristicId
     * @return
     */
    boolean characteristicRead(String characteristicId) {
        if (!this.characteristicMap.containsKey(characteristicId)) return false;
        BluetoothGattCharacteristic _characteristic = this.characteristicMap.get(characteristicId);

        return this.gatt.readCharacteristic(_characteristic);
    }

    /**
     * 向指定特征写入数据
     *
     * @param characteristicId
     * @param value
     * @param withoutResponse
     * @return
     */
    boolean characteristicWrite(String characteristicId, byte[] value, boolean withoutResponse) {
        if (!this.characteristicMap.containsKey(characteristicId)) return false;
        BluetoothGattCharacteristic _characteristic = this.characteristicMap.get(characteristicId);

        MyLog.debug("characteristicWrite length: {}", value.length);
        if (!_characteristic.setValue(value)) {
            MyLog.debug("could not set the local value of characteristic!");
            return false;
        }
        if (withoutResponse) {
            _characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        } else {
            _characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }

        return this.gatt.writeCharacteristic(_characteristic);
    }

    /**
     * 断开连接
     *
     * @return 是否成功
     */
    boolean disconnect() {
        this.connected = false;
        this.currentReply = null;
        MyHandler.me().removeCallback(MyHandler.ID_CONNECT_TIMEOUT);
        if (null == this.gatt) return false;
        MyLog.debug("disconnect gatt for : {}", this.device);
        this.gatt.disconnect();
        int _connectionState = MyBluetoothManager.me().getConnectionState(this.device);
        if (BluetoothProfile.STATE_DISCONNECTED == _connectionState && null != this.gatt) {
            MyLog.debug("close gatt for : {}", this.device);
            this.gatt.close();
        }
        this.gatt = null;
        return true;
    }

    // 刷新系统蓝牙设备缓存信息。
    boolean _refreshCache() {
        try {
            Method _refreshMethod = this.gatt.getClass().getMethod("refresh");
            if (null != _refreshMethod) {
                return (Boolean) _refreshMethod.invoke(this.gatt);
            }
        } catch (Exception _e) {
            MyLog.debug("refreshing device error: {}", _e.getMessage());
        }
        return false;
    }

    void destroy() {

    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder _builder = new StringBuilder();
        _builder.append("MyBluetoothDevice{device:").append(this.device);
        _builder.append(",gatt:").append(this.gatt);
        _builder.append("}");
        return _builder.toString();
    }
}
