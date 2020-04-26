package com.wee0.flutter.bluetooth_helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import java.io.ObjectStreamException;
import java.util.Calendar;

/**
 * 自定义闹钟管理器
 */
final class MyAlarmManager {

    // 默认间隔
//    private static final long DEF_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    private static final long DEF_INTERVAL = 15000L;

    private final AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    void start() {
        if (null != this.alarmIntent) return;

        Intent _intent = new Intent(PlatformHelper.me().getApplication(), MyAlarmIntentService.class);
        _intent.setAction(MyAlarmIntentService.ACTION_DEFAULT);
        PendingIntent _pendingIntent = PendingIntent.getService(PlatformHelper.me().getApplication(), 0, _intent, PendingIntent.FLAG_NO_CREATE);
        if (null != _pendingIntent) {
            MyLog.debug("already exists pendingIntent: {}", _pendingIntent);
            return;
        }

//        ComponentName _alarmReceiver = new ComponentName(PlatformHelper.me().getApplication(), MyAlarmReceiver.class);
//        PackageManager _packageManager = PlatformHelper.me().getApplication().getPackageManager();
//        _packageManager.setComponentEnabledSetting(_alarmReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        MyLog.debug("MyAlarmManager start.");
        this.alarmIntent = PendingIntent.getBroadcast(PlatformHelper.me().getApplication(), 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis(), DEF_INTERVAL, this.alarmIntent);
    }

    void stop() {
        if (null == this.alarmIntent) return;
        MyLog.debug("MyAlarmManager stop.");
        this.alarmManager.cancel(this.alarmIntent);
        this.alarmIntent = null;
    }

    /************************************************************
     ************* 单例对象。
     ************************************************************/
    private MyAlarmManager() {
        if (null != MyAlarmManagerHolder._INSTANCE) {
            // 防止使用反射API创建对象实例。
            throw new IllegalStateException("that's not allowed!");
        }

        AlarmManager _alarmManager = (AlarmManager) PlatformHelper.me().getApplication().getSystemService(Context.ALARM_SERVICE);
        if (null == _alarmManager) throw new IllegalStateException("alarmManager can not be null!");
        this.alarmManager = _alarmManager;
        MyLog.debug("MyAlarmManager.init. alarmManager: {}", this.alarmManager);
    }

    // 当前对象唯一实例持有者。
    private static final class MyAlarmManagerHolder {
        private static final MyAlarmManager _INSTANCE = new MyAlarmManager();
    }

    // 防止使用反序列化操作获取多个对象实例。
    private Object readResolve() throws ObjectStreamException {
        return MyAlarmManagerHolder._INSTANCE;
    }

    /**
     * 获取当前对象唯一实例。
     *
     * @return 当前对象唯一实例
     */
    public static MyAlarmManager me() {
        return MyAlarmManagerHolder._INSTANCE;
    }
}
