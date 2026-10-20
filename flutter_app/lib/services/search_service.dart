import 'package:flutter/foundation.dart';
import '../models/search_suggestion.dart';
import '../models/media_item.dart';
import 'backend_bridge.dart';

class SearchService extends ChangeNotifier {
  String _query = '';
  String get query => _query;

  bool _isLoading = false;
  bool get isLoading => _isLoading;

  List<MediaItem> _results = [];
  List<MediaItem> get results => _results;

  List<String> _recentSearches = [
    'Interstellar',
    'Severance',
    'Solo Leveling',
    'Cyberpunk',
  ];
  List<String> get recentSearches => _recentSearches;

  final List<SearchSuggestion> _trendingSuggestions = const [
    SearchSuggestion(
      title: 'Severance',
      subtitle: 'Apple TV+ • Trending #1 Series',
      poster: 'https://image.tmdb.org/t/p/w200/jFp1v64R2i64Xj7b5R9YhE4kG.jpg',
      rating: 8.9,
      isTrending: true,
      category: 'Sci-Fi',
    ),
    SearchSuggestion(
      title: 'Interstellar',
      subtitle: 'Christopher Nolan • 4K Remaster',
      poster: 'https://image.tmdb.org/t/p/w200/gEU2QniE6EwfVDxCzsxpnqdhaq9.jpg',
      rating: 9.0,
      isTrending: true,
      category: 'Sci-Fi',
    ),
    SearchSuggestion(
      title: 'Solo Leveling',
      subtitle: 'A-1 Pictures • Trending Anime',
      poster: 'https://image.tmdb.org/t/p/w200/geCRueV3ElhRTr0Q2xBu63z6tcJ.jpg',
      rating: 8.5,
      isTrending: true,
      category: 'Anime',
    ),
    SearchSuggestion(
      title: 'Dune: Part Two',
      subtitle: 'Denis Villeneuve • IMAX 4K',
      poster: 'https://image.tmdb.org/t/p/w200/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg',
      rating: 8.6,
      isTrending: true,
      category: 'Sci-Fi',
    ),
    SearchSuggestion(
      title: 'Blade Runner 2049',
      subtitle: 'Cyberpunk Masterpiece',
      poster: 'https://image.tmdb.org/t/p/w200/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg',
      rating: 8.0,
      isTrending: true,
      category: 'Cyberpunk',
    ),
    SearchSuggestion(
      title: 'The Batman',
      subtitle: 'Matt Reeves • Noir Detective',
      poster: 'https://image.tmdb.org/t/p/w200/74xTEgt7R36Fpooo50r9T25onhq.jpg',
      rating: 7.8,
      isTrending: true,
      category: 'Action',
    ),
  ];
  List<SearchSuggestion> get trendingSuggestions => _trendingSuggestions;

  List<SearchSuggestion> getSuggestionsForQuery(String q) {
    if (q.trim().isEmpty) {
      return _trendingSuggestions;
    }
    final lower = q.trim().toLowerCase();
    return _trendingSuggestions.where((s) => s.title.toLowerCase().contains(lower) || (s.category?.toLowerCase().contains(lower) ?? false)).toList();
  }

  void updateQuery(String newQuery) {
    _query = newQuery;
    notifyListeners();

    if (newQuery.trim().isEmpty) {
      _results = [];
      _isLoading = false;
      notifyListeners();
    } else {
      performSearch(newQuery);
    }
  }

  Future<void> performSearch(String searchKeyword) async {
    final cleanKeyword = searchKeyword.trim();
    if (cleanKeyword.isEmpty) return;

    _isLoading = true;
    notifyListeners();

    // Query backend bridge
    final items = await BackendBridge.search(cleanKeyword);

    _results = items;
    _isLoading = false;

    // Add to recent if not present
    if (!_recentSearches.contains(cleanKeyword)) {
      _recentSearches.insert(0, cleanKeyword);
      if (_recentSearches.length > 8) {
        _recentSearches.removeLast();
      }
    }

    notifyListeners();
  }

  void clearRecentSearches() {
    _recentSearches.clear();
    notifyListeners();
  }

  void removeRecentSearch(String item) {
    _recentSearches.remove(item);
    notifyListeners();
  }
}
