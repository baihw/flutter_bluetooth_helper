//
//  MyLog.m
//  bluetooth_helper
//
//  Created by 陈柏伶 on 2020/6/13.
//

#import "MyLog.h"

static BOOL _isDebug = NO;

@implementation MyLog

+ (void)enableDebug {
    _isDebug = YES;
}

+ (void)log:(NSString *)format, ... {
    if (_isDebug) {
        va_list ap;
        va_start (ap, format);
        NSString *body = [[NSString alloc] initWithFormat:format arguments:ap];
        va_end (ap);
        NSLog(@"%@", body);
    }
}

@end
