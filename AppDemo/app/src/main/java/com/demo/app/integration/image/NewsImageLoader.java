package com.demo.app.integration.image;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/** 新闻图床会拒绝 Glide 默认的 HttpURLConnection，改成带浏览器头的 OkHttp。 */
public final class NewsImageLoader {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";

    private NewsImageLoader() {
    }

    public static void install(@NonNull Context context) {
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .header("User-Agent", USER_AGENT)
                            .header("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
                            .build();
                    return chain.proceed(request);
                })
                .build();
        Glide.get(context).getRegistry().replace(
                GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }
}
