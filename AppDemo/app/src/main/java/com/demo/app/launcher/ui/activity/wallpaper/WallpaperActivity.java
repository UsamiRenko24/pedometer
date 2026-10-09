package com.demo.app.launcher.ui.activity.wallpaper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.databinding.ActivityWallpaperBinding;
import com.demo.app.launcher.ads.LauncherBackInsertPlaceholder;
import com.demo.app.launcher.ads.LauncherBackPressAds;
import com.demo.app.launcher.ads.SplashFeedPlaceholder;
import com.demo.app.launcher.utils.WallpaperDataUtils;
import com.demo.app.launcher.wallpaper.WallpaperCategories;
import com.demo.app.launcher.wallpaper.WallpaperSession;
import com.demo.app.launcher.wallpaper.model.CategoryUi;
import com.demo.app.launcher.wallpaper.model.WallpaperItemUi;
import com.demo.app.launcher.wallpaper.model.WallpaperListUi;
import com.demo.app.launcher.ui.adapter.wallpaper.WallpaperCategoryAdapter;
import com.demo.app.launcher.ui.adapter.wallpaper.WallpaperThumbAdapter;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

/** 壁纸网格，左上角菜单打开分类。 */
public class WallpaperActivity extends BaseActivity<ActivityWallpaperBinding> {

    /** 网格列数（一行几张缩略图）。 */
    private static final int GRID_SPAN_COUNT = 3;
    /** 进入分类后至少拉取到第几页接口数据（每页默认 20 条，见 WallpaperDataUtils）。 */
    private static final int INITIAL_API_PAGE_GOAL = 3;

    private final List<CategoryUi> categories = new ArrayList<>();
    private final List<WallpaperItemUi> wallpapers = new ArrayList<>();
    private final WallpaperCategoryAdapter categoryAdapter = new WallpaperCategoryAdapter(categories, this::onCategory);
    private final WallpaperThumbAdapter thumbAdapter = new WallpaperThumbAdapter(wallpapers, this::openPreview);

    private int selected;
    private int page = 1;
    private boolean hasMore;
    private boolean loading;
    private boolean drawerOpen;
    private int requestToken;

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, WallpaperActivity.class);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityWallpaperBinding getViewBinding() {
        return ActivityWallpaperBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.wallpaperRoot, (v, insets) -> {
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
        binding.wallpaperGrid.setLayoutManager(new GridLayoutManager(this, GRID_SPAN_COUNT));
        binding.wallpaperGrid.setAdapter(thumbAdapter);
        binding.wallpaperDrawer.setLayoutManager(new LinearLayoutManager(this));
        binding.wallpaperDrawer.setAdapter(categoryAdapter);
        binding.wallpaperRefresh.setColorSchemeColors(Color.parseColor("#2C8FFF"));
        binding.wallpaperRefresh.setOnChildScrollUpCallback((parent, child) ->
                binding.wallpaperGrid.getVisibility() == View.VISIBLE
                        && binding.wallpaperGrid.canScrollVertically(-1));
    }

    @Override
    protected void initEvents() {
        LauncherBackInsertPlaceholder.preload(this);
        LauncherBackPressAds.install(this, this::finish);
        binding.wallpaperMenu.setOnClickListener(v -> setDrawer(true));
        binding.wallpaperScrim.setOnClickListener(v -> setDrawer(false));
        binding.wallpaperRefresh.setOnRefreshListener(this::reload);
        binding.wallpaperNetworkError.setOnClickListener(v -> reload());
        binding.wallpaperEmpty.setOnClickListener(v -> reload());
        binding.wallpaperGrid.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0 || loading || !hasMore || categories.isEmpty()) {
                    return;
                }
                GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (manager == null) {
                    return;
                }
                int last = manager.findLastVisibleItemPosition();
                if (wallpapers.size() - last <= GRID_SPAN_COUNT * 2) {
                    loadPage(page + 1, true);
                }
            }
        });
    }

    /** 首屏条目不足一屏时不会触发滚动，需主动补拉下一页。 */
    private void maybePrefetchNextPage() {
        if (loading || !hasMore || categories.isEmpty()) {
            return;
        }
        binding.wallpaperGrid.post(() -> {
            if (loading || !hasMore || isFinishing()) {
                return;
            }
            GridLayoutManager manager = (GridLayoutManager) binding.wallpaperGrid.getLayoutManager();
            if (manager == null) {
                return;
            }
            int last = manager.findLastVisibleItemPosition();
            int prefetchLead = GRID_SPAN_COUNT * 2;
            boolean nearEnd = last >= wallpapers.size() - prefetchLead;
            boolean cannotScroll = !binding.wallpaperGrid.canScrollVertically(1);
            boolean belowPageGoal = page < INITIAL_API_PAGE_GOAL;
            if (belowPageGoal || nearEnd || cannotScroll) {
                loadPage(page + 1, true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerOpen) {
            setDrawer(false);
            return;
        }
        getOnBackPressedDispatcher().onBackPressed();
    }

    @Override
    protected void loadData() {
        reload();
    }

    private void reload() {
        if (loading) {
            binding.wallpaperRefresh.setRefreshing(false);
            return;
        }
        if (categories.isEmpty()) {
            loadCategories();
        } else {
            loadPage(1, false);
        }
    }

    private void loadCategories() {
        if (wallpapers.isEmpty()) {
            showBlockingLoad();
        } else {
            showRefreshLoad();
        }
        WallpaperDataUtils.INSTANCE.loadCategories((categoriesResult, error) -> {
            if (isFinishing()) {
                return Unit.INSTANCE;
            }
            if (error != null && (categoriesResult == null || categoriesResult.isEmpty())) {
                finishRequest();
                if (wallpapers.isEmpty()) {
                    showNetworkError();
                } else {
                    toastLoadFailed();
                }
                return Unit.INSTANCE;
            }
            categories.clear();
            if (categoriesResult != null) {
                categories.addAll(categoriesResult);
            }
            categoryAdapter.notifyDataSetChanged();
            if (categories.isEmpty()) {
                finishRequest();
                wallpapers.clear();
                thumbAdapter.notifyDataSetChanged();
                showEmpty();
                return Unit.INSTANCE;
            }
            if (selected >= categories.size()) {
                selected = 0;
            }
            categoryAdapter.setSelected(selected);
            loadPage(1, false);
            return Unit.INSTANCE;
        });
    }

    private void loadPage(int nextPage, boolean append) {
        if (categories.isEmpty()) {
            finishRequest();
            showEmpty();
            return;
        }
        if (append) {
            loading = true;
        } else if (wallpapers.isEmpty()) {
            showBlockingLoad();
        } else {
            showRefreshLoad();
        }
        int token = ++requestToken;
        String categoryId = categories.get(selected).getId();
        WallpaperDataUtils.INSTANCE.loadWallpapers(categoryId, nextPage, 20, nextPage == 1,
                (list, error) -> {
                    if (token != requestToken || isFinishing()) {
                        return Unit.INSTANCE;
                    }
                    finishRequest();
                    if (append) {
                        applyNextPage(list, error, nextPage);
                    } else {
                        applyFirstPage(list, error);
                    }
                    return Unit.INSTANCE;
                });
    }

    private void applyNextPage(@Nullable WallpaperListUi list, @Nullable Throwable error, int nextPage) {
        if (error != null || list == null) {
            toastLoadFailed();
            return;
        }
        if (list.getItems().isEmpty()) {
            hasMore = false;
            return;
        }
        page = nextPage;
        hasMore = list.getHasMore();
        rememberBaseUrl(list);
        wallpapers.addAll(list.getItems());
        thumbAdapter.notifyDataSetChanged();
        showContent();
        maybePrefetchNextPage();
    }

    private void applyFirstPage(@Nullable WallpaperListUi list, @Nullable Throwable error) {
        if (error != null || list == null) {
            if (wallpapers.isEmpty()) {
                showNetworkError();
            } else {
                toastLoadFailed();
            }
            return;
        }
        if (list.getItems().isEmpty()) {
            wallpapers.clear();
            thumbAdapter.notifyDataSetChanged();
            page = 1;
            hasMore = false;
            showEmpty();
            return;
        }
        page = 1;
        hasMore = list.getHasMore();
        rememberBaseUrl(list);
        wallpapers.clear();
        wallpapers.addAll(list.getItems());
        thumbAdapter.notifyDataSetChanged();
        showContent();
        maybePrefetchNextPage();
    }

    private void rememberBaseUrl(@NonNull WallpaperListUi list) {
        if (!list.getAssetBaseUrl().isEmpty()) {
            WallpaperSession.INSTANCE.updateAssetBaseUrl(list.getAssetBaseUrl());
        }
    }

    private void finishRequest() {
        loading = false;
        binding.wallpaperRefresh.setRefreshing(false);
    }

    private void toastLoadFailed() {
        Toast.makeText(this, R.string.wallpaper_refresh_failed, Toast.LENGTH_SHORT).show();
    }

    private void openPreview(int index) {
        WallpaperItemUi item = wallpapers.get(index);
        String categoryId = categories.isEmpty() ? item.getCategoryId() : categories.get(selected).getId();
        WallpaperSession.INSTANCE.setBrowseContext(
                new ArrayList<>(wallpapers), index, categoryId, page, hasMore);
        SplashFeedPlaceholder.run(this, () ->
                WallpaperPreviewActivity.start(this, item.getId(), item.getThumbUrl()));
    }

    private void setDrawer(boolean open) {
        drawerOpen = open;
        binding.wallpaperScrim.setVisibility(open ? View.VISIBLE : View.GONE);
        binding.wallpaperDrawer.setVisibility(open ? View.VISIBLE : View.GONE);
    }

    private void showBlockingLoad() {
        loading = true;
        binding.wallpaperRefresh.setRefreshing(false);
        binding.wallpaperLoading.setVisibility(View.VISIBLE);
        binding.wallpaperNetworkError.setVisibility(View.GONE);
        binding.wallpaperEmpty.setVisibility(View.GONE);
        binding.wallpaperGrid.setVisibility(View.GONE);
    }

    private void showRefreshLoad() {
        loading = true;
        binding.wallpaperRefresh.setRefreshing(true);
    }

    private void showContent() {
        binding.wallpaperLoading.setVisibility(View.GONE);
        binding.wallpaperNetworkError.setVisibility(View.GONE);
        binding.wallpaperEmpty.setVisibility(View.GONE);
        binding.wallpaperGrid.setVisibility(View.VISIBLE);
    }

    private void showNetworkError() {
        setDrawer(false);
        binding.wallpaperLoading.setVisibility(View.GONE);
        binding.wallpaperGrid.setVisibility(View.GONE);
        binding.wallpaperEmpty.setVisibility(View.GONE);
        binding.wallpaperNetworkError.setVisibility(View.VISIBLE);
    }

    private void showEmpty() {
        binding.wallpaperLoading.setVisibility(View.GONE);
        binding.wallpaperGrid.setVisibility(View.GONE);
        binding.wallpaperNetworkError.setVisibility(View.GONE);
        binding.wallpaperEmpty.setVisibility(View.VISIBLE);
    }

    private void onCategory(int position) {
        if (selected == position) {
            setDrawer(false);
            return;
        }
        int previous = selected;
        selected = position;
        categoryAdapter.setSelected(selected);
        categoryAdapter.notifyItemChanged(previous);
        categoryAdapter.notifyItemChanged(selected);
        setDrawer(false);
        wallpapers.clear();
        thumbAdapter.notifyDataSetChanged();
        page = 1;
        hasMore = false;
        loadPage(1, false);
    }
}
