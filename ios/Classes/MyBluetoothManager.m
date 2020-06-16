//
//  MyBluetoothManager.m
//  bluetooth_helper
//
//  Created by 陈柏伶 on 2020/6/13.
//

#import "MyBluetoothManager.h"
#import <CoreBluetooth/CoreBluetooth.h>
#import "MyMethodRouter.h"
#import "MyLog.h"
#import "BluetoothConstants.h"
#import "BasicMessageChannelReply.h"

@interface MyBluetoothManager () <CBCentralManagerDelegate, CBPeripheralDelegate>

@property (nonatomic, strong) CBCentralManager *centralManager;
/** 记录当前的Peripheral,后台扫描不到设备时,直接连接该外围设备 */
@property (nonatomic, strong) CBPeripheral *currentPeripheral;
@property (nonatomic, assign) BOOL isAuthorized;
@property (nonatomic, copy) NSString *deviceName;
@property (nonatomic, copy) NSString *deviceId;
@property (nonatomic, assign) BOOL connected;
@property (nonatomic, copy) FlutterReply connectCallback;
@property (nonatomic, copy) FlutterReply discoverServicesCallback;
@property (nonatomic, strong) NSMutableDictionary<NSString *, CBCharacteristic *> *characteristicDict;
@property (nonatomic, strong) NSMutableDictionary<NSString *, NSDictionary *> *scanData;
@property (nonatomic, strong) NSMutableDictionary<NSString *, CBPeripheral *> *scannedPeripheralDict;

@end

@implementation MyBluetoothManager

+ (instancetype)shared {
    static MyBluetoothManager *manager;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[self alloc] init];
    });
    return manager;
}

- (instancetype)init {
    if (self = [super init]) {
        [self centralManager];
    }
    return self;
}

- (BOOL)isEnabled {
    return self.isAuthorized;
}

- (void)startScan:(NSString *)deviceName deviceId:(NSString *)deviceId {
    if (![self isEnabled]) {
        [MyLog log:@"please turn on bluetooth."];
        return;
    }
    self.deviceName = deviceName ?: @"";
    self.deviceId = deviceId ?: @"";
    [self.scanData removeAllObjects];
    [self.scannedPeripheralDict removeAllObjects];
    [self.centralManager scanForPeripheralsWithServices:nil options:nil];
}

- (NSDictionary *)stopScan {
    [self.centralManager stopScan];
    return self.scanData;
}

- (int)getDeviceState:(NSString *)deviceId {
    BOOL isConnected = [deviceId isEqualToString:self.currentPeripheral.name] && self.connected;
    return isConnected ? 1 : 0;
}

- (void)connect:(NSString *)deviceId timeout:(int)timeout callback:(FlutterReply _Nonnull)callback {
    if ([self.scannedPeripheralDict.allKeys containsObject:deviceId]) {
        self.currentPeripheral = self.scannedPeripheralDict[deviceId];
    } else {
        self.currentPeripheral = nil;
        [MyLog log:@"not found device"];
        [self p_handleConnectPeripheral:NO];
        return;
    }
    if (self.connected) {
        [MyLog log:@"already connected!"];
        self.connectCallback = callback;
        [self p_handleConnectPeripheral:YES];
        return;
    }
    if (nil != self.connectCallback) {
        [MyLog log:@"please wait for another task to complete."];
        return;
    }
    if (![self isEnabled]) {
        [MyLog log:@"please turn on bluetooth."];
        return;
    }
    self.connectCallback = callback;
    [self.centralManager connectPeripheral:self.currentPeripheral options:nil];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(timeout * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if (nil != self.connectCallback) {
            self.connectCallback([[BasicMessageChannelReply sharedReply] success:@NO]);
            self.connectCallback = nil;
        }
        [self disconnect:self.deviceId];
    });
}

- (void)discoverServices:(int)timeout callback:(FlutterReply _Nonnull)callback {
    if (nil != self.discoverServicesCallback) {
        [MyLog log:@"please wait for another task to complete."];
        return;
    }
    if (!self.connected) {
        callback([[BasicMessageChannelReply sharedReply] error:BluetoothExceptionCodeConnectFirst message:@"please connect first!"]);
        return;
    }
    self.discoverServicesCallback = callback;
    self.currentPeripheral.delegate = self;
    [self.characteristicDict removeAllObjects];
    [self.currentPeripheral discoverServices:nil];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(timeout * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if (nil != self.discoverServicesCallback) {
            self.discoverServicesCallback([[BasicMessageChannelReply sharedReply] error:BluetoothConstantsKeyTimeout]);
            self.discoverServicesCallback = nil;
        }
    });
}

- (BOOL)disconnect:(NSString *)deviceId {
    BOOL disconnectResult = NO;
    if ([deviceId isEqualToString:self.currentPeripheral.name]) {
        [self.centralManager cancelPeripheralConnection:self.currentPeripheral];
        disconnectResult = YES;
    }
    return disconnectResult;
}

- (BOOL)characteristicSetNotification:(NSString *)characteristicId enable:(BOOL)enable {
    if (![self.characteristicDict.allKeys containsObject:characteristicId]) {
        return NO;
    }
    CBCharacteristic *characteristic = self.characteristicDict[characteristicId];
    [characteristic.service.peripheral setNotifyValue:enable forCharacteristic:characteristic];
    [MyLog log:@"characteristicId: %@, setNotificationResult: %d", characteristicId, enable];
    return YES;
}

- (BOOL)characteristicRead:(NSString *)characteristicId {
    if (![self.characteristicDict.allKeys containsObject:characteristicId]) {
        return NO;
    }
    CBCharacteristic *characteristic = self.characteristicDict[characteristicId];
    [characteristic.service.peripheral readValueForCharacteristic:characteristic];
    return YES;
}

- (BOOL)characteristicWrite:(NSString *)characteristicId value:(FlutterStandardTypedData *)value withoutResponse:(BOOL)withoutResponse {
    if (![self.characteristicDict.allKeys containsObject:characteristicId]) {
        return NO;
    }
    CBCharacteristic *characteristic = self.characteristicDict[characteristicId];
    [MyLog log:@"characteristicWrite length: %ld", (long)value.data.length];
    [characteristic.service.peripheral writeValue:value.data forCharacteristic:characteristic type:withoutResponse ? CBCharacteristicWriteWithoutResponse : CBCharacteristicWriteWithResponse];
    return YES;
}

#pragma mark - CBPeripheralDelegate

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error {
    if (error != nil) {
        if (nil != self.discoverServicesCallback) {
            self.discoverServicesCallback([[BasicMessageChannelReply sharedReply] error:[NSString stringWithFormat:@"%ld", (long)error.code] message:@"discover services error" data:error.userInfo]);
            self.discoverServicesCallback = nil;
        }
        return;
    }
    [MyLog log:@"onServicesDiscovered success %@", peripheral.services];
    for (CBService *service in peripheral.services) {
        [MyLog log:@"service %@", service];
        [service.peripheral discoverCharacteristics:nil forService:service];
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error {
    if (error != nil) {
        if (nil != self.discoverServicesCallback) {
            self.discoverServicesCallback([[BasicMessageChannelReply sharedReply] error:[NSString stringWithFormat:@"%ld", (long)error.code] message:@"discover characteristics for services error" data:error.userInfo]);
            self.discoverServicesCallback = nil;
        }
        return;
    }
    for (CBCharacteristic *characteristic in service.characteristics) {
        [MyLog log:@"characteristic %@", characteristic];
        [self.characteristicDict setValue:characteristic forKey:characteristic.UUID.UUIDString];
    }
    
    if (nil != self.discoverServicesCallback) {
        self.discoverServicesCallback([[BasicMessageChannelReply sharedReply] success:self.characteristicDict.allKeys]);
        self.discoverServicesCallback = nil;
    } else {
        [[MyMethodRouter shared] callOnServicesDiscovered:peripheral.identifier.UUIDString data:self.characteristicDict.allKeys];
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(nonnull CBCharacteristic *)characteristic error:(nullable NSError *)error {
    if (error) {
        [MyLog log:@"onCharacteristicRead error: %@", error];
        return;
    }
    [MyLog log:@"onCharacteristicRead success: %@", characteristic];
    [[MyMethodRouter shared] callOnCharacteristicReadResult:peripheral.name characteristicId:characteristic.UUID.UUIDString data:characteristic.value];
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateNotificationStateForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {
    if (error) {
        [MyLog log:@"update notification state error: %@", error];
        return;
    }
    [MyLog log:@"update notification state success"];
}

- (void)peripheral:(CBPeripheral *)peripheral didWriteValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {
    [[MyMethodRouter shared] callOnCharacteristicWriteResult:peripheral.name characteristicId:characteristic.UUID.UUIDString isOk:error == nil];
    if (error) {
        [MyLog log:@"write value for characteristic error: %@", error];
        return;
    }
    [MyLog log:@"onCharacteristicWrite success, characteristic: %@", characteristic];
}


#pragma mark - CBCentralManagerDelegate

- (void)centralManagerDidUpdateState:(CBCentralManager *)central{
    switch (central.state) {
        case CBCentralManagerStateUnknown:
            self.isAuthorized = NO;
            [[MyMethodRouter shared] callOnStateChange:0];
            [MyLog log:@">>>CBCentralManagerStateUnknown"];
            break;
        case CBCentralManagerStateResetting:
            self.isAuthorized = NO;
            [[MyMethodRouter shared] callOnStateChange:0];
            [MyLog log:@">>>CBCentralManagerStateResetting"];
            break;
        case CBCentralManagerStateUnsupported:
            self.isAuthorized = NO;
            [[MyMethodRouter shared] callOnStateChange:0];
            [MyLog log:@">>>CBCentralManagerStateUnsupported"];
            break;
        case CBCentralManagerStateUnauthorized:
            self.isAuthorized = NO;
            [[MyMethodRouter shared] callOnStateChange:0];
            [MyLog log:@">>>CBCentralManagerStateUnauthorized"];
            break;
        case CBCentralManagerStatePoweredOff:
            self.isAuthorized = NO;
            [[MyMethodRouter shared] callOnStateChange:0];
            [MyLog log:@">>>CBCentralManagerStatePoweredOff"];
            break;
        case CBCentralManagerStatePoweredOn:
        {
            self.isAuthorized = YES;
            [[MyMethodRouter shared] callOnStateChange:1];
            [MyLog log:@">>>CBCentralManagerStatePoweredOn"];
//            [self.centralManager scanForPeripheralsWithServices:nil options:nil];
        }
            break;
        default:
            self.isAuthorized = NO;
            break;
    }
}

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary<NSString *,id> *)advertisementData RSSI:(NSNumber *)RSSI {
    [MyLog log:@"scanned peripheral: %@", peripheral];
    NSString *identifier = peripheral.identifier.UUIDString;
    if (identifier != nil && ![self.scanData.allKeys containsObject:identifier]) {
        [self.scannedPeripheralDict setObject:peripheral forKey:identifier];
        NSDictionary *peripheralDict = @{
            BluetoothConstantsKeyDeviceId: identifier,
            BluetoothConstantsKeyDeviceName: peripheral.name ?: identifier
        };
        [self.scanData setObject:peripheralDict forKey:identifier];
    }
}

- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
    [self p_handleConnectPeripheral:YES];
    
}

- (void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)peripheral error:(nullable NSError *)error {
    [self p_handleConnectPeripheral:NO];
}

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    [self p_handleConnectPeripheral:NO];
}

#pragma mark - Private Method

- (void)p_handleConnectPeripheral:(BOOL)connected {
    self.connected = connected;
    if (nil != self.connectCallback) {
        self.connectCallback([[BasicMessageChannelReply sharedReply] success:@(connected)]);
        self.connectCallback = nil;
    }
    [[MyMethodRouter shared] callOnDeviceStateChange:self.deviceId deviceState:connected ? 1 : 0];
}

#pragma mark - Lazy Loading

- (CBCentralManager *)centralManager {
    if (_centralManager == nil) {
        _centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    }
    return _centralManager;
}

- (NSMutableDictionary<NSString *, CBCharacteristic *> *)characteristicDict {
    if (_characteristicDict == nil) {
        _characteristicDict = [NSMutableDictionary dictionary];
    }
    return _characteristicDict;
}

- (NSMutableDictionary<NSString *, NSDictionary *> *)scanData {
    if (_scanData == nil) {
        _scanData = [NSMutableDictionary dictionary];
    }
    return _scanData;
}

- (NSMutableDictionary<NSString *,CBPeripheral *> *)scannedPeripheralDict {
    if (_scannedPeripheralDict == nil) {
        _scannedPeripheralDict = [NSMutableDictionary dictionary];
    }
    return _scannedPeripheralDict;
}

@end
