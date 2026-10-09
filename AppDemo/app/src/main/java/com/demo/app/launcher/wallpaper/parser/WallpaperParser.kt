package com.demo.app.launcher.wallpaper.parser

import com.demo.app.launcher.wallpaper.model.CategoriesDataDto
import com.demo.app.launcher.wallpaper.model.CategoryDto
import com.demo.app.launcher.wallpaper.model.CategoryUi
import com.demo.app.launcher.wallpaper.model.WallpaperApiResponse
import com.demo.app.launcher.wallpaper.model.WallpaperDetailDto
import com.demo.app.launcher.wallpaper.model.WallpaperDetailUi
import com.demo.app.launcher.wallpaper.model.WallpaperItemDto
import com.demo.app.launcher.wallpaper.model.WallpaperItemUi
import com.demo.app.launcher.wallpaper.model.WallpaperListDataDto
import com.demo.app.launcher.wallpaper.model.WallpaperListUi
import com.demo.app.launcher.utils.WallpaperResponseHelper

object WallpaperParser {

    fun parseCategories(response: WallpaperApiResponse<CategoriesDataDto>?): List<CategoryUi> {
        if (response == null || !WallpaperResponseHelper.isSuccessCode(response.code, response.data != null)) {
            return emptyList()
        }
        return response.data?.categories.orEmpty()
            .mapNotNull { mapCategory(it) }
            .sortedBy { it.sortOrder }
    }

    /** API 返回 `data: [...]` 时使用（非 `data.categories` 包装）。 */
    fun parseCategoriesList(response: WallpaperApiResponse<List<CategoryDto>>?): List<CategoryUi> {
        if (response == null || !WallpaperResponseHelper.isSuccessCode(response.code, !response.data.isNullOrEmpty())) {
            return emptyList()
        }
        return response.data.orEmpty()
            .mapNotNull { mapCategory(it) }
            .sortedBy { it.sortOrder }
    }

    fun parseWallpaperList(
        response: WallpaperApiResponse<WallpaperListDataDto>?,
        fallbackBaseUrl: String = ""
    ): WallpaperListUi {
        if (response == null || response.data == null ||
            !WallpaperResponseHelper.isSuccessCode(response.code, true)
        ) {
            return WallpaperListUi()
        }
        val pagination = response.data.pagination
        val baseUrl = pagination?.base_url?.trim().orEmpty().ifBlank { fallbackBaseUrl }
        val items = response.data.items.orEmpty().mapNotNull { mapItem(it, baseUrl) }
        val page = pagination?.page ?: 1
        val pageSize = (pagination?.page_size ?: 0).coerceAtLeast(1)
        val total = pagination?.total ?: 0
        val loadedCount = (page - 1) * pageSize + items.size
        val hasMore = when (pagination?.has_more) {
            true -> true
            false -> total > 0 && loadedCount < total
            null -> when {
                total > 0 -> loadedCount < total
                else -> items.size >= pageSize
            }
        }
        return WallpaperListUi(
            items = items,
            page = page,
            hasMore = hasMore,
            assetBaseUrl = baseUrl
        )
    }

    fun parseDetail(
        response: WallpaperApiResponse<WallpaperDetailDto>?,
        fallbackBaseUrl: String = ""
    ): WallpaperDetailUi? {
        if (response == null || !WallpaperResponseHelper.isSuccessCode(response.code, response.data != null)) {
            return null
        }
        val dto = response.data ?: return null
        val baseUrl = dto.base_url?.trim().orEmpty().ifBlank { fallbackBaseUrl }
        val id = dto.id?.trim().orEmpty()
        if (id.isEmpty()) return null
        val imageUrl = WallpaperUrlResolver.absoluteUrl(baseUrl, dto.image_url)
            .ifBlank { WallpaperUrlResolver.absoluteUrl(baseUrl, dto.thumb_url) }
        val downloadUrl = WallpaperUrlResolver.absoluteUrl(baseUrl, dto.download_url)
            .ifBlank { imageUrl }
        return WallpaperDetailUi(
            id = id,
            title = dto.title?.trim().orEmpty(),
            thumbUrl = WallpaperUrlResolver.absoluteUrl(baseUrl, dto.thumb_url),
            imageUrl = imageUrl,
            downloadUrl = downloadUrl,
            width = dto.width,
            height = dto.height,
            source = dto.source?.trim().orEmpty(),
            author = dto.author?.trim().orEmpty(),
            link = dto.link?.trim().orEmpty().ifBlank { dto.source_link?.trim().orEmpty() }
        )
    }

    private fun mapCategory(dto: CategoryDto): CategoryUi? {
        val id = dto.id?.trim().orEmpty()
        val name = dto.name?.trim().orEmpty()
        if (id.isEmpty() || name.isEmpty()) return null
        return CategoryUi(
            id = id,
            name = name,
            sortOrder = dto.sort_order,
            coverThumbUrl = dto.cover_thumb_url?.trim().orEmpty()
        )
    }

    private fun mapItem(dto: WallpaperItemDto, baseUrl: String): WallpaperItemUi? {
        val id = dto.id?.trim().orEmpty()
        if (id.isEmpty()) return null
        return WallpaperItemUi(
            id = id,
            categoryId = dto.category_id?.trim().orEmpty(),
            title = dto.title?.trim().orEmpty(),
            thumbUrl = WallpaperUrlResolver.absoluteUrl(baseUrl, dto.thumb_url),
            width = dto.width,
            height = dto.height
        )
    }
}
