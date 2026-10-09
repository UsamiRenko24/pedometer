package com.demo.app.launcher.news;

import androidx.annotation.NonNull;

import com.demo.app.utils.SPUtils;

/** 新闻地区选择。没选过时用 United States。 */
public final class NewsStore {

    private static final String KEY_AREA = "news_area_name";

    private NewsStore() {
    }

    @NonNull
    public static NewsArea area() {
        String name = SPUtils.getInstance().getString(KEY_AREA, NewsArea.ENGLISH.name());
        if ("SIMPLIFIED".equals(name)) {
            name = NewsArea.TRADITIONAL.name();
        }
        try {
            return NewsArea.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return NewsArea.ENGLISH;
        }
    }

    public static void setArea(@NonNull NewsArea area) {
        SPUtils.getInstance().put(KEY_AREA, area.name());
    }
}
