package com.demo.app.launcher.gamecenter.model

import androidx.annotation.Keep

/** 游戏中心首页接口响应 DTO。 */
@Keep
data class GameCenterResponse(
    val code: Int = -1,
    val count: Int = 0,
    val data: GameCenterData? = null,
    val msg: String? = null,
    // 兼容新结构：顶层直接返回 list/url/version（无 data 包装）
    val list: List<GameSectionDto>? = null,
    val url: String? = null,
    val version: Int = 0
) {
    fun resolvedData(): GameCenterData? {
        if (data != null) return data
        if (list != null || !url.isNullOrBlank()) {
            return GameCenterData(list = list, url = url, version = version)
        }
        return null
    }

    fun resolvedCode(): Int {
        val resolved = resolvedData()
        return when {
            code == 0 -> 0
            code == -1 && resolved != null -> 0
            else -> code
        }
    }
}

@Keep
data class GameCenterData(
    val list: List<GameSectionDto>? = null,
    val url: String? = null,
    val version: Int = 0
)

@Keep
data class GameSectionDto(
    val cell: List<GameItemDto>? = null,
    /** 保留字段，分类匹配请用 [sort]（= pp_game_genre.csv genre_id）。 */
    val genre_id: Int = 0,
    val layout: Int = 0,
    /** 对应 pp_game_genre.csv 的 genre_id，用于分类 / 纵向卡片匹配。 */
    val sort: Int = 0,
    val title: String? = null
)

@Keep
data class GameItemDto(
    val ban: String? = null,
    val bid: String? = null,
    val desc: String? = null,
    val download: Long = 0,
    val icon: String? = null,
    val id: String? = null,
    val img: String? = null,
    val link: String? = null,
    val name: String? = null,
    val rating: Double = 0.0,
    val rating_count: Long = 0,
    val status: Int = 0,
    val title: String? = null,
    val version: String? = null
)
