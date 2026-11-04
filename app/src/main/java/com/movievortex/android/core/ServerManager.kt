package com.movievortex.android.core

/**
 * Centralized Server Manager for MovieVortex
 * Single high-performance server architecture:
 * - AniVortex -> Primary Dedicated High-Speed Server
 */
object ServerManager {
    const val MARK_1 = "AniVortex"
    const val MARK_2 = "AniVortex"
    const val MARK_3 = "AniVortex"
    const val MARK_4 = "AniVortex"

    val ALL_SERVERS = listOf(MARK_1)

    const val DEFAULT_SERVER = MARK_1

    // Underlying backend provider internal names
    const val PROVIDER_MARK_1 = "AniVortex"
    const val PROVIDER_MARK_2 = "AniVortex"
    const val PROVIDER_MARK_3 = "AniVortex"
    const val PROVIDER_MARK_4 = "AniVortex"

    fun toDisplayName(rawName: String?): String {
        return MARK_1
    }

    fun toProviderName(markName: String): String {
        return PROVIDER_MARK_1
    }

    fun toDescription(markName: String): String {
        return "Ultra Fast Direct Streaming (CloudStream Core)"
    }
}
