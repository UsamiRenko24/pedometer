package com.demo.app.launcher.ui.fragment.discover;



import android.os.Bundle;

import android.view.LayoutInflater;

import android.view.View;

import android.view.ViewGroup;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;



import com.demo.app.R;

import com.demo.app.databinding.FragmentDiscoverFeedBinding;

import com.demo.app.launcher.gamecenter.GameCenterSession;

import com.demo.app.launcher.gamecenter.model.GameCenterConfig;

import com.demo.app.launcher.gamecenter.model.GameCenterUiState;

import com.demo.app.launcher.gamecenter.model.GameItemUi;

import com.demo.app.launcher.gamecenter.model.GameSectionUi;

import com.demo.app.launcher.gamecenter.model.SectionDisplayStyle;

import com.demo.app.launcher.gamecenter.model.SectionDisplayStyleResolver;

import com.demo.app.launcher.ui.adapter.discover.DiscoverFeedAdapter;

import com.demo.app.launcher.ui.adapter.discover.DiscoverFeedRow;

import com.demo.app.launcher.utils.GameCenterDataUtils;



import java.util.ArrayList;

import java.util.List;



import kotlin.Unit;



/** 底部 Explore / Apps / Games 三个 Tab 的内容页。 */

public class DiscoverFeedFragment extends Fragment implements DiscoverFeedAdapter.Host {



    private static final String ARG_CATEGORY = "category_id";

    /** Explore：category_id=0，轮播下双芯片切换游戏 / 应用内容。 */

    private static final int CATEGORY_EXPLORE = DiscoverFeedAdapter.CATEGORY_EXPLORE;

    /** Apps：category_id=1。 */

    private static final int CATEGORY_APPS = 1;

    /** Games：category_id=2，保留分类芯片 + 下方灵活分组。 */

    private static final int CATEGORY_GAMES = 2;



    private FragmentDiscoverFeedBinding binding;

    private final List<DiscoverFeedRow> rows = new ArrayList<>();

    private final DiscoverFeedAdapter feedAdapter = new DiscoverFeedAdapter(rows, this);

    private GameCenterUiState state;

    /** Explore 芯片 0=游戏、1=应用 时使用的独立数据源。 */
    @Nullable
    private GameCenterUiState gamesState;

    @Nullable
    private GameCenterUiState appsState;

    private boolean loadingGamesSecondary;

    private boolean loadingAppsSecondary;

    private int selectedChip;

    /** Explore：0=热门游戏（对齐 Games Tab），1=热门应用（对齐 Apps Tab）。 */

    private int exploreSegment;

    private boolean loading;

    private boolean hasLoadedOnce;

    private int categoryId = DiscoverFeedAdapter.CATEGORY_EXPLORE;



    public static DiscoverFeedFragment newInstance(int categoryId) {

        DiscoverFeedFragment fragment = new DiscoverFeedFragment();

        Bundle args = new Bundle();

        args.putInt(ARG_CATEGORY, categoryId);

        fragment.setArguments(args);

        return fragment;

    }



    @Nullable

    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,

                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDiscoverFeedBinding.inflate(inflater, container, false);

        return binding.getRoot();

    }



    @Override

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        categoryId = getArguments() == null

                ? DiscoverFeedAdapter.CATEGORY_EXPLORE

                : getArguments().getInt(ARG_CATEGORY, DiscoverFeedAdapter.CATEGORY_EXPLORE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());

        binding.discoverFeedList.setLayoutManager(layoutManager);

        binding.discoverFeedList.setAdapter(feedAdapter);

        binding.discoverFeedList.setNestedScrollingEnabled(true);

        binding.discoverFeedError.setOnClickListener(v -> loadFeed(true));

    }



    @Override

    public void onResume() {

        super.onResume();

        if (!hasLoadedOnce) {

            hasLoadedOnce = true;

            if (getView() != null) {

                getView().post(this::loadFeed);

            }

        }

    }



    private void loadFeed() {

        loadFeed(false);

    }



    private void loadFeed(boolean fromRetry) {

        if (loading || binding == null) {

            return;

        }

        loading = true;

        binding.discoverFeedLoading.setVisibility(View.VISIBLE);

        binding.discoverFeedError.setVisibility(View.GONE);

        binding.discoverFeedList.setVisibility(View.GONE);

        GameCenterDataUtils.INSTANCE.loadHome(

                new GameCenterConfig("com.rwapp.fm.tvremote", categoryId),

                (loaded, error) -> {

                    loading = false;

                    if (!isAdded() || binding == null) {

                        return Unit.INSTANCE;

                    }

                    binding.discoverFeedLoading.setVisibility(View.GONE);

                    if (error != null || loaded == null) {

                        binding.discoverFeedError.setText(R.string.discover_no_network);

                        binding.discoverFeedError.setVisibility(View.VISIBLE);

                        if (fromRetry) {

                            hasLoadedOnce = true;

                        }

                        return Unit.INSTANCE;

                    }

                    if (loaded.isEmpty()) {

                        binding.discoverFeedError.setText(R.string.discover_empty);

                        binding.discoverFeedError.setVisibility(View.VISIBLE);

                        return Unit.INSTANCE;

                    }

                    if (!loaded.getAssetBaseUrl().isEmpty()) {

                        GameCenterSession.INSTANCE.updateAssetBaseUrl(loaded.getAssetBaseUrl());

                    }

                    state = loaded;

                    selectedChip = 0;

                    exploreSegment = 0;

                    gamesState = null;

                    appsState = null;

                    rebuild();

                    if (categoryId == CATEGORY_EXPLORE) {

                        prefetchExploreSecondary(CATEGORY_GAMES);

                    }

                    binding.discoverFeedList.setVisibility(View.VISIBLE);

                    return Unit.INSTANCE;

                });

    }



    private void rebuild() {

        rebuild(false);

    }



    private void rebuild(boolean fromChip) {

        LinearLayoutManager layoutManager = binding == null ? null

                : (LinearLayoutManager) binding.discoverFeedList.getLayoutManager();

        int anchorPos = -1;

        int anchorOffset = 0;

        if (fromChip && layoutManager != null) {

            anchorPos = layoutManager.findFirstVisibleItemPosition();

            View anchor = layoutManager.findViewByPosition(anchorPos);

            anchorOffset = anchor == null ? 0 : anchor.getTop();

        }

        rows.clear();

        rows.add(DiscoverFeedRow.marker(DiscoverFeedAdapter.TYPE_HEADER));

        GameSectionUi carousel = state.carouselSection();

        if (carousel != null && !carousel.getItems().isEmpty()) {

            rows.add(DiscoverFeedRow.section(DiscoverFeedAdapter.TYPE_CAROUSEL, carousel));

        }

        if (categoryId == CATEGORY_EXPLORE) {

            rebuildExploreFeed();

        } else if (categoryId == CATEGORY_APPS) {

            // 图二 Apps Tab：分类横滑 + 广告
            rebuildAppsTabFeed();

        } else if (categoryId == CATEGORY_GAMES) {

            // 图一 Game Center Tab：芯片 + Play 列表 + Action
            rebuildGamesFeed();

        }

        feedAdapter.notifyDataSetChanged();

        if (fromChip && layoutManager != null && anchorPos >= 0) {

            int pos = anchorPos;

            int offset = anchorOffset;

            binding.discoverFeedList.post(() -> layoutManager.scrollToPositionWithOffset(pos, offset));

        }

    }



    /** Explore：广告 + 游戏/应用双芯片 + 对应 Tab 同款列表。 */

    private void rebuildExploreFeed() {

        rows.add(DiscoverFeedRow.marker(DiscoverFeedAdapter.TYPE_AD));

        rows.add(DiscoverFeedRow.marker(DiscoverFeedAdapter.TYPE_EXPLORE_CHIPS));

        GameCenterUiState content = exploreSegment == 0 ? gamesState : appsState;

        if (content == null) {

            prefetchExploreSecondary(exploreSegment == 0 ? CATEGORY_GAMES : CATEGORY_APPS);

            return;

        }

        if (exploreSegment == 0) {

            appendGamesTabContent(content, true);

        } else {

            appendAppsTabContent(content);

        }

    }



    /** Apps Tab / Explore·热门应用：分类标题 + 横滑卡片 + 广告节奏。 */

    private void appendAppsTabContent(@NonNull GameCenterUiState appState) {

        for (GameSectionUi section : appState.feedCategorySections()) {

            rows.add(DiscoverFeedRow.section(DiscoverFeedAdapter.TYPE_TITLE, section));

            rows.add(DiscoverFeedRow.section(DiscoverFeedAdapter.TYPE_HORIZONTAL, section));

            rows.add(DiscoverFeedRow.marker(DiscoverFeedAdapter.TYPE_AD));

        }

    }



    /** Apps Tab：轮播下分类横滑 + 广告节奏。 */

    private void rebuildAppsTabFeed() {

        rows.add(DiscoverFeedRow.marker(DiscoverFeedAdapter.TYPE_AD));

        appendAppsTabContent(state);

    }



    /** Games Tab / Explore·热门游戏：分类芯片 + 列表行 + Action 等灵活分组。 */

    private void rebuildGamesFeed() {

        appendGamesTabContent(state, true);

    }



    private void appendGamesTabContent(@NonNull GameCenterUiState gameState, boolean showCategoryChips) {

        List<GameSectionUi> categories = gameState.tabChipSections(CATEGORY_GAMES);

        if (!categories.isEmpty()) {

            if (selectedChip >= categories.size()) {

                selectedChip = 0;

            }

            if (showCategoryChips) {

                rows.add(DiscoverFeedRow.marker(DiscoverFeedAdapter.TYPE_CHIPS));

            }

            for (GameItemUi item : categories.get(selectedChip).getItems()) {

                rows.add(DiscoverFeedRow.game(DiscoverFeedAdapter.TYPE_ROW, item));

            }

        }

        appendFlexibleSections(gameState);

    }



    private void appendFlexibleSections(@NonNull GameCenterUiState uiState) {

        int flexIndex = 0;
        for (GameSectionUi section : uiState.flexibleSectionsBelowChips()) {

            if (section.getItems().isEmpty()) {

                continue;

            }

            flexIndex++;
            rows.add(DiscoverFeedRow.section(DiscoverFeedAdapter.TYPE_TITLE, section));

            if (SectionDisplayStyleResolver.INSTANCE.resolve(section) == SectionDisplayStyle.VERTICAL_LIST) {

                for (GameItemUi item : section.getItems()) {

                    rows.add(DiscoverFeedRow.game(DiscoverFeedAdapter.TYPE_LARGE, item));

                }

            } else {

                rows.add(DiscoverFeedRow.section(DiscoverFeedAdapter.TYPE_HORIZONTAL, section));

            }

            if (flexIndex % 2 == 0) {
                rows.add(DiscoverFeedRow.marker(DiscoverFeedAdapter.TYPE_AD));
            }

        }

    }



    private void prefetchExploreSecondary(int secondaryCategoryId) {

        if (categoryId != CATEGORY_EXPLORE || binding == null) {

            return;

        }

        if (secondaryCategoryId == CATEGORY_GAMES) {

            if (gamesState != null || loadingGamesSecondary) {

                return;

            }

            loadingGamesSecondary = true;

        } else {

            if (appsState != null || loadingAppsSecondary) {

                return;

            }

            loadingAppsSecondary = true;

        }

        GameCenterDataUtils.INSTANCE.loadHome(

                new GameCenterConfig("com.rwapp.fm.tvremote", secondaryCategoryId),

                (loaded, error) -> {

                    if (secondaryCategoryId == CATEGORY_GAMES) {

                        loadingGamesSecondary = false;

                    } else {

                        loadingAppsSecondary = false;

                    }

                    if (!isAdded() || binding == null || categoryId != CATEGORY_EXPLORE) {

                        return Unit.INSTANCE;

                    }

                    if (error != null || loaded == null || loaded.isEmpty()) {

                        return Unit.INSTANCE;

                    }

                    if (secondaryCategoryId == CATEGORY_GAMES) {

                        gamesState = loaded;

                    } else {

                        appsState = loaded;

                    }

                    if ((exploreSegment == 0 && secondaryCategoryId == CATEGORY_GAMES)

                            || (exploreSegment == 1 && secondaryCategoryId == CATEGORY_APPS)) {

                        rebuild();

                    }

                    return Unit.INSTANCE;

                });

    }



    @Override

    public void onDestroyView() {

        hasLoadedOnce = false;

        gamesState = null;

        appsState = null;

        binding = null;

        super.onDestroyView();

    }



    @Override

    public int categoryId() {

        return categoryId;

    }



    @Override

    public int selectedChip() {

        return selectedChip;

    }



    @Override

    public int exploreSegment() {

        return exploreSegment;

    }



    @Override

    public int chipSectionsCategoryId() {

        if (categoryId == CATEGORY_GAMES) {

            return CATEGORY_GAMES;

        }

        if (categoryId == CATEGORY_EXPLORE && exploreSegment == 0) {

            return CATEGORY_GAMES;

        }

        return categoryId;

    }



    @Nullable

    @Override

    public GameCenterUiState state() {

        return state;

    }



    @Nullable

    @Override

    public GameCenterUiState chipSectionsState() {

        if (categoryId == CATEGORY_GAMES) {

            return state;

        }

        if (categoryId == CATEGORY_EXPLORE && exploreSegment == 0) {

            return gamesState;

        }

        return null;

    }



    @Override

    public void onChip(int index) {

        selectedChip = index;

        rebuild(true);

    }



    @Override

    public void onExploreSegment(int segment) {

        if (categoryId != CATEGORY_EXPLORE || exploreSegment == segment) {

            return;

        }

        exploreSegment = segment;

        GameCenterUiState cached = segment == 0 ? gamesState : appsState;

        if (cached == null) {

            prefetchExploreSecondary(segment == 0 ? CATEGORY_GAMES : CATEGORY_APPS);

        }

        rebuild(true);

    }

}

