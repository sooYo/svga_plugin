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
  static final _generator = GeneratorFactory.generatorOf(IDStrategy.combine);

  /// Shortcut for invalid widget ID
  static ResultInfo get _invalidWigetID => ResultInfo()
    ..code = StatusCodes.illegalWidgetId
    ..message = 'Plugin provides an illegal widget ID'
    ..textureId = Int64(-1);

  /// PB parse error, for result generating
  static ResultInfo get _parsePBFailed => ResultInfo()
    ..code = StatusCodes.pbParseFailed
    ..message = 'Cannot convert data bytes into legal PB struct'
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
    bool continual = true,
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
      ..continualResume = continual
      ..boxFitToScaleType(fit);

    if (remoted) {
      loadInfo.remoteUrl = source;
    } else {
      loadInfo.assetUrl = 'flutter_assets/$source';
    }

    final method = remoted ? Methods.loadFromURL : Methods.loadFromAsset;
    final bytes = await _channel.invokeMethod(method, loadInfo.writeToBuffer());

    try {
      return ResultInfo.fromBuffer(bytes as List<int>);
    } catch (e) {
      return _parsePBFailed;
    }
  }

  static Future<ResultInfo> pause(int widgetId) async {
    if (widgetId < 0) {
      return _invalidWigetID;
    }

    final bytes = await _channel.invokeMethod(
      Methods.pauseSVGAWidget,
      widgetId,
    );

    try {
      return ResultInfo.fromBuffer(bytes as List<int>);
    } catch (e) {
      return _parsePBFailed;
    }
  }

  static Future<ResultInfo> resume(int widgetId) async {
    if (widgetId < 0) {
      return _invalidWigetID;
    }

    final bytes = await _channel.invokeMethod(
      Methods.resumeSVGAWidget,
      widgetId,
    );

    try {
      return ResultInfo.fromBuffer(bytes as List<int>);
    } catch (e) {
      return _parsePBFailed;
    }
  }

  static Future<ResultInfo> dispose(int widgetId) async {
    if (widgetId < 0) {
      return _invalidWigetID;
    }

    final bytes = await _channel.invokeMethod(
      Methods.releaseSVGAWidget,
      widgetId,
    );

    try {
      return ResultInfo.fromBuffer(bytes as List<int>);
    } catch (e) {
      return _parsePBFailed;
    }
  }
}
