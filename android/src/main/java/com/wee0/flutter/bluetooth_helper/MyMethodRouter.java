package com.wee0.flutter.bluetooth_helper;

import android.service.media.MediaBrowserService;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.StandardMessageCodec;

/**
 * 方法路由
 */
final class MyMethodRouter implements BasicMessageChannel.MessageHandler<Object> {
    /**
     * 方法名
     */
    static final String KEY_METHOD = "method";

    /**
     * 参数名
     */
    static final String KEY_ARGS = "args";

    static final String C_onStateChange = "onStateChange";
    static final String C_onServicesDiscovered = "onServicesDiscovered";
    static final String C_onCharacteristicNotifyData = "onCharacteristicNotifyData";
    static final String C_onCharacteristicReadResult = "onCharacteristicReadResult";
    static final String C_onCharacteristicWriteResult = "onCharacteristicWriteResult";

    // 消息交互通道
    private BasicMessageChannel<Object> basicMessageChannel;
//    private MethodChannel methodChannel;
//    private EventChannel eventChannel;


    // Flutter到Android的方法调用
    @Override
    public void onMessage(Object message, BasicMessageChannel.Reply<Object> reply) {
//        MyLog.debug("message.type:" + message.getClass() + "message:" + message);
        IReply _reply = new BasicMessageChannelReply(reply);
        if (null == message) {
            _reply.error("message can not be null!");
            return;
        }
        if (null == message || !(message instanceof Map)) {
            _reply.error("unSupport message type:" + message.getClass().getName());
            return;
        }
        try {
            Map<String, Object> _messageData = (Map<String, Object>) message;
            Object _methodObj = _messageData.get(KEY_METHOD);
            String _method = null == _methodObj ? null : _methodObj.toString().trim();
            if (null == _method || 0 == _method.length()) {
                _reply.error("method can not be empty!");
                return;
            }
            MyLog.debug("invoke method: {}", _method);
            if ("debug".equals(_method)) {
//                Object _enable = _messageData.get(KEY_ARGS);
                MyLog.enableDebug();
                _reply.success(true);
                return;
            }
            if ("keepAlive".equals(_method)) {
                MyAlarmManager.me().start();
                _reply.success(true);
                return;
            }
            if ("bluetoothIsEnable".equals(_method)) {
                _reply.success(MyBluetoothManager.me().isEnabled());
                return;
            }
            if ("locationIsEnable".equals(_method)) {
                _reply.success(MyLocationManager.me.isEnabled());
                return;
            }
            if ("startScan".equals(_method)) {
                String _deviceName = null;
                String _deviceId = null;
                if (_messageData.containsKey(KEY_ARGS)) {
                    Map<String, Object> _args = (Map<String, Object>) _messageData.get(KEY_ARGS);
                    _deviceName = _args.containsKey("deviceName") ? (String) _args.get("deviceName") : null;
                    _deviceId = _args.containsKey("deviceId") ? (String) _args.get("deviceId") : null;
                }
                MyBluetoothManager.me().startScan(_deviceName, _deviceId);
                _reply.success(true);
                return;
            }
            if ("stopScan".equals(_method)) {
                Map<String, Map<String, String>> _scanResult = MyBluetoothManager.me().stopScan();
                _reply.success(_scanResult);
                return;
            }

            // 以下操作都必须传递键值对参数。
            Map<String, Object> _args = (Map<String, Object>) _messageData.get(KEY_ARGS);
            // 设备标识所有操作都必须传递。
            String _deviceId = (String) _args.get("deviceId");
            if (null == _deviceId || 0 == (_deviceId = _deviceId.trim()).length()) {
                _reply.error("deviceId can not be empty!");
                return;
            }
            if ("getDeviceState".equals(_method)) {
                int _deviceState = MyBluetoothManager.me().getDeviceState(_deviceId);
                _reply.success(_deviceState);
                return;
            }
            if ("connect".equals(_method)) {
                Object _timeoutObj = _args.get("timeout");
                int _timeout = null == _timeoutObj ? 3 : Integer.parseInt(_timeoutObj.toString());
                MyBluetoothManager.me().cacheDevice(_deviceId).connect(_timeout, _reply);
                return;
            }
            if ("discoverServices".equals(_method)) {
                Object _timeoutObj = _args.get("timeout");
                int _timeout = null == _timeoutObj ? 3 : Integer.parseInt(_timeoutObj.toString());
                MyBluetoothManager.me().cacheDevice(_deviceId).discoverServices(_timeout, _reply);
                return;
            }
            if ("setCharacteristicNotification".equals(_method)) {
                String _characteristicId = (String) _args.get("characteristicId");
                Boolean _enable = (Boolean) _args.get("enable");
                boolean _setCharacteristicNotificationResult = MyBluetoothManager.me().cacheDevice(_deviceId).characteristicSetNotification(_characteristicId, _enable);
                _reply.success(_setCharacteristicNotificationResult);
                return;
            }
            if ("characteristicRead".equals(_method)) {
                String _characteristicId = (String) _args.get("characteristicId");
                boolean _characteristicReadResult = MyBluetoothManager.me().cacheDevice(_deviceId).characteristicRead(_characteristicId);
                _reply.success(_characteristicReadResult);
                return;
            }
            if ("characteristicWrite".equals(_method)) {
                String _characteristicId = (String) _args.get("characteristicId");
                byte[] _data = (byte[]) _args.get("data");

                boolean _characteristicWriteResult = MyBluetoothManager.me().cacheDevice(_deviceId).characteristicWrite(_characteristicId, _data, false);
                _reply.success(_characteristicWriteResult);
                return;
            }
            if ("disconnect".equals(_method)) {
                MyBluetoothDevice _device = MyBluetoothManager.me().getDevice(_deviceId);
                boolean _disconnectResult = null == _device ? false : _device.disconnect();
                _reply.success(_disconnectResult);
                return;
            }
//            if ("".equals(_method)) {
//                return;
//            }

            _reply.error("unKnow method: " + _method);
        } catch (MyBluetoothException e) {
            _reply.error(e.getCode(), e.getMessage());
        } catch (RuntimeException e) {
            _reply.error(e.getMessage());
        }
    }

    /**
     * 调用Dart方法
     *
     * @param methodName 方法名称
     * @param args       方法参数
     */
    void callMethod(final String methodName, final Object args) {
        final Map<String, Object> _params = new HashMap<>(2);
        _params.put(KEY_METHOD, methodName);
        _params.put(KEY_ARGS, args);
        PlatformHelper.me().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                basicMessageChannel.send(_params);
            }
        });
    }

    /// 状态变化时的通知
    void callOnStateChange(String deviceId, int deviceState) {
        Map<String, Object> _data = new HashMap<>(2);
        _data.put("deviceId", deviceId);
        _data.put("deviceState", deviceState);
        callMethod(C_onStateChange, _data);
    }

    /// 发现服务时的通知
    void callOnServicesDiscovered(String deviceId, Object data) {
        Map<String, Object> _data = new HashMap<>(2);
        _data.put("deviceId", deviceId);
        _data.put("data", data);
        callMethod(C_onServicesDiscovered, _data);
    }

    /// 接收到广播数据时的通知
    void callOnCharacteristicNotifyData(String deviceId, String characteristicId, Object data) {
        Map<String, Object> _data = new HashMap<>(2);
        _data.put("deviceId", deviceId);
        _data.put("characteristicId", characteristicId);
        _data.put("data", data);
        callMethod(C_onCharacteristicNotifyData, _data);
    }

    /// 接收到读取数据结果时的通知
    void callOnCharacteristicReadResult(String deviceId, String characteristicId, Object data) {
        Map<String, Object> _data = new HashMap<>(2);
        _data.put("deviceId", deviceId);
        _data.put("characteristicId", characteristicId);
        _data.put("data", data);
        callMethod(C_onCharacteristicReadResult, _data);
    }

    /// 接收到写入数据结果时的通知
    void callOnCharacteristicWriteResult(String deviceId, String characteristicId, boolean isOk) {
        Map<String, Object> _data = new HashMap<>(2);
        _data.put("deviceId", deviceId);
        _data.put("characteristicId", characteristicId);
        _data.put("isOk", isOk);
        callMethod(C_onCharacteristicWriteResult, _data);
    }

    /************************************************************
     ************* 单例对象。
     ************************************************************/
    private MyMethodRouter() {
        if (null != MyMethodRouterHolder._INSTANCE) {
            // 防止使用反射API创建对象实例。
            throw new IllegalStateException("that's not allowed!");
        }
    }

    // 当前对象唯一实例持有者。
    private static final class MyMethodRouterHolder {
        private static final MyMethodRouter _INSTANCE = new MyMethodRouter();
    }

    // 防止使用反序列化操作获取多个对象实例。
    private Object readResolve() throws ObjectStreamException {
        return MyMethodRouterHolder._INSTANCE;
    }

    /**
     * 获取当前对象唯一实例。
     *
     * @return 当前对象唯一实例
     */
    public static MyMethodRouter me() {
        return MyMethodRouterHolder._INSTANCE;
    }

    /**
     * 初始化
     *
     * @param messenger 消息信使对象
     */
    void init(BinaryMessenger messenger) {
        if (null != this.basicMessageChannel) {
            MyLog.debug("messageChannel already init.");
            return;
        }
        MyLog.debug("int messageChannel to BinaryMessenger: {}", messenger);
        this.basicMessageChannel = new BasicMessageChannel(messenger, "bluetooth_helper", StandardMessageCodec.INSTANCE);
        this.basicMessageChannel.setMessageHandler(this);
    }

    /**
     * 资源释放
     */
    void destroy() {
        if (null != this.basicMessageChannel) {
            this.basicMessageChannel.setMessageHandler(null);
            this.basicMessageChannel = null;
        }
    }
}
