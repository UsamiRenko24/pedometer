package com.demo.app.launcher.ui.activity.news;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.databinding.ActivityNewsAreaBinding;
import com.demo.app.launcher.news.NewsArea;
import com.demo.app.launcher.news.NewsStore;
import com.demo.app.launcher.ui.adapter.news.NewsAreaAdapter;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;

/** 新闻地区。从新闻首页右上角定位图标进入。 */
public class NewsAreaActivity extends BaseActivity<ActivityNewsAreaBinding> {

    private NewsArea selected = NewsStore.area();
    private NewsAreaAdapter areas;

    public static void start(@NonNull Context context) {
        context.startActivity(new Intent(context, NewsAreaActivity.class));
    }

    @Nullable
    @Override
    protected ActivityNewsAreaBinding getViewBinding() {
        return ActivityNewsAreaBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.newsAreaRoot, (v, insets) -> {
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
        areas = new NewsAreaAdapter(selected, area -> {
            selected = area;
            areas.setSelected(area);
        });
        binding.newsAreaList.setLayoutManager(new LinearLayoutManager(this));
        binding.newsAreaList.setAdapter(areas);
    }

    @Override
    protected void initEvents() {
        binding.newsAreaDone.setOnClickListener(v -> {
            NewsStore.setArea(selected);
            finish();
        });
    }
}
