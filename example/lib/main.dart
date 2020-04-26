import 'dart:async';

import 'package:bluetooth_helper/bluetooth_device.dart';
import 'package:bluetooth_helper/bluetooth_helper.dart';
import 'package:flutter/material.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _readCharacteristic, _writeCharacteristic;
  BluetoothDevice _device;

  Timer _timer;
  List<BluetoothDevice> _deviceList;

  @override
  void initState() {
    super.initState();
    BluetoothHelper.enableDebug();

    _device = BluetoothDevice.create("D8:CB:BD:91:D5:D5", "ZLY_2003020101038");
    _device.eventCallback = (BluetoothEvent event) {
      print("event: $event");
    };
    _timer = Timer.periodic(Duration(seconds: 20), (_timer) {
      BluetoothHelper.me.scan().then((List<BluetoothDevice> _scanResult) {
        print("${_timer.tick} scanResult: $_scanResult");
        if (!mounted) return;
        setState(() {
          _deviceList = _scanResult;
        });
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: <Widget>[
            Text('Bluetooth Test...'),
            Row(
              children: <Widget>[
                RaisedButton(
                  child: Text("keepAlive"),
                  onPressed: () {
                    BluetoothHelper.me.keepAlive().then((_res) => print("keepAlive: $_res"));
                  },
                ),
                RaisedButton(
                  child: Text("scan"),
                  onPressed: () => BluetoothHelper.me.scan(timeout: 2).then((List<BluetoothDevice> _res) {
                    for (BluetoothDevice _device in _res) {
                      print("scan device: $_device");
                    }
                  }),
                ),
                RaisedButton(
                  child: Text("connect"),
                  onPressed: () => this._device.connect().then((_res) => print("connect result: $_res")).catchError((_e) {
                    print("error: $_e");
                    this._device.disconnect();
                  }),
                ),
                RaisedButton(
                  child: Text("disconnect"),
                  onPressed: () => this._device.disconnect().then((_res) => print("disconnect result: $_res")),
                )
              ],
            ),
            Row(
              children: <Widget>[
                RaisedButton(
                  child: Text("discoverCharacteristics"),
                  onPressed: () async {
                    List _characteristics = await _device.discoverCharacteristics(3);
                    print("discoverCharacteristics: $_characteristics");
                    for (String _characteristic in _characteristics) {
                      if (0 == _characteristic.indexOf("00001526-")) {
                        _readCharacteristic = _characteristic;
                      } else if (0 == _characteristic.indexOf("00001525-")) {
                        _writeCharacteristic = _characteristic;
                      }
                    }
                    print("read characteristic: $_readCharacteristic");
                    print("write characteristic: $_writeCharacteristic");
                    if (null != _readCharacteristic) {
                      bool _setResult = await _device.setCharacteristicNotification(_readCharacteristic, true);
                      print("setResult: $_setResult");
                    }
                  },
                ),
                RaisedButton(
                  child: Text("read"),
                  onPressed: () => _device.characteristicRead(_readCharacteristic).then((_res) => print("read result: $_res")),
                ),
                RaisedButton(
                  child: Text("write"),
                  onPressed: () => _device.characteristicWrite(_writeCharacteristic, [1, 1, 1, 1, 1, 1, 1, 1, 1, 1]).then((_res) => print("write result: $_res")),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
