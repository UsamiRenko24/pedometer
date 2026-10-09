package com.demo.app.launcher.ui.view.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.launcher.ui.activity.discover.DiscoverLauncherActivity;
import com.demo.app.launcher.ui.activity.news.NewsLauncherActivity;
import com.demo.app.launcher.ui.activity.wallpaper.WallpaperLauncherActivity;
import com.demo.app.launcher.ui.activity.weather.WeatherLauncherActivity;

import java.util.ArrayList;
import java.util.List;

/** 搜索页顶部固定展示的四个快捷入口。 */
public final class LauncherSearchShortcuts {

    private LauncherSearchShortcuts() {
    }

    public static void attach(@NonNull RecyclerView list) {
        Context context = list.getContext();
        list.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        list.setAdapter(new Adapter(context, entries(context)));
        list.setNestedScrollingEnabled(false);
    }

    @NonNull
    private static List<Entry> entries(@NonNull Context context) {
        List<Entry> items = new ArrayList<>();
        items.add(new Entry(R.drawable.icon_home_app_news, R.string.screen0_news,
                () -> NewsLauncherActivity.start(context)));
        items.add(new Entry(R.drawable.icon_home_app_weather, R.string.screen0_weather,
                () -> WeatherLauncherActivity.start(context)));
        items.add(new Entry(R.drawable.icon_home_app_wallpaper, R.string.screen0_wallpaper,
                () -> WallpaperLauncherActivity.start(context)));
        items.add(new Entry(R.drawable.icon_home_app_discover, R.string.screen0_discover,
                () -> DiscoverLauncherActivity.start(context)));
        return items;
    }

    private static final class Entry {
        final int icon;
        final int title;
        final Runnable action;

        Entry(int icon, int title, @NonNull Runnable action) {
            this.icon = icon;
            this.title = title;
            this.action = action;
        }
    }

    private static final class Adapter extends RecyclerView.Adapter<Adapter.Holder> {
        private final List<Entry> items;

        Adapter(@NonNull Context context, @NonNull List<Entry> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_launcher_search_recommend, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Entry entry = items.get(position);
            holder.icon.setImageResource(entry.icon);
            holder.title.setText(entry.title);
            holder.itemView.setOnClickListener(v -> entry.action.run());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView title;

            Holder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.launcher_search_recommend_icon);
                title = itemView.findViewById(R.id.launcher_search_recommend_title);
            }
        }
    }
}
