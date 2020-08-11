//package com.wee0.flutter.bluetooth_helper;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//
//public class MyAlarmReceiver extends BroadcastReceiver {
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        MyLog.debug("MyAlarmReceiver onReceive. intent: {}, context: {}", intent, context);
//        Intent _intent = new Intent(context, MyAlarmService.class);
//        context.startService(_intent);
//    }
//}
