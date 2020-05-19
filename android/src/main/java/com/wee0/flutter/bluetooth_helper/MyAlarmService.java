package com.wee0.flutter.bluetooth_helper;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * 自定义闹钟接收器
 */
public class MyAlarmService extends IntentService {

    static final String ACTION_DEFAULT = "com.wee0.flutter.bluetooth_helper.action.alarm.default";

    public MyAlarmService() {
        super("MyAlarmIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        MyLog.debug("MyAlarmService onHandleIntent intent: {}", intent);
        if (null == intent) return;
        String _action = intent.getAction();
        MyLog.debug("intent.action: {}", _action);
        if (ACTION_DEFAULT.equals(_action)) {
            MyLog.debug("intent.action.default...");
//            MyAlarmManager.me().start();
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        MyLog.debug("MyAlarmIntentService onStartCommand. intent: {}, flags: {}, startId: {}", intent, flags, startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        MyLog.debug("MyAlarmService onCreate.");
        super.onCreate();
        if (PlatformHelper.sdkGE26()) {
            Context _context = PlatformHelper.me().getActivity();

//            Intent _mainActivityIntent = new Intent(_context, _context.getClass());
//            PendingIntent _pIntent = PendingIntent.getActivity(_context, 0, _mainActivityIntent, 0);

            final String _CHANNEL_ID = "WEE0_BT_001";
            final String _CHANNEL_NAME = "WEE0_BT_ALARM";

            NotificationCompat.Builder _builder = new NotificationCompat.Builder(_context, _CHANNEL_ID);
//            _builder.setContentIntent(_pIntent);
//            _builder.setSmallIcon(R.mipmap.ic_launcher); // 设置状态栏内的小图标
            _builder.setContentTitle("TestTitle");
            _builder.setContentText("TestText");
            _builder.setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
            Notification _notification = _builder.build();

            NotificationChannel _channel = new NotificationChannel(_CHANNEL_ID, _CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            _channel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            _channel.setShowBadge(false);//是否显示角标
            _channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

            MyLog.debug("startForeground... channel: {}", _channel);
            PlatformHelper.me().getNotificationManager().createNotificationChannel(_channel);
            this.startForeground(1, _notification);
        } else {
            MyLog.debug("start...");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        MyLog.debug("MyAlarmIntentService onDestroy.");
        super.onDestroy();
    }
}
