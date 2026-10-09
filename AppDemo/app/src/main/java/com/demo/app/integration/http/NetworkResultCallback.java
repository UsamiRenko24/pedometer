package com.demo.app.integration.http;

import androidx.annotation.Nullable;

public interface NetworkResultCallback {
    void onResult(@Nullable String result, @Nullable Exception error) throws Throwable;
}
