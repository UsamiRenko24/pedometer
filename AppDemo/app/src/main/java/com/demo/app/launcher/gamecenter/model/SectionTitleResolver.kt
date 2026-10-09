package com.demo.app.launcher.gamecenter.model

import android.content.Context
import com.demo.app.launcher.gamecenter.parser.GameCenterTextDecoder

/**
 * 分组标题解析：灵活分组按 sort(=csv genre_id) 匹配注册表后走 strings.xml 多语言；
 * 固定槽位按 title 匹配；未识别分组回退服务端 title。
 */
object SectionTitleResolver {

    /** Apps / Explore 顶栏四个芯片与 Games 同款文案（Premium / Editor / More / For You）。 */
    fun resolveChipTab(
        context: Context,
        categoryId: Int,
        chipIndex: Int,
        section: GameSectionUi,
    ): String {
        if (categoryId == 2) {
            return resolve(context, section)
        }
        val keys = SectionMetaRegistry.categoryTabKeys
        if (chipIndex in keys.indices) {
            SectionMetaRegistry.byKey(keys[chipIndex])?.titleResId?.let { resId ->
                return context.getString(resId)
            }
        }
        return resolve(context, section)
    }

    /** 分类 Tab 等固定槽位：按解析出的 [GameSectionUi.sectionKey] 走多语言资源。 */
    fun resolve(context: Context, section: GameSectionUi): String {
        localizedTitle(context, section)?.let { return it }
        return displayTitleFromField(section)
    }

    /** 横排 / 纵向灵活槽位：优先 sectionKey / sort，再走多语言资源。 */
    fun resolveByField(context: Context, section: GameSectionUi): String {
        localizedTitle(context, section)?.let { return it }
        return displayTitleFromField(section)
    }

    private fun localizedTitle(context: Context, section: GameSectionUi): String? {
        section.sectionKey?.let { key ->
            SectionMetaRegistry.byKey(key)?.titleResId?.let { resId ->
                return context.getString(resId)
            }
        }
        SectionMetaRegistry.fromSort(section.section.sort)?.titleResId?.let { resId ->
            return context.getString(resId)
        }
        titleCandidates(section).forEach { title ->
            SectionMetaRegistry.fromFlexibleTitle(title)?.titleResId?.let { resId ->
                return context.getString(resId)
            }
            SectionMetaRegistry.fromRawTitle(title)?.titleResId?.let { resId ->
                return context.getString(resId)
            }
        }
        return null
    }

    private fun displayTitleFromField(section: GameSectionUi): String {
        val raw = section.rawTitle.trim()
        val decoded = GameCenterTextDecoder.decodeBase64Text(raw)
        return decoded.ifBlank { raw }
    }

    private fun titleCandidates(section: GameSectionUi): List<String> {
        val raw = section.rawTitle.trim()
        val decoded = GameCenterTextDecoder.decodeBase64Text(raw)
        return listOf(raw, decoded).filter { it.isNotEmpty() }.distinct()
    }
}
