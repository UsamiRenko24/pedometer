package com.demo.app.integration.image;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.HttpException;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/** 用 OkHttp 拉图片，带上浏览器请求头，避免图床把默认客户端直接断开。 */
final class OkHttpStreamFetcher implements DataFetcher<InputStream> {

    private final Call.Factory client;
    private final GlideUrl url;
    private volatile Call call;
    private ResponseBody body;
    private InputStream stream;

    OkHttpStreamFetcher(@NonNull Call.Factory client, @NonNull GlideUrl url) {
        this.client = client;
        this.url = url;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        Request.Builder builder = new Request.Builder().url(url.toStringUrl());
        for (Map.Entry<String, String> header : url.getHeaders().entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }
        call = client.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException error) {
                callback.onLoadFailed(error);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                body = response.body();
                if (!response.isSuccessful() || body == null) {
                    callback.onLoadFailed(new HttpException(response.message(), response.code()));
                    return;
                }
                stream = body.byteStream();
                callback.onDataReady(stream);
            }
        });
    }

    @Override
    public void cleanup() {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
        if (body != null) {
            body.close();
        }
    }

    @Override
    public void cancel() {
        Call current = call;
        if (current != null) {
            current.cancel();
        }
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }
}
