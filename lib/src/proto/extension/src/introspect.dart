import 'package:svga_plugin/src/proto/pb_header.dart';

import '../../../constants/status_codes.dart';

extension ResultInfoIntroSpect on ResultInfo {
  bool get isOK => code == StatusCodes.ok;
}
