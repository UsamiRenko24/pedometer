package com.demo.app.integration.http;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public final class GsonUtils {

    private static final Gson GSON = new Gson();

    private GsonUtils() {
    }

    @Nullable
    public static <T> T fromJson(@Nullable String json, Class<T> type) {
        try {
            return json == null ? null : GSON.fromJson(json, type);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public static <T> T fromJson(@Nullable String json, Type type) {
        try {
            return json == null ? null : GSON.fromJson(json, type);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public static String toJson(@Nullable Object value) {
        try {
            return value == null ? null : GSON.toJson(value);
        } catch (Exception ignored) {
            return null;
        }
    }
}
