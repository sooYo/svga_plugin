import 'dart:async';

import 'package:flutter/services.dart';

import 'widget_id.dart';

class SvgaPlugin {
  static const _channel = const MethodChannel('svga_plugin');
  static final _generator = GeneratorFactory.generatorOf(IDStrategy.timeStamp);

  /// Get widget's temparary ID
  ///
  /// This ID should be stored in the [SVGAWidget]'s state and used to invoke
  /// plugin methods. Once a negative ID is returned, that means the entire
  /// plugin is not ready for SVGA file parsing. You should not invoke any
  /// interface if you get into this situation
  static int generateID() => _generator?.generateID() ?? -1;

  static Future<int> crateSVGA(double width, double height) async {
    final String? version = await _channel.invokeMethod('getPlatformVersion', {
      'width': width,
      'height': height,
    });

    return int.tryParse(version!) ?? 0;
  }
}
