package com.wee0.flutter.bluetooth_helper;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * BluetoothHelperPlugin
 */
public class BluetoothHelperPlugin implements FlutterPlugin, ActivityAware, PluginRegistry.RequestPermissionsResultListener {

    private static final BluetoothHelperPlugin _me = new BluetoothHelperPlugin();

    // 插件绑定对象
    private static Registrar _registrar;
    private static FlutterPluginBinding _flutterPluginBinding;
    private static ActivityPluginBinding _activityPluginBinding;
    private static BinaryMessenger _messenger;

    // 初始化操作
    private synchronized void _init() {
        MyLog.info("init begin...");
        MyLog.info("android version: {}, sdk: {}", android.os.Build.VERSION.RELEASE, android.os.Build.VERSION.SDK_INT);
        MyLog.info("_registrar: {}", _registrar);
        MyLog.info("_flutterPluginBinding: {}", _flutterPluginBinding);
        MyLog.info("_activityPluginBinding: {}", _activityPluginBinding);
        MyLog.info("_activity: {}", PlatformHelper.me().getActivity());
        MyLog.info("_application: {}", PlatformHelper.me().getApplication());
        MyLog.info("_messenger: {}", _me._messenger);

        if (null != _messenger) {
            MyLog.info("skip repetition init.");
            return;
        }

        Activity _activity = null;
        Application _application = null;
        if (null != _registrar) {
            _registrar.addRequestPermissionsResultListener(_me);
            _activity = _registrar.activity();
            if (null != _activity) {
                _application = _activity.getApplication();
            } else if (null != _registrar.context()) {
                _application = (Application) _registrar.context().getApplicationContext();
            }
            _messenger = _registrar.messenger();
        } else {
            if (null != _activityPluginBinding) {
                _activityPluginBinding.addRequestPermissionsResultListener(_me);
                _activity = _activityPluginBinding.getActivity();
                _application = null == _activity ? (Application) _flutterPluginBinding.getApplicationContext() : _activity.getApplication();
                _messenger = _flutterPluginBinding.getBinaryMessenger();
//                _messenger = _flutterPluginBinding.getFlutterEngine().getDartExecutor();
            }
        }
        PlatformHelper.me().setActivity(_activity);
        PlatformHelper.me().setApplication(_application);

        MyLog.info("_activity: {}", PlatformHelper.me().getActivity());
        MyLog.info("_application: {}", PlatformHelper.me().getApplication());
        MyLog.info("_application.packageName: {}", _application.getPackageName());
        MyLog.info("_messenger: {}", _messenger);

        if (null == _messenger) {
            MyLog.warn("BinaryMessenger cant not be null!");
            return;
        }

        MyMethodRouter.me().init(_messenger);

        BluetoothManager _bluetoothManager = (BluetoothManager) _application.getSystemService(Context.BLUETOOTH_SERVICE);
        MyBluetoothManager.me().init(_bluetoothManager);

        MyLog.info("init end.");
    }

    // 资源销毁操作
    private synchronized void _destroy() {
        MyLog.debug("destroy begin...");
        MyMethodRouter.me().destroy();
        MyBluetoothManager.me().destroy();
        PlatformHelper.me().destroy();

        if (null != _messenger) _messenger = null;
        if (null != _registrar) _registrar = null;
        if (null != _flutterPluginBinding) _flutterPluginBinding = null;
        if (null != _activityPluginBinding) {
            _activityPluginBinding.removeRequestPermissionsResultListener(_me);
            _activityPluginBinding = null;
        }
        if (null != _registrar) _registrar = null;

        MyLog.debug("destroy end.");
    }

    public static void registerWith(Registrar registrar) {
        MyLog.debug("registerWith: {}", registrar);
        _registrar = registrar;
        _me._init();
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        MyLog.debug("onAttachedToEngine: {}", flutterPluginBinding);
        _flutterPluginBinding = flutterPluginBinding;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        MyLog.debug("onDetachedFromEngine: {}", binding);
        _flutterPluginBinding = null;
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        MyLog.debug("onAttachedToActivity: {}", binding);
        _activityPluginBinding = binding;
        _me._init();
    }

    @Override
    public void onDetachedFromActivity() {
        MyLog.debug("onDetachedFromActivity...");
        _me._destroy();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        MyLog.debug("onDetachedFromActivityForConfigChanges...");
        onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        MyLog.debug("onReattachedToActivityForConfigChanges: {}", binding);
        onAttachedToActivity(binding);
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        return PermissionHelper.me().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
