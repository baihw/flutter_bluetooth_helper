package com.wee0.flutter.bluetooth_helper;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

/**
 * 自定义闹钟接收器
 */
final class MyAlarmIntentService extends IntentService {

    static final String ACTION_DEFAULT = "com.wee0.flutter.bluetooth_helper.action.alarm.default";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MyAlarmIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        MyLog.debug("MyAlarmIntentService intent: {}", intent);
        if (null == intent) return;
        String _action = intent.getAction();
        MyLog.debug("intent.action: {}", _action);
        if (ACTION_DEFAULT.equals(_action)) {
            MyLog.debug("intent.action.default...");
        }
    }
}
