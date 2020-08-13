//
//  MyBluetoothManager.h
//  bluetooth_helper
//
//  Created by 陈柏伶 on 2020/6/13.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN

@interface MyBluetoothManager : NSObject

/// 蓝牙是否可用
@property (nonatomic, assign, readonly) BOOL isEnabled;
/// 单例对象
+ (instancetype)shared;
/// 开始扫描
- (void)startScan:(NSString * _Nullable)deviceName deviceId:(NSString * _Nullable)deviceId;
/// 结束扫描，获取扫描结果
- (NSDictionary *)stopScan;
- (int)getDeviceState:(NSString *)deviceId;
/// 建立连接
- (void)connect:(NSString *)deviceId timeout:(int)timeout callback:(FlutterReply _Nonnull)callback;
/// 发现服务
- (void)discoverServices:(int)timeout callback:(FlutterReply _Nonnull)callback;
/// 设置特征通知开关
- (BOOL)characteristicSetNotification:(NSString *)characteristicId enable:(BOOL)enable;
/// 从指定特征读取数据
- (BOOL)characteristicRead:(NSString *)characteristicId;
/// 向指定特征写入数据
- (BOOL)characteristicWrite:(NSString *)characteristicId value:(FlutterStandardTypedData *)value withoutResponse:(BOOL)withoutResponse;
/// 断开连接
- (BOOL)disconnect:(NSString *)deviceId;

@end

NS_ASSUME_NONNULL_END
