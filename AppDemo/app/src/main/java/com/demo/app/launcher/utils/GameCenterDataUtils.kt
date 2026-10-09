package com.demo.app.launcher.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.demo.app.BuildConfig
import com.demo.app.http.ResponseValidator
import com.demo.app.http.Urls
import com.demo.app.integration.http.RxHttp
import com.demo.app.integration.http.cache.CacheCallback
import com.demo.app.integration.http.cache.CacheConfig
import com.demo.app.integration.http.cache.CacheStrategy
import com.demo.app.kts.HttpKts
import com.demo.app.launcher.gamecenter.model.GameCenterResponse
import com.demo.app.launcher.gamecenter.model.GameCenterUiState
import com.demo.app.launcher.gamecenter.model.GameDetailResponse
import com.demo.app.launcher.gamecenter.parser.GameCenterParser
import com.demo.app.launcher.gamecenter.parser.GameDetailParser
import com.demo.app.launcher.gamecenter.GameCenterSession
import com.demo.app.launcher.gamecenter.model.GameCenterConfig
import com.demo.app.launcher.gamecenter.model.GameDetailUi
import java.util.concurrent.Executors

/** 游戏中心相关接口（与 [NewsDataUtils] 同属 launcher 数据出口）。 */
object GameCenterDataUtils {

    private const val TAG = "GameCenterDataUtils"
    private const val CACHE_KEY_PREFIX_HOME = "game_center_home"
    private const val CACHE_KEY_PREFIX_DETAIL = "game_center_detail"
    private const val CACHE_TTL_MS_HOME = 6L * 60L * 60L * 1000L
    private const val CACHE_TTL_MS_DETAILS = 24L * 60L * 60L * 1000L
    private val mainHandler = Handler(Looper.getMainLooper())
    private val ioExecutor = Executors.newSingleThreadExecutor()

    private fun runOnMainThread(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    fun loadHome(
        config: GameCenterConfig = GameCenterConfig(),
        callback: (state: GameCenterUiState?, error: Throwable?) -> Unit,
    ) {
        val url = Urls.URL_GAME_CENTER_HOME
        val queries = buildHomeQueries(config)
        val cacheKey = "${CACHE_KEY_PREFIX_HOME}_${config.bid}_${config.resolveCountry()}_${config.categoryId}"

        val cacheConfig = CacheConfig.Builder()
            .key(cacheKey)
            .expireTime(CACHE_TTL_MS_HOME)
            .strategy(CacheStrategy.CACHE_OR_NETWORK)
            .validator { ResponseValidator.shouldCacheGameCenterHome(it) }
            .build()

        RxHttp.getWithCache(url, queries, cacheConfig, object : CacheCallback {
            override fun onCacheSuccess(data: String) {
                deliverHome(data, callback)
            }

            override fun onNetworkSuccess(data: String) {
                deliverHome(data, callback)
            }

            override fun onFailure(e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "loadHome: failed", e)
                }
                runOnMainThread { callback(null, e) }
            }
        })
    }

    fun loadDetail(
        gameBid: String,
        config: GameCenterConfig = GameCenterConfig(),
        callback: (detail: GameDetailUi?, error: Throwable?) -> Unit,
    ) {
        val baseUrl = GameCenterSession.assetBaseUrl
        if (baseUrl.isBlank()) {
            runOnMainThread { callback(null, IllegalStateException("asset base url is empty")) }
            return
        }
        if (gameBid.isBlank()) {
            runOnMainThread { callback(null, IllegalArgumentException("game bid is empty")) }
            return
        }

        val url = Urls.URL_GAME_CENTER_DETAIL
        val queries = buildDetailQueries(gameBid, config)
        val cacheKey = "${CACHE_KEY_PREFIX_DETAIL}_${gameBid}_${config.resolveCountry()}"
        val cacheConfig = CacheConfig.Builder()
            .key(cacheKey)
            .expireTime(CACHE_TTL_MS_DETAILS)
            .strategy(CacheStrategy.CACHE_OR_NETWORK)
            .validator { ResponseValidator.shouldCacheGameCenterDetail(it) }
            .build()

        RxHttp.getWithCache(url, queries, cacheConfig, object : CacheCallback {
            override fun onCacheSuccess(data: String) {
                deliverDetail(data, baseUrl, callback)
            }

            override fun onNetworkSuccess(data: String) {
                deliverDetail(data, baseUrl, callback)
            }

            override fun onFailure(e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "loadDetail: failed", e)
                }
                runOnMainThread { callback(null, e) }
            }
        })
    }

    private fun buildHomeQueries(config: GameCenterConfig): Map<String, Any> = mapOf(
        "bid" to config.bid,
        "country" to config.resolveCountry(),
        "category_id" to config.categoryId,
        "fix" to 1
    )

    private fun buildDetailQueries(gameBid: String, config: GameCenterConfig): Map<String, Any> =
        mapOf(
            "bid" to gameBid,
            "country" to config.resolveCountry(),
            "fix" to 1
        )

    private fun deliverHome(json: String, callback: (GameCenterUiState?, Throwable?) -> Unit) {
        ioExecutor.execute {
            val result = try {
                val response = HttpKts.decodeRaw<GameCenterResponse>(json, GameCenterResponse::class.java)
                    ?: throw IllegalStateException("Invalid encrypted response")
                if (response.resolvedCode() != 0) {
                    throw IllegalStateException(response.msg ?: "code=${response.resolvedCode()}")
                }
                val state = GameCenterParser.parse(response)
                state to null
            } catch (e: Throwable) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "deliverHome: parse failed", e)
                }
                null to e
            }
            runOnMainThread { callback(result.first, result.second) }
        }
    }

    private fun deliverDetail(
        json: String?,
        baseUrl: String,
        callback: (GameDetailUi?, Throwable?) -> Unit,
    ) {
        ioExecutor.execute {
            val result = try {
                if (json.isNullOrBlank()) throw IllegalStateException("Empty response")
                val response = HttpKts.decodeRaw<GameDetailResponse>(json, GameDetailResponse::class.java)
                    ?: throw IllegalStateException("Invalid encrypted response")
                if (response.resolvedCode() != 0) {
                    throw IllegalStateException(response.msg ?: "code=${response.resolvedCode()}")
                }
                val detail = GameDetailParser.parse(response, baseUrl)
                    ?: throw IllegalStateException("Invalid detail data")
                detail to null
            } catch (e: Throwable) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "deliverDetail: parse failed", e)
                }
                null to e
            }
            runOnMainThread { callback(result.first, result.second) }
        }
    }
}
