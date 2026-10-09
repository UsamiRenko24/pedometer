package com.demo.app.launcher.ui.activity.news;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
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
import com.demo.app.databinding.ActivityNewsDetailBinding;
import com.demo.app.launcher.news.NewsArticle;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;
import com.google.gson.Gson;

/** 新闻详情：顶栏返回与标题同行；有 link 时用 WebView 展示正文。 */
public class NewsDetailActivity extends BaseActivity<ActivityNewsDetailBinding> {

    private static final String EXTRA_ARTICLE = "news_article";

    public static void start(@NonNull Context context, @NonNull NewsArticle article) {
        Intent intent = new Intent(context, NewsDetailActivity.class);
        intent.putExtra(EXTRA_ARTICLE, new Gson().toJson(article));
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityNewsDetailBinding getViewBinding() {
        return ActivityNewsDetailBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.newsDetailRoot, (v, insets) -> {
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
        binding.newsDetailTitle.setText(article.title);
        String source = article.source_name == null ? "" : article.source_name;
        String time = article.pub_date == null ? "" : article.pub_date;
        binding.newsDetailMeta.setText(source.isEmpty() ? time : time.isEmpty() ? source : source + " · " + time);

        String link = article.link == null ? "" : article.link.trim();
        if (!link.isEmpty()) {
            binding.newsDetailScroll.setVisibility(View.GONE);
            binding.newsDetailWeb.setVisibility(View.VISIBLE);
            setupWebView(link);
            return;
        }
        binding.newsDetailWeb.setVisibility(View.GONE);
        binding.newsDetailScroll.setVisibility(View.VISIBLE);
        bindNativeBody(article);
    }

    private void bindNativeBody(@NonNull NewsArticle article) {
        CharSequence body = formatDescription(article.description);
        binding.newsDetailBody.setText(body);
        binding.newsDetailImage.setScaleType(ImageView.ScaleType.CENTER);
        Glide.with(this)
                .load(article.imageUrl())
                .placeholder(R.drawable.icon_home_app_news_detail_img)
                .error(R.drawable.icon_home_app_news_detail_img)
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
                        binding.newsDetailImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        return false;
                    }
                })
                .into(binding.newsDetailImage);
    }

    private void setupWebView(@NonNull String url) {
        WebView webView = binding.newsDetailWeb;
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 100) {
                    binding.newsDetailWebProgress.setVisibility(View.GONE);
                } else {
                    binding.newsDetailWebProgress.setVisibility(View.VISIBLE);
                    binding.newsDetailWebProgress.setProgress(newProgress);
                }
            }
        });
        binding.newsDetailWebProgress.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
    }

    @NonNull
    private CharSequence formatDescription(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return getString(R.string.news_detail_body_empty);
        }
        String trimmed = raw.trim();
        if (trimmed.contains("<") && trimmed.contains(">")) {
            return HtmlCompat.fromHtml(trimmed, HtmlCompat.FROM_HTML_MODE_LEGACY);
        }
        return trimmed;
    }

    @Override
    protected void initEvents() {
        binding.newsDetailBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        WebView webView = binding.newsDetailWeb;
        if (webView.getVisibility() == View.VISIBLE && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        WebView webView = binding.newsDetailWeb;
        webView.stopLoading();
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.destroy();
        super.onDestroy();
    }
}
