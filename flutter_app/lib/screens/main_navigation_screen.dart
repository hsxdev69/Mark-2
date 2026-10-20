import 'package:flutter/material.dart';
import '../models/media_item.dart';
import '../services/backend_bridge.dart';
import '../widgets/floating_nav_bar.dart';
import 'home_screen.dart';
import 'search_screen.dart';
import 'downloads_screen.dart';
import 'profile_screen.dart';
import 'detail_screen.dart';

class MainNavigationScreen extends StatefulWidget {
  const MainNavigationScreen({Key? key}) : super(key: key);

  @override
  State<MainNavigationScreen> createState() => _MainNavigationScreenState();
}

class _MainNavigationScreenState extends State<MainNavigationScreen> {
  int _currentIndex = 0;

  void _navigateToDetail(MediaItem media) {
    Navigator.push(
      context,
      PageRouteBuilder(
        pageBuilder: (context, animation, secondaryAnimation) => DetailScreen(media: media),
        transitionsBuilder: (context, animation, secondaryAnimation, child) {
          const begin = Offset(0.0, 0.05);
          const end = Offset.zero;
          const curve = Curves.fastOutSlowIn;
          var tween = Tween(begin: begin, end: end).chain(CurveTween(curve: curve));
          return FadeTransition(
            opacity: animation,
            child: SlideTransition(position: animation.drive(tween), child: child),
          );
        },
        transitionDuration: const Duration(milliseconds: 300),
      ),
    );
  }

  void _playMedia(MediaItem media) {
    BackendBridge.playMedia(media);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
          // Screen Switcher
          IndexedStack(
            index: _currentIndex,
            children: [
              HomeScreen(
                onMediaClick: _navigateToDetail,
                onPlayClick: _playMedia,
              ),
              SearchScreen(
                onMediaClick: _navigateToDetail,
              ),
              const DownloadsScreen(),
              const ProfileScreen(),
            ],
          ),

          // Floating Glassmorphic Nav Bar
          Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            child: FloatingNavBar(
              currentIndex: _currentIndex,
              onTap: (index) {
                setState(() => _currentIndex = index);
              },
            ),
          ),
        ],
      ),
    );
  }
}
