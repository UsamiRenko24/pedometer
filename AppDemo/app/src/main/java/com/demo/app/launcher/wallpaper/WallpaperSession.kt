package com.demo.app.launcher.wallpaper

import com.demo.app.launcher.wallpaper.model.WallpaperItemUi

/**
 * 壁纸会话级状态：列表/详情接口返回的 CDN 根地址在打开壁纸模块期间持久存在，
 * 关闭 [com.demo.app.launcher.ui.activity.wallpaper.WallpaperHostActivity] 时释放。
 */
object WallpaperSession {

    data class BrowseContext(
        val items: MutableList<WallpaperItemUi>,
        val initialIndex: Int,
        val categoryId: String = "",
        var page: Int = 1,
        var hasMore: Boolean = false,
        var isLoadingMore: Boolean = false,
    )

    @Volatile
    var assetBaseUrl: String = ""
        private set

    @Volatile
    var browseContext: BrowseContext? = null
        private set

    fun updateAssetBaseUrl(url: String) {
        assetBaseUrl = url.trim()
    }

    fun setBrowseContext(
        items: MutableList<WallpaperItemUi>,
        initialIndex: Int,
        categoryId: String = "",
        page: Int = 1,
        hasMore: Boolean = false,
    ) {
        browseContext = BrowseContext(
            items = items,
            initialIndex = initialIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0)),
            categoryId = categoryId,
            page = page,
            hasMore = hasMore,
        )
    }

    fun clear() {
        assetBaseUrl = ""
        browseContext = null
    }
}
