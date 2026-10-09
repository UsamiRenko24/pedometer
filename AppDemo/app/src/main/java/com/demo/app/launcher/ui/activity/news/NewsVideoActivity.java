package com.demo.app.launcher.ui.activity.news;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.demo.app.R;
import com.demo.app.databinding.ActivityNewsVideoBinding;
import com.demo.app.launcher.news.NewsArticle;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;
import com.google.gson.Gson;

/** 短视频样式。画面出来之前是灰色底加加载图。 */
public class NewsVideoActivity extends BaseActivity<ActivityNewsVideoBinding> {

    private static final String EXTRA_ARTICLE = "news_article";

    private boolean muted = true;

    public static void start(@NonNull Context context, @NonNull NewsArticle article) {
        Intent intent = new Intent(context, NewsVideoActivity.class);
        intent.putExtra(EXTRA_ARTICLE, new Gson().toJson(article));
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityNewsVideoBinding getViewBinding() {
        return ActivityNewsVideoBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.newsVideoRoot, (v, insets) -> {
            Insets bars = insets.getInsets(
                    SystemUiHelper.contentInsetTypes());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow(), true, true);
    }

    @Override
    protected void initViews() {
        String json = getIntent().getStringExtra(EXTRA_ARTICLE);
        NewsArticle article = json == null ? null : new Gson().fromJson(json, NewsArticle.class);
        if (article == null) {
            finish();
            return;
        }
        binding.newsVideoSource.setText(article.source_name);
        binding.newsVideoTitle.setText(article.title);
        applyMute();
        Glide.with(this)
                .load(article.imageUrl())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                @NonNull Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model,
                                                   Target<Drawable> target, @NonNull DataSource dataSource,
                                                   boolean isFirstResource) {
                        binding.newsVideoLoading.setVisibility(View.GONE);
                        binding.newsVideoImage.setVisibility(View.VISIBLE);
                        binding.newsVideoCaption.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(binding.newsVideoImage);
    }

    @Override
    protected void initEvents() {
        binding.newsVideoBack.setOnClickListener(v -> finish());
        binding.newsVideoSound.setOnClickListener(v -> {
            muted = false;
            applyMute();
        });
        binding.newsVideoMute.setOnClickListener(v -> {
            muted = true;
            applyMute();
        });
    }

    private void applyMute() {
        binding.newsVideoSound.setImageResource(muted
                ? R.drawable.icon_home_app_news_volume
                : R.drawable.icon_home_app_news_volume);
        binding.newsVideoSound.setAlpha(muted ? 0.35f : 1f);
        binding.newsVideoMute.setAlpha(muted ? 1f : 0.35f);
    }
}
