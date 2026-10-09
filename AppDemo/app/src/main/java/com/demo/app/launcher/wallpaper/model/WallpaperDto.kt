package com.demo.app.launcher.wallpaper.model

import androidx.annotation.Keep

@Keep
data class WallpaperApiResponse<T>(
    val code: Int = -1,
    val data: T? = null,
    val msg: String? = null
)

@Keep
data class CategoriesDataDto(
    val categories: List<CategoryDto>? = null
)

@Keep
data class CategoryDto(
    val id: String? = null,
    val name: String? = null,
    val sort_order: Int = 0,
    val cover_thumb_url: String? = null
)

@Keep
data class WallpaperListDataDto(
    val items: List<WallpaperItemDto>? = null,
    val pagination: PaginationDto? = null
)

@Keep
data class WallpaperItemDto(
    val id: String? = null,
    val category_id: String? = null,
    val title: String? = null,
    val thumb_url: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val updated_at: String? = null
)

@Keep
data class PaginationDto(
    val base_url: String? = null,
    val page: Int = 1,
    val page_size: Int = 20,
    val total: Int = 0,
    val has_more: Boolean = false
)

@Keep
data class WallpaperDetailDto(
    val id: String? = null,
    val category_id: String? = null,
    val title: String? = null,
    val thumb_url: String? = null,
    val image_url: String? = null,
    val download_url: String? = null,
    val base_url: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val mime_type: String? = null,
    val file_size_bytes: Long = 0,
    val updated_at: String? = null,
    val author: String? = null,
    val source: String? = null,
    val source_link: String? = null,
    val link: String? = null
)
