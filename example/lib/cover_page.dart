import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class CoverPageToMuteAuidos extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Mute Audios', style: TextStyle(fontSize: 18)),
      ),
      body: Center(
        child: Text(
          'Mute auidos when this page pushed in\n when return back, you should hear the sound again',
          textAlign: TextAlign.center,
        ),
      ),
    );
  }
}
