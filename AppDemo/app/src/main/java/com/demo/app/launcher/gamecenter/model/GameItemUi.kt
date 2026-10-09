package com.demo.app.launcher.gamecenter.model

/** 单个游戏条目 UI 模型（由 Parser 从 DTO 解码并拼接资源 URL）。 */
data class GameItemUi(
    val displayTitle: String,
    val displayDesc: String,
    val name: String,
    val iconUrl: String,
    val bannerUrl: String,
    val imageUrl: String,
    val rating: Double,
    val ratingCount: Long,
    val link: String,
    val isFree: Boolean,
    val bid: String,
    val id: String
)

/** 保留服务端 section 整包，附带分组语义与已解析的 cell 列表。 */
data class GameSectionUi(
    val section: GameSectionDto,
    val sectionKey: SectionKey?,
    val kind: SectionKind?,
    val items: List<GameItemUi>
) {
    /** 服务端下发的原始分组标题（用于匹配与未识别分组兜底展示）。 */
    val rawTitle: String get() = section.title.orEmpty()
}

data class GameCenterUiState(
    val assetBaseUrl: String = "",
    val sections: List<GameSectionUi> = emptyList()
) {
    /** 轮播图固定槽位。 */
    fun carouselSection(): GameSectionUi? = sectionByKey(SectionKey.CAROUSEL)

    /**
     * 分类 Tab 固定槽位：Premium Games、Editor's Pick、More Picks、For You
     *（按既定顺序，有数据才展示）。
     */
    fun categorySections(): List<GameSectionUi> =
        SectionMetaRegistry.categoryTabKeys.mapNotNull { sectionByKey(it) }

    /** 顶部分类芯片：Games 用四个固定槽位；Apps / Explore 对齐四个 Tab 并尽量凑满数据。 */
    fun tabChipSections(categoryId: Int): List<GameSectionUi> {
        if (categoryId == 2) {
            return categorySections()
        }
        val pool = sections.filter { section ->
            section.items.isNotEmpty() && section.kind != SectionKind.CAROUSEL
        }
        if (pool.isEmpty()) {
            return emptyList()
        }
        val result = ArrayList<GameSectionUi>()
        val used = HashSet<GameSectionUi>()
        for (key in SectionMetaRegistry.categoryTabKeys) {
            sectionByKey(key)?.let { section ->
                result.add(section)
                used.add(section)
            }
        }
        for (section in pool) {
            if (section in used) {
                continue
            }
            result.add(section)
            used.add(section)
            if (result.size >= 4) {
                break
            }
        }
        return result.take(4)
    }

    /** Apps / Explore：非轮播且有数据的分组（与 RemoteControl100 一致，保持接口顺序）。 */
    fun feedCategorySections(): List<GameSectionUi> = sections.filter { section ->
        section.items.isNotEmpty() && section.kind != SectionKind.CAROUSEL
    }

    /** 除去轮播与四个分类 Tab 后的灵活分组（保持接口顺序）。 */
    fun flexibleSections(): List<GameSectionUi> = sections.filter { section ->
        section.items.isNotEmpty()
            && section.kind != SectionKind.CAROUSEL
            && section.kind != SectionKind.CATEGORY
    }

    /** 分类芯片下方的灵活分组（Action 等横滑/大卡）；与 Games 一致，仅排除 CATEGORY 槽位。 */
    fun flexibleSectionsBelowChips(): List<GameSectionUi> = flexibleSections()

    /** 分类 Tab 区域下方展示的灵活分组。 */
    fun flexibleSectionsAfterGuess(): List<GameSectionUi> = flexibleSections()

    /** 轮播以下所有分组（含分类 Tab），保持接口顺序。 */
    fun listSections(): List<GameSectionUi> = sections.filter { section ->
        section.items.isNotEmpty() && section.kind != SectionKind.CAROUSEL
    }

    fun isEmpty(): Boolean = sections.all { it.items.isEmpty() }

    private fun sectionByKey(key: SectionKey): GameSectionUi? =
        sections.firstOrNull { it.sectionKey == key && it.items.isNotEmpty() }
}
