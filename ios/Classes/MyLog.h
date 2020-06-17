//
//  MyLog.h
//  bluetooth_helper
//
//  Created by 陈柏伶 on 2020/6/13.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MyLog : NSObject

+ (void)enableDebug;
+ (void)log:(NSString *)format, ... NS_FORMAT_FUNCTION(1,2);

@end

NS_ASSUME_NONNULL_END
