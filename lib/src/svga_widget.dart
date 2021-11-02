import 'package:flutter/material.dart';

import 'constants/type_alias.dart';
import 'plugin.dart';
import 'proto/pb_header.dart';

class SVGAWidget extends StatefulWidget {
  const SVGAWidget.asset(
    String assetPath, {
    this.width,
    this.height,
    this.loadingWidget,
    this.errorWidget,
    this.onComplete,
    this.mute = false,
    this.loopCount = 0,
    this.fit = BoxFit.contain,
    this.indicatorRadius = 8.0,
    this.showLoadingIndicator = true,
  })  : _remoted = false,
        _source = assetPath;

  const SVGAWidget.network(
    String url, {
    this.width,
    this.height,
    this.loadingWidget,
    this.errorWidget,
    this.onComplete,
    this.mute = false,
    this.loopCount = 0,
    this.fit = BoxFit.contain,
    this.indicatorRadius = 8.0,
    this.showLoadingIndicator = true,
  })  : _remoted = true,
        _source = url;

  /// If widget cannot gain size info from outside, it will automatically
  /// embed a [LayoutBuilder] into the widget hierarchy. The native parser
  /// needs size info the scale the SVGA file properly. It's good to efficiency
  /// if size info is provided from outside
  final double? width;
  final double? height;

  /// Looping control
  ///
  /// It's the count to run this SVGA file, positive numbers means running the
  /// animation for exact [loopCount] time(s). Negative numbers and 0 both make
  /// this animation enter an infinite looping.
  final int loopCount;

  /// Mute this SVGA file
  ///
  /// If the target SVGA file contains soundtracks, control whether to
  /// mute them or not. Otherwise, this property has no effect.
  final bool mute;

  final BoxFit fit;

  /// Custom loading widget
  ///
  /// Display a custom loading widget instead of the default loading indicator.
  /// If it's not null, then both [showLoadingIndicator] and [indicatorRadius]
  /// will be ingored by this widget.
  final Widget? loadingWidget;

  /// Custom error widget
  ///
  /// Display a custom error widget instead of showing nothing when error occured.
  final Widget? errorWidget;

  /// Show default indicator or not
  ///
  /// If it's `true` and [loadingWidget] is null, this widget will diplay a
  /// [CupertinoActivityIndicator] with radius [ indicatorRadius] when the
  /// data is loading. Has no effect if [loadingWidget] is not null
  final bool showLoadingIndicator;

  /// The default indicator size, minimum size is 8.0,
  /// has no effect if [loadingWidget] is not null
  final double indicatorRadius;

  /// Load callback
  final SVGALoadCompletion? onComplete;

  /// For different parser entries
  final String _source;
  final bool _remoted;

  @override
  State<StatefulWidget> createState() => _SVGAState();
}

class _SVGAState extends State<SVGAWidget> {
  int _id = -1;

  @override
  void initState() {
    super.initState();
    _id = SvgaPlugin.generateID();
  }

  @override
  Widget build(BuildContext context) {
    return Container();
  }

  Future<ResultInfo> _loadSVGA(double width, double height) async {
    final result = await SvgaPlugin.load(
      _id,
      source: widget._source,
      width: width,
      height: height,
      mute: widget.mute,
    );

    widget.onComplete?.call(result);
    return result;
  }
}
