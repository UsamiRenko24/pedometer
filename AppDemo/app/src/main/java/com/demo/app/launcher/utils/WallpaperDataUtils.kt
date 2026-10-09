package com.demo.app.launcher.utils

import android.os.Handler
import android.os.Looper
import com.google.gson.reflect.TypeToken
import com.demo.app.http.ResponseValidator
import com.demo.app.http.Urls
import com.demo.app.integration.http.NetworkCallbackImpl
import com.demo.app.integration.http.NetworkResultCallback
import com.demo.app.integration.http.RxHttp
import com.demo.app.integration.http.cache.CacheCallback
import com.demo.app.integration.http.cache.CacheConfig
import com.demo.app.integration.http.cache.CacheStrategy
import com.demo.app.kts.HttpKts
import com.demo.app.launcher.wallpaper.WallpaperSession
import com.demo.app.launcher.wallpaper.model.CategoriesDataDto
import com.demo.app.launcher.wallpaper.model.CategoryDto
import com.demo.app.launcher.wallpaper.model.CategoryUi
import com.demo.app.launcher.wallpaper.model.WallpaperApiResponse
import com.demo.app.launcher.wallpaper.model.WallpaperDetailDto
import com.demo.app.launcher.wallpaper.model.WallpaperDetailUi
import com.demo.app.launcher.wallpaper.model.WallpaperListDataDto
import com.demo.app.launcher.wallpaper.model.WallpaperListUi
import com.demo.app.launcher.wallpaper.parser.WallpaperParser
import java.util.concurrent.Executors

/** 壁纸相关接口（与 [NewsDataUtils] 同属 launcher 数据出口）。 */
object WallpaperDataUtils {

    private const val CACHE_KEY_CATEGORIES = "wallpaper_categories"
    private const val CACHE_KEY_PREFIX_DETAIL = "wallpaper_detail"
    private const val CACHE_TTL_MS_TAB = 24L * 60L * 60L * 1000L
    private const val CACHE_TTL_MS_LIST = 6L * 60L * 60L * 1000L
    private const val CACHE_TTL_MS_DETAILS = 24L * 60L * 60L * 1000L
    private const val DEFAULT_PAGE_SIZE = 20

    private val mainHandler = Handler(Looper.getMainLooper())
    private val ioExecutor = Executors.newSingleThreadExecutor()

    private fun runOnMainThread(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    fun loadCategories(callback: (List<CategoryUi>, Throwable?) -> Unit) {
        val cacheConfig = CacheConfig.Builder()
            .key(CACHE_KEY_CATEGORIES)
            .expireTime(CACHE_TTL_MS_TAB)
            .strategy(CacheStrategy.CACHE_OR_NETWORK)
            .validator { ResponseValidator.shouldCacheWallpaperCategories(it) }
            .build()
        val queries = mapOf(
            "package_name" to "com.rwapp.fm.tvremote",
        )
        RxHttp.getWithCache(Urls.URL_WALLPAPER_CATEGORIES, queries, cacheConfig, object : CacheCallback {
            override fun onCacheSuccess(data: String) {
                deliverCategories(data, callback)
            }

            override fun onNetworkSuccess(data: String) {
                deliverCategories(data, callback)
            }

            override fun onFailure(e: Exception) {
                runOnMainThread { callback(emptyList(), e) }
            }
        })
    }

    fun loadWallpapers(
        categoryId: String,
        page: Int = 1,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        useCache: Boolean = false,
        callback: (WallpaperListUi, Throwable?) -> Unit,
    ) {
        if (categoryId.isBlank()) {
            runOnMainThread { callback(WallpaperListUi(), IllegalArgumentException("category_id is empty")) }
            return
        }
        val queries = mapOf(
            "category_id" to categoryId,
            "package_name" to "com.rwapp.fm.tvremote",
            "page" to page,
            "page_size" to pageSize,
        )
        val url = Urls.URL_WALLPAPER_LIST
        val fallbackBase = WallpaperSession.assetBaseUrl

        if (useCache && page == 1) {
            val cacheKey = "wallpaper_list_${categoryId}_$page"
            val cacheConfig = CacheConfig.Builder()
                .key(cacheKey)
                .expireTime(CACHE_TTL_MS_LIST)
                .strategy(CacheStrategy.CACHE_OR_NETWORK)
                .validator { ResponseValidator.shouldCacheWallpaperList(it) }
                .build()
            RxHttp.getWithCache(url, queries, cacheConfig, object : CacheCallback {
                override fun onCacheSuccess(data: String) {
                    deliverWallpaperList(data, fallbackBase, callback)
                }

                override fun onNetworkSuccess(data: String) {
                    deliverWallpaperList(data, fallbackBase, callback)
                }

                override fun onFailure(e: Exception) {
                    runOnMainThread { callback(WallpaperListUi(), e) }
                }
            })
        } else {
            RxHttp.get(url, queries, NetworkCallbackImpl(NetworkResultCallback { json, error ->
                runOnMainThread {
                    if (error != null) {
                        callback(WallpaperListUi(), error)
                    } else {
                        deliverWallpaperList(json.orEmpty(), fallbackBase, callback)
                    }
                }
            }))
        }
    }

    fun loadDetail(
        wallpaperId: String,
        callback: (WallpaperDetailUi?, Throwable?) -> Unit,
    ) {
        if (wallpaperId.isBlank()) {
            runOnMainThread { callback(null, IllegalArgumentException("wallpaper id is empty")) }
            return
        }
        val queries = mapOf(
            "package_name" to "com.rwapp.fm.tvremote",
        )
        val url = Urls.buildWallpaperDetailUrl(wallpaperId)
        val fallbackBase = WallpaperSession.assetBaseUrl
        val cacheKey = "${CACHE_KEY_PREFIX_DETAIL}_$wallpaperId"
        val cacheConfig = CacheConfig.Builder()
            .key(cacheKey)
            .expireTime(CACHE_TTL_MS_DETAILS)
            .strategy(CacheStrategy.CACHE_OR_NETWORK)
            .validator { ResponseValidator.shouldCacheWallpaperDetail(it) }
            .build()

        RxHttp.getWithCache(url, queries, cacheConfig, object : CacheCallback {
            override fun onCacheSuccess(data: String) {
                deliverDetail(data, fallbackBase, callback)
            }

            override fun onNetworkSuccess(data: String) {
                deliverDetail(data, fallbackBase, callback)
            }

            override fun onFailure(e: Exception) {
                runOnMainThread { callback(null, e) }
            }
        })
    }

    private fun deliverCategories(json: String, callback: (List<CategoryUi>, Throwable?) -> Unit) {
        ioExecutor.execute {
            val result = runCatching {
                val listType = object : TypeToken<WallpaperApiResponse<List<CategoryDto>>>() {}.type
                val listResponse = HttpKts.decodeRaw<WallpaperApiResponse<List<CategoryDto>>>(json, listType)
                if (listResponse != null && !listResponse.data.isNullOrEmpty()) {
                    WallpaperParser.parseCategoriesList(
                        WallpaperResponseHelper.ensureSuccess(listResponse),
                    )
                } else {
                    val wrappedType = object : TypeToken<WallpaperApiResponse<CategoriesDataDto>>() {}.type
                    val response = HttpKts.decodeRaw<WallpaperApiResponse<CategoriesDataDto>>(json, wrappedType)
                        ?: throw IllegalStateException("Invalid response")
                    WallpaperParser.parseCategories(WallpaperResponseHelper.ensureSuccess(response))
                }
            }
            runOnMainThread {
                result.fold(
                    onSuccess = { callback(it, null) },
                    onFailure = { callback(emptyList(), it) },
                )
            }
        }
    }

    private fun deliverWallpaperList(
        json: String,
        fallbackBaseUrl: String,
        callback: (WallpaperListUi, Throwable?) -> Unit,
    ) {
        ioExecutor.execute {
            val result = runCatching {
                val type = object : TypeToken<WallpaperApiResponse<WallpaperListDataDto>>() {}.type
                val response = HttpKts.decodeRaw<WallpaperApiResponse<WallpaperListDataDto>>(json, type)
                    ?: throw IllegalStateException("Invalid response")
                WallpaperParser.parseWallpaperList(
                    WallpaperResponseHelper.ensureSuccess(response),
                    fallbackBaseUrl,
                )
            }
            runOnMainThread {
                result.fold(
                    onSuccess = { callback(it, null) },
                    onFailure = { callback(WallpaperListUi(), it) },
                )
            }
        }
    }

    private fun deliverDetail(
        json: String?,
        fallbackBaseUrl: String,
        callback: (WallpaperDetailUi?, Throwable?) -> Unit,
    ) {
        ioExecutor.execute {
            val result = runCatching {
                if (json.isNullOrBlank()) throw IllegalStateException("Empty response")
                val type = object : TypeToken<WallpaperApiResponse<WallpaperDetailDto>>() {}.type
                val response = HttpKts.decodeRaw<WallpaperApiResponse<WallpaperDetailDto>>(json, type)
                    ?: throw IllegalStateException("Invalid response")
                WallpaperParser.parseDetail(
                    WallpaperResponseHelper.ensureSuccess(response),
                    fallbackBaseUrl,
                ) ?: throw IllegalStateException("Invalid detail data")
            }
            runOnMainThread {
                result.fold(
                    onSuccess = { callback(it, null) },
                    onFailure = { callback(null, it) },
                )
            }
        }
    }
}
