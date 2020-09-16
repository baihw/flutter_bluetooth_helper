package com.wee0.flutter.bluetooth_helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.ObjectStreamException;
import java.util.ArrayList;
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
    private String _lastVisitDeviceAddress = null;

    // 最后一次开关时间
    private long lastChangeTime = 0l;

    private final BroadcastReceiver _receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String _action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(_action)) {
                final int _state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                MyLog.debug("onStateChange, state: {}", _state);
                lastChangeTime = System.currentTimeMillis();
                switch (_state) {
                    case BluetoothAdapter.STATE_OFF:
                        MyMethodRouter.me().callOnStateChange(0);
                        break;
                    case BluetoothAdapter.STATE_ON:
                        MyMethodRouter.me().callOnStateChange(1);
                        // 蓝牙刚打开时，自动执行一次随机扫描。
                        _backgroundScan();
                        break;
//                    case BluetoothAdapter.STATE_TURNING_OFF:
//                    case BluetoothAdapter.STATE_TURNING_ON:
                    default:
                        MyLog.debug("ignore state: {}", _state);
                        break;
                }
            }
        }
    };

    /**
     * 初始化逻辑
     *
     * @param bluetoothManager 托管的蓝牙管理器对象
     */
    void init(BluetoothManager bluetoothManager) {
        if (null == bluetoothManager)
            throw new IllegalStateException("bluetoothManager can not be null!");
        this._bluetoothManager = bluetoothManager;
        this._leScanner = new MyBluetoothLeScanner();
        MyLog.debug("MyBluetoothManager.init. bluetoothManager: {}, isEnable: {}", this._bluetoothManager, this.isEnabled());

//        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        activity.startActivityForResult(enableBtIntent, 1);
        this._deviceMap = new HashMap<>(8);

        IntentFilter _filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        PlatformHelper.me().getActivity().registerReceiver(_receiver, _filter);
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
        if (null != PlatformHelper.me().getActivity())
            PlatformHelper.me().getActivity().unregisterReceiver(_receiver);
    }

    // 获取设备连接状态
    int getConnectionState(BluetoothDevice device) {
        try {
            return this._bluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
        } catch (Exception e) {
            return BluetoothProfile.STATE_DISCONNECTED;
        }
    }

    // 根据地址获取设备对象
    BluetoothDevice getBluetoothDevice(String address) {
        return this._bluetoothAdapter.getRemoteDevice(address);
    }

    /**
     * @return 最后次状态改变时间
     */
    long getLastChangeTime(){
        return lastChangeTime;
    }

    /**
     * @return 蓝牙是否开启
     */
    public boolean isEnabled() {
        if (null == this._bluetoothAdapter)
            this._bluetoothAdapter = this._bluetoothManager.getAdapter();
        return null != this._bluetoothAdapter && this._bluetoothAdapter.isEnabled();
    }

    /**
     * 开启蓝牙
     *
     * @return 是否开启
     */
    public boolean enable() {
        if (null == this._bluetoothAdapter)
            this._bluetoothAdapter = this._bluetoothManager.getAdapter();
        if (null == this._bluetoothAdapter) return false;
        if (this._bluetoothAdapter.isEnabled()) return true;
        return this._bluetoothAdapter.enable();
    }

    /**
     * 关闭蓝牙
     *
     * @return 是否关闭
     */
    public boolean disable() {
        if (null == this._bluetoothAdapter)
            this._bluetoothAdapter = this._bluetoothManager.getAdapter();
        if (null == this._bluetoothAdapter) return false;
        if (this._bluetoothAdapter.isEnabled())
            return this._bluetoothAdapter.disable();
        return true;
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
        this._lastVisitDeviceAddress = address;
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
    public void startScan(String deviceName, String deviceAddress, final IReply reply) {

        if (!isEnabled())
            throw new MyBluetoothException(MyBluetoothException.CODE_BLUETOOTH_NOT_ENABLE, "please turn on bluetooth.");

        if (null == this._leScanner.getBluetoothLeScanner()) {
            final BluetoothLeScanner _scanner = this._bluetoothAdapter.getBluetoothLeScanner();
            if (null == _scanner)
                throw new IllegalStateException("BluetoothLeScanner can not be null!");
            this._leScanner.setBluetoothLeScanner(_scanner);
        }
        this._leScanner.startScan(deviceName, deviceAddress, reply);
    }

    /**
     * 结束扫描，获取扫描结果
     *
     * @return 扫描结果
     */
    public Map<String, Map<String, String>> stopScan() {
        return this._leScanner.stopScan();
    }

    // 后台扫描结果回调方法
    private ScanCallback _backgroundScanCallback = null;

    /**
     * 执行后台扫描
     */
    private void _backgroundScan() {
        if (null != this._backgroundScanCallback) {
            MyLog.debug("already exists background scan.");
            return;
        }
        if (!MyLocationManager.me.isEnabled(true)) {
            MyLog.debug("location is not enable, skip background scan.");
            return;
        }
        final BluetoothLeScanner _leScanner;
        if (null == _getBluetoothAdapter() || null == (_leScanner = _getBluetoothAdapter().getBluetoothLeScanner())) {
            MyLog.debug("BluetoothLeScanner is not found, skip background scan.");
            return;
        }

        this._backgroundScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
//                MyLog.debug("background scan result: {}", result);
                super.onScanResult(callbackType, result);
                if (null != _lastVisitDeviceAddress) {
                    BluetoothDevice _device = result.getDevice();
                    if (null == _device) return;
                    if (_lastVisitDeviceAddress.equals(_device.getAddress())) {
                        MyLog.debug("stop background scan by find device: {}.", _lastVisitDeviceAddress);
                        MyHandler.me().removeCallback(MyHandler.ID_BACKGROUND_SCAN_STOP);
                        try {
                            _leScanner.stopScan(_backgroundScanCallback);
                        } catch (IllegalStateException e) {
                            MyLog.debug("stopScan error: {}", e.getMessage());
                        }
                        _backgroundScanCallback = null;
                    }
                }
            }
        };

        List<ScanFilter> _scanFilters = null;

        if (null != this._lastVisitDeviceAddress) {
            ScanFilter.Builder _filterBuilder = new ScanFilter.Builder();
            _filterBuilder.setDeviceAddress(this._lastVisitDeviceAddress);
            _scanFilters = new ArrayList<>();
            _scanFilters.add(_filterBuilder.build());
        }

        ScanSettings.Builder _settingsBuilder = new ScanSettings.Builder();
        _settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);

        _leScanner.startScan(_scanFilters, _settingsBuilder.build(), this._backgroundScanCallback);
        long _delayMillis = null == _scanFilters ? 2000 : 30000;
        MyHandler.me().delayed(MyHandler.ID_BACKGROUND_SCAN_STOP, _delayMillis, new ICallback() {
            @Override
            public void execute(Object args) {
                MyLog.debug("stop background scan by timeout.");
                try {
                    _leScanner.stopScan(_backgroundScanCallback);
                } catch (IllegalStateException e) {
                    MyLog.debug("stopScan error: {}", e.getMessage());
                }
                _backgroundScanCallback = null;
            }
        });
    }

    /**
     * @return 获取蓝牙适配器
     */
    private BluetoothAdapter _getBluetoothAdapter() {
        if (null == this._bluetoothAdapter)
            this._bluetoothAdapter = this._bluetoothManager.getAdapter();
        return this._bluetoothAdapter;
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
