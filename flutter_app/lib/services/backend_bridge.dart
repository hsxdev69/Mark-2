import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import '../models/media_item.dart';
import '../models/episode_item.dart';

class BackendBridge {
  static const MethodChannel _channel = MethodChannel('com.movievortex.flutter/core');

  // Check if native channel is available
  static Future<bool> isNativeBackendAvailable() async {
    try {
      final bool available = await _channel.invokeMethod('isBackendReady') ?? false;
      return available;
    } catch (_) {
      return false;
    }
  }

  // Load Home Content
  static Future<Map<String, dynamic>> getHomeContent() async {
    try {
      final String? result = await _channel.invokeMethod('getHomeContent');
      if (result != null && result.isNotEmpty) {
        return jsonDecode(result);
      }
    } catch (e) {
      debugPrint('Native getHomeContent fallback: $e');
    }
    return _getFallbackHomeContent();
  }

  // Search Movies / Series
  static Future<List<MediaItem>> search(String query) async {
    try {
      final String? result = await _channel.invokeMethod('search', {'query': query});
      if (result != null && result.isNotEmpty) {
        final List<dynamic> list = jsonDecode(result);
        return list.map((item) => MediaItem.fromJson(item)).toList();
      }
    } catch (e) {
      debugPrint('Native search fallback: $e');
    }
    return _getFallbackSearchResults(query);
  }

  // Load Full Media Details
  static Future<Map<String, dynamic>> loadDetails(String rawUrl, String apiName) async {
    try {
      final String? result = await _channel.invokeMethod('loadDetails', {
        'rawUrl': rawUrl,
        'apiName': apiName,
      });
      if (result != null && result.isNotEmpty) {
        return jsonDecode(result);
      }
    } catch (e) {
      debugPrint('Native loadDetails fallback: $e');
    }
    return {};
  }

  // Launch Native ExoPlayer
  static Future<void> playMedia(MediaItem media, {EpisodeItem? episode}) async {
    try {
      await _channel.invokeMethod('playMedia', {
        'rawUrl': media.rawUrl,
        'title': media.title,
        'apiName': media.providerApiName,
        'episodeData': episode?.dataUrl,
        'episodeNum': episode?.episodeNumber,
        'seasonNum': episode?.seasonNumber,
      });
    } catch (e) {
      debugPrint('Native playMedia error: $e');
    }
  }

  // Fallback / Initial Data for Instant Smooth Rendering
  static Map<String, dynamic> _getFallbackHomeContent() {
    return {
      'hero': [
        {
          'id': 'hero_1',
          'title': 'Severance Season 2',
          'poster': 'https://image.tmdb.org/t/p/w780/jFp1v64R2i64Xj7b5R9YhE4kG.jpg',
          'backdrop': 'https://image.tmdb.org/t/p/original/jFp1v64R2i64Xj7b5R9YhE4kG.jpg',
          'type': 'tvSeries',
          'year': 2025,
          'rating': 8.9,
          'synopsis': 'Mark leads a team of office workers whose memories have been surgically divided between their work and personal lives.',
          'genres': ['Sci-Fi', 'Thriller', 'Mystery'],
          'rawUrl': 'https://example.com/severance',
          'providerApiName': 'IStreamFlare',
        },
        {
          'id': 'hero_2',
          'title': 'Interstellar',
          'poster': 'https://image.tmdb.org/t/p/w780/gEU2QniE6EwfVDxCzsxpnqdhaq9.jpg',
          'backdrop': 'https://image.tmdb.org/t/p/original/gEU2QniE6EwfVDxCzsxpnqdhaq9.jpg',
          'type': 'movie',
          'year': 2014,
          'rating': 9.0,
          'synopsis': 'When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot, Joseph Cooper, is tasked to pilot a spacecraft.',
          'genres': ['Sci-Fi', 'Adventure', 'Drama'],
          'rawUrl': 'https://example.com/interstellar',
          'providerApiName': 'MovieBoxProvider',
        },
        {
          'id': 'hero_3',
          'title': 'Cyberpunk: Edgerunners',
          'poster': 'https://image.tmdb.org/t/p/w780/7jEPVbI4k4dG9c0iE3U1WkM5k.jpg',
          'backdrop': 'https://image.tmdb.org/t/p/original/7jEPVbI4k4dG9c0iE3U1WkM5k.jpg',
          'type': 'anime',
          'year': 2022,
          'rating': 8.7,
          'synopsis': 'A street kid trying to survive in a technology and body modification-obsessed city of the future.',
          'genres': ['Anime', 'Action', 'Sci-Fi'],
          'rawUrl': 'https://example.com/cyberpunk',
          'providerApiName': 'AniVortex',
        }
      ],
      'continueWatching': [
        {
          'id': 'cw_1',
          'title': 'Dune: Part Two',
          'poster': 'https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg',
          'watchProgress': 0.72,
          'rawUrl': 'https://example.com/dune2',
          'year': 2024,
          'rating': 8.6,
        },
        {
          'id': 'cw_2',
          'title': 'Arcane Season 2',
          'poster': 'https://image.tmdb.org/t/p/w500/abf3xC09q2uH5P6eT0q9.jpg',
          'watchProgress': 0.45,
          'rawUrl': 'https://example.com/arcane',
          'year': 2024,
          'rating': 9.1,
        }
      ],
      'trending': [
        {
          'id': 'tr_1',
          'title': 'The Batman',
          'poster': 'https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg',
          'year': 2022,
          'rating': 7.8,
          'type': 'movie',
          'rawUrl': 'https://example.com/thebatman',
        },
        {
          'id': 'tr_2',
          'title': 'Solo Leveling',
          'poster': 'https://image.tmdb.org/t/p/w500/geCRueV3ElhRTr0Q2xBu63z6tcJ.jpg',
          'year': 2024,
          'rating': 8.5,
          'type': 'anime',
          'rawUrl': 'https://example.com/sololeveling',
        },
        {
          'id': 'tr_3',
          'title': 'Oppenheimer',
          'poster': 'https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg',
          'year': 2023,
          'rating': 8.9,
          'type': 'movie',
          'rawUrl': 'https://example.com/oppenheimer',
        },
        {
          'id': 'tr_4',
          'title': 'Stranger Things',
          'poster': 'https://image.tmdb.org/t/p/w500/49WJfeN0moxb9IPfGn8AIqMGskD.jpg',
          'year': 2022,
          'rating': 8.7,
          'type': 'tvSeries',
          'rawUrl': 'https://example.com/strangerthings',
        }
      ]
    };
  }

  static List<MediaItem> _getFallbackSearchResults(String query) {
    final all = [
      const MediaItem(
        id: 's_1',
        title: 'Interstellar',
        poster: 'https://image.tmdb.org/t/p/w500/gEU2QniE6EwfVDxCzsxpnqdhaq9.jpg',
        year: 2014,
        rating: 9.0,
        genres: ['Sci-Fi', 'Adventure'],
        rawUrl: 'https://example.com/interstellar',
      ),
      const MediaItem(
        id: 's_2',
        title: 'Severance',
        poster: 'https://image.tmdb.org/t/p/w500/jFp1v64R2i64Xj7b5R9YhE4kG.jpg',
        year: 2025,
        rating: 8.9,
        genres: ['Sci-Fi', 'Mystery'],
        rawUrl: 'https://example.com/severance',
      ),
      const MediaItem(
        id: 's_3',
        title: 'Solo Leveling',
        poster: 'https://image.tmdb.org/t/p/w500/geCRueV3ElhRTr0Q2xBu63z6tcJ.jpg',
        year: 2024,
        rating: 8.5,
        genres: ['Anime', 'Action'],
        rawUrl: 'https://example.com/sololeveling',
      ),
      const MediaItem(
        id: 's_4',
        title: 'Blade Runner 2049',
        poster: 'https://image.tmdb.org/t/p/w500/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg',
        year: 2017,
        rating: 8.0,
        genres: ['Sci-Fi', 'Action'],
        rawUrl: 'https://example.com/bladerunner',
      ),
      const MediaItem(
        id: 's_5',
        title: 'The Matrix',
        poster: 'https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg',
        year: 1999,
        rating: 8.7,
        genres: ['Sci-Fi', 'Action'],
        rawUrl: 'https://example.com/thematrix',
      ),
    ];

    if (query.trim().isEmpty) return all;
    return all.where((e) => e.title.toLowerCase().contains(query.toLowerCase())).toList();
  }
}
