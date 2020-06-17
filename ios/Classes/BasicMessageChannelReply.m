//
//  BasicMessageChannelReply.m
//  bluetooth_helper
//
//  Created by 陈柏伶 on 2020/6/13.
//

#import "BasicMessageChannelReply.h"
#import "BluetoothConstants.h"

/// 默认成功代码
NSString *const ReplySuccessCode = @"200";
/// 默认错误代码
NSString *const ReplyErrorCode = @"500";

@interface BasicMessageChannelReply()

@property (nonatomic, strong) NSMutableDictionary *replyDict;

@end

@implementation BasicMessageChannelReply

+ (instancetype)sharedReply {
    static BasicMessageChannelReply *reply;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        reply = [[self alloc] init];
    });
    return reply;
}

- (NSDictionary *)success:(nullable id)data {
    self.replyDict[BluetoothConstantsKeyCode] = ReplySuccessCode;
    self.replyDict[BluetoothConstantsKeyMsg] = @"ok";
    if (data != nil) {
        self.replyDict[BluetoothConstantsKeyData] = data;
    } else {
        [self.replyDict removeObjectForKey:BluetoothConstantsKeyData];
    }
    return self.replyDict;
}

- (NSDictionary *)error:(NSString *)code message:(NSString *)message data:(nullable id)details {
    self.replyDict[BluetoothConstantsKeyCode] = code;
    self.replyDict[BluetoothConstantsKeyMsg] = message;
    if (details != nil) {
        self.replyDict[BluetoothConstantsKeyData] = details;
    } else {
        [self.replyDict removeObjectForKey:BluetoothConstantsKeyData];
    }
    return self.replyDict;
}

- (NSDictionary *)error:(NSString *)code message:(NSString *)message {
    return [self error:code message:message data:nil];
}

- (NSDictionary *)error:(NSString *)message {
    return [self error:ReplyErrorCode message:message data:nil];
}

- (NSMutableDictionary *)replyDict {
    if (_replyDict == nil) {
        _replyDict = [NSMutableDictionary dictionaryWithCapacity:8];
    }
    return _replyDict;
}

@end
