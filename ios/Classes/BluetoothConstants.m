//
//  BluetoothConstants.m
//  bluetooth_helper
//
//  Created by 陈柏伶 on 2020/6/13.
//

#import <UIKit/UIKit.h>

NSString *const BluetoothConstantsKeyMethod = @"method";
NSString *const BluetoothConstantsKeyArgs = @"args";
NSString *const BluetoothConstantsKeyCode = @"code";
NSString *const BluetoothConstantsKeyMsg = @"message";
NSString *const BluetoothConstantsKeyData = @"data";
NSString *const BluetoothConstantsKeyDeviceId = @"deviceId";
NSString *const BluetoothConstantsKeyDeviceName = @"deviceName";
NSString *const BluetoothConstantsKeyDeviceState = @"deviceState";
NSString *const BluetoothConstantsKeyTimeout = @"timeout";
NSString *const BluetoothConstantsKeyState = @"state";
NSString *const BluetoothConstantsKeyCharacteristicId = @"characteristicId";
NSString *const BluetoothConstantsKeyIsOk = @"isOk";

NSString *const BluetoothConstantsMethodDebug = @"debug";
NSString *const BluetoothConstantsMethodKeepAlive = @"keepAlive";
NSString *const BluetoothConstantsMethodBluetoothIsEnable = @"bluetoothIsEnable";
NSString *const BluetoothConstantsMethodLocationIsEnable = @"locationIsEnable";
NSString *const BluetoothConstantsMethodStartScan = @"startScan";
NSString *const BluetoothConstantsMethodStopScan = @"stopScan";
NSString *const BluetoothConstantsMethodGetDeviceState = @"getDeviceState";
NSString *const BluetoothConstantsMethodConnect = @"connect";
NSString *const BluetoothConstantsMethodDiscoverServices = @"discoverServices";
NSString *const BluetoothConstantsMethodSetCharacteristicNotification = @"setCharacteristicNotification";
NSString *const BluetoothConstantsMethodCharacteristicRead = @"characteristicRead";
NSString *const BluetoothConstantsMethodCharacteristicWrite = @"characteristicWrite";
NSString *const BluetoothConstantsMethodDisonnect = @"disconnect";

NSString *const BluetoothConstantsMethodOnStateChange = @"onStateChange";
NSString *const BluetoothConstantsMethodOnDeviceStateChange = @"onDeviceStateChange";
NSString *const BluetoothConstantsMethodOnServicesDiscovered = @"onServicesDiscovered";
NSString *const BluetoothConstantsMethodOnCharacteristicNotifyData = @"onCharacteristicNotifyData";
NSString *const BluetoothConstantsMethodOnCharacteristicReadResult = @"onCharacteristicReadResult";
NSString *const BluetoothConstantsMethodOnCharacteristicWriteResult = @"onCharacteristicWriteResult";


/// 暂时未确定下来编码的异常，统一先使用此编码。
NSString *const BluetoothExceptionCodeCommon = @"600";
/// 蓝牙未开启
NSString *const BluetoothExceptionCodeBluetoothNotEnable = @"601";
/// 定位未开启
NSString *const BluetoothExceptionCodeLocationNotEnable = @"602";
/// 需要先建立连接
NSString *const BluetoothExceptionCodeConnectFirst = @"603";
