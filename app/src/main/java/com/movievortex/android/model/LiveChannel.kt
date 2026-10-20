package com.movievortex.android.model

import java.io.Serializable

data class LiveChannel(
    val id: String,
    val name: String,
    val logoUrl: String,
    val streamUrl: String,
    val category: String, // "News", "Sports", "Movies", "Entertainment", "Music", "Kids & Anime"
    val resolution: String = "1080p FHD",
    val currentProgram: String = "Live Streaming 24/7",
    val isFeatured: Boolean = false,
    val country: String = "IN"
) : Serializable {
    fun toMovieVortexMedia(): MovieVortexMedia = MovieVortexMedia(
        id = "live_$id",
        title = name,
        poster = logoUrl,
        backdrop = logoUrl,
        type = MediaType.LiveStream,
        genres = listOf(category, "LIVE", resolution),
        rawUrl = streamUrl,
        providerApiName = "LiveTV"
    )
}

object LiveChannelsRepository {
    val Categories = listOf(
        "All Channels",
        "🏏 Sports",
        "📰 News",
        "🎬 Movies",
        "🍿 Entertainment",
        "🎵 Music",
        "🎌 Anime & Kids"
    )

    val AllChannels: List<LiveChannel> = listOf(
        // Featured Flagship Channels
        LiveChannel(
            id = "aaj_tak_hd",
            name = "Aaj Tak HD",
            logoUrl = "https://xstreamcp-assets-msp.streamready.in/assets/LIVETV/LIVECHANNEL/LIVETV_LIVETVCHANNEL_AAJ_TAK/images/LOGO_HD/image.png",
            streamUrl = "https://feeds.intoday.in/aajtak/api/aajtakhd/master.m3u8",
            category = "News",
            resolution = "1080p FHD",
            currentProgram = "Sabse Tez • 24x7 Breaking News & Debate",
            isFeatured = true
        ),
        LiveChannel(
            id = "red_bull_tv",
            name = "Red Bull TV",
            logoUrl = "https://i.imgur.com/Qh15Eea.png",
            streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8",
            category = "Sports",
            resolution = "1080p FHD",
            currentProgram = "Motorsport, F1, Extreme Sports & Live Events",
            isFeatured = true
        ),
        LiveChannel(
            id = "50_cent_action",
            name = "50 Cent Action",
            logoUrl = "https://provider-static.plex.tv/epg/cms/production/bcfb9977-809f-49ae-acc7-430f2c6ffb26/50CentAction_Logo_1500x1000_DarkBG_-_Chris_Connors.png",
            streamUrl = "https://jmp2.uk/plu-68487fb3f212bedacf5a53e3.m3u8",
            category = "Movies",
            resolution = "1080p FHD",
            currentProgram = "High-Octane Action Blockbusters 24/7",
            isFeatured = true
        ),
        LiveChannel(
            id = "aniplus_asia",
            name = "Aniplus Asia",
            logoUrl = "https://i.imgur.com/Im3MePy.png",
            streamUrl = "https://amg18481-amg18481c1-amgplt0352.playout.now3.amagi.tv/playlist/amg18481-amg18481c1-amgplt0352/playlist.m3u8",
            category = "Kids & Anime",
            resolution = "720p HD",
            currentProgram = "Simulcast Anime Series & Manga Premieres",
            isFeatured = true
        ),

        // Sports
        LiveChannel(
            id = "acc_sports",
            name = "ACC Sports Live",
            logoUrl = "https://i.imgur.com/V6Kaqha.png",
            streamUrl = "https://raycom-accdn-firetv.amagi.tv/playlist.m3u8",
            category = "Sports",
            resolution = "1080p FHD",
            currentProgram = "Championship Football, Basketball & Match Highlights"
        ),
        LiveChannel(
            id = "aspor_hd",
            name = "A Spor HD",
            logoUrl = "https://i.imgur.com/ZhkZzLf.png",
            streamUrl = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor.m3u8",
            category = "Sports",
            resolution = "1080p HD",
            currentProgram = "European Football Tournaments & Studio Live"
        ),
        LiveChannel(
            id = "africa_24_sport",
            name = "Africa 24 Sport",
            logoUrl = "https://i0.wp.com/africa24tv.com/wp-content/uploads/2023/12/LOGO-AFRICASPORT-4-HD-sans-fond.png?fit=512%2C107&ssl=1",
            streamUrl = "https://africa24.vedge.infomaniak.com/livecast/ik:africa24sport/manifest.m3u8",
            category = "Sports",
            resolution = "1080p HD",
            currentProgram = "Global Athletics, Football Leagues & Combat Sports"
        ),
        LiveChannel(
            id = "golf_kingdom",
            name = "30A Golf Kingdom",
            logoUrl = "https://i.imgur.com/Lv53nh4.png",
            streamUrl = "https://30a-tv.com/feeds/vidaa/golf.m3u8",
            category = "Sports",
            resolution = "720p HD",
            currentProgram = "PGA Tour Insights & Golf Championship Replays"
        ),

        // News
        LiveChannel(
            id = "ndtv_india",
            name = "NDTV India",
            logoUrl = "https://i.imgur.com/QjJYohG.png",
            streamUrl = "https://ndtvindiaelemarchana.akamaized.net/hls/live/2003679/ndtvindia/master.m3u8",
            category = "News",
            resolution = "1080p FHD",
            currentProgram = "Prime Time & Ground Realities with Ravish"
        ),
        LiveChannel(
            id = "abp_news",
            name = "ABP News",
            logoUrl = "https://i.imgur.com/DKHUFVQ.png",
            streamUrl = "https://d2l4ar6y3mrs4k.cloudfront.net/live-streaming/abpnews-livetv/master.m3u8",
            category = "News",
            resolution = "1080p FHD",
            currentProgram = "Desh Ki Har Badi Khabar & 24x7 Updates"
        ),
        LiveChannel(
            id = "ndtv_profit",
            name = "NDTV Profit",
            logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/1/18/NDTV_Profit.svg/1280px-NDTV_Profit.svg.png",
            streamUrl = "https://ndtvprofit.akamaized.net/hls/live/2107404/ndtvprofit/master_1.m3u8",
            category = "News",
            resolution = "1080p FHD",
            currentProgram = "Sensex, Nifty, Stock Market & Global Economy"
        ),
        LiveChannel(
            id = "10_tv",
            name = "10 TV News",
            logoUrl = "https://xstreamcp-assets-msp.streamready.in/assets/LIVETV/LIVECHANNEL/LIVETV_LIVETVCHANNEL_10TV/images/LOGO_HD/image.png",
            streamUrl = "https://mumbai-edge.smartplaytv.in/10TV/index.m3u8",
            category = "News",
            resolution = "720p HD",
            currentProgram = "Regional & National Headline Broadcast"
        ),
        LiveChannel(
            id = "24_news",
            name = "24 News",
            logoUrl = "https://sund-images.sunnxt.com/202222/300x300_24News_202222_d63feca0-79ae-47ea-b75a-66c17d456f4c.png",
            streamUrl = "https://mumt07.tangotv.in/zHjX9OFlTWENTYFOURNEWS/index.m3u8",
            category = "News",
            resolution = "576p SD",
            currentProgram = "24x7 Live News Coverage & Current Affairs"
        ),
        LiveChannel(
            id = "4tv_news",
            name = "4TV News",
            logoUrl = "https://jiotvimages.cdn.jio.com/dare_images/images/4_TV.png",
            streamUrl = "https://cdn.pishow.tv/ott/live/1007/master.m3u8",
            category = "News",
            resolution = "576p SD",
            currentProgram = "Continuous City & Regional News Flash"
        ),
        LiveChannel(
            id = "aaj_ki_khabar",
            name = "Aaj Ki Khabar",
            logoUrl = "https://m.media-amazon.com/images/I/71q8sOMPwHL.png",
            streamUrl = "https://stream.ottlive.co.in/aajkikhabar/index.m3u8",
            category = "News",
            resolution = "1080p FHD",
            currentProgram = "Hindi Samachar & Special Documentaries"
        ),

        // Movies
        LiveChannel(
            id = "24h_movies",
            name = "24H Movie Central",
            logoUrl = "https://i.imgur.com/iSVnzR1.png",
            streamUrl = "https://d1j2u714xk898n.cloudfront.net/scheduler/scheduleMaster/145.m3u8",
            category = "Movies",
            resolution = "720p HD",
            currentProgram = "Non-stop Cinema Marathon & Indie Gems"
        ),
        LiveChannel(
            id = "80s_rewind",
            name = "80s Rewind Cinema",
            logoUrl = "https://i.imgur.com/nkEeYfI.png",
            streamUrl = "https://jmp2.uk/plu-5ca525b650be2571e3943c63.m3u8",
            category = "Movies",
            resolution = "1080p FHD",
            currentProgram = "Iconic 80s Thrillers, Sci-Fi & Cult Hits"
        ),
        LiveChannel(
            id = "90s_throwback",
            name = "90s Throwback",
            logoUrl = "https://i.imgur.com/KoGko6M.png",
            streamUrl = "https://jmp2.uk/plu-5f4d86f519358a00072b978e.m3u8",
            category = "Movies",
            resolution = "1080p FHD",
            currentProgram = "Golden 90s Blockbusters & Action Dramas"
        ),
        LiveChannel(
            id = "00s_replay",
            name = "00s Replay Movies",
            logoUrl = "https://images.pluto.tv/channels/62ba60f059624e000781c436/colorLogoPNG.png",
            streamUrl = "https://jmp2.uk/plu-62ba60f059624e000781c436.m3u8",
            category = "Movies",
            resolution = "1080p FHD",
            currentProgram = "Millennium Classics & High-Energy Movies"
        ),
        LiveChannel(
            id = "70s_cinema",
            name = "70s Cinema Club",
            logoUrl = "https://i.imgur.com/mgXeEE4.png",
            streamUrl = "https://jmp2.uk/plu-5f4d878d3d19b30007d2e782.m3u8",
            category = "Movies",
            resolution = "1080p FHD",
            currentProgram = "Vintage Crime, Neo-Noir & Suspense Masterpieces"
        ),

        // Music
        LiveChannel(
            id = "9x_jalwa",
            name = "9X Jalwa",
            logoUrl = "https://xstreamcp-assets-msp.streamready.in/assets/LIVETV/LIVECHANNEL/LIVETV_LIVETVCHANNEL_9X_JALWA/images/LOGO_HD/image.png",
            streamUrl = "https://wiselp.wiseplayout.com/9X_Jalwa/master.m3u8",
            category = "Music",
            resolution = "1080p FHD",
            currentProgram = "Evergreen Bollywood Hits & Nostalgic Melodies"
        ),
        LiveChannel(
            id = "9x_jhakaas",
            name = "9X Jhakaas",
            logoUrl = "https://dtil.tmsimg.com/assets/s90012_ld_h15_aa.png?lock=720x540",
            streamUrl = "https://amg01281-9xmediapvtltd-9xjhakaas-samsungin-ci2cs.amagi.tv/playlist/amg01281-9xmediapvtltd-9xjhakaas-samsungin/playlist.m3u8",
            category = "Music",
            resolution = "1080p FHD",
            currentProgram = "High-Energy Regional & Party Anthems"
        ),
        LiveChannel(
            id = "9x_tashan",
            name = "9X Tashan",
            logoUrl = "https://xstreamcp-assets-msp.streamready.in/assets/LIVETV/LIVECHANNEL/LIVETV_LIVETVCHANNEL_9X_TASHAN/images/LOGO_HD/image.png",
            streamUrl = "https://amg01281-9xmediapvtltd-9xtashan-samsungin-xz1sd.amagi.tv/playlist/amg01281-9xmediapvtltd-9xtashan-samsungin/playlist.m3u8",
            category = "Music",
            resolution = "1080p FHD",
            currentProgram = "Punjabi Chartbusters & Swag Bangers"
        ),
        LiveChannel(
            id = "7s_music",
            name = "7S Music",
            logoUrl = "https://i.imgur.com/zDiIhdN.png",
            streamUrl = "https://cdn.pishow.tv/ott/live/1257/master.m3u8",
            category = "Music",
            resolution = "576p SD",
            currentProgram = "Indie Beats, Pop Top 40 & DJ Sets"
        ),

        // Entertainment
        LiveChannel(
            id = "4_sides_tv",
            name = "4 SiDES TV",
            logoUrl = "https://jiotvimages.cdn.jio.com/dare_images/images/4sites.png",
            streamUrl = "https://stream.ottlive.co.in/4sidestv/index.m3u8",
            category = "Entertainment",
            resolution = "1080p FHD",
            currentProgram = "Entertainment Shows, Reality TV & Lifestyle"
        ),
        LiveChannel(
            id = "4u_tv",
            name = "4U TV",
            logoUrl = "https://hls.4utv.live/hls/stream.m3u8",
            streamUrl = "https://hls.4utv.live/hls/stream.m3u8",
            category = "Entertainment",
            resolution = "720p HD",
            currentProgram = "Comedy Shorts, Drama & Celebrity Gossip"
        ),

        // Anime & Kids
        LiveChannel(
            id = "cartoon_classics",
            name = "Cartoon Classics",
            logoUrl = "https://images-3.rakuten.tv/storage/global-live-channel/translation/artwork/3227c06e-333b-4b1b-b657-3e3ab99ebd06-width200-quality90.jpeg",
            streamUrl = "https://daiconnect.com/live/hls/tvup/rk-cartoonclassics/578f4b7eb725168349ec0af81b21d388/index.m3u8",
            category = "Kids & Anime",
            resolution = "720p HD",
            currentProgram = "Looney Tunes, Popeye, Betty Boop & Classic Toons"
        ),
        LiveChannel(
            id = "anime_hidive",
            name = "Anime x HIDIVE",
            logoUrl = "https://i.imgur.com/v0BlxCa.png",
            streamUrl = "https://jmp2.uk/plu-6793eaa4bc03978b9bc63db1.m3u8",
            category = "Kids & Anime",
            resolution = "1080p FHD",
            currentProgram = "Exclusive Anime Premieres, Isekai & Shonen"
        ),
        LiveChannel(
            id = "3abn_kids",
            name = "3ABN Kids TV",
            logoUrl = "https://i.imgur.com/z3npqO1.png",
            streamUrl = "https://3abn.bozztv.com/3abn2/Kids_live/smil:Kids_live.smil/playlist.m3u8",
            category = "Kids & Anime",
            resolution = "720p HD",
            currentProgram = "Creative Animation, Storytime & Kids Science"
        )
    )

    fun getChannelsByCategory(category: String): List<LiveChannel> {
        return when (category) {
            "All Channels" -> AllChannels
            "🏏 Sports" -> AllChannels.filter { it.category == "Sports" }
            "📰 News" -> AllChannels.filter { it.category == "News" }
            "🎬 Movies" -> AllChannels.filter { it.category == "Movies" }
            "🍿 Entertainment" -> AllChannels.filter { it.category == "Entertainment" }
            "🎵 Music" -> AllChannels.filter { it.category == "Music" }
            "🎌 Anime & Kids" -> AllChannels.filter { it.category == "Kids & Anime" }
            else -> AllChannels.filter { it.category.contains(category, ignoreCase = true) }
        }
    }
}
