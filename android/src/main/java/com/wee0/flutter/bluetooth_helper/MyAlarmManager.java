package com.wee0.flutter.bluetooth_helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import java.io.ObjectStreamException;

/**
 * 自定义闹钟管理器
 */
final class MyAlarmManager {

    // 默认间隔
//    private static final long DEF_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    private static final long DEF_INTERVAL = 5000L;

    private final AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    void start() {
        if (null != this.alarmIntent) return;

        Context _context = PlatformHelper.me().getActivity();

//        Intent _intent = new Intent(_context, MyAlarmReceiver.class);
        Intent _intent = new Intent(_context, MyAlarmService.class);
        _intent.setAction(MyAlarmService.ACTION_DEFAULT);
        PendingIntent _pendingIntent = null;
        if (PlatformHelper.sdkGE26()) {
            _context.startForegroundService(_intent);
            _pendingIntent = PendingIntent.getForegroundService(_context, 1, _intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            _context.startService(_intent);
            _pendingIntent = PendingIntent.getService(_context, 1, _intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
//        PendingIntent _pendingIntent = PendingIntent.getService(PlatformHelper.me().getActivity(), 0, _intent, PendingIntent.FLAG_NO_CREATE);
//        if (null != _pendingIntent) {
//            MyLog.debug("already exists pendingIntent: {}", _pendingIntent);
//            return;
//        }
//        this.alarmIntent = PendingIntent.getBroadcast(_context, 0, _intent, 0);
        this.alarmIntent = _pendingIntent;
        this.alarmManager.cancel(_pendingIntent);

//        ComponentName _alarmReceiver = new ComponentName(PlatformHelper.me().getApplication(), MyAlarmReceiver.class);
//        PackageManager _packageManager = PlatformHelper.me().getApplication().getPackageManager();
//        _packageManager.setComponentEnabledSetting(_alarmReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

//        long _nextTime = SystemClock.elapsedRealtime() + DEF_INTERVAL;
        long _nextTime = System.currentTimeMillis() + DEF_INTERVAL;
        MyLog.debug("MyAlarmManager nextTime: {}, intent: {}, pendingIntent: {}", _nextTime, _intent, _pendingIntent);

//        MyLog.debug("FLAG_ONE_SHOT:{}, FLAG_NO_CREATE:{}, FLAG_UPDATE_CURRENT:{}, FLAG_CANCEL_CURRENT{}, FLAG_IMMUTABLE:{}.", PendingIntent.FLAG_ONE_SHOT, PendingIntent.FLAG_NO_CREATE, PendingIntent.FLAG_UPDATE_CURRENT, PendingIntent.FLAG_CANCEL_CURRENT, PendingIntent.FLAG_IMMUTABLE);
//        this.alarmManager.set(AlarmManager.RTC_WAKEUP, _nextTime, this.alarmIntent);
        this.alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, _nextTime, DEF_INTERVAL, _pendingIntent);
//        this.alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, _nextTime, DEF_INTERVAL, _pendingIntent);
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
