import 'dart:async';

import 'package:fixnum/fixnum.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

import 'constants/methods.dart';
import 'constants/status_codes.dart';
import 'proto/pb_header.dart';
import 'widget_id.dart';

class SvgaPlugin {
  static const _channel = const MethodChannel('svga_plugin');
  static final _generator = GeneratorFactory.generatorOf(IDStrategy.timeStamp);

  /// Shortcut for invalid widget ID
  static ResultInfo get _invalidWigetID => ResultInfo()
    ..code = StatusCodes.illegalWidgetId
    ..message = 'Plugin provides an illegal widget ID'
    ..textureId = Int64(-1);

  /// Get widget's temparary ID
  ///
  /// This ID should be stored in the [SVGAWidget]'s state and used to invoke
  /// plugin methods. Once a negative ID is returned, that means the entire
  /// plugin is not ready for SVGA file parsing. You should not invoke any
  /// interface if you get into this situation
  static int generateID() => _generator?.generateID() ?? -1;

  static Future<ResultInfo> load(
    int widgetId, {
    required String source,
    required double width,
    required double height,
    int loopCount = 0,
    bool remoted = false,
    bool mute = false,
    BoxFit fit = BoxFit.contain,
  }) async {
    if (widgetId < 0) {
      return _invalidWigetID;
    }

    final loadInfo = SVGALoadInfo()
      ..widgetId = Int64(widgetId)
      ..mute = mute
      ..width = width
      ..height = height
      ..loopCount = loopCount
      ..boxFitToScaleType(fit);

    if (remoted) {
      loadInfo.remoteUrl = source;
    } else {
      loadInfo.assetUrl = source;
    }

    final method = remoted ? Methods.loadFromURL : Methods.loadFromAsset;
    return await _channel.invokeMethod(method, loadInfo.writeToBuffer());
  }

  Future<ResultInfo> pause(int widgetId) async {
    if (widgetId < 0) {
      return _invalidWigetID;
    }

    return await _channel.invokeMethod(Methods.pauseSVGAWidget, widgetId);
  }

  Future<ResultInfo> resume(int widgetId) async {
    if (widgetId < 0) {
      return _invalidWigetID;
    }

    return await _channel.invokeMethod(Methods.resumeSVGAWidget, widgetId);
  }

  Future<ResultInfo> dispose(int widgetId) async {
    if (widgetId < 0) {
      return _invalidWigetID;
    }

    return await _channel.invokeMethod(Methods.releaseSVGAWidget, widgetId);
  }
}
