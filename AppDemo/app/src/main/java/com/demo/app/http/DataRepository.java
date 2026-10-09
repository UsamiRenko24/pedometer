package com.demo.app.http;

import androidx.annotation.NonNull;

import com.demo.app.integration.http.HttpCallback;
import com.demo.app.integration.http.NetworkCallbackImpl;
import com.demo.app.integration.http.RxHttp;
import com.demo.app.integration.http.cache.CacheCallback;
import com.demo.app.integration.http.cache.CacheCallbackImpl;
import com.demo.app.integration.http.cache.CacheConfig;
import com.demo.app.integration.http.cache.CacheStrategy;
import com.demo.app.launcher.weather.WeatherLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DataRepository {

    private DataRepository() {
    }

    public static void getNews(
            int page,
            int limit,
            @NonNull String tag,
            @NonNull String country,
            @NonNull HttpCallback callback
    ) {
        Map<String, Object> queries = new HashMap<>();
        queries.put("page", page);
        queries.put("limit", limit);
        queries.put("tag", tag);
        queries.put("country", country);
        queries.put("from", "1");

        String[] parts = randomParts("c", "d");
        String url = Urls.URL_NEWS + "/" + parts[0] + "/e/" + parts[1];
        String cacheKey = Urls.URL_NEWS + "e" + page + limit + tag + country;
        CacheConfig config = new CacheConfig.Builder()
                .key(cacheKey)
                .expireTime(30 * 60 * 1000)
                .strategy(CacheStrategy.CACHE_OR_NETWORK)
                .validator(data -> ResponseValidator.isSuccessResponse(data, 0))
                .build();
        CacheCallback cacheCallback = new CacheCallbackImpl((result, error, fromCache) ->
                callback.onResult(result, error));
        RxHttp.getWithCache(url, queries, config, cacheCallback);
    }

    public static void getNewsTag(@NonNull String country, @NonNull HttpCallback callback) {
        Map<String, Object> queries = new HashMap<>();
        queries.put("country", country);
        queries.put("from", "1");

        String[] parts = randomParts("a", "b");
        String url = Urls.URL_NEWS + "/" + parts[0] + "/i/" + parts[1];
        String cacheKey = Urls.URL_NEWS + "i" + country;
        CacheConfig config = new CacheConfig.Builder()
                .key(cacheKey)
                .expireTime(12 * 60 * 60 * 1000)
                .strategy(CacheStrategy.CACHE_OR_NETWORK)
                .validator(data -> ResponseValidator.isSuccessResponse(data, 0))
                .build();
        CacheCallback cacheCallback = new CacheCallbackImpl((result, error, fromCache) ->
                callback.onResult(result, error));
        RxHttp.getWithCache(url, queries, config, cacheCallback);
    }

    public static void getWeatherData(
            boolean forceRefresh,
            @NonNull WeatherLocation location,
            @NonNull HttpCallback callback
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("language", "en");
        params.put("locale", "US");
        params.put("address", location.toAddressJson());

        CacheStrategy strategy = forceRefresh
                ? CacheStrategy.NETWORK_ONLY
                : CacheStrategy.CACHE_OR_NETWORK;
        CacheConfig config = new CacheConfig.Builder()
                .key(Urls.URL_WEATHER_DATA + location.sid)
                .expireTime(3 * 60 * 1000)
                .strategy(strategy)
                .validator(data -> ResponseValidator.isSuccessEncryptedResponse(data, 0))
                .build();
        CacheCallback cacheCallback = new CacheCallbackImpl((result, error, fromCache) ->
                callback.onResult(result, error));
        RxHttp.postWithCache(Urls.URL_WEATHER_DATA, params, true, config, cacheCallback);
    }

    public static void getHotCity(@NonNull HttpCallback callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("foreign", "1");
        params.put("language", "en");
        params.put("locale", "US");

        CacheConfig config = new CacheConfig.Builder()
                .key(Urls.URL_GET_HOT)
                .expireTime(24 * 60 * 60 * 1000)
                .strategy(CacheStrategy.CACHE_OR_NETWORK)
                .validator(data -> ResponseValidator.isSuccessResponse(data, 0)
                        || ResponseValidator.isSuccessEncryptedResponse(data, 0))
                .build();
        CacheCallback cacheCallback = new CacheCallbackImpl((result, error, fromCache) ->
                callback.onResult(result, error));
        RxHttp.postWithCache(Urls.URL_GET_HOT, params, config, cacheCallback);
    }

    public static void searchCity(@NonNull String cityName, @NonNull HttpCallback callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("key", cityName);
        params.put("language", "en");
        params.put("locale", "US");
        RxHttp.post(Urls.URL_CITY_SEARCH, params, new NetworkCallbackImpl((result, error) ->
                callback.onResult(result, error)));
    }

    @NonNull
    private static String[] randomParts(@NonNull String firstFallback, @NonNull String secondFallback) {
        String[] parts = UUID.randomUUID().toString().split("-");
        if (parts.length >= 2) {
            return new String[]{parts[0], parts[1]};
        }
        return new String[]{firstFallback, secondFallback};
    }
}
