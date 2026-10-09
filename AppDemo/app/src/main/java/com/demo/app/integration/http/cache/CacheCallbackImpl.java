package com.demo.app.integration.http.cache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CacheCallbackImpl implements CacheCallback {

    @Nullable
    private final CacheResultCallback callback;

    public CacheCallbackImpl(@Nullable CacheResultCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onCacheSuccess(@NonNull String data) {
        deliver(data, null, true);
    }

    @Override
    public void onNetworkSuccess(@NonNull String data) {
        deliver(data, null, false);
    }

    @Override
    public void onFailure(@NonNull Exception exception) {
        deliver(null, exception, false);
    }

    private void deliver(@Nullable String data, @Nullable Exception error, boolean fromCache) {
        if (callback == null) {
            return;
        }
        try {
            callback.onResult(data, error, fromCache);
        } catch (Throwable ignored) {
        }
    }
}
