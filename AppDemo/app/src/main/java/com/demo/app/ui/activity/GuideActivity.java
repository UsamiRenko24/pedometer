package com.demo.app.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.demo.app.MainActivity;
import com.demo.app.R;
import com.demo.app.databinding.ActivityGuideBinding;
import com.demo.app.integration.language.MultiLanguages;
import com.demo.app.ui.adapter.GuidePagerAdapter;
import com.demo.app.ui.guide.GuideNavigator;
import com.demo.app.ui.guide.GuidePageMotion;
import com.demo.app.utils.AppLog;
import com.demo.app.utils.CacheUtils;
import com.demo.app.utils.DefaultLauncher;
import com.demo.app.launcher.ads.LauncherSplashPlaceholder;
import com.demo.app.launcher.ads.SplashFeedPlaceholder;

/**
 * 8 页引导。第 1 页已设默认桌面会跳过第 2 页；第 2 页未设默认桌面不能继续。
 * 最后一页结束后进入主页。
 */
public class GuideActivity extends BaseActivity<ActivityGuideBinding> implements GuideNavigator {

    public static final String EXTRA_FROM_SPLASH = "extra_from_splash";

    private static final long PAGE_SCROLL_MS = 460L;

    private boolean pageIdle = true;
    private boolean adjustingPage;
    private boolean openedFromSplash;
    private int settledPage = GuidePagerAdapter.PAGE_WELCOME;
    private ValueAnimator pageAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        openedFromSplash = savedInstanceState == null
                && getIntent().getBooleanExtra(EXTRA_FROM_SPLASH, false);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    protected ActivityGuideBinding getViewBinding() {
        return ActivityGuideBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initViews() {
        LauncherSplashPlaceholder.show(this, R.id.launcherGuide_flSplash);
        binding.guidePager.setAdapter(new GuidePagerAdapter(this));
        binding.guidePager.setOffscreenPageLimit(2);
        installSkippedPageTransformer();
        binding.guidePager.setUserInputEnabled(true);
        binding.guidePager.setClipToPadding(false);
        binding.guidePager.setClipChildren(false);
        if (binding.guidePager.getChildAt(0) instanceof RecyclerView) {
            RecyclerView pagerChild = (RecyclerView) binding.guidePager.getChildAt(0);
            pagerChild.setClipToPadding(false);
            pagerChild.setClipChildren(false);
            pagerChild.setItemAnimator(null);
            pagerChild.setOverScrollMode(android.view.View.OVER_SCROLL_NEVER);
            blockForwardSwipeUntilDefault(pagerChild);
        }
        binding.guidePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                boolean idle = state == ViewPager2.SCROLL_STATE_IDLE;
                if (idle == pageIdle) {
                    return;
                }
                pageIdle = idle;
                if (idle) {
                    syncSkippedPages();
                }
                dispatchPageSettled(idle);
            }
        });
        restoreGuidePage();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.guidePager.getCurrentItem() > 0) {
                    goPrevGuidePage();
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });
        // binding.btnSkip.setOnClickListener(v -> skipGuideFlow());
        SplashActivity.finishIfRunning();
    }

    /**
     * 从启动页重新进来：还没设成默认桌面就回到欢迎页。
     * 已经是默认桌面时也从欢迎页开始，下一步再进通知页。
     * 设默认桌面时系统当场拉起引导，仍从通知页继续。
     */
    private void restoreGuidePage() {
        if (openedFromSplash && !DefaultLauncher.isDefault(this)) {
            CacheUtils.setGuideContinueNotify(false);
            settledPage = GuidePagerAdapter.PAGE_WELCOME;
            binding.guidePager.setCurrentItem(GuidePagerAdapter.PAGE_WELCOME, false);
            CacheUtils.setGuidePage(GuidePagerAdapter.PAGE_WELCOME);
            return;
        }
        if (continueNotifyAfterDefault()) {
            return;
        }
        if (openedFromSplash && DefaultLauncher.isDefault(this)) {
            settledPage = GuidePagerAdapter.PAGE_WELCOME;
            binding.guidePager.setCurrentItem(GuidePagerAdapter.PAGE_WELCOME, false);
            CacheUtils.setGuidePage(GuidePagerAdapter.PAGE_WELCOME);
            return;
        }
        int page = CacheUtils.getGuidePage();
        if (DefaultLauncher.isDefault(this) && page == GuidePagerAdapter.PAGE_DEFAULT) {
            page = GuidePagerAdapter.PAGE_NOTIFY;
        }
        if (page <= GuidePagerAdapter.PAGE_WELCOME || page >= GuidePagerAdapter.PAGE_COUNT) {
            return;
        }
        settledPage = page;
        binding.guidePager.setCurrentItem(page, false);
        CacheUtils.setGuidePage(page);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding == null) {
            return;
        }
        if (continueNotifyAfterDefault()) {
            return;
        }
        if (DefaultLauncher.isDefault(this)
                && binding.guidePager.getCurrentItem() <= GuidePagerAdapter.PAGE_DEFAULT
                && CacheUtils.getGuidePage() == GuidePagerAdapter.PAGE_DEFAULT) {
            openGuidePage(GuidePagerAdapter.PAGE_NOTIFY);
        }
    }

    /** 第一页刚把本应用设成默认桌面。系统会重新打开引导，这时进通知页。 */
    private boolean continueNotifyAfterDefault() {
        if (!CacheUtils.isGuideContinueNotify() || binding == null) {
            return false;
        }
        if (CacheUtils.getGuidePage() > GuidePagerAdapter.PAGE_DEFAULT) {
            CacheUtils.setGuideContinueNotify(false);
            return false;
        }
        if (!DefaultLauncher.isDefault(this)) {
            return false;
        }
        CacheUtils.setGuideContinueNotify(false);
        openGuidePage(GuidePagerAdapter.PAGE_NOTIFY);
        return true;
    }

    @Override
    protected void onPause() {
        if (binding != null) {
            int current = binding.guidePager.getCurrentItem();
            if (current != GuidePagerAdapter.PAGE_DEFAULT || !DefaultLauncher.isDefault(this)) {
                CacheUtils.setGuidePage(current);
            }
        }
        super.onPause();
    }

    /**
     * 已经是默认桌面时，欢迎页和通知页在滑动时紧挨着。
     * 第二页仍占一个页宽，但内容隐藏，两侧页面各错开一页填上这个空位。
     */
    private void installSkippedPageTransformer() {
        binding.guidePager.setPageTransformer((page, position) -> {
            if (!(page.getParent() instanceof RecyclerView) || page.getWidth() <= 0) {
                return;
            }
            int index = ((RecyclerView) page.getParent()).getChildAdapterPosition(page);
            if (index == RecyclerView.NO_POSITION) {
                return;
            }
            boolean skipping = DefaultLauncher.isDefault(this);
            if (index == GuidePagerAdapter.PAGE_DEFAULT) {
                page.setAlpha(skipping ? 0f : 1f);
                page.setTranslationX(0f);
                return;
            }
            float shift = 0f;
            if (skipping) {
                float width = page.getWidth();
                if (index == GuidePagerAdapter.PAGE_NOTIFY && position >= 1f) {
                    boolean holdNotify = settledPage >= GuidePagerAdapter.PAGE_NOTIFY && position <= 1.001f;
                    if (!holdNotify) {
                        shift = -width;
                    }
                } else if (index == GuidePagerAdapter.PAGE_WELCOME && position <= -1f) {
                    boolean holdWelcome = settledPage <= GuidePagerAdapter.PAGE_WELCOME && position >= -1.001f;
                    if (!holdWelcome) {
                        shift = width;
                    }
                }
            }
            page.setTranslationX(shift);
        });
    }

    /** 没设成默认桌面时，滑动不能越过第 2 页。回上一页仍然可以。 */
    private void blockForwardSwipeUntilDefault(RecyclerView pagerChild) {
        pagerChild.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private boolean correcting;

            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (correcting || DefaultLauncher.isDefault(GuideActivity.this)) {
                    return;
                }
                int width = rv.getWidth();
                if (width <= 0) {
                    return;
                }
                int limit = width * GuidePagerAdapter.PAGE_DEFAULT;
                int offset = rv.computeHorizontalScrollOffset();
                boolean pastDefault = rv.getLayoutDirection() == android.view.View.LAYOUT_DIRECTION_RTL
                        ? offset < limit
                        : offset > limit;
                if (!pastDefault) {
                    return;
                }
                correcting = true;
                rv.scrollBy(limit - offset, 0);
                correcting = false;
            }
        });
    }

    @Override
    public boolean isGuidePageIdle() {
        return pageIdle;
    }

    @Override
    public void goNextGuidePage() {
        int current = binding.guidePager.getCurrentItem();
        AppLog.i("GuideActivity", "next from page " + current);
        if (current == GuidePagerAdapter.PAGE_DEFAULT && !DefaultLauncher.isDefault(this)) {
            AppLog.i("GuideActivity", "blocked, launcher is not default");
            return;
        }
        if (current == GuidePagerAdapter.PAGE_WELCOME && DefaultLauncher.isDefault(this)) {
            AppLog.i("GuideActivity", "skip default-launcher page");
            smoothMoveTo(GuidePagerAdapter.PAGE_DEFAULT);
            return;
        }
        if (current < GuidePagerAdapter.PAGE_COUNT - 1) {
            smoothMoveTo(current + 1);
        } else {
            finishGuideFlow();
        }
    }

    @Override
    public void goPrevGuidePage() {
        int current = binding.guidePager.getCurrentItem();
        if (current == GuidePagerAdapter.PAGE_NOTIFY && DefaultLauncher.isDefault(this)) {
            smoothMoveTo(GuidePagerAdapter.PAGE_DEFAULT);
            return;
        }
        if (current > 0) {
            smoothMoveTo(current - 1);
        }
    }

    /**
     * 第 1 页已经设成默认桌面时不停留在第 2 页。
     * 还没设置时，不能从第 2 页滑到后面的引导页。
     */
    private void syncSkippedPages() {
        if (adjustingPage) {
            return;
        }
        int current = binding.guidePager.getCurrentItem();
        boolean isDefault = DefaultLauncher.isDefault(this);
        int target = current;
        if (current == GuidePagerAdapter.PAGE_DEFAULT && isDefault) {
            target = settledPage > GuidePagerAdapter.PAGE_DEFAULT
                    ? GuidePagerAdapter.PAGE_WELCOME
                    : GuidePagerAdapter.PAGE_NOTIFY;
        } else if (current > GuidePagerAdapter.PAGE_DEFAULT && !isDefault) {
            target = GuidePagerAdapter.PAGE_DEFAULT;
        }
        if (target != current) {
            adjustingPage = true;
            binding.guidePager.setCurrentItem(target, false);
            adjustingPage = false;
            current = target;
        }
        settledPage = current;
        CacheUtils.setGuidePage(current);
    }

    /** 打开指定引导页，并记下来，避免回到欢迎页。 */
    @Override
    public void openGuidePage(int page) {
        if (page < 0 || page >= GuidePagerAdapter.PAGE_COUNT || binding == null) {
            return;
        }
        AppLog.i("GuideActivity", "open page " + page);
        settledPage = page;
        CacheUtils.setGuidePage(page);
        if (binding.guidePager.getCurrentItem() != page) {
            binding.guidePager.setCurrentItem(page, false);
        }
    }

    @Override
    public void reloadGuideAfterLanguage() {
        if (binding == null) {
            return;
        }
        int next = binding.guidePager.getCurrentItem() + 1;
        AppLog.i("GuideActivity", "reload guide after language, next " + next);
        MultiLanguages.updateAppLanguage(this);
        binding.guidePager.setAdapter(new GuidePagerAdapter(this));
        if (next >= GuidePagerAdapter.PAGE_COUNT) {
            finishGuideFlow();
            return;
        }
        openGuidePage(next);
    }

    /** 按整页距离拖过去，避免默认滚动停住后再被吸附一刀。 */
    private void smoothMoveTo(int target) {
        if (pageAnimator != null) {
            return;
        }
        RecyclerView pagerChild = (RecyclerView) binding.guidePager.getChildAt(0);
        int pageWidth = pagerChild == null ? 0 : pagerChild.getWidth();
        int from = binding.guidePager.getCurrentItem();
        if (pageWidth <= 0 || target == from || !binding.guidePager.beginFakeDrag()) {
            binding.guidePager.setCurrentItem(target, false);
            return;
        }
        float distance = pageWidth * Math.abs(target - from);
        float direction = target > from ? -1f : 1f;
        float[] dragged = {0f};
        pageAnimator = ValueAnimator.ofFloat(0f, distance);
        pageAnimator.setDuration(PAGE_SCROLL_MS);
        pageAnimator.setInterpolator(new DecelerateInterpolator(1.6f));
        pageAnimator.addUpdateListener(animation -> {
            if (!binding.guidePager.isFakeDragging()) {
                return;
            }
            float value = (float) animation.getAnimatedValue();
            binding.guidePager.fakeDragBy((value - dragged[0]) * direction);
            dragged[0] = value;
        });
        pageAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finishFakeDrag();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                finishFakeDrag();
            }
        });
        pageAnimator.start();
    }

    private void finishFakeDrag() {
        if (binding != null && binding.guidePager.isFakeDragging()) {
            binding.guidePager.endFakeDrag();
        }
        pageAnimator = null;
    }

    private void dispatchPageSettled(boolean settled) {
        int current = binding.guidePager.getCurrentItem();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (!(fragment instanceof GuidePageMotion)) {
                continue;
            }
            boolean run = settled && ("f" + current).equals(fragment.getTag());
            ((GuidePageMotion) fragment).setPageSettled(run);
        }
    }

    @Override
    public void finishGuideFlow() {
        AppLog.i("GuideActivity", "guide finished");
        LauncherSplashPlaceholder.dismiss(this, R.id.launcherGuide_flSplash);
        CacheUtils.setGuidePage(GuidePagerAdapter.PAGE_WELCOME);
        CacheUtils.setShowGuidePage(false);
        SplashFeedPlaceholder.runForFirstGuide(this, () -> {
            Intent intent = new Intent(GuideActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_REFRESH_LANGUAGE, true);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    public void skipGuideFlow() {
        finishGuideFlow();
    }

    @Override
    protected void onDestroy() {
        if (pageAnimator != null) {
            pageAnimator.cancel();
            pageAnimator = null;
        }
        super.onDestroy();
    }
}
