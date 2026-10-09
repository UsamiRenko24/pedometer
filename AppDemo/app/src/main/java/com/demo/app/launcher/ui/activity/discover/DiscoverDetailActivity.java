package com.demo.app.launcher.ui.activity.discover;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.demo.app.R;
import com.demo.app.databinding.ActivityDiscoverDetailBinding;
import com.demo.app.launcher.gamecenter.model.GameCenterConfig;
import com.demo.app.launcher.gamecenter.model.GameCountFormatter;
import com.demo.app.launcher.gamecenter.model.GameDetailUi;
import com.demo.app.launcher.utils.GameCenterDataUtils;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;

/** 游戏或应用详情。 */
public class DiscoverDetailActivity extends BaseActivity<ActivityDiscoverDetailBinding> {

    private static final String EXTRA_BID = "bid";
    private static final Pattern FEATURES = Pattern.compile("Features\\s*:", Pattern.CASE_INSENSITIVE);

    private String bid = "";
    private String storeLink = "";
    private boolean loading;

    public static void start(@NonNull Context context, @NonNull String bid) {
        Intent intent = new Intent(context, DiscoverDetailActivity.class);
        intent.putExtra(EXTRA_BID, bid);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityDiscoverDetailBinding getViewBinding() {
        return ActivityDiscoverDetailBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initData(@Nullable Bundle bundle) {
        if (bundle != null) {
            bid = bundle.getString(EXTRA_BID, "");
        }
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.discoverDetailRoot, (v, insets) -> {
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
    }

    @Override
    protected void initEvents() {
        binding.discoverDetailBack.setOnClickListener(v -> finish());
        binding.discoverDetailError.setOnClickListener(v -> loadData());
        binding.discoverDetailPlay.setOnClickListener(v -> openStore());
    }

    @Override
    protected void loadData() {
        if (loading) {
            return;
        }
        loading = true;
        binding.discoverDetailLoading.setVisibility(View.VISIBLE);
        binding.discoverDetailError.setVisibility(View.GONE);
        binding.discoverDetailScroll.setVisibility(View.GONE);
        GameCenterDataUtils.INSTANCE.loadDetail(bid, new GameCenterConfig("com.rwapp.fm.tvremote", 2),
                (detail, error) -> {
                    loading = false;
                    if (isFinishing()) {
                        return Unit.INSTANCE;
                    }
                    binding.discoverDetailLoading.setVisibility(View.GONE);
                    if (error != null || detail == null) {
                        binding.discoverDetailError.setVisibility(View.VISIBLE);
                        return Unit.INSTANCE;
                    }
                    bind(detail);
                    binding.discoverDetailScroll.setVisibility(View.VISIBLE);
                    return Unit.INSTANCE;
                });
    }

    private void bind(@NonNull GameDetailUi detail) {
        if (binding.gameDetailNativeBottom != null) {
            binding.gameDetailNativeBottom.refreshVisibility();
        }
        String title = detail.getDisplayTitle().isEmpty() ? detail.getName() : detail.getDisplayTitle();
        binding.discoverDetailToolbarTitle.setText(title);
        binding.discoverDetailName.setText(title);
        binding.discoverDetailSummary.setText(detail.getDisplayDesc());
        storeLink = detail.getLink();
        if (storeLink.isEmpty() && !detail.getBid().isEmpty()) {
            storeLink = "https://play.google.com/store/apps/details?id=" + detail.getBid();
        }
        String icon = detail.getIconUrl().isEmpty() ? detail.getImageUrl() : detail.getIconUrl();
        Glide.with(binding.discoverDetailIcon).load(icon).centerCrop().into(binding.discoverDetailIcon);
        binding.discoverDetailIcon.setClipToOutline(true);
        String banner = detail.getBannerUrl().isEmpty() ? detail.getImageUrl() : detail.getBannerUrl();
        Glide.with(binding.discoverDetailBanner).load(banner).centerCrop().into(binding.discoverDetailBanner);
        binding.discoverDetailBanner.setClipToOutline(true);

        String rating = detail.getRating() <= 0
                ? ""
                : String.format(Locale.US, "%.1f", detail.getRating());
        binding.discoverDetailRating.setText(rating);
        binding.discoverDetailStatRating.setText(rating.isEmpty() ? "—" : rating);
        tintStars(detail.getRating());
        binding.discoverDetailStatDownload.setText(
                detail.getDownload() <= 0 ? "—" : GameCountFormatter.INSTANCE.formatCompact(detail.getDownload()));
        binding.discoverDetailStatVolume.setText(volumeText(detail.getRatingCount()));
        binding.discoverDetailDesc.setText(prose(detail));
        bindFeatures(detail);
    }

    private void tintStars(double rating) {
        LinearLayout stars = binding.discoverDetailStars;
        int index = 0;
        for (int i = 0; i < stars.getChildCount(); i++) {
            View child = stars.getChildAt(i);
            if (!(child instanceof ImageView)) {
                continue;
            }
            int color = index < Math.round(rating) ? getColor(R.color.color_FFAE00) : Color.parseColor("#E5E5EA");
            ((ImageView) child).setImageTintList(ColorStateList.valueOf(color));
            index++;
        }
    }

    @NonNull
    private String volumeText(long count) {
        if (count <= 0) {
            return "—";
        }
        if (count >= 1000) {
            return GameCountFormatter.INSTANCE.formatCompact(count) + "+";
        }
        return String.valueOf(count);
    }

    @NonNull
    private String prose(@NonNull GameDetailUi detail) {
        String text = detail.getDisplayLongDesc().trim();
        if (text.isEmpty()) {
            text = detail.getDisplayDesc().trim();
        }
        Matcher matcher = FEATURES.matcher(text);
        if (matcher.find()) {
            text = text.substring(0, matcher.start()).trim();
        }
        return text.isEmpty() ? detail.getDisplayDesc() : text;
    }

    private void bindFeatures(@NonNull GameDetailUi detail) {
        binding.discoverDetailFeatures.removeAllViews();
        if (detail.getFeatures().isEmpty()) {
            binding.discoverDetailFeaturesTitle.setVisibility(View.GONE);
            binding.discoverDetailFeatures.setVisibility(View.GONE);
            return;
        }
        binding.discoverDetailFeaturesTitle.setVisibility(View.VISIBLE);
        binding.discoverDetailFeatures.setVisibility(View.VISIBLE);
        for (String feature : detail.getFeatures()) {
            TextView line = new TextView(this);
            line.setText(getString(R.string.discover_detail_bullet, feature));
            line.setTextColor(Color.parseColor("#3A3A3C"));
            line.setTextSize(13);
            line.setPadding(0, dp(4), 0, dp(4));
            binding.discoverDetailFeatures.addView(line);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void openStore() {
        if (storeLink.isEmpty()) {
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(storeLink)));
        } catch (Exception ignored) {
        }
    }
}
