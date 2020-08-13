//
//  MyLocationManager.h
//  bluetooth_helper
//
//  Created by 陈柏伶 on 2020/6/13.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface MyLocationManager : NSObject

+ (instancetype)shared;
- (BOOL)isEnabled;

@end

NS_ASSUME_NONNULL_END
