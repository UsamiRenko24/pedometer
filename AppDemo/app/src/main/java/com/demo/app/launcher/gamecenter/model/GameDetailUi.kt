package com.demo.app.launcher.gamecenter.model

/** 游戏详情页 UI 模型。 */
data class GameDetailUi(
    val name: String,
    val displayTitle: String,
    val displayDesc: String,
    val displayLongDesc: String,
    val iconUrl: String,
    val bannerUrl: String,
    val imageUrl: String,
    val rating: Double,
    val ratingCount: Long,
    val download: Long,
    val version: String,
    val link: String,
    val bid: String,
    val features: List<String>
)
