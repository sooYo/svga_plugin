import 'dart:math';

import 'package:flutter/cupertino.dart';

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
    this.play = true,
    this.loopCount = 0,
    this.fit = BoxFit.contain,
    this.indicatorRadius = 8.0,
    this.showLoadingIndicator = true,
  })  : _remoted = false,
        _source = assetPath,
        assert(loadingWidget != null || indicatorRadius >= 0.0);

  const SVGAWidget.network(
    String url, {
    this.width,
    this.height,
    this.loadingWidget,
    this.errorWidget,
    this.onComplete,
    this.mute = false,
    this.play = true,
    this.loopCount = 0,
    this.fit = BoxFit.contain,
    this.indicatorRadius = 8.0,
    this.showLoadingIndicator = true,
  })  : _remoted = true,
        _source = url,
        assert(loadingWidget != null || indicatorRadius >= 0.0);

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

  final bool play;

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

  /// The default indicator size, minimum size is 3.0,
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
  void dispose() {
    SvgaPlugin.dispose(_id);
    super.dispose();
  }

  @override
  void didUpdateWidget(covariant SVGAWidget oldWidget) {
    super.didUpdateWidget(oldWidget);

    if (widget.play != oldWidget.play) {
      widget.play ? SvgaPlugin.resume(_id) : SvgaPlugin.pause(_id);
    }
  }

  @override
  Widget build(BuildContext context) {
    // Custom loading style
    final loadingWidget = widget.loadingWidget ??
        Visibility(
          child: CupertinoActivityIndicator(
            radius: max(widget.indicatorRadius, 3.0),
          ),
        );

    // Custom error widget
    final errorWidget = widget.errorWidget ?? Container();

    // Embeded LayoutBuilder
    if (widget.width == null || widget.height == null) {
      return LayoutBuilder(
        builder: (context, constraints) => _SVGACore(
          _id,
          width: constraints.maxWidth,
          height: constraints.maxHeight,
          source: widget._source,
          remoted: widget._remoted,
          fit: widget.fit,
          mute: widget.mute,
          loopCount: widget.loopCount,
          loadingWidget: loadingWidget,
          errorWidget: errorWidget,
          onComplete: widget.onComplete,
        ),
      );
    }

    // Container without size calculation
    return Container(
      width: widget.width,
      height: widget.height,
      child: _SVGACore(
        _id,
        width: widget.width!,
        height: widget.height!,
        source: widget._source,
        remoted: widget._remoted,
        fit: widget.fit,
        mute: widget.mute,
        loopCount: widget.loopCount,
        loadingWidget: loadingWidget,
        errorWidget: errorWidget,
        onComplete: widget.onComplete,
      ),
    );
  }
}

class _SVGACore extends StatelessWidget {
  _SVGACore(
    this.widgetId, {
    required this.width,
    required this.height,
    required this.source,
    required this.remoted,
    required this.loadingWidget,
    required this.errorWidget,
    this.onComplete,
    this.loopCount = 0,
    this.mute = false,
    this.fit = BoxFit.contain,
  });

  final int widgetId;

  final String source;
  final bool remoted;

  final double width;
  final double height;
  final Widget loadingWidget;
  final Widget errorWidget;

  final int loopCount;
  final bool mute;
  final BoxFit fit;

  final SVGALoadCompletion? onComplete;

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: _parseSvga(width, height),
      builder: (BuildContext context, AsyncSnapshot<ResultInfo> snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting ||
            snapshot.connectionState == ConnectionState.active) {
          return Center(child: loadingWidget);
        }

        return (snapshot.data?.isOK ?? false)
            ? Texture(textureId: snapshot.data!.textureId.toInt())
            : errorWidget;
      },
    );
  }

  Future<ResultInfo> _parseSvga(double width, double height) async {
    final result = await SvgaPlugin.load(
      widgetId,
      width: width,
      height: height,
      fit: fit,
      mute: mute,
      source: source,
      remoted: remoted,
      loopCount: max(loopCount, 0),
    );

    onComplete?.call(result);
    return result;
  }
}
