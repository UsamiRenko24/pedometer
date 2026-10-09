package com.demo.app.integration.http.cache;

import androidx.annotation.Nullable;

public interface CacheValidator {
    boolean shouldCache(@Nullable String data);
}
