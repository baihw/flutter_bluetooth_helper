//
//  MyMethodRouter.h
//  bluetooth_helper
//
//  Created by 陈柏伶 on 2020/6/14.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN

@interface MyMethodRouter : NSObject

+ (instancetype)shared;
- (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar;
/// 处理收到flutter调用native
- (void)handleMessageCall:(id _Nullable)message callback:(FlutterReply _Nonnull)callback;
/// 蓝牙状态变化时的通知
- (void)callOnStateChange:(int)state;
/// 设备状态变化时的通知
- (void)callOnDeviceStateChange:(NSString *)deviceId deviceState:(int)deviceState;
/// 发现服务时的通知
- (void)callOnServicesDiscovered:(NSString *)deviceId data:(id)data;
/// 接收到广播数据时的通知
- (void)callOnCharacteristicNotifyData:(NSString *)deviceId characteristicId:(NSString *)characteristicId data:(id)data;
/// 接收到读取数据结果时的通知
- (void)callOnCharacteristicReadResult:(NSString *)deviceId characteristicId:(NSString *) characteristicId data:(id)data;
/// 接收到写入数据结果时的通知
- (void)callOnCharacteristicWriteResult:(NSString *)deviceId characteristicId:(NSString *) characteristicId isOk:(BOOL)isOk;

@end

NS_ASSUME_NONNULL_END
