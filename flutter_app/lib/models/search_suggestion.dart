class SearchSuggestion {
  final String title;
  final String? subtitle;
  final String? poster;
  final double? rating;
  final bool isTrending;
  final String? category;

  const SearchSuggestion({
    required this.title,
    this.subtitle,
    this.poster,
    this.rating,
    this.isTrending = false,
    this.category,
  });
}
