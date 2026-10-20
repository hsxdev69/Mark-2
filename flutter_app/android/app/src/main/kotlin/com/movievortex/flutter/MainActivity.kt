package com.movievortex.flutter

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
                    scope.launch {
                        try {
                            val jsonResult = JSONObject()
                            // Try calling native CloudStream APIHolder if available
                            val apis = com.lagradost.cloudstream3.APIHolder.apis
                            val mainApi = apis.firstOrNull { it.hasMainPage } ?: apis.firstOrNull()

                            if (mainApi != null) {
                                val repo = com.lagradost.cloudstream3.ui.APIRepository(mainApi)
                                val hpRes = withContext(Dispatchers.IO) { repo.getMainPage(1) }
                                if (hpRes is com.lagradost.cloudstream3.mvvm.Resource.Success) {
                                    val heroArr = JSONArray()
                                    val trArr = JSONArray()

                                    hpRes.value.forEach { hp ->
                                        hp?.items?.forEach { pageList ->
                                            pageList.list.take(6).forEach { sr ->
                                                val obj = JSONObject().apply {
                                                    put("id", sr.url.hashCode().toString())
                                                    put("title", sr.name)
                                                    put("poster", sr.posterUrl)
                                                    put("rawUrl", sr.url)
                                                    put("providerApiName", sr.apiName)
                                                    put("type", sr.type?.name ?: "movie")
                                                    put("rating", (sr as? com.lagradost.cloudstream3.SearchResponseWithRating)?.rating ?: 0.0)
                                                }
                                                if (heroArr.length() < 5) heroArr.put(obj) else trArr.put(obj)
                                            }
                                        }
                                    }
                                    jsonResult.put("hero", heroArr)
                                    jsonResult.put("trending", trArr)
                                    result.success(jsonResult.toString())
                                    return@launch
                                }
                            }
                            result.success(null)
                        } catch (e: Exception) {
                            result.success(null)
                        }
                    }
                }

                "search" -> {
                    val query = call.argument<String>("query") ?: ""
                    scope.launch {
                        try {
                            val apis = com.lagradost.cloudstream3.APIHolder.apis
                            val resultsArr = JSONArray()

                            withContext(Dispatchers.IO) {
                                apis.forEach { api ->
                                    try {
                                        val repo = com.lagradost.cloudstream3.ui.APIRepository(api)
                                        val res = repo.search(query, 1)
                                        if (res is com.lagradost.cloudstream3.mvvm.Resource.Success) {
                                            res.value.items.forEach { sr ->
                                                val obj = JSONObject().apply {
                                                    put("id", sr.url.hashCode().toString())
                                                    put("title", sr.name)
                                                    put("poster", sr.posterUrl)
                                                    put("rawUrl", sr.url)
                                                    put("providerApiName", sr.apiName)
                                                    put("type", sr.type?.name ?: "movie")
                                                }
                                                resultsArr.put(obj)
                                            }
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                            result.success(resultsArr.toString())
                        } catch (e: Exception) {
                            result.success(null)
                        }
                    }
                }

                "playMedia" -> {
                    val rawUrl = call.argument<String>("rawUrl") ?: ""
                    val title = call.argument<String>("title") ?: ""
                    val apiName = call.argument<String>("apiName") ?: ""
                    val epData = call.argument<String>("episodeData")
                    val epNum = call.argument<Int>("episodeNum")
                    val seasonNum = call.argument<Int>("seasonNum")

                    try {
                        val intent = Intent(this@MainActivity, com.lagradost.cloudstream3.ui.player.DownloadedPlayerActivity::class.java).apply {
                            putExtra(com.lagradost.cloudstream3.MainActivity.API_NAME_EXTRA_KEY, apiName)
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
