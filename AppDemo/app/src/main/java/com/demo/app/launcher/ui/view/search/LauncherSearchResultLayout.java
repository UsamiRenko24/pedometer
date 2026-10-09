package com.demo.app.launcher.ui.view.search;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 桌面搜索结果：本机应用在前，Google 建议在后。 */
public final class LauncherSearchResultLayout extends FrameLayout {

    private static final int TYPE_APP = 0;
    private static final int TYPE_SUGGESTION = 1;
    private static final long DEBOUNCE_MS = 500L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<ResolveInfo> allApps = new ArrayList<>();
    private final ResultAdapter adapter = new ResultAdapter();
    private ProgressBar loading;
    private TextView empty;
    private int generation;

    public LauncherSearchResultLayout(@NonNull Context context) {
        this(context, null);
    }

    public LauncherSearchResultLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_launcher_search_result, this, true);
        RecyclerView list = findViewById(R.id.launcher_search_result_list);
        list.setLayoutManager(new LinearLayoutManager(context));
        list.setAdapter(adapter);
        loading = findViewById(R.id.launcher_search_result_loading);
        empty = findViewById(R.id.launcher_search_result_empty);
        loadApps();
    }

    public void setSearchQuery(@Nullable String query) {
        generation++;
        int token = generation;
        handler.removeCallbacksAndMessages(null);
        String text = query == null ? "" : query.trim();
        List<ResolveInfo> apps = matchApps(text);
        if (text.isEmpty()) {
            loading.setVisibility(GONE);
            empty.setVisibility(GONE);
            adapter.submit(apps, Collections.emptyList());
            return;
        }
        adapter.submit(apps, Collections.emptyList());
        loading.setVisibility(apps.isEmpty() ? VISIBLE : GONE);
        empty.setVisibility(GONE);
        handler.postDelayed(() -> SearchSuggestClient.fetch(text, suggestions -> {
            if (token != generation) {
                return;
            }
            loading.setVisibility(GONE);
            adapter.submit(apps, suggestions);
            empty.setVisibility(apps.isEmpty() && suggestions.isEmpty() ? VISIBLE : GONE);
        }), DEBOUNCE_MS);
    }

    public void release() {
        generation++;
        handler.removeCallbacksAndMessages(null);
    }

    private void loadApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found = getContext().getPackageManager().queryIntentActivities(intent, 0);
        if (found != null) {
            allApps.addAll(found);
        }
        PackageManager pm = getContext().getPackageManager();
        Collections.sort(allApps, (left, right) -> String.valueOf(left.loadLabel(pm))
                .compareToIgnoreCase(String.valueOf(right.loadLabel(pm))));
    }

    @NonNull
    private List<ResolveInfo> matchApps(@NonNull String query) {
        if (query.isEmpty()) {
            return Collections.emptyList();
        }
        String needle = query.toLowerCase();
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> matched = new ArrayList<>();
        for (ResolveInfo info : allApps) {
            String name = String.valueOf(info.loadLabel(pm)).toLowerCase();
            String pkg = info.activityInfo.packageName.toLowerCase();
            if (name.contains(needle) || pkg.contains(needle)) {
                matched.add(info);
            }
        }
        return matched;
    }

    private void openSuggestion(@NonNull String suggestion) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=" + Uri.encode(suggestion)));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    private final class ResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<ResolveInfo> apps = new ArrayList<>();
        private final List<String> suggestions = new ArrayList<>();

        void submit(@NonNull List<ResolveInfo> nextApps, @NonNull List<String> nextSuggestions) {
            apps.clear();
            apps.addAll(nextApps);
            suggestions.clear();
            suggestions.addAll(nextSuggestions);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return position < apps.size() ? TYPE_APP : TYPE_SUGGESTION;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_APP) {
                return new AppHolder(inflater.inflate(R.layout.item_top_panel_app, parent, false));
            }
            return new SuggestionHolder(inflater.inflate(R.layout.item_launcher_search_suggestion, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof AppHolder) {
                ResolveInfo info = apps.get(position);
                PackageManager pm = getContext().getPackageManager();
                ((AppHolder) holder).icon.setImageDrawable(info.loadIcon(pm));
                ((AppHolder) holder).title.setText(info.loadLabel(pm));
                holder.itemView.setOnClickListener(v -> {
                    Intent launch = new Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_LAUNCHER)
                            .setClassName(info.activityInfo.packageName, info.activityInfo.name)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(launch);
                });
                return;
            }
            String suggestion = suggestions.get(position - apps.size());
            ((SuggestionHolder) holder).label.setText(suggestion);
            holder.itemView.setOnClickListener(v -> openSuggestion(suggestion));
        }

        @Override
        public int getItemCount() {
            return apps.size() + suggestions.size();
        }

        final class AppHolder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView title;

            AppHolder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.top_panel_app_icon);
                title = itemView.findViewById(R.id.top_panel_app_title);
            }
        }

        final class SuggestionHolder extends RecyclerView.ViewHolder {
            final TextView label;

            SuggestionHolder(@NonNull View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.launcher_search_suggestion);
            }
        }
    }
}
