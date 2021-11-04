import 'package:flutter/material.dart';
import 'package:svga_plugin/svga_plugin.dart';
import 'package:svga_plugin_example/cover_page.dart';

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
      navigatorObservers: [observer],
      home: Builder(builder: (context) => SVGAList()),
    );
  }
}

class SVGAList extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _SVGAListState();
}

class _SVGAListState extends State<SVGAList> with NavigatorObserver {
  final assetPaths = ['lib/assets/jojo_audio.svga', 'lib/assets/angel.svga'];
  final urls = [
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/EmptyState.svga",
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/HamburgerArrow.svga",
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/PinJump.svga",
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/TwitterHeart.svga",
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/Walkthrough.svga",
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/kingset.svga",
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/halloween.svga",
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/heartbeat.svga",
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/matteBitmap.svga",
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/matteBitmap_1.x.svga",
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/matteRect.svga",
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/mutiMatte.svga",
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/posche.svga",
    // "https://cdn.jsdelivr.net/gh/svga/SVGA-Samples@master/rose.svga",
    "https://img.ah-suuwaa.com/jojo_audio.svga",
  ];

  bool _covered = false;

  void onCoverChanged(bool cover) {
    if (mounted) {
      setState(() {
        _covered = cover;
      });
    }
  }

  @override
  void initState() {
    super.initState();
    observer.callback = onCoverChanged;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
        actions: [
          IconButton(
            icon: Icon(Icons.assistant_navigation, size: 20),
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (context) => CoverPageToMuteAuidos(),
                  settings: RouteSettings(name: "pageName", arguments: "Cover"),
                ),
              );
            },
          )
        ],
      ),
      // body: GridView.builder(
      //   gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
      //     crossAxisCount: 2,
      //     childAspectRatio: 1.2,
      //     mainAxisSpacing: 5,
      //     crossAxisSpacing: 5,
      //   ),
      //   itemCount: 2,
      //   itemBuilder: (context, index) => SVGAWidget.asset(assetPaths[index]),
      //   // itemBuilder: (context, index) => Container(),
      // ),
      body: GridView.builder(
        gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 2,
          childAspectRatio: 1.2,
          mainAxisSpacing: 5,
          crossAxisSpacing: 5,
        ),
        itemCount: urls.length,
        itemBuilder: (context, index) => SVGAWidget.network(
          urls[index],
          play: !_covered,
        ),
      ),
    );
  }
}

final observer = NavObserver();

typedef CoverCallback = void Function(bool covered);

class NavObserver with NavigatorObserver {
  CoverCallback? callback;

  @override
  void didPop(Route route, Route? previousRoute) {
    super.didPop(route, previousRoute);

    if (route.settings.arguments == "Cover") {
      callback?.call(false);
    }
  }

  @override
  void didPush(Route route, Route? previousRoute) {
    super.didPush(route, previousRoute);
    if (route.settings.arguments == "Cover") {
      callback?.call(true);
    }
  }
}
