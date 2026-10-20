package com.movievortex.flutter

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.movievortex.flutter/core"
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "isBackendReady" -> {
                    result.success(true)
                }

                "getHomeContent" -> {
                    // Returns null so Flutter uses instant responsive structured catalog
                    result.success(null)
                }

                "search" -> {
                    // Returns null so Flutter uses fast debounced trending/search engine
                    result.success(null)
                }

                "playMedia" -> {
                    val rawUrl = call.argument<String>("rawUrl") ?: ""
                    val title = call.argument<String>("title") ?: ""
                    val apiName = call.argument<String>("apiName") ?: ""
                    val epData = call.argument<String>("episodeData")
                    val epNum = call.argument<Int>("episodeNum")
                    val seasonNum = call.argument<Int>("seasonNum")

                    try {
                        val intent = Intent().apply {
                            component = ComponentName(
                                this@MainActivity,
                                "com.lagradost.cloudstream3.ui.player.DownloadedPlayerActivity"
                            )
                            putExtra("API_NAME_EXTRA_KEY", apiName)
                            putExtra("CONTENT_URL", rawUrl)
                            putExtra("TITLE", title)
                            if (epData != null) {
                                putExtra("EPISODE_DATA", epData)
                                if (epNum != null) putExtra("EPISODE", epNum)
                                if (seasonNum != null) putExtra("SEASON", seasonNum)
                            }
                        }
                        startActivity(intent)
                        result.success(true)
                    } catch (e: Exception) {
                        result.error("PLAYER_ERROR", e.message, null)
                    }
                }

                else -> result.notImplemented()
            }
        }
    }
}
