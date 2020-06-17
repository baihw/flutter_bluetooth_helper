//
//  MyLocationManager.m
//  bluetooth_helper
//
//  Created by 陈柏伶 on 2020/6/13.
//

#import "MyLocationManager.h"
#import <CoreLocation/CoreLocation.h>

@implementation MyLocationManager

+ (instancetype)shared {
    static MyLocationManager *manager;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[self alloc] init];
    });
    return manager;
}

- (BOOL)isEnabled {
    BOOL isLocationEnabled = [CLLocationManager locationServicesEnabled];
    CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
    BOOL isAuthorized = (status != kCLAuthorizationStatusNotDetermined && status != kCLAuthorizationStatusDenied);
    return isLocationEnabled && isAuthorized;
}

@end
