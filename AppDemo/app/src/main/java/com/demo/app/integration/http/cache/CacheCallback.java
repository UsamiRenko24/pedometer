package com.demo.app.integration.http.cache;

import androidx.annotation.NonNull;

public interface CacheCallback {
    void onCacheSuccess(@NonNull String data);

    void onNetworkSuccess(@NonNull String data);

    void onFailure(@NonNull Exception exception);
}
