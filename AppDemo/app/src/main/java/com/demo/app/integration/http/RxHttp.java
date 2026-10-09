package com.demo.app.integration.http;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.BuildConfig;
import com.demo.app.integration.http.cache.CacheCallback;
import com.demo.app.integration.http.cache.CacheConfig;
import com.demo.app.integration.http.cache.CacheManager;
import com.demo.app.integration.http.cache.CacheStorage;

import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

public final class RxHttp {
    private static OkHttpClient okHttpClient;
    private static Context appContext;

    private RxHttp() {
    }

    public static void init(@NonNull Context context) {
        appContext = context.getApplicationContext();
    }

    @NonNull
    public static synchronized OkHttpClient getOrCreateClient() {
        if (okHttpClient == null) {
            X509TrustManager trust = new TrustAll();
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, new TrustManager[]{trust}, null);
                OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .connectTimeout(25, TimeUnit.SECONDS)
                        .readTimeout(25, TimeUnit.SECONDS)
                        .writeTimeout(25, TimeUnit.SECONDS)
                        .sslSocketFactory(context.getSocketFactory(), trust)
                        .hostnameVerifier(new HostnameVerifier() {
                            @SuppressLint("BadHostnameVerifier")
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                        });
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    builder.addInterceptor(logging);
                }
                okHttpClient = builder.build();
            } catch (Exception exception) {
                throw new AssertionError(exception);
            }
        }
        return okHttpClient;
    }

    public static void get(
            @NonNull String url,
            @Nullable Map<String, Object> queries,
            @NonNull Callback callback
    ) {
        String newUrl = queries == null || queries.isEmpty() ? url : HttpUtils.addQueries(url, queries);
        Request request = new Request.Builder().url(newUrl).get().build();
        getOrCreateClient().newCall(request).enqueue(callback);
    }

    public static void post(
            @NonNull String url,
            @Nullable Map<String, Object> params,
            @NonNull Callback callback
    ) {
        post(url, params, false, callback);
    }

    public static void post(
            @NonNull String url,
            @Nullable Map<String, Object> params,
            boolean confuseParams,
            @NonNull Callback callback
    ) {
        RequestBody body = convert(params, confuseParams);
        if (body == null) {
            return;
        }
        Request request = new Request.Builder().url(url).post(body).build();
        getOrCreateClient().newCall(request).enqueue(callback);
    }

    public static void getWithCache(
            @NonNull String url,
            @Nullable Map<String, Object> queries,
            @NonNull CacheConfig config,
            @NonNull CacheCallback callback
    ) {
        CacheManager manager = new CacheManager(CacheStorage.getInstance(requireContext()));
        manager.execute(config, callback, networkCallback -> get(url, queries, networkCallback));
    }

    public static void postWithCache(
            @NonNull String url,
            @Nullable Map<String, Object> params,
            @NonNull CacheConfig config,
            @NonNull CacheCallback callback
    ) {
        postWithCache(url, params, false, config, callback);
    }

    public static void postWithCache(
            @NonNull String url,
            @Nullable Map<String, Object> params,
            boolean confuseParams,
            @NonNull CacheConfig config,
            @NonNull CacheCallback callback
    ) {
        CacheManager manager = new CacheManager(CacheStorage.getInstance(requireContext()));
        manager.execute(config, callback, networkCallback -> post(url, params, confuseParams, networkCallback));
    }

    @NonNull
    private static Context requireContext() {
        if (appContext == null) {
            throw new IllegalStateException("RxHttp 未初始化，请先调用 RxHttp.init(context)");
        }
        return appContext;
    }

    @Nullable
    private static RequestBody convert(@Nullable Map<String, Object> params, boolean confuseParams) {
        if (params == null) {
            return null;
        }
        try {
            List<String> keys = new ArrayList<>(params.keySet());
            Collections.shuffle(keys);
            Map<String, Object> shuffled = new LinkedHashMap<>();
            for (String key : keys) {
                shuffled.put(key, params.get(key));
            }
            String content = GsonUtils.toJson(shuffled);
            if (content == null) {
                return null;
            }
            if (confuseParams) {
                content = encodeParams(content);
            }
            if (content == null) {
                return null;
            }
            return RequestBody.create(content, MediaType.get("application/json; charset=UTF-8"));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static String encodeParams(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String encoded = Base64.encodeToString(value.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        if (encoded.length() > 1) {
            encoded = encoded.substring(1) + encoded.charAt(0);
        }
        return encoded;
    }

    @SuppressLint("CustomX509TrustManager")
    private static final class TrustAll implements X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
