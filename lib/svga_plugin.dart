import 'dart:async';

import 'package:flutter/services.dart';

class SvgaPlugin {
  static const MethodChannel _channel = const MethodChannel('svga_plugin');

  static Future<int> crateSVGA(double width, double height) async {
    final String? version = await _channel.invokeMethod('getPlatformVersion', {
      'width': width,
      'height': height,
    });

    return int.tryParse(version!) ?? 0;
  }
}
