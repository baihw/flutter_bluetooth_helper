#import "BluetoothHelperPlugin.h"
#import "MyMethodRouter.h"

@interface BluetoothHelperPlugin()

@end

@implementation BluetoothHelperPlugin

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    [[MyMethodRouter shared] registerWithRegistrar:registrar];
}

@end
