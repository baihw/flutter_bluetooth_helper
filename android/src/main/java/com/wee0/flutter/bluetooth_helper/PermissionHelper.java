package com.wee0.flutter.bluetooth_helper;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ObjectStreamException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 权限操作助手
 */
final class PermissionHelper {

    public static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    private Map<Integer, ICallback> _requests = new HashMap<>(8, 1.0f);

    /**
     * 请求用户授于指定的权限
     *
     * @param permission 权限标识
     * @param callback   用户授权结果回调，回调参数为boolean类型，true为获得用户授权，false为未获得用户授权。
     * @return 如果已经有权限返回false，没有权限则返回true，然后请求用户授权。
     */
    public boolean requestPermission(String permission, ICallback callback) {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(PlatformHelper.me().getActivity(), permission)) return false;
        int _requestCode = buildRequestCode(permission);
        if (!this._requests.containsKey(_requestCode)) {
            this._requests.put(_requestCode, callback);
            ActivityCompat.requestPermissions(PlatformHelper.me().getActivity(), new String[]{permission}, _requestCode);
        }
        return true;
    }

    /**
     * 应用安装后第一次访问，则直接返回false；
     * 第一次请求权限时，用户Deny了，再次调用shouldShowRequestPermissionRationale()，则返回true；
     * 第二次请求权限时，用户Deny了，并选择了“dont ask me again”的选项时，再次调用shouldShowRequestPermissionRationale()时，返回false；
     * 设备的系统设置中，禁止了应用获取这个权限的授权，则调用shouldShowRequestPermissionRationale()，返回false。
     *
     * @param permission 权限标识
     * @return true / false
     */
    public static boolean shouldShowRequestPermissionRationale(String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(PlatformHelper.me().getActivity(), permission);
    }

    /**
     * @param feature
     * @return
     */
    public static boolean hasSystemFeature(String feature) {
        // PackageManager.FEATURE_BLUETOOTH_LE
        return PlatformHelper.me().getApplication().getPackageManager().hasSystemFeature(feature);
    }

    /**
     * 权限请求响应结果处理
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @return
     */
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MyLog.debug("permissions: {}", Arrays.toString(permissions));
        MyLog.debug("grantResults: {}", Arrays.toString(grantResults));
        if (!this._requests.containsKey(requestCode)) return false;
        ICallback _callback = this._requests.remove(requestCode);
        if (null != _callback) _callback.execute(PackageManager.PERMISSION_GRANTED == grantResults[0]);
        return true;
    }

//    static void startAppSettings() {
//        Intent _intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        _intent.setData(Uri.parse("package:" + PlatformHelper.me().getApplication().getPackageName()));
//        PlatformHelper.me().getApplication().startActivity(_intent);
//    }

    // 根据权限字符串，计算一个数字值作为请求代码
    private static int buildRequestCode(String val) {
        int _result = 0;
        for (char _char : val.toCharArray()) {
            _result += _char;
        }
        return _result;
    }


    /************************************************************
     ************* 单例对象。
     ************************************************************/
    private PermissionHelper() {
        if (null != PermissionHelperHolder._INSTANCE) {
            // 防止使用反射API创建对象实例。
            throw new IllegalStateException("that's not allowed!");
        }
    }

    // 当前对象唯一实例持有者。
    private static final class PermissionHelperHolder {
        private static final PermissionHelper _INSTANCE = new PermissionHelper();
    }

    // 防止使用反序列化操作获取多个对象实例。
    private Object readResolve() throws ObjectStreamException {
        return PermissionHelperHolder._INSTANCE;
    }

    /**
     * 获取当前对象唯一实例。
     *
     * @return 当前对象唯一实例
     */
    public static PermissionHelper me() {
        return PermissionHelperHolder._INSTANCE;
    }
}
