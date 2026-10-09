package com.demo.app.launcher.gamecenter.model

import androidx.annotation.Keep

/** 游戏详情接口响应 DTO。 */
@Keep
data class GameDetailResponse(
    val code: Int = -1,
    val data: GameDetailDto? = null,
    val msg: String? = null,
    // 兼容新结构：顶层直接返回详情字段（无 data 包装）
    val ban: String? = null,
    val bid: String? = null,
    val desc: String? = null,
    val download: Long = 0,
    val icon: String? = null,
    val id: String? = null,
    val img: String? = null,
    val link: String? = null,
    val long_desc: String? = null,
    val name: String? = null,
    val rating: Double = 0.0,
    val rating_count: Long = 0,
    val release_data: String? = null,
    val status: Int = 0,
    val title: String? = null,
    val update_data: String? = null,
    val version: String? = null
) {
    fun resolvedData(): GameDetailDto? {
        if (data != null) return data
        if (id != null || bid != null || name != null || title != null) {
            return GameDetailDto(
                ban = ban,
                bid = bid,
                desc = desc,
                download = download,
                icon = icon,
                id = id,
                img = img,
                link = link,
                long_desc = long_desc,
                name = name,
                rating = rating,
                rating_count = rating_count,
                release_data = release_data,
                status = status,
                title = title,
                update_data = update_data,
                version = version
            )
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
data class GameDetailDto(
    val ban: String? = null,
    val bid: String? = null,
    val desc: String? = null,
    val download: Long = 0,
    val icon: String? = null,
    val id: String? = null,
    val img: String? = null,
    val link: String? = null,
    val long_desc: String? = null,
    val name: String? = null,
    val rating: Double = 0.0,
    val rating_count: Long = 0,
    val release_data: String? = null,
    val status: Int = 0,
    val title: String? = null,
    val update_data: String? = null,
    val version: String? = null
)
