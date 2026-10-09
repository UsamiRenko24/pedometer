package com.demo.app.launcher.gamecenter.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.demo.app.R

/** 首页固定槽位英文标题（轮播 / 四个分类 Tab，无 sort，仍按 title 匹配）。 */
object SectionTitles {
    const val CAROUSEL = "Home Tap"
    const val PREMIUM = "Premium Games"
    const val EDITOR = "Editor's Picks"
    const val MORE = "More Picks"
    const val GUESS = "For You"

    val CATEGORY_TABS = listOf(PREMIUM, EDITOR, MORE, GUESS)

    /** 以纵向卡片展示的 sort（= pp_game_genre.csv genre_id）。 */
    val FLEXIBLE_VERTICAL_CARD_SORTS = listOf(
        GenreSort.EDUCATION,
        GenreSort.ADVENTURE,
        GenreSort.CARD,
        GenreSort.ROLE_PLAY,
        GenreSort.QUIZ,
        GenreSort.PUZZLE,
    )
}

/** 分组在 UI 中的展示形态。 */
enum class SectionKind {
    CAROUSEL,
    CATEGORY,
    FLEXIBLE,
}

/** 分组稳定标识，与 sort / 多语言 title / 图标资源绑定。 */
enum class SectionKey {
    CAROUSEL,
    PREMIUM,
    EDITOR,
    MORE,
    GUESS,
    STRATEGY,
    RACING,
    ADVENTURE,
    PUZZLE,
    CASUAL,
    SPORTS,
    EDUCATION,
    CARD,
    BOARD,
    MUSIC,
    ACTION,
    SIMULATION,
    ARCADE,
    ROLE_PLAY,
    QUIZ,
    WORD,
    CASINO,
    PRODUCTIVITY,
    SOCIAL,
    SHOPPING,
    PERSONALIZATION,
    COMMUNICATION,
    TOOLS,
    VIDEO_PLAYERS,
    FINANCE,
    ENTERTAINMENT,
    MUSIC_AUDIO,
    DATING,
    LIFESTYLE,
    BUSINESS,
    WEATHER,
    MEDICAL,
    FOOD_DRINK,
    NEWS_MAGAZINES,
    EDUCATION_APP,
    ART_DESIGN,
    SPORTS_APP,
    HEALTH_FITNESS,
    BOOKS_REFERENCE,
    AUTO_VEHICLES,
    MAPS_NAVIGATION,
    TRAVEL_LOCAL,
    PHOTOGRAPHY,
    COMICS,
    EVENTS,
    HOUSE_HOME,
    BEAUTY,
    LIBRARIES_DEMO,
    PARENTING,
}

/** 单个分组的 key、sort(=csv genre_id)、多语言资源与 UI 形态。 */
data class SectionMeta(
    val key: SectionKey,
    /** pp_game_genre.csv genre_id，与接口 list[].sort 一致。 */
    val sort: Int? = null,
    @StringRes val titleResId: Int,
    @DrawableRes val iconResId: Int,
    val kind: SectionKind,
) {
    val flexibleDisplayStyle: SectionDisplayStyle
        get() = when {
            kind != SectionKind.FLEXIBLE -> SectionDisplayStyle.HORIZONTAL_ROW
            sort != null && sort in SectionTitles.FLEXIBLE_VERTICAL_CARD_SORTS ->
                SectionDisplayStyle.VERTICAL_LIST
            else -> SectionDisplayStyle.HORIZONTAL_ROW
        }
}

/** 分组元数据注册表：灵活分组按接口 sort 匹配，固定槽位按 title 匹配。 */
object SectionMetaRegistry {
    private val fixedSlots = listOf(
        SectionMeta(
            key = SectionKey.CAROUSEL,
            titleResId = R.string.gamecenter_title_carousel,
            iconResId = R.drawable.ic_gc_cat_default,
            kind = SectionKind.CAROUSEL,
        ),
        SectionMeta(
            key = SectionKey.PREMIUM,
            titleResId = R.string.gamecenter_title_premium,
            iconResId = R.drawable.ic_gc_cat_premium,
            kind = SectionKind.CATEGORY,
        ),
        SectionMeta(
            key = SectionKey.EDITOR,
            titleResId = R.string.gamecenter_title_editor,
            iconResId = R.drawable.ic_gc_cat_editor,
            kind = SectionKind.CATEGORY,
        ),
        SectionMeta(
            key = SectionKey.MORE,
            titleResId = R.string.gamecenter_title_more,
            iconResId = R.drawable.ic_gc_cat_more,
            kind = SectionKind.CATEGORY,
        ),
        SectionMeta(
            key = SectionKey.GUESS,
            titleResId = R.string.gamecenter_title_guess,
            iconResId = R.drawable.ic_gc_cat_guess,
            kind = SectionKind.CATEGORY,
        ),
    )

    private val flexibleFromCatalog = PpGameGenreCatalog.entries.map { entry ->
        SectionMeta(
            key = entry.key,
            sort = entry.sort,
            titleResId = entry.titleResId,
            iconResId = entry.iconResId,
            kind = SectionKind.FLEXIBLE,
        )
    }

    private val ordered = fixedSlots + flexibleFromCatalog

    private val bySort = ordered.mapNotNull { meta ->
        meta.sort?.let { sort -> sort to meta }
    }.toMap()

    private val byKey = ordered.associateBy { it.key }

    private val fixedTitleEntries = ordered.filter { it.kind != SectionKind.FLEXIBLE }
    private val fixedTitleByNormalized = fixedTitleEntries.associateBy { normalizeTitle(titleOf(it)) }
    private val flexibleTitleByNormalized: Map<String, SectionMeta> = buildFlexibleTitleMap()
    private val titleAliases: Map<String, SectionKey> = buildAliasMap()

    val categoryTabKeys: List<SectionKey> = listOf(
        SectionKey.PREMIUM,
        SectionKey.EDITOR,
        SectionKey.MORE,
        SectionKey.GUESS,
    )

    fun fromSort(sort: Int): SectionMeta? {
        if (sort <= 0) return null
        return bySort[sort]
    }

    fun fromRawTitle(rawTitle: String): SectionMeta? {
        val normalized = normalizeTitle(rawTitle)
        fixedTitleByNormalized[normalized]?.let { return it }
        titleAliases[normalized]?.let { key -> return byKey[key] }
        return null
    }

    fun fromFlexibleTitle(rawTitle: String): SectionMeta? {
        return flexibleTitleByNormalized[normalizeTitle(rawTitle)]
    }

    fun byKey(key: SectionKey): SectionMeta? = byKey[key]

    fun resolve(sort: Int, rawTitle: String, decodedTitle: String = rawTitle): SectionMeta? {
        val titles = listOf(rawTitle, decodedTitle).filter { it.isNotBlank() }.distinct()
        titles.firstNotNullOfOrNull { fromRawTitle(it) }?.let { return it }
        fromSort(sort)?.let { return it }
        return titles.firstNotNullOfOrNull { fromFlexibleTitle(it) }
    }

    fun isRecognizable(sort: Int, rawTitle: String?, decodedTitle: String? = null): Boolean {
        if (fromSort(sort) != null) return true
        return titleCandidates(rawTitle, decodedTitle).any { title ->
            fromRawTitle(title) != null || fromFlexibleTitle(title) != null
        }
    }

    private fun titleCandidates(rawTitle: String?, decodedTitle: String?): List<String> {
        return listOfNotNull(rawTitle?.trim(), decodedTitle?.trim())
            .filter { it.isNotEmpty() }
            .distinct()
    }

    fun normalizeTitle(rawTitle: String): String {
        return rawTitle
            .trim()
            .lowercase()
            .replace("（", "(")
            .replace("）", ")")
            .replace("\uFEFF", "")
            .replace("\u200B", "")
            .replace("\u00A0", " ")
            .replace("\\s+".toRegex(), "")
    }

    private fun titleOf(meta: SectionMeta): String {
        return when (meta.key) {
            SectionKey.CAROUSEL -> SectionTitles.CAROUSEL
            SectionKey.PREMIUM -> SectionTitles.PREMIUM
            SectionKey.EDITOR -> SectionTitles.EDITOR
            SectionKey.MORE -> SectionTitles.MORE
            SectionKey.GUESS -> SectionTitles.GUESS
            else -> meta.key.name
        }
    }

    private fun buildAliasMap(): Map<String, SectionKey> {
        val aliases = listOf(
            "Carousel" to SectionKey.CAROUSEL,
            "Featured Games" to SectionKey.PREMIUM,
            "Editor's Pick" to SectionKey.EDITOR,
            "Editor's Picks" to SectionKey.EDITOR,
            "More Recommendations" to SectionKey.MORE,
            "More Picks" to SectionKey.MORE,
            "You May Like" to SectionKey.GUESS,
            "For You" to SectionKey.GUESS,
            "Popular Games" to SectionKey.PREMIUM,
            "Popular Apps" to SectionKey.EDITOR,
        )
        return aliases.associate { (title, key) -> normalizeTitle(title) to key }
    }

    private fun buildFlexibleTitleMap(): Map<String, SectionMeta> {
        val result = mutableMapOf<String, SectionMeta>()
        PpGameGenreCatalog.entries.forEach { entry ->
            val meta = byKey[entry.key] ?: return@forEach
            entry.titleAliases.forEach { title ->
                result[normalizeTitle(title)] = meta
            }
        }
        return result
    }
}
