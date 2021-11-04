# svga_plugin

This plugin copies [SVGA frameworks](https://github.com/svga) and combine  both iOS and Android codes into a single Flutter plugin. And implements the Flutter SVGA widget by external texture, also enable audio-playing feature on Flutter side additionally. 

Currrently, iOS implementation is still under going and thus  be unavailable temporarily.

## Install 
Currently, you can import this plugin to your Flutter project by altering the `pubspec.yaml` file, adding lines below to the `dependencies` entry
	
	 svga_plugin: 
	 	git:
	 	   url: https://github.com/sooYo/svga_plugin.git
	 	 
## Usage 
 You can follow the example on [SVGA Android](https://github.com/svga/SVGAPlayer-Android#svgaplayer) or [SVGA iOS](https://github.com/svga/SVGAPlayer-iOS#svgaplayer) to use SVGA animation on native side.
 
 On Flutter, please use the `SVGAWidget` to load your SVGA source from both  assets and URL,  just as simple as below:
 
 	import 'package:svga_plugin/svga_plugin.dart';

	SVGAWidget.network('https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/EmptyState.svga')
	SVGAWidget.asset('lib/assets/jojo_audio.svga') 
	 
For more detail, please refer to the [SVGA widget definition](https://github.com/sooYo/svga_plugin/blob/main/lib/src/svga_widget.dart)