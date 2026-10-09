package com.demo.app.http;

import androidx.annotation.NonNull;

/** 业务接口地址。和 RemoteControl100 的 Urls 一致。 */
public final class Urls {

    private static final String BASE_URL = "https://rainova.xyz/";

    public static final String URL_NEWS = BASE_URL + "nw";
    public static final String URL_CITY_SEARCH = BASE_URL + "wt/ffaa/city/search/ewfw";
    public static final String URL_GET_HOT = BASE_URL + "wt/tewe/city/get-hot/fewr";
    public static final String URL_WEATHER_DATA = BASE_URL + "wt/weather/data/v3";
    public static final String URL_GAME_CENTER_HOME = BASE_URL + "gm/gf23f/a/fewe";
    public static final String URL_GAME_CENTER_DETAIL = BASE_URL + "gm/gf23f/l/fewe";
    public static final String URL_WALLPAPER_CATEGORIES = BASE_URL + "pr/v1/categories";
    public static final String URL_WALLPAPER_LIST = BASE_URL + "pr/v1/wallpapers";

    private Urls() {
    }

    @NonNull
    public static String buildWallpaperDetailUrl(@NonNull String wallpaperId) {
        return URL_WALLPAPER_LIST + "/" + wallpaperId.trim();
    }
}
