package com.demo.app.launcher.gamecenter.parser

import com.demo.app.launcher.gamecenter.model.GameCenterResponse
import com.demo.app.launcher.gamecenter.model.GameCenterUiState
import com.demo.app.launcher.gamecenter.model.GameItemDto
import com.demo.app.launcher.gamecenter.model.GameItemUi
import com.demo.app.launcher.gamecenter.model.GameSectionUi
import com.demo.app.launcher.gamecenter.model.SectionMetaRegistry

/** 将接口 section 映射为 [GameSectionUi]，保留原始 DTO，cell 仅在此层转为 [GameItemUi]。 */
object GameCenterParser {
    /** 将接口响应转为页面 [GameCenterUiState]；展示顺序保持接口 list 原序（sort 为分类 id 非排序键）。 */
    fun parse(response: GameCenterResponse?): GameCenterUiState {
        val resolvedData = response?.resolvedData()
        if (response == null || response.resolvedCode() != 0 || resolvedData == null) {
            return GameCenterUiState()
        }
        val baseUrl = resolvedData.url.orEmpty()
        val rawSections = resolvedData.list.orEmpty()
        val sections = rawSections
            .filter { dto ->
                val rawTitle = dto.title.orEmpty()
                val decodedTitle = GameCenterTextDecoder.decodeBase64Text(rawTitle)
                !dto.cell.isNullOrEmpty() && (
                    SectionMetaRegistry.fromSort(dto.sort) != null ||
                        rawTitle.isNotBlank() ||
                        SectionMetaRegistry.isRecognizable(dto.sort, rawTitle, decodedTitle)
                )
            }
            .map { dto ->
                val rawTitle = dto.title.orEmpty()
                val decodedTitle = GameCenterTextDecoder.decodeBase64Text(rawTitle)
                val meta = SectionMetaRegistry.resolve(
                    sort = dto.sort,
                    rawTitle = rawTitle,
                    decodedTitle = decodedTitle,
                )
                GameSectionUi(
                    section = dto,
                    sectionKey = meta?.key,
                    kind = meta?.kind,
                    items = dto.cell.orEmpty().mapNotNull { mapItem(it, baseUrl) }
                )
            }

        return GameCenterUiState(assetBaseUrl = baseUrl, sections = sections)
    }

    private fun mapItem(dto: GameItemDto, baseUrl: String): GameItemUi? {
        val decodedTitle = GameCenterTextDecoder.decodeBase64Text(dto.title)
        // 冒险等分组部分数据 name 为空；使用解码标题兜底避免整组被过滤掉。
        val name = dto.name?.trim().orEmpty().ifBlank { decodedTitle }.ifBlank { "Unknown" }

        val decodedDesc = GameCenterTextDecoder.decodeBase64Text(dto.desc)
        val iconUrl = GameCenterTextDecoder.absoluteUrl(baseUrl, dto.icon)
        val bannerUrl = GameCenterTextDecoder.absoluteUrl(baseUrl, dto.ban)
        val imageUrl = when {
            !dto.img.isNullOrBlank() -> GameCenterTextDecoder.absoluteUrl(baseUrl, dto.img)
            bannerUrl.isNotBlank() -> bannerUrl
            else -> iconUrl
        }

        return GameItemUi(
            displayTitle = decodedTitle.ifBlank { name },
            displayDesc = decodedDesc,
            name = name,
            iconUrl = iconUrl,
            bannerUrl = bannerUrl,
            imageUrl = imageUrl,
            rating = dto.rating,
            ratingCount = dto.rating_count,
            link = dto.link.orEmpty(),
            isFree = dto.status == 2,
            bid = dto.bid.orEmpty(),
            id = dto.id.orEmpty()
        )
    }
}
