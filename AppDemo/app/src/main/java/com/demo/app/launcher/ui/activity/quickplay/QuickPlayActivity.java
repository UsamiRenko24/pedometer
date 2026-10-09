package com.demo.app.launcher.ui.activity.quickplay;

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
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.databinding.ActivityQuickPlayBinding;
import com.demo.app.launcher.gamecenter.GameCenterSession;
import com.demo.app.launcher.gamecenter.GamePlayStoreLauncher;
import com.demo.app.launcher.gamecenter.model.GameCenterConfig;
import com.demo.app.launcher.gamecenter.model.GameCenterUiState;
import com.demo.app.launcher.gamecenter.model.GameItemUi;
import com.demo.app.launcher.gamecenter.model.GameSectionUi;
import com.demo.app.launcher.ui.adapter.quickplay.QuickPlayGridAdapter;
import com.demo.app.launcher.utils.GameCenterDataUtils;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 快游戏 Play Hub：顶部头图 + Quick Play 三列网格。 */
public class QuickPlayActivity extends BaseActivity<ActivityQuickPlayBinding> {

    private static final int GRID_SPAN = 3;

    private final QuickPlayGridAdapter adapter =
            new QuickPlayGridAdapter(this::openGame);
    private boolean loading;

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, QuickPlayActivity.class);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityQuickPlayBinding getViewBinding() {
        return ActivityQuickPlayBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.quickPlayRoot, (v, insets) -> {
            Insets bars = insets.getInsets(SystemUiHelper.contentInsetTypes());
            Insets nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(bars.left, 0, bars.right, 0);
            int bottomPad = (int) (16 * getResources().getDisplayMetrics().density) + nav.bottom;
            binding.quickPlayList.setPadding(0, 0, 0, bottomPad);
            adapter.setHeaderStatusBarInset(bars.top);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(binding.quickPlayRoot);
    }

    @Override
    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow(), true, true);
    }

    @Override
    protected void initViews() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, GRID_SPAN);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? GRID_SPAN : 1;
            }
        });
        binding.quickPlayList.setLayoutManager(layoutManager);
        binding.quickPlayList.setAdapter(adapter);
        binding.quickPlayError.setOnClickListener(v -> loadGames());
    }

    @Override
    protected void loadData() {
        loadGames();
    }

    private void loadGames() {
        if (loading) {
            return;
        }
        loading = true;
        showLoading();
        GameCenterDataUtils.INSTANCE.loadHome(new GameCenterConfig(), (state, error) -> {
            loading = false;
            if (isFinishing()) {
                return kotlin.Unit.INSTANCE;
            }
            if (error != null || state == null) {
                showError();
                return kotlin.Unit.INSTANCE;
            }
            if (!state.getAssetBaseUrl().isEmpty()) {
                GameCenterSession.INSTANCE.updateAssetBaseUrl(state.getAssetBaseUrl());
            }
            List<GameItemUi> games = flattenGames(state);
            if (games.isEmpty()) {
                showError();
                binding.quickPlayError.setText(R.string.quick_play_empty);
            } else {
                showContent(games);
            }
            return kotlin.Unit.INSTANCE;
        });
    }

    @NonNull
    private List<GameItemUi> flattenGames(@NonNull GameCenterUiState state) {
        List<GameItemUi> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (GameSectionUi section : state.getSections()) {
            for (GameItemUi item : section.getItems()) {
                String key = !item.getBid().isEmpty() ? item.getBid() : item.getId();
                if (key.isEmpty() || !seen.add(key)) {
                    continue;
                }
                out.add(item);
            }
        }
        return out;
    }

    private void openGame(@NonNull GameItemUi item) {
        if (!GamePlayStoreLauncher.open(this, item.getLink(), item.getBid())) {
            Toast.makeText(this, R.string.quick_play_load_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading() {
        binding.quickPlayLoading.setVisibility(View.VISIBLE);
        binding.quickPlayError.setVisibility(View.GONE);
        binding.quickPlayList.setVisibility(View.INVISIBLE);
    }

    private void showError() {
        binding.quickPlayLoading.setVisibility(View.GONE);
        binding.quickPlayError.setVisibility(View.VISIBLE);
        binding.quickPlayList.setVisibility(View.INVISIBLE);
    }

    private void showContent(@NonNull List<GameItemUi> games) {
        binding.quickPlayLoading.setVisibility(View.GONE);
        binding.quickPlayError.setVisibility(View.GONE);
        binding.quickPlayList.setVisibility(View.VISIBLE);
        adapter.submit(games);
    }
}
