# CloudStream 3 Core Backend Reference Architecture

Yeh folder MovieVortex 2.0 ke liye **CloudStream 3 Modular Backend Core** ka dedicated reference repository hai. Yahan CloudStream engine ke sabhi essential backend components organized hain.

---

## 📁 Folder Structure

```
backend/
├── README.md                 # Yeh documentation
├── library/                  # CloudStream Core Multiplatform Library (Kotlin Multiplatform)
│   ├── src/
│   │   ├── commonMain/       # MainAPI, APIRepository, ExtractorLink, Extractors, SearchResponse
│   │   ├── androidMain/      # Android-specific bindings & context helpers
│   │   └── jvmMain/          # JVM runtime bindings
│   └── build.gradle.kts      # Library build configuration
└── cloudstream-core/         # CloudStream 3 Android Runtime Engine
    ├── network/              # DoH (DNS over HTTPS), OkHttp clients, SSL handlers
    ├── plugins/              # PluginManager, RepositoryManager (.cs3 plugin loading)
    ├── services/             # VideoDownloadService, DownloadQueueService
    ├── utils/                # VideoDownloadManager, DataStoreHelper, Coroutines
    ├── player/               # Native ExoPlayer/mpv engine (DownloadedPlayerActivity, GeneratorPlayer, CS3IPlayer)
    ├── syncproviders/        # MAL, AniList, Trakt, Simkl sync APIs
    ├── subtitles/            # Subtitle decoders, parsers, offset management
    ├── mvvm/                 # ArchComponentExt, Resource state wrappers
    ├── actions/              # Media actions & intent handlers
    ├── receivers/            # Download & package broadcast receivers
    ├── CloudStreamApp.kt     # Global Application context wrapper
    └── CommonActivity.kt     # Activity instance tracking for core modules
```

---

## ⚙️ Core Subsystems & Responsibilities

### 1. Provider & Extractor Engine (`library/` & `cloudstream-core/plugins/`)
* **`MainAPI` / `APIRepository`**: Har video source (AniVortex, IStreamFlare, MovieBoxProvider) `MainAPI` implement karta hai. Queries safely `APIRepository(api)` ke through coroutine contexts me execute hoti hain.
* **`PluginManager` / `RepositoryManager`**: Plugins ko online repositories (jaise Phisher Repo) se download, cache aur dynamically load karta hai.

### 2. Video Player Core (`cloudstream-core/player/`)
* **`DownloadedPlayerActivity`**: Hardware decoding (ExoPlayer/Media3, mpv), subtitle synchronization, custom aspect ratios, audio track switching, aur picture-in-picture handle karta hai.
* **`GeneratorPlayer`**: Direct link resolution (`RepoFileGenerator`) aur offline local files (`DownloadFileGenerator`) ko dispatch karta hai.

### 3. In-App Download Manager (`cloudstream-core/utils/` & `services/`)
* **`VideoDownloadManager`**: Background downloading, byte tracking, pause/resume, multi-part download pipelines.
* **`DownloadQueueManager`**: Persistent FIFO queue jo app restart hone par bhi active downloads ko restore karta hai.
* **Storage Path**: Completed files automatically `/storage/emulated/0/Download/MovieVortex/` me verify aur save hoti hain.

### 4. Storage & Persistence (`cloudstream-core/utils/DataStoreHelper.kt`)
* Watch progress (playback position/duration), bookmarks (Watchlist), aur cached metadata local key-value store me store hote hain.

---

## 🔗 MovieVortex 2.0 Integration Point

MovieVortex UI layer (`com.movievortex.android`) in backend modules ko **`CoreAdapter.kt`** aur ViewModels (`HomeController`, `DetailController`, `DownloadController`) ke through consume karti hai:
- Raw API responses -> Normalized `MovieVortexMedia` models.
- Download triggers -> `VideoDownloadManager.addToQueue()`.
- Playback triggers -> `launchCloudStreamPlayer()` -> `DownloadedPlayerActivity`.
