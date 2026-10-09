package com.demo.app.launcher.gamecenter.parser

import com.demo.app.launcher.gamecenter.model.GameDetailDto
import com.demo.app.launcher.gamecenter.model.GameDetailResponse
import com.demo.app.launcher.gamecenter.model.GameDetailUi


/** 将详情接口 DTO 转为 [GameDetailUi]。 */
object GameDetailParser {

    fun parse(response: GameDetailResponse?, assetBaseUrl: String): GameDetailUi? {
        if (response == null || response.resolvedCode() != 0) return null
        return mapDetail(response.resolvedData() ?: return null, assetBaseUrl)
    }

    fun mapDetail(dto: GameDetailDto, baseUrl: String): GameDetailUi? {
        val decodedTitle = GameCenterTextDecoder.decodeBase64Text(dto.title)
        val decodedDesc = GameCenterTextDecoder.decodeBase64Text(dto.desc)
        val decodedLongDesc = GameCenterTextDecoder.decodeBase64Text(dto.long_desc)
        val name = dto.name?.trim().orEmpty().ifBlank { decodedTitle }.ifBlank { return null }

        val iconUrl = GameCenterTextDecoder.absoluteUrl(baseUrl, dto.icon)
        val bannerUrl = GameCenterTextDecoder.absoluteUrl(baseUrl, dto.ban)
        val imageUrl = when {
            !dto.img.isNullOrBlank() -> GameCenterTextDecoder.absoluteUrl(baseUrl, dto.img)
            bannerUrl.isNotBlank() -> bannerUrl
            else -> iconUrl
        }

        return GameDetailUi(
            name = name,
            displayTitle = decodedTitle.ifBlank { name },
            displayDesc = decodedDesc,
            displayLongDesc = decodedLongDesc,
            iconUrl = iconUrl,
            bannerUrl = bannerUrl,
            imageUrl = imageUrl,
            rating = dto.rating,
            ratingCount = dto.rating_count,
            download = dto.download,
            version = dto.version?.trim().orEmpty(),
            link = dto.link.orEmpty(),
            bid = dto.bid.orEmpty(),
            features = parseFeatureBullets(decodedLongDesc, decodedDesc)
        )
    }

    fun parseFeatureBullets(longDesc: String, desc: String): List<String> {
        val fromLong = extractBulletLines(longDesc)
        if (fromLong.isNotEmpty()) return fromLong
        return extractBulletLines(descAfterFeaturesMarker(desc))
    }

    internal fun extractBulletLines(text: String): List<String> {
        return text.lineSequence()
            .map { it.trim() }
            .filter { line ->
                line.startsWith("-") || line.startsWith("•") || line.startsWith("*")
            }
            .map { line -> line.trimStart('-', '•', '*', ' ').trim() }
            .filter { it.isNotBlank() }
            .toList()
    }

    private fun descAfterFeaturesMarker(desc: String): String {
        val marker = Regex("Features\\s*:", RegexOption.IGNORE_CASE)
        val match = marker.find(desc) ?: return desc
        return desc.substring(match.range.first)
    }
}
