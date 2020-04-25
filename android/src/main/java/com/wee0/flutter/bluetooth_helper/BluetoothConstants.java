package com.wee0.flutter.bluetooth_helper;

import java.util.UUID;

/**
 * 蓝牙相关常量
 */
final class BluetoothConstants {

    /**
     * GATT Declarations: Primary Service
     */
    public static final UUID declarationPrimaryService = UUID.fromString("00002800-0000-1000-8000-00805f9b34fb");

    /**
     * GATT Declarations: Secondary Service
     */
    public static final UUID declarationSecondService = UUID.fromString("00002801-0000-1000-8000-00805f9b34fb");

    /**
     * GATT Descriptors: Client Characteristic Configuration
     */
    public static final UUID descCharacteristicClientConfig = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /**
     * GATT Descriptors: Server Characteristic Configuration
     */
    public static final UUID descCharacteristicServerConfig = UUID.fromString("00002903-0000-1000-8000-00805f9b34fb");

}
