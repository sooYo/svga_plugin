/// Status code list
///
/// This file is generated by shell script on 2021-11-02, please don't edit this file manually

class StatusCodes {
  // Success
  static const ok = 200;
  
  // Cannot parse PB data
  static const pbParseFailed = 100001;
  
  // Arguments from channel method params map with a wrong data type
  static const argumentTypeError = 100002;
  
  // Error encountered during the texture creation process
  static const textureError = 100003;
  
  // Some of the key params are missing
  static const dataMissing = 100004;
  
  // Data format or data content gets wrong
  static const dataError = 100005;
  
  // SVGA parser failed to handle the source
  static const svgaParseError = 100006;
  
  // Plugin does not provide a legal widget id
  static const illegalWidgetId = 100007;
  
}