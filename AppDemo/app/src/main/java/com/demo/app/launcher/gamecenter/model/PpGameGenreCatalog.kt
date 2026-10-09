package com.demo.app.launcher.gamecenter.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.demo.app.R

/**
 * pp_game_genre.csv 全量分类（接口 list[].sort = genre_id）。
 * 含 category_id=2 游戏类与 category_id=1 应用类。
 */
data class PpGameGenreEntry(
    val sort: Int,
    val key: SectionKey,
    @StringRes val titleResId: Int,
    @DrawableRes val iconResId: Int,
    /** 英文 / 中文等别名，用于 title 兜底匹配。 */
    val titleAliases: List<String>,
)

object PpGameGenreCatalog {

    private const val DEFAULT_ICON = R.drawable.ic_gc_cat_default

    /** csv 全量条目，顺序与 pp_game_genre.csv id 列一致。 */
    val entries: List<PpGameGenreEntry> = listOf(
        entry(GenreSort.PUZZLE, SectionKey.PUZZLE, R.string.gamecenter_title_puzzle, R.drawable.ic_gc_cat_yizhi,
            "PUZZLE", "Puzzle", "益智"),
        entry(GenreSort.CASUAL, SectionKey.CASUAL, R.string.gamecenter_title_casual, R.drawable.ic_gc_cat_xiuxian,
            "CASUAL", "Casual", "Leisure", "休闲"),
        entry(GenreSort.BOARD, SectionKey.BOARD, R.string.gamecenter_title_board, R.drawable.ic_gc_cat_zhuomianqilei,
            "BOARD", "Board", "Board Games", "桌面和棋类"),
        entry(GenreSort.ADVENTURE, SectionKey.ADVENTURE, R.string.gamecenter_title_adventure, R.drawable.ic_gc_cat_maoxian,
            "ADVENTURE", "Adventure", "冒险"),
        entry(GenreSort.SIMULATION, SectionKey.SIMULATION, R.string.gamecenter_title_simulation, R.drawable.ic_gc_cat_moni,
            "SIMULATION", "Simulation", "模拟"),
        entry(GenreSort.CARD, SectionKey.CARD, R.string.gamecenter_title_card, R.drawable.ic_gc_cat_kapai,
            "CARD", "Card", "Cards", "卡牌"),
        entry(GenreSort.STRATEGY, SectionKey.STRATEGY, R.string.gamecenter_title_strategy, R.drawable.ic_gc_cat_celue,
            "STRATEGY", "Strategy", "策略"),
        entry(GenreSort.ARCADE, SectionKey.ARCADE, R.string.gamecenter_title_arcade, R.drawable.ic_gc_cat_jieji,
            "ARCADE", "Arcade", "街机"),
        entry(GenreSort.ACTION, SectionKey.ACTION, R.string.gamecenter_title_action, R.drawable.ic_gc_cat_dongzuo,
            "ACTION", "Action", "动作"),
        entry(GenreSort.WORD, SectionKey.WORD, R.string.gamecenter_title_word, R.drawable.ic_gc_cat_wenzi,
            "WORD", "Word", "Word Games", "文字"),
        entry(GenreSort.MUSIC, SectionKey.MUSIC, R.string.gamecenter_title_music, R.drawable.ic_gc_cat_yinyue,
            "MUSIC", "Music", "音乐"),
        entry(GenreSort.CASINO, SectionKey.CASINO, R.string.gamecenter_title_casino, R.drawable.ic_gc_cat_duchang,
            "CASINO", "Casino", "赌场"),
        entry(GenreSort.ROLE_PLAY, SectionKey.ROLE_PLAY, R.string.gamecenter_title_role_play, R.drawable.ic_gc_cat_juesebanyan,
            "ROLE_PLAYING", "Role Playing", "RPG", "角色扮演"),
        entry(GenreSort.RACING, SectionKey.RACING, R.string.gamecenter_title_racing, R.drawable.ic_gc_cat_jingsu,
            "RACING", "Racing", "竞速"),
        entry(GenreSort.EDUCATION, SectionKey.EDUCATION, R.string.gamecenter_title_education, R.drawable.ic_gc_cat_jiaoyu,
            "EDUCATIONAL", "Education", "Educational", "教育"),
        entry(GenreSort.SPORTS, SectionKey.SPORTS, R.string.gamecenter_title_sports, R.drawable.ic_gc_cat_tiyu,
            "SPORTS", "Sports", "体育"),
        entry(GenreSort.QUIZ, SectionKey.QUIZ, R.string.gamecenter_title_quiz, R.drawable.ic_gc_cat_zhishiwenda,
            "TRIVIA", "Quiz", "Knowledge Quiz", "知识问答"),
        entry(GenreSort.PRODUCTIVITY, SectionKey.PRODUCTIVITY, R.string.gamecenter_title_productivity, DEFAULT_ICON,
            "Productivity", "效率"),
        entry(GenreSort.SOCIAL, SectionKey.SOCIAL, R.string.gamecenter_title_social, DEFAULT_ICON,
            "Social", "社交"),
        entry(GenreSort.SHOPPING, SectionKey.SHOPPING, R.string.gamecenter_title_shopping, DEFAULT_ICON,
            "Shopping", "购物"),
        entry(GenreSort.PERSONALIZATION, SectionKey.PERSONALIZATION, R.string.gamecenter_title_personalization, DEFAULT_ICON,
            "Personalization", "个性定制"),
        entry(GenreSort.COMMUNICATION, SectionKey.COMMUNICATION, R.string.gamecenter_title_communication, DEFAULT_ICON,
            "Communication", "通讯"),
        entry(GenreSort.TOOLS, SectionKey.TOOLS, R.string.gamecenter_title_tools, DEFAULT_ICON,
            "Tools", "工具"),
        entry(GenreSort.VIDEO_PLAYERS, SectionKey.VIDEO_PLAYERS, R.string.gamecenter_title_video_players, DEFAULT_ICON,
            "Video Players & Editors", "视频播放和编辑"),
        entry(GenreSort.FINANCE, SectionKey.FINANCE, R.string.gamecenter_title_finance, DEFAULT_ICON,
            "Finance", "财务"),
        entry(GenreSort.ENTERTAINMENT, SectionKey.ENTERTAINMENT, R.string.gamecenter_title_entertainment, DEFAULT_ICON,
            "Entertainment", "娱乐"),
        entry(GenreSort.MUSIC_AUDIO, SectionKey.MUSIC_AUDIO, R.string.gamecenter_title_music_audio, DEFAULT_ICON,
            "Music & Audio", "音乐与音频"),
        entry(GenreSort.DATING, SectionKey.DATING, R.string.gamecenter_title_dating, DEFAULT_ICON,
            "Dating", "约会交友"),
        entry(GenreSort.LIFESTYLE, SectionKey.LIFESTYLE, R.string.gamecenter_title_lifestyle, DEFAULT_ICON,
            "Lifestyle", "生活时尚"),
        entry(GenreSort.BUSINESS, SectionKey.BUSINESS, R.string.gamecenter_title_business, DEFAULT_ICON,
            "Business", "办公"),
        entry(GenreSort.WEATHER, SectionKey.WEATHER, R.string.gamecenter_title_weather, DEFAULT_ICON,
            "Weather", "天气"),
        entry(GenreSort.MEDICAL, SectionKey.MEDICAL, R.string.gamecenter_title_medical, DEFAULT_ICON,
            "Medical", "医疗"),
        entry(GenreSort.FOOD_DRINK, SectionKey.FOOD_DRINK, R.string.gamecenter_title_food_drink, DEFAULT_ICON,
            "Food & Drink", "餐饮美食"),
        entry(GenreSort.NEWS_MAGAZINES, SectionKey.NEWS_MAGAZINES, R.string.gamecenter_title_news_magazines, DEFAULT_ICON,
            "News & Magazines", "新闻杂志"),
        entry(GenreSort.EDUCATION_APP, SectionKey.EDUCATION_APP, R.string.gamecenter_title_education_app, DEFAULT_ICON),
        entry(GenreSort.ART_DESIGN, SectionKey.ART_DESIGN, R.string.gamecenter_title_art_design, DEFAULT_ICON,
            "Art & Design", "艺术和设计"),
        entry(GenreSort.SPORTS_APP, SectionKey.SPORTS_APP, R.string.gamecenter_title_sports_app, DEFAULT_ICON),
        entry(GenreSort.HEALTH_FITNESS, SectionKey.HEALTH_FITNESS, R.string.gamecenter_title_health_fitness, DEFAULT_ICON,
            "Health & Fitness", "健康与健身"),
        entry(GenreSort.BOOKS_REFERENCE, SectionKey.BOOKS_REFERENCE, R.string.gamecenter_title_books_reference, DEFAULT_ICON,
            "Books & Reference", "图书与工具书"),
        entry(GenreSort.AUTO_VEHICLES, SectionKey.AUTO_VEHICLES, R.string.gamecenter_title_auto_vehicles, DEFAULT_ICON,
            "Auto & Vehicles", "车辆和交通"),
        entry(GenreSort.MAPS_NAVIGATION, SectionKey.MAPS_NAVIGATION, R.string.gamecenter_title_maps_navigation, DEFAULT_ICON,
            "Maps & Navigation", "地图和导航"),
        entry(GenreSort.TRAVEL_LOCAL, SectionKey.TRAVEL_LOCAL, R.string.gamecenter_title_travel_local, DEFAULT_ICON,
            "Travel & Local", "外出旅行与本地生活"),
        entry(GenreSort.PHOTOGRAPHY, SectionKey.PHOTOGRAPHY, R.string.gamecenter_title_photography, DEFAULT_ICON,
            "Photography", "摄影"),
        entry(GenreSort.COMICS, SectionKey.COMICS, R.string.gamecenter_title_comics, DEFAULT_ICON,
            "Comics", "漫画"),
        entry(GenreSort.EVENTS, SectionKey.EVENTS, R.string.gamecenter_title_events, DEFAULT_ICON,
            "Events", "活动"),
        entry(GenreSort.HOUSE_HOME, SectionKey.HOUSE_HOME, R.string.gamecenter_title_house_home, DEFAULT_ICON,
            "House & Home", "家居装修"),
        entry(GenreSort.BEAUTY, SectionKey.BEAUTY, R.string.gamecenter_title_beauty, DEFAULT_ICON,
            "Beauty", "美容时尚"),
        entry(GenreSort.LIBRARIES_DEMO, SectionKey.LIBRARIES_DEMO, R.string.gamecenter_title_libraries_demo, DEFAULT_ICON,
            "Libraries & Demo", "软件库与演示"),
        entry(GenreSort.PARENTING, SectionKey.PARENTING, R.string.gamecenter_title_parenting, DEFAULT_ICON,
            "Parenting", "育儿"),
    )

    private fun entry(
        sort: Int,
        key: SectionKey,
        @StringRes titleResId: Int,
        @DrawableRes iconResId: Int,
        vararg aliases: String,
    ): PpGameGenreEntry = PpGameGenreEntry(sort, key, titleResId, iconResId, aliases.toList())
}

/** pp_game_genre.csv genre_id，与接口 sort 一致。 */
object GenreSort {
    const val PUZZLE = 50
    const val CASUAL = 49
    const val BOARD = 53
    const val ADVENTURE = 45
    const val SIMULATION = 46
    const val CARD = 44
    const val STRATEGY = 37
    const val ARCADE = 42
    const val ACTION = 38
    const val WORD = 48
    const val MUSIC = 51
    const val CASINO = 39
    const val ROLE_PLAY = 40
    const val RACING = 43
    const val EDUCATION = 41
    const val SPORTS = 47
    const val QUIZ = 52
    const val PRODUCTIVITY = 19
    const val SOCIAL = 20
    const val SHOPPING = 9
    const val PERSONALIZATION = 6
    const val COMMUNICATION = 27
    const val TOOLS = 7
    const val VIDEO_PLAYERS = 24
    const val FINANCE = 2
    const val ENTERTAINMENT = 34
    const val MUSIC_AUDIO = 32
    const val DATING = 21
    const val LIFESTYLE = 23
    const val BUSINESS = 8
    const val WEATHER = 26
    const val MEDICAL = 30
    const val FOOD_DRINK = 3
    const val NEWS_MAGAZINES = 29
    const val EDUCATION_APP = 14
    const val ART_DESIGN = 31
    const val SPORTS_APP = 25
    const val HEALTH_FITNESS = 13
    const val BOOKS_REFERENCE = 28
    const val AUTO_VEHICLES = 4
    const val MAPS_NAVIGATION = 5
    const val TRAVEL_LOCAL = 15
    const val PHOTOGRAPHY = 22
    const val COMICS = 16
    const val EVENTS = 10
    const val HOUSE_HOME = 11
    const val BEAUTY = 17
    const val LIBRARIES_DEMO = 18
    const val PARENTING = 35
}
