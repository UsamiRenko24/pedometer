package com.demo.app.launcher.ui.activity.wallpaper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.demo.app.R;
import com.demo.app.databinding.ActivityWallpaperPreviewBinding;
import com.demo.app.launcher.ads.LauncherBottomBannerPlaceholderView;
import com.demo.app.launcher.ui.adapter.wallpaper.WallpaperPreviewPagerAdapter;
import com.demo.app.launcher.utils.WallpaperDataUtils;
import com.demo.app.launcher.wallpaper.WallpaperActions;
import com.demo.app.launcher.wallpaper.WallpaperSession;
import com.demo.app.launcher.wallpaper.model.WallpaperDetailUi;
import com.demo.app.launcher.wallpaper.model.WallpaperItemUi;
import com.demo.app.launcher.wallpaper.model.WallpaperListUi;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kotlin.Unit;

/** 壁纸详情，支持左右滑动切换同列表壁纸。 */
public class WallpaperPreviewActivity extends BaseActivity<ActivityWallpaperPreviewBinding> {

    private static final String EXTRA_ID = "wallpaper_id";
    private static final String EXTRA_THUMB = "wallpaper_thumb";
    private static final int LOAD_MORE_THRESHOLD = 3;

    private String wallpaperId = "";
    private String thumbUrl = "";
    private String imageUrl = "";
    private String downloadUrl = "";
    private String creditLink = "";
    private boolean working;

    private final List<WallpaperItemUi> browseItems = new ArrayList<>();
    private final Map<String, WallpaperDetailUi> detailCache = new HashMap<>();
    private int currentIndex;
    @Nullable
    private WallpaperSession.BrowseContext browseContext;
    @Nullable
    private WallpaperPreviewPagerAdapter pagerAdapter;

    public static void start(@NonNull Context context, @NonNull String id, @NonNull String thumbUrl) {
        Intent intent = new Intent(context, WallpaperPreviewActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_THUMB, thumbUrl);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityWallpaperPreviewBinding getViewBinding() {
        return ActivityWallpaperPreviewBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initData(@Nullable android.os.Bundle bundle) {
        if (bundle == null) {
            bundle = getIntent().getExtras();
        }
        if (bundle != null) {
            wallpaperId = bundle.getString(EXTRA_ID, "");
            thumbUrl = bundle.getString(EXTRA_THUMB, "");
        }
        imageUrl = thumbUrl;
        downloadUrl = thumbUrl;
        browseContext = WallpaperSession.INSTANCE.getBrowseContext();
        initBrowseItems();
        currentIndex = resolveInitialIndex();
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.wallpaperPreviewRoot, (v, insets) -> {
            Insets bars = insets.getInsets(
                    SystemUiHelper.contentInsetTypes());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow(), true, false);
    }

    @Override
    protected void initViews() {
        setupPager();
        LauncherBottomBannerPlaceholderView banner = binding.wallpaperBottomBannerView;
        if (banner != null) {
            banner.refreshVisibility();
        }
    }

    @Override
    protected void initEvents() {
        binding.wallpaperPreviewBack.setOnClickListener(v -> finish());
        binding.wallpaperPreviewOpen.setOnClickListener(v -> openDesktopPreview());
        binding.wallpaperPreviewSetting.setOnClickListener(v -> openSetting());
        binding.wallpaperPreviewDownload.setOnClickListener(v -> downloadWallpaper());
        binding.wallpaperPreviewCredit.setOnClickListener(v -> openCredit());
    }

    @Override
    protected void loadData() {
        // 详情由 Pager 按页加载。
    }

    private void initBrowseItems() {
        WallpaperSession.BrowseContext context = browseContext;
        if (context != null && !context.getItems().isEmpty()) {
            browseItems.addAll(context.getItems());
            return;
        }
        browseItems.add(new WallpaperItemUi(wallpaperId, "", "", thumbUrl, 0, 0));
    }

    private int resolveInitialIndex() {
        if (!wallpaperId.isEmpty()) {
            for (int i = 0; i < browseItems.size(); i++) {
                if (wallpaperId.equals(browseItems.get(i).getId())) {
                    return i;
                }
            }
        }
        if (browseContext != null) {
            return browseContext.getInitialIndex();
        }
        return 0;
    }

    private void setupPager() {
        pagerAdapter = new WallpaperPreviewPagerAdapter(browseItems, detailCache, this::onDetailLoaded);
        binding.wallpaperPreviewPager.setAdapter(pagerAdapter);
        binding.wallpaperPreviewPager.setOffscreenPageLimit(1);
        binding.wallpaperPreviewPager.registerOnPageChangeCallback(pageChangeCallback);
        if (currentIndex > 0) {
            binding.wallpaperPreviewPager.setCurrentItem(currentIndex, false);
        }
        handlePageSelected(currentIndex);
        preloadAdjacent(currentIndex);
    }

    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            currentIndex = position;
            handlePageSelected(position);
        }
    };

    private void handlePageSelected(int position) {
        if (position < 0 || position >= browseItems.size()) {
            return;
        }
        WallpaperItemUi item = browseItems.get(position);
        wallpaperId = item.getId();
        WallpaperDetailUi cached = detailCache.get(wallpaperId);
        if (cached != null) {
            applyDetail(cached);
        } else {
            imageUrl = item.getThumbUrl();
            downloadUrl = item.getThumbUrl();
            creditLink = "";
            binding.wallpaperPreviewCredit.setVisibility(View.GONE);
        }
        preloadAdjacent(position);
        maybeLoadMore(position);
    }

    private void onDetailLoaded(int position, @NonNull WallpaperDetailUi detail) {
        if (isFinishing() || position != currentIndex) {
            return;
        }
        applyDetail(detail);
    }

    private void applyDetail(@NonNull WallpaperDetailUi detail) {
        if (!detail.getImageUrl().isEmpty()) {
            imageUrl = detail.getImageUrl();
        }
        if (!detail.getDownloadUrl().isEmpty()) {
            downloadUrl = detail.getDownloadUrl();
        } else if (!detail.getImageUrl().isEmpty()) {
            downloadUrl = detail.getImageUrl();
        }
        creditLink = detail.getLink();
        String credit = creditText(detail.getSource(), detail.getAuthor());
        if (credit.isEmpty()) {
            binding.wallpaperPreviewCredit.setVisibility(View.GONE);
        } else {
            binding.wallpaperPreviewCreditText.setText(credit);
            binding.wallpaperPreviewCredit.setVisibility(View.VISIBLE);
        }
    }

    private void preloadAdjacent(int position) {
        if (pagerAdapter == null) {
            return;
        }
        for (int offset = -2; offset <= 2; offset++) {
            if (offset == 0) {
                continue;
            }
            pagerAdapter.preload(position + offset);
        }
    }

    private void maybeLoadMore(int position) {
        WallpaperSession.BrowseContext context = browseContext;
        if (context == null || context.getCategoryId().isEmpty() || !context.getHasMore()
                || context.isLoadingMore()) {
            return;
        }
        if (position < browseItems.size() - LOAD_MORE_THRESHOLD) {
            return;
        }
        context.setLoadingMore(true);
        int nextPage = context.getPage() + 1;
        WallpaperDataUtils.INSTANCE.loadWallpapers(
                context.getCategoryId(), nextPage, 20, false, (listUi, error) -> {
                    onBrowseLoadMoreResult(context, listUi, error);
                    return Unit.INSTANCE;
                });
    }

    private void onBrowseLoadMoreResult(
            @NonNull WallpaperSession.BrowseContext context,
            @NonNull WallpaperListUi listUi,
            @Nullable Throwable error) {
        context.setLoadingMore(false);
        if (isFinishing()) {
            return;
        }
        if (error != null || listUi.getItems().isEmpty()) {
            if (listUi.getItems().isEmpty()) {
                context.setHasMore(false);
            }
            return;
        }
        if (!listUi.getAssetBaseUrl().isEmpty()) {
            WallpaperSession.INSTANCE.updateAssetBaseUrl(listUi.getAssetBaseUrl());
        }
        context.setPage(listUi.getPage());
        context.setHasMore(listUi.getHasMore());
        int start = browseItems.size();
        List<WallpaperItemUi> newItems = new ArrayList<>();
        Set<String> existing = new HashSet<>();
        for (WallpaperItemUi item : browseItems) {
            existing.add(item.getId());
        }
        for (WallpaperItemUi item : listUi.getItems()) {
            if (!existing.contains(item.getId())) {
                newItems.add(item);
                existing.add(item.getId());
            }
        }
        if (newItems.isEmpty()) {
            return;
        }
        browseItems.addAll(newItems);
        context.getItems().addAll(newItems);
        if (pagerAdapter != null) {
            pagerAdapter.notifyItemRangeInserted(start, newItems.size());
        }
        preloadAdjacent(binding.wallpaperPreviewPager.getCurrentItem());
    }

    @NonNull
    private String creditText(@NonNull String source, @NonNull String author) {
        if (source.isEmpty() && author.isEmpty()) {
            return "";
        }
        if (author.isEmpty()) {
            return getString(R.string.wallpaper_credit_source, source);
        }
        if (source.isEmpty()) {
            return author;
        }
        return getString(R.string.wallpaper_credit, source, author);
    }

    private void openDesktopPreview() {
        if (imageUrl.isEmpty()) {
            return;
        }
        WallpaperDesktopPreviewDialog.open(getSupportFragmentManager(), imageUrl);
    }

    private void openSetting() {
        if (imageUrl.isEmpty()) {
            return;
        }
        WallpaperSettingSheet.open(getSupportFragmentManager(), imageUrl);
    }

    private void downloadWallpaper() {
        if (working || downloadUrl.isEmpty()) {
            return;
        }
        working = true;
        String name = "wallpaper_" + (wallpaperId.isEmpty() ? System.currentTimeMillis() : wallpaperId) + ".jpg";
        WallpaperActions.download(this, downloadUrl, name, success -> {
            working = false;
            if (isFinishing()) {
                return;
            }
            Toast.makeText(this,
                    success ? R.string.wallpaper_download_done : R.string.wallpaper_download_failed,
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void openCredit() {
        if (creditLink.isEmpty()) {
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(creditLink)));
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        if (binding != null) {
            binding.wallpaperPreviewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        super.onDestroy();
    }
}
