import 'package:flutter/material.dart';
import 'package:svga_plugin/svga_plugin.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: LayoutBuilder(
          builder: (context, constraint) {
            return FutureBuilder(
              future: SvgaPlugin.crateSVGA(
                constraint.maxWidth,
                constraint.maxHeight,
              ),
              builder: (context, AsyncSnapshot<int> snapshot) {
                if (snapshot.connectionState == ConnectionState.done &&
                    snapshot.data != null) {
                  return Texture(textureId: snapshot.data!);
                }

                return Container();
              },
            );
          },
        ),
      ),
    );
  }
}
