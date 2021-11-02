import 'package:flutter/cupertino.dart';
import 'package:svga_plugin/src/proto/pb_header.dart';

extension LoadInfoConverter on SVGALoadInfo {
  void boxFitToScaleType(BoxFit fit) {
    switch (fit) {
      case BoxFit.contain:
        scaleType = 3; // ScaleType.FIT_CENTER
        break;
      case BoxFit.cover:
        scaleType = 6; // ScaleType.CENTER_CROP
        break;
      case BoxFit.fill:
        scaleType = 1; // ScaleType.FIT_XY
        break;
      case BoxFit.fitWidth:
      case BoxFit.fitHeight:
        scaleType = 7; // ScaleType.CENTER_INSIDE
        break;
      default:
        scaleType = 5; // ScaleType.CENTER
        break;
    }
  }
}
