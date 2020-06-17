//
//  BasicMessageChannelReply.h
//  bluetooth_helper
//
//  Created by 陈柏伶 on 2020/6/13.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BasicMessageChannelReply : NSObject

+ (instancetype)sharedReply;

- (NSDictionary *)success:(nullable id)data;
- (NSDictionary *)error:(NSString *)code message:(NSString *)message data:(nullable id)details;
- (NSDictionary *)error:(NSString *)code message:(NSString *)message;
- (NSDictionary *)error:(NSString *)message;

@end

NS_ASSUME_NONNULL_END
