package com.demo.app.launcher.wallpaper.model

data class CategoryUi(
    val id: String,
    val name: String,
    val sortOrder: Int = 0,
    val coverThumbUrl: String = ""
)

data class WallpaperItemUi(
    val id: String,
    val categoryId: String = "",
    val title: String = "",
    val thumbUrl: String = "",
    val width: Int = 0,
    val height: Int = 0
)

data class WallpaperListUi(
    val items: List<WallpaperItemUi> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = false,
    val assetBaseUrl: String = ""
)

data class WallpaperDetailUi(
    val id: String,
    val title: String = "",
    val thumbUrl: String = "",
    val imageUrl: String = "",
    val downloadUrl: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val source: String = "",
    val author: String = "",
    val link: String = ""
)
