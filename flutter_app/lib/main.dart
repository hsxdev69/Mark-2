import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'theme/monochrome_theme.dart';
import 'services/search_service.dart';
import 'screens/main_navigation_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  // Edge to Edge Transparent System Bars
  SystemChrome.setSystemUIOverlayStyle(
    const SystemUiOverlayStyle(
      statusBarColor: Colors.transparent,
      statusBarIconBrightness: Brightness.light,
      systemNavigationBarColor: Colors.transparent,
      systemNavigationBarIconBrightness: Brightness.light,
    ),
  );

  runApp(const MovieVortexApp());
}

class MovieVortexApp extends StatelessWidget {
  const MovieVortexApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => SearchService()),
      ],
      child: MaterialApp(
        title: 'MovieVortex',
        debugShowCheckedModeBanner: false,
        theme: MonochromeTheme.themeData,
        home: const MainNavigationScreen(),
      ),
    );
  }
}
