class EpisodeItem {
  final String id;
  final String title;
  final int episodeNumber;
  final int seasonNumber;
  final String? poster;
  final String? synopsis;
  final String dataUrl;

  const EpisodeItem({
    required this.id,
    required this.title,
    required this.episodeNumber,
    required this.seasonNumber,
    this.poster,
    this.synopsis,
    required this.dataUrl,
  });

  factory EpisodeItem.fromJson(Map<String, dynamic> json) {
    return EpisodeItem(
      id: json['id']?.toString() ?? '',
      title: json['title']?.toString() ?? 'Episode',
      episodeNumber: json['episodeNumber'] is int ? json['episodeNumber'] : (int.tryParse(json['episodeNumber']?.toString() ?? '1') ?? 1),
      seasonNumber: json['seasonNumber'] is int ? json['seasonNumber'] : (int.tryParse(json['seasonNumber']?.toString() ?? '1') ?? 1),
      poster: json['poster']?.toString(),
      synopsis: json['synopsis']?.toString(),
      dataUrl: json['dataUrl']?.toString() ?? '',
    );
  }
}
