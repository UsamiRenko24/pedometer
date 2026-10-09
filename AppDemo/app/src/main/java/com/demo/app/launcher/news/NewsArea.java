package com.demo.app.launcher.news;

import androidx.annotation.StringRes;

import com.demo.app.R;

/** 新闻地区。country 是接口参数，和 RemoteControl100 的 NewsLanguage 一致。 */
public enum NewsArea {
    ENGLISH("US", R.string.news_area_us),
    GERMAN("DE", R.string.news_area_de),
    GREEK("US", R.string.news_area_gr),
    SPANISH("ES", R.string.news_area_es),
    FRENCH("FR", R.string.news_area_fr),
    HINDI("US", R.string.news_area_in),
    HUNGARIAN("US", R.string.news_area_hu),
    ITALIAN("US", R.string.news_area_it),
    JAPANESE("JP", R.string.news_area_jp),
    KOREAN("US", R.string.news_area_kr),
    DUTCH("US", R.string.news_area_nl),
    POLISH("US", R.string.news_area_pl),
    PORTUGUESE("US", R.string.news_area_pt),
    RUSSIAN("US", R.string.news_area_ru),
    SWEDISH("US", R.string.news_area_se),
    THAI("US", R.string.news_area_th),
    TURKISH("US", R.string.news_area_tr),
    VIETNAMESE("US", R.string.news_area_vn),
    TRADITIONAL("US", R.string.news_area_cn);

    public final String country;
    @StringRes
    public final int labelRes;

    NewsArea(String country, int labelRes) {
        this.country = country;
        this.labelRes = labelRes;
    }
}
