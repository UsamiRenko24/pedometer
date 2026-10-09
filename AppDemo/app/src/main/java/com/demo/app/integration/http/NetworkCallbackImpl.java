package com.demo.app.integration.http;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/** 把 OkHttp 回调收成一段结果字符串。 */
public final class NetworkCallbackImpl implements Callback {

    @Nullable
    private final NetworkResultCallback callback;

    public NetworkCallbackImpl(@Nullable NetworkResultCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception) {
        if (callback == null) {
            return;
        }
        try {
            callback.onResult(null, exception);
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) {
        if (callback == null) {
            response.close();
            return;
        }
        try (Response ignored = response) {
            if (!response.isSuccessful() || response.body() == null) {
                callback.onResult(null, new Exception("Network request failed: " + response.code()));
                return;
            }
            callback.onResult(response.body().string(), null);
        } catch (Throwable throwable) {
            try {
                Exception exception = throwable instanceof Exception
                        ? (Exception) throwable
                        : new Exception(throwable);
                callback.onResult(null, exception);
            } catch (Throwable ignored) {
            }
        }
    }
}
