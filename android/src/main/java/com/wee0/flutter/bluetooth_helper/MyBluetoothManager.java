package com.wee0.flutter.bluetooth_helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;

import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 自定义蓝牙管理
 */
final class MyBluetoothManager {

    // 托管的蓝牙管理器对象
    private BluetoothManager _bluetoothManager;
    private BluetoothAdapter _bluetoothAdapter;
    private MyBluetoothLeScanner _leScanner = null;
    private Map<String, MyBluetoothDevice> _deviceMap;

    /**
     * 初始化逻辑
     *
     * @param bluetoothManager 托管的蓝牙管理器对象
     */
    void init(BluetoothManager bluetoothManager) {
        if (null == bluetoothManager) throw new IllegalStateException("bluetoothManager can not be null!");
        this._bluetoothManager = bluetoothManager;
        this._leScanner = new MyBluetoothLeScanner();
        MyLog.debug("MyBluetoothManager.init. bluetoothManager: {}, isEnable: {}", this._bluetoothManager, this.isEnabled());

//        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        activity.startActivityForResult(enableBtIntent, 1);
        this._deviceMap = new HashMap<>(8);
    }

    /**
     * 资源释放逻辑
     */
    void destroy() {
        if (null != this._deviceMap) {
            Iterator<MyBluetoothDevice> _devices = this._deviceMap.values().iterator();
            while (_devices.hasNext()) {
                _devices.next().destroy();
            }
            this._deviceMap.clear();
            this._deviceMap = null;
        }
        this._leScanner = null;
        this._bluetoothAdapter = null;
        this._bluetoothManager = null;
    }

    // 获取设备连接状态
    int getConnectionState(BluetoothDevice device) {
        // BluetoothProfile.STATE_DISCONNECTED
        return this._bluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
    }

    // 根据地址获取设备对象
    BluetoothDevice getBluetoothDevice(String address) {
        return this._bluetoothAdapter.getRemoteDevice(address);
    }


    /**
     * @return 蓝牙是否开启
     */
    public boolean isEnabled() {
        if (null == this._bluetoothAdapter) this._bluetoothAdapter = this._bluetoothManager.getAdapter();
        return null != this._bluetoothAdapter && this._bluetoothAdapter.isEnabled();
    }

    public int getDeviceState(String deviceId) {
        BluetoothDevice _device = this._bluetoothAdapter.getRemoteDevice(deviceId);
        List<BluetoothDevice> _deviceList = this._bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        if (_deviceList.contains(_device)) {
            return 1;
        }
        return 0;
    }

    /**
     * 缓存指定地址的蓝牙设备对象
     *
     * @param address 设备地址
     * @return 自定义蓝牙设备对象
     */
    public MyBluetoothDevice cacheDevice(String address) {
        if (null == address || 0 == (address = address.trim()).length())
            throw new IllegalArgumentException("device address can not be empty!");
        MyBluetoothDevice _myDevice = this._deviceMap.get(address);
        if (null == _myDevice) {
            BluetoothDevice _device = this._bluetoothAdapter.getRemoteDevice(address);
            _myDevice = new MyBluetoothDevice(_device);
            this._deviceMap.put(address, _myDevice);
        }
        MyLog.debug("myDevice: {}", _myDevice);
        return _myDevice;
    }

    /**
     * 获取指定地址的蓝牙设备对象
     *
     * @param address 设备地址
     * @return 自定义蓝牙设备对象
     */
    public MyBluetoothDevice getDevice(String address) {
        if (null == address || 0 == (address = address.trim()).length())
            throw new IllegalArgumentException("device address can not be empty!");
        return this._deviceMap.get(address);
    }

    /**
     * 开始扫描
     */
    public void startScan(String deviceName, String deviceAddress) {

        if (!isEnabled()) throw new MyBluetoothException(MyBluetoothException.CODE_BLUETOOTH_NOT_ENABLE, "please turn on bluetooth.");

        if (null == this._leScanner.getBluetoothLeScanner()) {
            final BluetoothLeScanner _scanner = this._bluetoothAdapter.getBluetoothLeScanner();
            if (null == _scanner) throw new IllegalStateException("BluetoothLeScanner can not be null!");
            this._leScanner.setBluetoothLeScanner(_scanner);
        }
        this._leScanner.startScan(deviceName, deviceAddress);
    }

    /**
     * 结束扫描，获取扫描结果
     *
     * @return 扫描结果
     */
    public Map<String, Map<String, String>> stopScan() {
        return this._leScanner.stopScan();
    }

    /************************************************************
     ************* 单例对象。
     ************************************************************/
    private MyBluetoothManager() {
        if (null != MyBluetoothManagerHolder._INSTANCE) {
            // 防止使用反射API创建对象实例。
            throw new IllegalStateException("that's not allowed!");
        }
    }

    // 当前对象唯一实例持有者。
    private static final class MyBluetoothManagerHolder {
        private static final MyBluetoothManager _INSTANCE = new MyBluetoothManager();
    }

    // 防止使用反序列化操作获取多个对象实例。
    private Object readResolve() throws ObjectStreamException {
        return MyBluetoothManagerHolder._INSTANCE;
    }

    /**
     * 获取当前对象唯一实例。
     *
     * @return 当前对象唯一实例
     */
    public static MyBluetoothManager me() {
        return MyBluetoothManagerHolder._INSTANCE;
    }

}
