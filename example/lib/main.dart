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
        body: GridView.builder(
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 2,
            childAspectRatio: 1.2,
            mainAxisSpacing: 5,
            crossAxisSpacing: 5,
          ),
          itemCount: 10,
          itemBuilder: (context, index) => SVGAWidget.asset(
            'lib/assets/jojo_audio.svga',
          ),
        ),
      ),
    );
  }
}
