package com.demo.app.launcher.gamecenter.model

import com.demo.app.launcher.gamecenter.parser.GameCenterTextDecoder

/** For You 以下灵活分组的展示形态。 */
enum class SectionDisplayStyle {
    HORIZONTAL_ROW,
    VERTICAL_LIST,
}

object SectionDisplayStyleResolver {

    private val verticalCardSorts = SectionTitles.FLEXIBLE_VERTICAL_CARD_SORTS.toSet()

    fun resolve(section: GameSectionUi): SectionDisplayStyle {
        val sort = section.section.sort
        if (sort in verticalCardSorts) {
            return SectionDisplayStyle.VERTICAL_LIST
        }
        section.sectionKey?.let { key ->
            SectionMetaRegistry.byKey(key)?.sort?.let { metaSort ->
                if (metaSort in verticalCardSorts) {
                    return SectionDisplayStyle.VERTICAL_LIST
                }
            }
        }
        SectionMetaRegistry.fromSort(sort)?.let { meta ->
            if (meta.kind == SectionKind.FLEXIBLE) return meta.flexibleDisplayStyle
        }
        resolveFromMeta(section.sectionKey)?.let { return it }
        val raw = section.rawTitle.trim()
        val decoded = GameCenterTextDecoder.decodeBase64Text(raw)
        listOf(raw, decoded).filter { it.isNotEmpty() }.distinct().forEach { title ->
            SectionMetaRegistry.fromFlexibleTitle(title)?.let { meta ->
                if (meta.kind == SectionKind.FLEXIBLE) return meta.flexibleDisplayStyle
            }
            SectionMetaRegistry.fromRawTitle(title)?.let { meta ->
                if (meta.kind == SectionKind.FLEXIBLE) return meta.flexibleDisplayStyle
            }
        }
        return when (section.section.layout) {
            LAYOUT_VERTICAL_LIST -> SectionDisplayStyle.VERTICAL_LIST
            else -> SectionDisplayStyle.HORIZONTAL_ROW
        }
    }

    private fun resolveFromMeta(key: SectionKey?): SectionDisplayStyle? {
        key ?: return null
        val meta = SectionMetaRegistry.byKey(key) ?: return null
        return if (meta.kind == SectionKind.FLEXIBLE) meta.flexibleDisplayStyle else null
    }

    private const val LAYOUT_VERTICAL_LIST = 2
}
