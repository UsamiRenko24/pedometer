package com.demo.app.integration.http;

import androidx.annotation.Nullable;

public interface HttpCallback {
    void onResult(@Nullable String result, @Nullable Throwable error) throws Throwable;
}
