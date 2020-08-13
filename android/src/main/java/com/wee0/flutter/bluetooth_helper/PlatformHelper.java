package com.wee0.flutter.bluetooth_helper;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import java.io.ObjectStreamException;

/**
 * 平台相关信息操作助手
 */
final class PlatformHelper {

    /**
     * @return sdk版本是否大于等于18
     */
    public static boolean sdkGE18() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    /**
     * @return sdk版本是否大于等于19
     */
    public static boolean sdkGE19() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT;
    }

    /**
     * @return sdk版本是否大于等于21
     */
    public static boolean sdkGE21() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * @return sdk版本是否大于等于23
     */
    public static boolean sdkGE23() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

//    /**
//     * @return sdk版本是否大于等于26
//     */
//    public static boolean sdkGE26() {
//        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
//    }

    /******************************
     * 非静态部分
     ******************************/
    private Application _application;
    private Activity _activity;

    void setApplication(Application application) {
        this._application = application;
    }

    public Application getApplication() {
        return this._application;
    }

    void setActivity(Activity activity) {
        this._activity = activity;
    }

    public Activity getActivity() {
        return this._activity;
    }

    /**
     * @return 获取系统通知服务管理对象
     */
    public NotificationManager getNotificationManager() {
        if (null == this._application) return null;
        return (NotificationManager) this._application.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * 资源销毁
     */
    public void destroy() {
        if (null != _activity) _activity = null;
        if (null != _application) _application = null;
    }

    /************************************************************
     ************* 单例对象。
     ************************************************************/
    private PlatformHelper() {
        if (null != PlatformHelperHolder._INSTANCE) {
            // 防止使用反射API创建对象实例。
            throw new IllegalStateException("that's not allowed!");
        }
    }

    // 当前对象唯一实例持有者。
    private static final class PlatformHelperHolder {
        private static final PlatformHelper _INSTANCE = new PlatformHelper();
    }

    // 防止使用反序列化操作获取多个对象实例。
    private Object readResolve() throws ObjectStreamException {
        return PlatformHelperHolder._INSTANCE;
    }

    /**
     * 获取当前对象唯一实例。
     *
     * @return 当前对象唯一实例
     */
    public static PlatformHelper me() {
        return PlatformHelperHolder._INSTANCE;
    }
}
