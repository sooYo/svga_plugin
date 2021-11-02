///
//  Generated code. Do not modify.
//  source: svga_info.proto
//
// @dart = 2.12
// ignore_for_file: annotate_overrides,camel_case_types,unnecessary_const,non_constant_identifier_names,library_prefixes,unused_import,unused_shown_name,return_of_invalid_type,unnecessary_this,prefer_final_fields

import 'dart:core' as $core;

import 'package:fixnum/fixnum.dart' as $fixnum;
import 'package:protobuf/protobuf.dart' as $pb;

class SVGALoadInfo extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'SVGALoadInfo', createEmptyInstance: create)
    ..aOS(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'assetUrl', protoName: 'assetUrl')
    ..aOS(2, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'remoteUrl', protoName: 'remoteUrl')
    ..a<$core.double>(3, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'width', $pb.PbFieldType.OD)
    ..a<$core.double>(4, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'height', $pb.PbFieldType.OD)
    ..a<$core.int>(5, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'widgetId', $pb.PbFieldType.O3, protoName: 'widgetId')
    ..a<$core.int>(6, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'scaleType', $pb.PbFieldType.O3, protoName: 'scaleType')
    ..aOB(7, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'mute')
    ..hasRequiredFields = false
  ;

  SVGALoadInfo._() : super();
  factory SVGALoadInfo({
    $core.String? assetUrl,
    $core.String? remoteUrl,
    $core.double? width,
    $core.double? height,
    $core.int? widgetId,
    $core.int? scaleType,
    $core.bool? mute,
  }) {
    final _result = create();
    if (assetUrl != null) {
      _result.assetUrl = assetUrl;
    }
    if (remoteUrl != null) {
      _result.remoteUrl = remoteUrl;
    }
    if (width != null) {
      _result.width = width;
    }
    if (height != null) {
      _result.height = height;
    }
    if (widgetId != null) {
      _result.widgetId = widgetId;
    }
    if (scaleType != null) {
      _result.scaleType = scaleType;
    }
    if (mute != null) {
      _result.mute = mute;
    }
    return _result;
  }
  factory SVGALoadInfo.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory SVGALoadInfo.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  SVGALoadInfo clone() => SVGALoadInfo()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  SVGALoadInfo copyWith(void Function(SVGALoadInfo) updates) => super.copyWith((message) => updates(message as SVGALoadInfo)) as SVGALoadInfo; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static SVGALoadInfo create() => SVGALoadInfo._();
  SVGALoadInfo createEmptyInstance() => create();
  static $pb.PbList<SVGALoadInfo> createRepeated() => $pb.PbList<SVGALoadInfo>();
  @$core.pragma('dart2js:noInline')
  static SVGALoadInfo getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<SVGALoadInfo>(create);
  static SVGALoadInfo? _defaultInstance;

  @$pb.TagNumber(1)
  $core.String get assetUrl => $_getSZ(0);
  @$pb.TagNumber(1)
  set assetUrl($core.String v) { $_setString(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasAssetUrl() => $_has(0);
  @$pb.TagNumber(1)
  void clearAssetUrl() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get remoteUrl => $_getSZ(1);
  @$pb.TagNumber(2)
  set remoteUrl($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasRemoteUrl() => $_has(1);
  @$pb.TagNumber(2)
  void clearRemoteUrl() => clearField(2);

  @$pb.TagNumber(3)
  $core.double get width => $_getN(2);
  @$pb.TagNumber(3)
  set width($core.double v) { $_setDouble(2, v); }
  @$pb.TagNumber(3)
  $core.bool hasWidth() => $_has(2);
  @$pb.TagNumber(3)
  void clearWidth() => clearField(3);

  @$pb.TagNumber(4)
  $core.double get height => $_getN(3);
  @$pb.TagNumber(4)
  set height($core.double v) { $_setDouble(3, v); }
  @$pb.TagNumber(4)
  $core.bool hasHeight() => $_has(3);
  @$pb.TagNumber(4)
  void clearHeight() => clearField(4);

  @$pb.TagNumber(5)
  $core.int get widgetId => $_getIZ(4);
  @$pb.TagNumber(5)
  set widgetId($core.int v) { $_setSignedInt32(4, v); }
  @$pb.TagNumber(5)
  $core.bool hasWidgetId() => $_has(4);
  @$pb.TagNumber(5)
  void clearWidgetId() => clearField(5);

  @$pb.TagNumber(6)
  $core.int get scaleType => $_getIZ(5);
  @$pb.TagNumber(6)
  set scaleType($core.int v) { $_setSignedInt32(5, v); }
  @$pb.TagNumber(6)
  $core.bool hasScaleType() => $_has(5);
  @$pb.TagNumber(6)
  void clearScaleType() => clearField(6);

  @$pb.TagNumber(7)
  $core.bool get mute => $_getBF(6);
  @$pb.TagNumber(7)
  set mute($core.bool v) { $_setBool(6, v); }
  @$pb.TagNumber(7)
  $core.bool hasMute() => $_has(6);
  @$pb.TagNumber(7)
  void clearMute() => clearField(7);
}

class ResultInfo extends $pb.GeneratedMessage {
  static final $pb.BuilderInfo _i = $pb.BuilderInfo(const $core.bool.fromEnvironment('protobuf.omit_message_names') ? '' : 'ResultInfo', createEmptyInstance: create)
    ..a<$core.int>(1, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'code', $pb.PbFieldType.O3)
    ..aOS(2, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'message')
    ..aInt64(3, const $core.bool.fromEnvironment('protobuf.omit_field_names') ? '' : 'textureId', protoName: 'textureId')
    ..hasRequiredFields = false
  ;

  ResultInfo._() : super();
  factory ResultInfo({
    $core.int? code,
    $core.String? message,
    $fixnum.Int64? textureId,
  }) {
    final _result = create();
    if (code != null) {
      _result.code = code;
    }
    if (message != null) {
      _result.message = message;
    }
    if (textureId != null) {
      _result.textureId = textureId;
    }
    return _result;
  }
  factory ResultInfo.fromBuffer($core.List<$core.int> i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromBuffer(i, r);
  factory ResultInfo.fromJson($core.String i, [$pb.ExtensionRegistry r = $pb.ExtensionRegistry.EMPTY]) => create()..mergeFromJson(i, r);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.deepCopy] instead. '
  'Will be removed in next major version')
  ResultInfo clone() => ResultInfo()..mergeFromMessage(this);
  @$core.Deprecated(
  'Using this can add significant overhead to your binary. '
  'Use [GeneratedMessageGenericExtensions.rebuild] instead. '
  'Will be removed in next major version')
  ResultInfo copyWith(void Function(ResultInfo) updates) => super.copyWith((message) => updates(message as ResultInfo)) as ResultInfo; // ignore: deprecated_member_use
  $pb.BuilderInfo get info_ => _i;
  @$core.pragma('dart2js:noInline')
  static ResultInfo create() => ResultInfo._();
  ResultInfo createEmptyInstance() => create();
  static $pb.PbList<ResultInfo> createRepeated() => $pb.PbList<ResultInfo>();
  @$core.pragma('dart2js:noInline')
  static ResultInfo getDefault() => _defaultInstance ??= $pb.GeneratedMessage.$_defaultFor<ResultInfo>(create);
  static ResultInfo? _defaultInstance;

  @$pb.TagNumber(1)
  $core.int get code => $_getIZ(0);
  @$pb.TagNumber(1)
  set code($core.int v) { $_setSignedInt32(0, v); }
  @$pb.TagNumber(1)
  $core.bool hasCode() => $_has(0);
  @$pb.TagNumber(1)
  void clearCode() => clearField(1);

  @$pb.TagNumber(2)
  $core.String get message => $_getSZ(1);
  @$pb.TagNumber(2)
  set message($core.String v) { $_setString(1, v); }
  @$pb.TagNumber(2)
  $core.bool hasMessage() => $_has(1);
  @$pb.TagNumber(2)
  void clearMessage() => clearField(2);

  @$pb.TagNumber(3)
  $fixnum.Int64 get textureId => $_getI64(2);
  @$pb.TagNumber(3)
  set textureId($fixnum.Int64 v) { $_setInt64(2, v); }
  @$pb.TagNumber(3)
  $core.bool hasTextureId() => $_has(2);
  @$pb.TagNumber(3)
  void clearTextureId() => clearField(3);
}

