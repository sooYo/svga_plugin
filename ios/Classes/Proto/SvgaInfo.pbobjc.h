// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: svga_info.proto

// This CPP symbol can be defined to use imports that match up to the framework
// imports needed when using CocoaPods.
#if !defined(GPB_USE_PROTOBUF_FRAMEWORK_IMPORTS)
 #define GPB_USE_PROTOBUF_FRAMEWORK_IMPORTS 0
#endif

#if GPB_USE_PROTOBUF_FRAMEWORK_IMPORTS
 #import <Protobuf/GPBProtocolBuffers.h>
#else
 #import "GPBProtocolBuffers.h"
#endif

#if GOOGLE_PROTOBUF_OBJC_VERSION < 30004
#error This file was generated by a newer version of protoc which is incompatible with your Protocol Buffer library sources.
#endif
#if 30004 < GOOGLE_PROTOBUF_OBJC_MIN_SUPPORTED_VERSION
#error This file was generated by an older version of protoc which is incompatible with your Protocol Buffer library sources.
#endif

// @@protoc_insertion_point(imports)

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"

CF_EXTERN_C_BEGIN

NS_ASSUME_NONNULL_BEGIN

#pragma mark - SvgaInfoRoot

/**
 * Exposes the extension registry for this file.
 *
 * The base class provides:
 * @code
 *   + (GPBExtensionRegistry *)extensionRegistry;
 * @endcode
 * which is a @c GPBExtensionRegistry that includes all the extensions defined by
 * this file and all files that it depends on.
 **/
GPB_FINAL @interface SvgaInfoRoot : GPBRootObject
@end

#pragma mark - SVGALoadInfo

typedef GPB_ENUM(SVGALoadInfo_FieldNumber) {
  SVGALoadInfo_FieldNumber_AssetURL = 1,
  SVGALoadInfo_FieldNumber_RemoteURL = 2,
  SVGALoadInfo_FieldNumber_Width = 3,
  SVGALoadInfo_FieldNumber_Height = 4,
  SVGALoadInfo_FieldNumber_WidgetId = 5,
  SVGALoadInfo_FieldNumber_ScaleType = 6,
  SVGALoadInfo_FieldNumber_Mute = 7,
  SVGALoadInfo_FieldNumber_LoopCount = 8,
  SVGALoadInfo_FieldNumber_ContinualResume = 9,
};

GPB_FINAL @interface SVGALoadInfo : GPBMessage

/** Exclusive with remoteUrl */
@property(nonatomic, readwrite, copy, null_resettable) NSString *assetURL;

/** Exclusive with assetUrl */
@property(nonatomic, readwrite, copy, null_resettable) NSString *remoteURL;

@property(nonatomic, readwrite) double width;

@property(nonatomic, readwrite) double height;

/**
 * Created on Flutter side and should be unique accross all
 * SVGA widgets, used to handle quick-dispose occasion
 **/
@property(nonatomic, readwrite) int64_t widgetId;

/** Alias to andriod's ImageView.ScaleType */
@property(nonatomic, readwrite) int32_t scaleType;

@property(nonatomic, readwrite) BOOL mute;

@property(nonatomic, readwrite) int32_t loopCount;

/**
 * Where resume the animation, from frame where it's paused
 * or the first frame. If it's `true`, it should continue to
 * play next frame from where it's paused
 **/
@property(nonatomic, readwrite) BOOL continualResume;

@end

#pragma mark - ResultInfo

typedef GPB_ENUM(ResultInfo_FieldNumber) {
  ResultInfo_FieldNumber_Code = 1,
  ResultInfo_FieldNumber_Message = 2,
  ResultInfo_FieldNumber_TextureId = 3,
};

GPB_FINAL @interface ResultInfo : GPBMessage

@property(nonatomic, readwrite) int32_t code;

@property(nonatomic, readwrite, copy, null_resettable) NSString *message;

/**
 * Available when invoke a load function, and shall all be
 * -1 if it's not. when it's less than 0, it shouldn't be
 * uesed to construct Texture widget in Flutter
 **/
@property(nonatomic, readwrite) int64_t textureId;

@end

NS_ASSUME_NONNULL_END

CF_EXTERN_C_END

#pragma clang diagnostic pop

// @@protoc_insertion_point(global_scope)
