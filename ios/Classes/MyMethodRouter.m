//
//  MyMethodRouter.m
//  bluetooth_helper
//
//  Created by 陈柏伶 on 2020/6/14.
//

#import "MyMethodRouter.h"
#import "BasicMessageChannelReply.h"
#import "BluetoothConstants.h"
#import "MyLog.h"
#import "MyLocationManager.h"
#import "MyBluetoothManager.h"

@interface MyMethodRouter ()

@property (nonatomic, strong) FlutterBasicMessageChannel *messageChannel;
@property (nonatomic, strong) NSMutableDictionary *paramsDict;

@end

@implementation MyMethodRouter

+ (instancetype)shared {
    static MyMethodRouter *manager;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[self alloc] init];
    });
    return manager;
}

- (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    self.messageChannel = [FlutterBasicMessageChannel messageChannelWithName:@"bluetooth_helper" binaryMessenger:[registrar messenger]];
    @weakify(self);
    [self.messageChannel setMessageHandler:^(id  _Nullable message, FlutterReply  _Nonnull callback) {
        @strongify(self);
        [self handleMessageCall:message callback:callback];
    }];
}

#pragma mark - handle flutter -> native message call

- (void)handleMessageCall:(id _Nullable)message callback:(FlutterReply _Nonnull)callback {
    NSLog(@"flutter->na message %@ ", message);
    BasicMessageChannelReply *reply = [BasicMessageChannelReply sharedReply];
    if (message == nil) {
        callback([reply error:@"message can not be null!"]);
        return;
    }
    if (![message isKindOfClass:[NSDictionary class]]) {
        callback([reply error:[NSString stringWithFormat:@"unSupport message type: %@", NSStringFromClass(message)]]);
        return;
    }
    NSDictionary *messageData = (NSDictionary *)message;
    id methodObj = messageData[BluetoothConstantsKeyMethod];
    NSString *method = methodObj == nil ? nil : [[NSString stringWithFormat:@"%@", methodObj] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];//_methodObj.toString().trim();
    if (method == nil || method.length == 0) {
        callback([reply error:@"method can not be empty!"]);
        return;
    }
    [MyLog log:@"invoke method: %@", method];
    if ([BluetoothConstantsMethodDebug isEqualToString:method]) {
        [MyLog enableDebug];
        callback([reply success:@YES]);
        return;
    }
    if ([BluetoothConstantsMethodKeepAlive isEqualToString:method]) {
        // iOS后台保活系统自动处理
        callback([reply success:@YES]);
        return;
    }
    if ([BluetoothConstantsMethodBluetoothIsEnable isEqualToString:method]) {
        [reply success:@([[MyBluetoothManager shared] isEnabled])];
        return;
    }
    if ([BluetoothConstantsMethodLocationIsEnable isEqualToString:method]) {
        [reply success:@([[MyLocationManager shared] isEnabled])];
        return;
    }
    if ([BluetoothConstantsMethodStartScan isEqualToString:method]) {
        NSString *deviceName;
        NSString *deviceId;
        if ([messageData.allKeys containsObject:BluetoothConstantsKeyArgs]) {
            NSDictionary<NSString *, id> *args = messageData[BluetoothConstantsKeyArgs];
            deviceName = [args.allKeys containsObject:BluetoothConstantsKeyDeviceName] ? args[BluetoothConstantsKeyDeviceName] : nil;
            deviceId = [args.allKeys containsObject:BluetoothConstantsKeyDeviceId] ? args[BluetoothConstantsKeyDeviceId] : nil;
            if ([deviceId isKindOfClass:[NSNull class]]) {
                deviceId = nil;
            }
            if ([deviceName isKindOfClass:[NSNull class]]) {
                deviceName = nil;
            }
        }
        [[MyBluetoothManager shared] startScan:deviceName deviceId:deviceId];
        callback([reply success:@YES]);
        return;
    }
    if ([BluetoothConstantsMethodStopScan isEqualToString:method]) {
        NSDictionary *scanResult = [[MyBluetoothManager shared] stopScan];
        callback([reply success:scanResult]);
        return;
    }
    // 以下操作都必须传递键值对参数。
    NSDictionary<NSString *, id> *args = messageData[BluetoothConstantsKeyArgs];
    // 设备标识所有操作都必须传递。
    NSString *deviceId = @"";
    id deviceIdObj = args[BluetoothConstantsKeyDeviceId];
    if ([deviceIdObj isKindOfClass:[NSString class]]) {
        deviceId = [((NSString *)deviceIdObj) stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
    }
    if (deviceId == nil || deviceId.length == 0) {
        callback([reply error:@"deviceId can not be empty!"]);
        return;
    }
    if ([BluetoothConstantsMethodGetDeviceState isEqualToString:method]) {
        int deviceState = [[MyBluetoothManager shared] getDeviceState:deviceId];
        callback([reply success:@(deviceState)]);
        return;
    }
    if ([BluetoothConstantsMethodConnect isEqualToString:method]) {
        id timeoutObj = args[BluetoothConstantsKeyTimeout];
        int timeout = nil == timeoutObj ? 3 : [timeoutObj intValue];
        [[MyBluetoothManager shared] connect:deviceId timeout:timeout callback:callback];
        return;
    }
    if ([BluetoothConstantsMethodDiscoverServices isEqualToString:method]) {
        id timeoutObj = args[BluetoothConstantsKeyTimeout];
        int timeout = nil == timeoutObj ? 3 : [timeoutObj intValue];
        [[MyBluetoothManager shared] discoverServices:timeout callback:callback];
        return;
    }
    if ([BluetoothConstantsMethodSetCharacteristicNotification isEqualToString:method]) {
        NSString *characteristicId = args[BluetoothConstantsKeyCharacteristicId];
        BOOL enable = [args[@"enable"] boolValue];
        BOOL setCharacteristicNotificationResult = [[MyBluetoothManager shared] characteristicSetNotification:characteristicId enable:enable];
        callback([reply success:@(setCharacteristicNotificationResult)]);
        return;
    }
    if ([BluetoothConstantsMethodCharacteristicRead isEqualToString:method]) {
        NSString *characteristicId = args[BluetoothConstantsKeyCharacteristicId];
        BOOL characteristicReadResult = [[MyBluetoothManager shared] characteristicRead:characteristicId];
        callback([reply success:@(characteristicReadResult)]);
        return;
    }
    if ([BluetoothConstantsMethodCharacteristicWrite isEqualToString:method]) {
        NSString *characteristicId = args[BluetoothConstantsKeyCharacteristicId];
        FlutterStandardTypedData *data = args[BluetoothConstantsKeyData];
        BOOL characteristicWriteResult = [[MyBluetoothManager shared] characteristicWrite:characteristicId value:data withoutResponse:NO];
        callback([reply success:@(characteristicWriteResult)]);
        return;
    }
    if ([BluetoothConstantsMethodDisonnect isEqualToString:method]) {
        BOOL disconnectResult = [[MyBluetoothManager shared] disconnect:deviceId];
        callback([reply success:@(disconnectResult)]);
        return;
    }
    callback([reply error:[NSString stringWithFormat:@"unKnow method: %@", method]]);
}

#pragma mark - handle native -> flutter message call

- (void)callMethod:(NSString *)methodName args:(id)args {
    if (methodName == nil) {
        return;
    }
    self.paramsDict[BluetoothConstantsKeyMethod] = methodName;
    if (args != nil) {
        self.paramsDict[BluetoothConstantsKeyArgs] = args;
    } else {
        [self.paramsDict removeObjectForKey:BluetoothConstantsKeyArgs];
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.messageChannel sendMessage:self.paramsDict];
    });
}


/// 蓝牙状态变化时的通知
- (void)callOnStateChange:(int)state {
    NSDictionary *args = @{
        BluetoothConstantsKeyState: @(state)
    };
    [self callMethod:BluetoothConstantsMethodOnStateChange args:args];
}

/// 设备状态变化时的通知
- (void)callOnDeviceStateChange:(NSString *)deviceId deviceState:(int)deviceState {
    NSDictionary *args = @{
        BluetoothConstantsKeyDeviceId: deviceId,
        BluetoothConstantsKeyDeviceState: @(deviceState)
    };
    [self callMethod:BluetoothConstantsMethodOnDeviceStateChange args:args];
}

/// 发现服务时的通知
- (void)callOnServicesDiscovered:(NSString *)deviceId data:(id)data {
    NSDictionary *args = @{
        BluetoothConstantsKeyDeviceId: deviceId,
        BluetoothConstantsKeyData: data
    };
    [self callMethod:BluetoothConstantsMethodOnServicesDiscovered args:args];
}

/// 接收到广播数据时的通知
- (void)callOnCharacteristicNotifyData:(NSString *)deviceId characteristicId:(NSString *)characteristicId data:(id)data {
    NSDictionary *args = @{
        BluetoothConstantsKeyDeviceId: deviceId,
        BluetoothConstantsKeyCharacteristicId: characteristicId,
        BluetoothConstantsKeyData: data
    };
    [self callMethod:BluetoothConstantsMethodOnCharacteristicNotifyData args:args];
}

/// 接收到读取数据结果时的通知
- (void)callOnCharacteristicReadResult:(NSString *)deviceId characteristicId:(NSString *) characteristicId data:(id)data {
    NSDictionary *args = @{
        BluetoothConstantsKeyDeviceId: deviceId,
        BluetoothConstantsKeyCharacteristicId: characteristicId,
        BluetoothConstantsKeyData: data
    };
    [self callMethod:BluetoothConstantsMethodOnCharacteristicReadResult args:args];
}

/// 接收到写入数据结果时的通知
- (void)callOnCharacteristicWriteResult:(NSString *)deviceId characteristicId:(NSString *) characteristicId isOk:(BOOL)isOk {
    NSDictionary *args = @{
        BluetoothConstantsKeyDeviceId: deviceId,
        BluetoothConstantsKeyCharacteristicId: characteristicId,
        BluetoothConstantsKeyIsOk: @(isOk)
    };
    [self callMethod:BluetoothConstantsMethodOnCharacteristicWriteResult args:args];
}

#pragma mark - Lazy Loading

- (NSMutableDictionary *)paramsDict {
    if (_paramsDict == nil) {
        _paramsDict = [NSMutableDictionary dictionary];
    }
    return _paramsDict;
}

@end
