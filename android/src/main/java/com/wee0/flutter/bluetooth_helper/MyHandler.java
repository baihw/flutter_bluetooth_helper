package com.wee0.flutter.bluetooth_helper;

import android.os.Handler;
import android.os.Message;

import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;

final class MyHandler {

    /**
     * 扫描超时
     */
    public static final int ID_SCAN_TIMEOUT = 0;

    /**
     * 结束后台扫描
     */
    public static final int ID_BACKGROUND_SCAN_STOP = 4;

    /**
     * 连接超时
     */
    public static final int ID_CONNECT_TIMEOUT = 1;

    /**
     * 发现服务超时
     */
    public static final int ID_DISCOVER_SERVICES_TIMEOUT = 2;

    private final Handler handler;
    private final Map<Integer, ICallback> callbackMap;

    /**
     * 注册指定延时后执行的回调。
     *
     * @param id
     * @param delayMillis
     * @param callback
     */
    void delayed(int id, long delayMillis, ICallback callback) {
        if (null == callback) throw new IllegalArgumentException("callback can not be null!");
        this.callbackMap.put(id, callback);
        this.handler.sendEmptyMessageDelayed(id, delayMillis);
    }

    /**
     * 是否存在指定标识的回调。
     *
     * @param id
     * @return
     */
    boolean hasCallback(int id) {
        if (!this.callbackMap.containsKey(id)) return false;
        if (this.handler.hasMessages(id)) return true;
        this.callbackMap.remove(id);
        return false;
    }

    /**
     * 移除指定标识的回调
     *
     * @param id
     */
    void removeCallback(int id) {
        if (this.callbackMap.containsKey(id)) {
            this.callbackMap.remove(id);
            if (this.handler.hasMessages(id)) {
                this.handler.removeMessages(id);
            }
        }
    }

    /************************************************************
     ************* 单例对象。
     ************************************************************/
    private MyHandler() {
        if (null != MyHandlerHolder._INSTANCE) {
            // 防止使用反射API创建对象实例。
            throw new IllegalStateException("that's not allowed!");
        }
        this.callbackMap = new HashMap<>(32, 1.0f);

        this.handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (!callbackMap.containsKey(msg.what))
                    return false;
                ICallback _callback = callbackMap.remove(msg.what);
                if (null != _callback) _callback.execute(null);
                return true;
            }
        });
    }

    // 当前对象唯一实例持有者。
    private static final class MyHandlerHolder {
        private static final MyHandler _INSTANCE = new MyHandler();
    }

    // 防止使用反序列化操作获取多个对象实例。
    private Object readResolve() throws ObjectStreamException {
        return MyHandlerHolder._INSTANCE;
    }

    /**
     * 获取当前对象唯一实例。
     *
     * @return 当前对象唯一实例
     */
    public static MyHandler me() {
        return MyHandlerHolder._INSTANCE;
    }
}
