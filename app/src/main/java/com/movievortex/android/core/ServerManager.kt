package com.movievortex.android.core

/**
 * Centralized Server Manager for MovieVortex
 * Aliases backend providers to user-facing Mark server names:
 * - AniVortex -> Mark-1 (Default)
 * - IStreamFlare -> Mark-2
 * - MovieBoxProvider -> Mark-3
 * - SuperStream -> Mark-4
 */
object ServerManager {
    const val MARK_1 = "Mark-1"
    const val MARK_2 = "Mark-2"
    const val MARK_3 = "Mark-3"
    const val MARK_4 = "Mark-4"

    val ALL_SERVERS = listOf(MARK_1, MARK_2, MARK_3, MARK_4)

    const val DEFAULT_SERVER = MARK_1

    // Underlying backend provider internal names
    const val PROVIDER_MARK_1 = "AniVortex"
    const val PROVIDER_MARK_2 = "IStreamFlare"
    const val PROVIDER_MARK_3 = "MovieBoxProvider"
    const val PROVIDER_MARK_4 = "SuperStream"

    fun toDisplayName(rawName: String?): String {
        if (rawName.isNullOrBlank()) return MARK_1
        val lower = rawName.lowercase()
        return when {
            lower.contains("anivortex") || lower.contains("anvortex") || lower.contains("turbo") || lower.contains("mark-1") || lower.contains("mark1") -> MARK_1
            lower.contains("streamflare") || lower.contains("istreamflare") || lower.contains("istream") || lower.contains("core") || lower.contains("mark-2") || lower.contains("mark2") -> MARK_2
            lower.contains("moviebox") || lower.contains("rive") || lower.contains("mark-3") || lower.contains("mark3") -> MARK_3
            lower.contains("superstream") || lower.contains("spiral") || lower.contains("mark-4") || lower.contains("mark4") -> MARK_4
            else -> MARK_1
        }
    }

    fun toProviderName(markName: String): String {
        return when (markName) {
            MARK_1 -> PROVIDER_MARK_1
            MARK_2 -> PROVIDER_MARK_2
            MARK_3 -> PROVIDER_MARK_3
            MARK_4 -> PROVIDER_MARK_4
            else -> PROVIDER_MARK_1
        }
    }

    fun toDescription(markName: String): String {
        return when (markName) {
            MARK_1 -> "Anime, Asian Drama & Global Series"
            MARK_2 -> "Western Movies, Hollywood & TV Shows"
            MARK_3 -> "Blockbuster Movies & Multi-Quality Streams"
            MARK_4 -> "Ultra Fast Mirror & 4K/1080p Vault"
            else -> "High-Speed Streaming Server"
        }
    }
}
