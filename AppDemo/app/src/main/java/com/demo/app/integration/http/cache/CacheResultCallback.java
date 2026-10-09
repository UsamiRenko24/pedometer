package com.demo.app.integration.http.cache;

import androidx.annotation.Nullable;

public interface CacheResultCallback {
    void onResult(@Nullable String result, @Nullable Exception error, boolean fromCache) throws Throwable;
}
