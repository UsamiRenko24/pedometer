package com.demo.app.launcher.weather;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.utils.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** 已选城市和城市列表，对应远程项目里的选中城市缓存。 */
public final class WeatherStore {

    private static final String KEY_SELECTED = "weather_selected_city";
    private static final String KEY_CITIES = "weather_city_list";
    private static final Gson GSON = new Gson();
    private static final Type CITY_LIST = new TypeToken<List<WeatherLocation>>() {
    }.getType();

    private WeatherStore() {
    }

    @Nullable
    public static WeatherLocation selected() {
        String json = SPUtils.getInstance().getString(KEY_SELECTED, "");
        if (json.isEmpty()) {
            return null;
        }
        return GSON.fromJson(json, WeatherLocation.class);
    }

    public static void select(@NonNull WeatherLocation location) {
        SPUtils.getInstance().put(KEY_SELECTED, GSON.toJson(location));
        List<WeatherLocation> cities = cities();
        drop(cities, location);
        cities.add(0, location);
        while (cities.size() > 10) {
            cities.remove(cities.size() - 1);
        }
        SPUtils.getInstance().put(KEY_CITIES, GSON.toJson(cities));
    }

    public static void remove(@NonNull WeatherLocation location) {
        List<WeatherLocation> cities = cities();
        drop(cities, location);
        SPUtils.getInstance().put(KEY_CITIES, GSON.toJson(cities));
        WeatherLocation selected = selected();
        if (selected != null && selected.sameAs(location)) {
            if (cities.isEmpty()) {
                SPUtils.getInstance().put(KEY_SELECTED, "");
            } else {
                SPUtils.getInstance().put(KEY_SELECTED, GSON.toJson(cities.get(0)));
            }
        }
    }

    @NonNull
    public static List<WeatherLocation> cities() {
        String json = SPUtils.getInstance().getString(KEY_CITIES, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        List<WeatherLocation> list = GSON.fromJson(json, CITY_LIST);
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    private static void drop(@NonNull List<WeatherLocation> cities, @NonNull WeatherLocation location) {
        for (int index = cities.size() - 1; index >= 0; index--) {
            if (cities.get(index).sameAs(location)) {
                cities.remove(index);
            }
        }
    }
}
