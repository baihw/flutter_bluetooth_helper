//
//  BluetoothConstants.h
//  bluetooth_helper
//
//  Created by 陈柏伶 on 2020/6/13.
//

#import <UIKit/UIKit.h>

/// 方法名
UIKIT_EXTERN NSString *const BluetoothConstantsKeyMethod;
/// 参数名
UIKIT_EXTERN NSString *const BluetoothConstantsKeyArgs;
/// 响应代码
UIKIT_EXTERN NSString *const BluetoothConstantsKeyCode;
/// 响应消息
UIKIT_EXTERN NSString *const BluetoothConstantsKeyMsg;
/// 响应数据
UIKIT_EXTERN NSString *const BluetoothConstantsKeyData;
/// 设备id
UIKIT_EXTERN NSString *const BluetoothConstantsKeyDeviceId;
/// 设备名称
UIKIT_EXTERN NSString *const BluetoothConstantsKeyDeviceName;
/// 设备状态
UIKIT_EXTERN NSString *const BluetoothConstantsKeyDeviceState;
/// 超时时间
UIKIT_EXTERN NSString *const BluetoothConstantsKeyTimeout;
/// 设备连接状态
UIKIT_EXTERN NSString *const BluetoothConstantsKeyState;
/// 服务特征id
UIKIT_EXTERN NSString *const BluetoothConstantsKeyCharacteristicId;
/// 是否成功
UIKIT_EXTERN NSString *const BluetoothConstantsKeyIsOk;


UIKIT_EXTERN NSString *const BluetoothConstantsMethodDebug;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodKeepAlive;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodBluetoothIsEnable;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodLocationIsEnable;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodStartScan;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodStopScan;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodGetDeviceState;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodConnect;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodDiscoverServices;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodSetCharacteristicNotification;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodCharacteristicRead;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodCharacteristicWrite;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodDisonnect;

UIKIT_EXTERN NSString *const BluetoothConstantsMethodOnStateChange;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodOnDeviceStateChange;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodOnServicesDiscovered;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodOnCharacteristicNotifyData;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodOnCharacteristicReadResult;
UIKIT_EXTERN NSString *const BluetoothConstantsMethodOnCharacteristicWriteResult;


/// 暂时未确定下来编码的异常，统一先使用此编码。
UIKIT_EXTERN NSString *const BluetoothExceptionCodeCommon;
/// 蓝牙未开启
UIKIT_EXTERN NSString *const BluetoothExceptionCodeBluetoothNotEnable;
/// 定位未开启
UIKIT_EXTERN NSString *const BluetoothExceptionCodeLocationNotEnable;
/// 需要先建立连接
UIKIT_EXTERN NSString *const BluetoothExceptionCodeConnectFirst;


#ifndef weakify
    #if DEBUG
        #if __has_feature(objc_arc)
        #define weakify(object) autoreleasepool{} __weak __typeof__(object) weak##_##object = object;
        #else
        #define weakify(object) autoreleasepool{} __block __typeof__(object) block##_##object = object;
        #endif
    #else
        #if __has_feature(objc_arc)
        #define weakify(object) try{} @finally{} {} __weak __typeof__(object) weak##_##object = object;
        #else
        #define weakify(object) try{} @finally{} {} __block __typeof__(object) block##_##object = object;
        #endif
    #endif
#endif

#ifndef strongify
    #if DEBUG
        #if __has_feature(objc_arc)
        #define strongify(object) autoreleasepool{} __typeof__(object) object = weak##_##object;
        #else
        #define strongify(object) autoreleasepool{} __typeof__(object) object = block##_##object;
        #endif
    #else
        #if __has_feature(objc_arc)
        #define strongify(object) try{} @finally{} __typeof__(object) object = weak##_##object;
        #else
        #define strongify(object) try{} @finally{} __typeof__(object) object = block##_##object;
        #endif
    #endif
#endif
