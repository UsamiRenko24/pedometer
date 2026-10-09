package com.demo.app.integration.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public final class HttpUtils {

    private HttpUtils() {
    }

    @Nullable
    public static RequestBody convert(@Nullable String content) {
        try {
            MediaType contentType = MediaType.get("application/json; charset=UTF-8");
            return RequestBody.create(content != null ? content : "", contentType);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public static String map2String(@NonNull Map<String, Object> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.shuffle(keys);
        LinkedHashMap<String, Object> shuffled = new LinkedHashMap<>();
        for (String key : keys) {
            shuffled.put(key, params.get(key));
        }
        return GsonUtils.toJson(shuffled);
    }

    @NonNull
    public static String addQueries(@NonNull String url, @NonNull Map<String, Object> queries) {
        if (queries.isEmpty()) {
            return url;
        }
        StringBuilder builder = new StringBuilder(url);
        builder.append(url.indexOf('?') > 0 || url.indexOf('&') > 0 ? "&" : "?");
        for (Map.Entry<String, Object> entry : queries.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
