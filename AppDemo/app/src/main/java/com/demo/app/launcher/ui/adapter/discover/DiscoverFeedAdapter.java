package com.demo.app.launcher.ui.adapter.discover;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.demo.app.R;
import com.demo.app.databinding.ItemDiscoverCarouselBinding;
import com.demo.app.databinding.ItemDiscoverChipsBinding;
import com.demo.app.databinding.ItemDiscoverHeaderBinding;
import com.demo.app.databinding.ItemDiscoverHorizontalBinding;
import com.demo.app.databinding.ItemDiscoverLargeBinding;
import com.demo.app.databinding.ItemDiscoverRowBinding;
import com.demo.app.databinding.ItemDiscoverSectionTitleBinding;
import com.demo.app.launcher.gamecenter.model.GameCenterUiState;
import com.demo.app.launcher.gamecenter.model.GameItemUi;
import com.demo.app.launcher.gamecenter.model.GameSectionUi;
import com.demo.app.launcher.gamecenter.model.SectionTitleResolver;

import java.util.ArrayList;
import java.util.List;

/** Discover 信息流。轮播、分类和横向卡片各自用独立 Adapter。 */
public final class DiscoverFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int CATEGORY_EXPLORE = 0;
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_CAROUSEL = 1;
    public static final int TYPE_CHIPS = 2;
    public static final int TYPE_ROW = 3;
    public static final int TYPE_TITLE = 4;
    public static final int TYPE_HORIZONTAL = 5;
    public static final int TYPE_LARGE = 6;
    public static final int TYPE_AD = 7;
    /** Explore：轮播下「热门游戏 / 热门应用」双芯片。 */
    public static final int TYPE_EXPLORE_CHIPS = 8;

    public interface Host {
        int categoryId();

        /** Games Tab / Explore·热门游戏 内部分类芯片选中项。 */
        int selectedChip();

        /** Explore Tab：0=热门游戏，1=热门应用。 */
        int exploreSegment();

        /** 分类芯片数据源（Explore 热门游戏时用 gamesState）。 */
        int chipSectionsCategoryId();

        @Nullable
        GameCenterUiState state();

        @Nullable
        GameCenterUiState chipSectionsState();

        void onChip(int index);

        void onExploreSegment(int segment);
    }

    private final List<DiscoverFeedRow> rows;
    private final Host host;

    public DiscoverFeedAdapter(@NonNull List<DiscoverFeedRow> rows, @NonNull Host host) {
        this.rows = rows;
        this.host = host;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_CAROUSEL:
                return new CarouselHolder(ItemDiscoverCarouselBinding.inflate(inflater, parent, false));
            case TYPE_CHIPS:
                return new ChipsHolder(ItemDiscoverChipsBinding.inflate(inflater, parent, false));
            case TYPE_ROW:
                return new RowHolder(ItemDiscoverRowBinding.inflate(inflater, parent, false));
            case TYPE_TITLE:
                return new TitleHolder(ItemDiscoverSectionTitleBinding.inflate(inflater, parent, false));
            case TYPE_HORIZONTAL:
                return new HorizontalHolder(ItemDiscoverHorizontalBinding.inflate(inflater, parent, false));
            case TYPE_LARGE:
                return new LargeHolder(ItemDiscoverLargeBinding.inflate(inflater, parent, false));
            case TYPE_AD:
                return new AdHolder(inflater.inflate(R.layout.item_discover_ad, parent, false));
            case TYPE_EXPLORE_CHIPS:
                return new ExploreChipsHolder(inflater.inflate(
                        R.layout.item_discover_explore_toggle_chips, parent, false));
            default:
                return new HeaderHolder(ItemDiscoverHeaderBinding.inflate(inflater, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DiscoverFeedRow row = rows.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).bind();
        } else if (holder instanceof CarouselHolder) {
            ((CarouselHolder) holder).bind(row.section);
        } else if (holder instanceof ChipsHolder) {
            ((ChipsHolder) holder).bind();
        } else if (holder instanceof ExploreChipsHolder) {
            ((ExploreChipsHolder) holder).bind();
        } else if (holder instanceof RowHolder && row.game != null) {
            ((RowHolder) holder).bind(row.game);
        } else if (holder instanceof TitleHolder && row.section != null) {
            ((TitleHolder) holder).bind(row.section);
        } else if (holder instanceof HorizontalHolder && row.section != null) {
            ((HorizontalHolder) holder).bind(row.section);
        } else if (holder instanceof LargeHolder && row.game != null) {
            ((LargeHolder) holder).bind(row.game);
        } else if (holder instanceof AdHolder) {
            // 静态占位，与 RemoteControl100 广告位布局节奏一致
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof CarouselHolder) {
            ((CarouselHolder) holder).detach();
        }
        super.onViewRecycled(holder);
    }

    private final class HeaderHolder extends RecyclerView.ViewHolder {
        private final ItemDiscoverHeaderBinding binding;

        HeaderHolder(ItemDiscoverHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind() {
            int title;
            int subtitle;
            if (host.categoryId() == CATEGORY_EXPLORE) {
                title = R.string.discover_explore_title;
                subtitle = R.string.discover_explore_subtitle;
            } else if (host.categoryId() == 1) {
                title = R.string.discover_apps_title;
                subtitle = R.string.discover_apps_subtitle;
            } else {
                title = R.string.discover_games_title;
                subtitle = R.string.discover_games_subtitle;
            }
            binding.discoverPageTitle.setText(title);
            binding.discoverPageSubtitle.setText(subtitle);
        }
    }

    private final class CarouselHolder extends RecyclerView.ViewHolder {
        private final ItemDiscoverCarouselBinding binding;
        private ViewPager2.OnPageChangeCallback callback;

        CarouselHolder(ItemDiscoverCarouselBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@Nullable GameSectionUi section) {
            if (binding.carouselNativeAd != null) {
                binding.carouselNativeAd.refreshVisibility();
            }
            List<GameItemUi> items = section == null ? new ArrayList<>() : section.getItems();
            if (callback != null) {
                binding.discoverCarouselPager.unregisterOnPageChangeCallback(callback);
                callback = null;
            }
            binding.discoverCarouselPager.setOffscreenPageLimit(1);
            binding.discoverCarouselPager.setAdapter(new DiscoverBannerAdapter(items, host.categoryId()));
            binding.discoverCarouselPager.setCurrentItem(0, false);
            callback = new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    drawDots(items.size(), position);
                }
            };
            binding.discoverCarouselPager.registerOnPageChangeCallback(callback);
            binding.discoverCarouselPager.post(() -> finishCarouselSetup(items));
        }

        private void finishCarouselSetup(@NonNull List<GameItemUi> items) {
            RecyclerView pagerList = (RecyclerView) binding.discoverCarouselPager.getChildAt(0);
            if (pagerList != null) {
                int start = (int) (18 * itemView.getResources().getDisplayMetrics().density);
                int end = (int) (36 * itemView.getResources().getDisplayMetrics().density);
                pagerList.setPadding(start, 0, end, 0);
                pagerList.setClipToPadding(false);
                pagerList.setOverScrollMode(View.OVER_SCROLL_NEVER);
                DiscoverHorizontalTouchHelper.installOnce(pagerList);
            }
            int count = items.size();
            int selected = count == 0 ? 0 : binding.discoverCarouselPager.getCurrentItem();
            if (selected >= count) {
                selected = 0;
                binding.discoverCarouselPager.setCurrentItem(0, false);
            }
            drawDots(count, selected);
        }

        void detach() {
            if (callback != null) {
                binding.discoverCarouselPager.unregisterOnPageChangeCallback(callback);
                callback = null;
            }
        }

        private void drawDots(int count, int selected) {
            LinearLayout dots = binding.discoverCarouselDots;
            dots.removeAllViews();
            int size = (int) (6 * itemView.getResources().getDisplayMetrics().density);
            int gap = (int) (6 * itemView.getResources().getDisplayMetrics().density);
            for (int i = 0; i < count; i++) {
                View dot = new View(itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                if (i > 0) {
                    params.setMarginStart(gap);
                }
                dot.setLayoutParams(params);
                dot.setBackgroundResource(i == selected
                        ? R.drawable.bg_discover_dot_on
                        : R.drawable.bg_discover_dot_off);
                dots.addView(dot);
            }
        }
    }

    private final class ExploreChipsHolder extends RecyclerView.ViewHolder {
        private final View gamesChip;
        private final View appsChip;

        ExploreChipsHolder(@NonNull View itemView) {
            super(itemView);
            gamesChip = itemView.findViewById(R.id.discover_explore_chip_games);
            appsChip = itemView.findViewById(R.id.discover_explore_chip_apps);
        }

        void bind() {
            boolean games = host.exploreSegment() == 0;
            applyExploreChip(gamesChip, games);
            applyExploreChip(appsChip, !games);
            gamesChip.setOnClickListener(v -> {
                if (host.exploreSegment() != 0) {
                    host.onExploreSegment(0);
                }
            });
            appsChip.setOnClickListener(v -> {
                if (host.exploreSegment() != 1) {
                    host.onExploreSegment(1);
                }
            });
        }

        private void applyExploreChip(@NonNull View chip, boolean selected) {
            int onBg = R.drawable.bg_discover_chip_on;
            int offBg = R.drawable.bg_discover_chip_off;
            chip.setBackgroundResource(selected ? onBg : offBg);
            int labelColor = ContextCompat.getColor(chip.getContext(),
                    selected ? R.color.color_white : R.color.color_4F7CFB);
            int iconColor = labelColor;
            View label = chip instanceof ViewGroup
                    ? ((ViewGroup) chip).getChildAt(1)
                    : null;
            if (label instanceof android.widget.TextView) {
                ((android.widget.TextView) label).setTextColor(labelColor);
            }
            View icon = chip instanceof ViewGroup
                    ? ((ViewGroup) chip).getChildAt(0)
                    : null;
            if (icon instanceof android.widget.ImageView) {
                ImageViewCompat.setImageTintList(
                        (android.widget.ImageView) icon, ColorStateList.valueOf(iconColor));
            }
        }
    }

    private final class ChipsHolder extends RecyclerView.ViewHolder {
        private final ItemDiscoverChipsBinding binding;

        ChipsHolder(ItemDiscoverChipsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.discoverChips.setLayoutManager(
                    new LinearLayoutManager(binding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.discoverChips.setNestedScrollingEnabled(false);
        }

        void bind() {
            GameCenterUiState chipState = host.chipSectionsState();
            if (chipState == null) {
                return;
            }
            binding.discoverChips.setAdapter(new DiscoverChipAdapter(
                    host.chipSectionsCategoryId(),
                    chipState.tabChipSections(host.chipSectionsCategoryId()),
                    host.selectedChip(),
                    host::onChip));
        }
    }

    private static final class RowHolder extends RecyclerView.ViewHolder {
        private final ItemDiscoverRowBinding binding;

        RowHolder(ItemDiscoverRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull GameItemUi item) {
            binding.discoverRowName.setText(DiscoverItemBinder.titleOf(item));
            binding.discoverRowDesc.setText(item.getDisplayDesc());
            String rating = DiscoverItemBinder.ratingOf(item);
            binding.discoverRowRating.setText(rating);
            int ratingVisible = rating.isEmpty() ? View.GONE : View.VISIBLE;
            binding.discoverRowRating.setVisibility(ratingVisible);
            binding.discoverRowStar.setVisibility(ratingVisible);
            DiscoverItemBinder.loadIcon(binding.discoverRowIcon, item);
            binding.discoverRowPlay.setOnClickListener(v -> DiscoverItemBinder.openLink(v, item));
            itemView.setOnClickListener(v -> DiscoverItemBinder.openLink(v, item));
        }
    }

    private static final class TitleHolder extends RecyclerView.ViewHolder {
        private final ItemDiscoverSectionTitleBinding binding;

        TitleHolder(ItemDiscoverSectionTitleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull GameSectionUi section) {
            binding.discoverSectionTitle.setText(SectionTitleResolver.INSTANCE.resolveByField(
                    itemView.getContext(), section));
        }
    }

    private static final class HorizontalHolder extends RecyclerView.ViewHolder {
        private final ItemDiscoverHorizontalBinding binding;

        HorizontalHolder(ItemDiscoverHorizontalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.discoverHorizontalList.setLayoutManager(new LinearLayoutManager(
                    binding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.discoverHorizontalList.setNestedScrollingEnabled(false);
        }

        void bind(@NonNull GameSectionUi section) {
            binding.discoverHorizontalList.setAdapter(new DiscoverCardAdapter(section.getItems()));
        }
    }

    private static final class AdHolder extends RecyclerView.ViewHolder {
        AdHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private static final class LargeHolder extends RecyclerView.ViewHolder {
        private final ItemDiscoverLargeBinding binding;

        LargeHolder(ItemDiscoverLargeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull GameItemUi item) {
            binding.discoverLargeName.setText(DiscoverItemBinder.titleOf(item));
            binding.discoverLargeDesc.setText(item.getDisplayDesc());
            String rating = DiscoverItemBinder.ratingOf(item);
            binding.discoverLargeRating.setText(rating);
            binding.discoverLargeRating.setVisibility(rating.isEmpty() ? View.GONE : View.VISIBLE);
            DiscoverItemBinder.loadIcon(binding.discoverLargeIcon, item);
            DiscoverItemBinder.loadBanner(binding.discoverLargeBanner, item);
            binding.discoverLargeAction.setOnClickListener(v -> DiscoverItemBinder.openLink(v, item));
            itemView.setOnClickListener(v -> DiscoverItemBinder.openLink(v, item));
        }
    }
}
