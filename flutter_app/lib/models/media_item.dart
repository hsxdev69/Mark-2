enum MediaType { movie, tvSeries, anime }

class MediaItem {
  final String id;
  final String title;
  final String? poster;
  final String? backdrop;
  final MediaType type;
  final int? year;
  final double rating;
  final String? synopsis;
  final List<String> genres;
  final double watchProgress;
  final String rawUrl;
  final String providerApiName;
  final bool isInMyList;

  const MediaItem({
    required this.id,
    required this.title,
    this.poster,
    this.backdrop,
    this.type = MediaType.movie,
    this.year,
    this.rating = 0.0,
    this.synopsis,
    this.genres = const [],
    this.watchProgress = 0.0,
    required this.rawUrl,
    this.providerApiName = 'MovieBoxProvider',
    this.isInMyList = false,
  });

  factory MediaItem.fromJson(Map<String, dynamic> json) {
    MediaType type = MediaType.movie;
    final typeStr = (json['type'] ?? '').toString().toLowerCase();
    if (typeStr.contains('series') || typeStr.contains('tv')) {
      type = MediaType.tvSeries;
    } else if (typeStr.contains('anime')) {
      type = MediaType.anime;
    }

    return MediaItem(
      id: json['id']?.toString() ?? json['rawUrl']?.toString() ?? '',
      title: json['title']?.toString() ?? 'Untitled',
      poster: json['poster']?.toString(),
      backdrop: json['backdrop']?.toString() ?? json['poster']?.toString(),
      type: type,
      year: json['year'] is int ? json['year'] : int.tryParse(json['year']?.toString() ?? ''),
      rating: (json['rating'] is num) ? (json['rating'] as num).toDouble() : 0.0,
      synopsis: json['synopsis']?.toString(),
      genres: (json['genres'] as List<dynamic>?)?.map((e) => e.toString()).toList() ?? const [],
      watchProgress: (json['watchProgress'] is num) ? (json['watchProgress'] as num).toDouble() : 0.0,
      rawUrl: json['rawUrl']?.toString() ?? '',
      providerApiName: json['providerApiName']?.toString() ?? 'MovieBoxProvider',
      isInMyList: json['isInMyList'] == true,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'poster': poster,
      'backdrop': backdrop,
      'type': type.name,
      'year': year,
      'rating': rating,
      'synopsis': synopsis,
      'genres': genres,
      'watchProgress': watchProgress,
      'rawUrl': rawUrl,
      'providerApiName': providerApiName,
      'isInMyList': isInMyList,
    };
  }

  MediaItem copyWith({
    String? id,
    String? title,
    String? poster,
    String? backdrop,
    MediaType? type,
    int? year,
    double? rating,
    String? synopsis,
    List<String>? genres,
    double? watchProgress,
    String? rawUrl,
    String? providerApiName,
    bool? isInMyList,
  }) {
    return MediaItem(
      id: id ?? this.id,
      title: title ?? this.title,
      poster: poster ?? this.poster,
      backdrop: backdrop ?? this.backdrop,
      type: type ?? this.type,
      year: year ?? this.year,
      rating: rating ?? this.rating,
      synopsis: synopsis ?? this.synopsis,
      genres: genres ?? this.genres,
      watchProgress: watchProgress ?? this.watchProgress,
      rawUrl: rawUrl ?? this.rawUrl,
      providerApiName: providerApiName ?? this.providerApiName,
      isInMyList: isInMyList ?? this.isInMyList,
    );
  }
}
