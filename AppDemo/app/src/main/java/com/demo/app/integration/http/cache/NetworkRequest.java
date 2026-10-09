package com.demo.app.integration.http.cache;

import androidx.annotation.NonNull;

import okhttp3.Callback;

public interface NetworkRequest {
    void execute(@NonNull Callback callback);
}
