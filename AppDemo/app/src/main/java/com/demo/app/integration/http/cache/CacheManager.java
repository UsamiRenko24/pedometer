package com.demo.app.integration.http.cache;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/** 按缓存策略决定读本地还是发请求。成功结果回到主线程。 */
public class CacheManager {
    private final CacheStorage storage;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public CacheManager(@NonNull CacheStorage storage) {
        this.storage = storage;
    }

    public void execute(
            @NonNull CacheConfig config,
            @NonNull CacheCallback callback,
            @NonNull NetworkRequest networkRequest
    ) {
        switch (config.getStrategy()) {
            case CACHE_ONLY:
                handleCacheOnly(config, callback);
                break;
            case CACHE_FIRST:
                handleCacheFirst(config, callback, networkRequest);
                break;
            case NETWORK_ONLY:
                handleNetworkOnly(config, callback, networkRequest);
                break;
            case CACHE_OR_NETWORK:
                handleCacheOrNetwork(config, callback, networkRequest);
                break;
            default:
                break;
        }
    }

    private void handleCacheOnly(@NonNull CacheConfig config, @NonNull CacheCallback callback) {
        CacheData cacheData = readCache(config.getKey());
        if (cacheData != null && !cacheData.isExpired() && cacheData.getData() != null) {
            String data = cacheData.getData();
            postToMainThread(() -> callback.onCacheSuccess(data));
            return;
        }
        postToMainThread(() -> callback.onFailure(new Exception("Cache does not exist or has expired")));
    }

    private void handleCacheFirst(
            @NonNull CacheConfig config,
            @NonNull CacheCallback callback,
            @NonNull NetworkRequest networkRequest
    ) {
        CacheData cacheData = readCache(config.getKey());
        boolean hasCacheData = cacheData != null;
        if (cacheData != null && !cacheData.isExpired() && cacheData.getData() != null) {
            String data = cacheData.getData();
            postToMainThread(() -> callback.onCacheSuccess(data));
        }
        networkRequest.execute(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException exception) {
                if (!hasCacheData) {
                    postToMainThread(() -> callback.onFailure(exception));
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String data = response.body().string();
                        writeCache(config.getKey(), new CacheData(data, config.getExpireTime()), config);
                        postToMainThread(() -> callback.onNetworkSuccess(data));
                    } else if (!hasCacheData) {
                        postToMainThread(() -> callback.onFailure(
                                new Exception("Network request failed: " + response.code())));
                    }
                } catch (Exception exception) {
                    if (!hasCacheData) {
                        postToMainThread(() -> callback.onFailure(exception));
                    }
                } finally {
                    response.close();
                }
            }
        });
    }

    private void handleNetworkOnly(
            @NonNull CacheConfig config,
            @NonNull CacheCallback callback,
            @NonNull NetworkRequest networkRequest
    ) {
        networkRequest.execute(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException exception) {
                postToMainThread(() -> callback.onFailure(exception));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String data = response.body().string();
                        writeCache(config.getKey(), new CacheData(data, config.getExpireTime()), config);
                        postToMainThread(() -> callback.onNetworkSuccess(data));
                    } else {
                        postToMainThread(() -> callback.onFailure(
                                new Exception("Network request failed: " + response.code())));
                    }
                } catch (Exception exception) {
                    postToMainThread(() -> callback.onFailure(exception));
                } finally {
                    response.close();
                }
            }
        });
    }

    private void handleCacheOrNetwork(
            @NonNull CacheConfig config,
            @NonNull CacheCallback callback,
            @NonNull NetworkRequest networkRequest
    ) {
        CacheData cacheData = readCache(config.getKey());
        if (cacheData != null && !cacheData.isExpired() && cacheData.getData() != null) {
            String data = cacheData.getData();
            postToMainThread(() -> callback.onCacheSuccess(data));
            return;
        }
        handleNetworkOnly(config, callback, networkRequest);
    }

    private void postToMainThread(@NonNull Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }

    @Nullable
    private synchronized CacheData readCache(@NonNull String key) {
        try {
            return storage.get(key);
        } catch (Exception ignored) {
            return null;
        }
    }

    private synchronized void writeCache(@NonNull String key, @NonNull CacheData data, @NonNull CacheConfig config) {
        try {
            if (config.getValidator() != null && !config.getValidator().shouldCache(data.getData())) {
                return;
            }
            storage.put(key, data);
        } catch (Exception ignored) {
        }
    }
}
