package com.demo.app.launcher.faq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.data.enum2.FaqItem;
import com.demo.app.databinding.ActivityLauncherFaqBinding;
import androidx.activity.OnBackPressedCallback;

import com.demo.app.launcher.ads.LauncherBackInsertPlaceholder;
import com.demo.app.launcher.ads.LauncherBottomBannerPlaceholderView;
import com.demo.app.ui.activity.BaseActivity;

/** 桌面常见问题列表。 */
public class LauncherFaqActivity extends BaseActivity<ActivityLauncherFaqBinding> {

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, LauncherFaqActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityLauncherFaqBinding getViewBinding() {
        return ActivityLauncherFaqBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initViews() {
        binding.launcherFaqList.setLayoutManager(new LinearLayoutManager(this));
        binding.launcherFaqList.setAdapter(new LauncherFaqAdapter(item ->
                LauncherFaqDetailsActivity.start(this, item)));
        LauncherBottomBannerPlaceholderView banner = binding.launcherFaqActFlBanner;
        if (banner != null) {
            banner.refreshVisibility();
        }
        LauncherBackInsertPlaceholder.preload(this);
    }

    @Override
    protected void initEvents() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onFaqBack();
            }
        });
        binding.launcherFaqBack.setOnClickListener(v -> onFaqBack());
    }

    private void onFaqBack() {
        if (LauncherBackInsertPlaceholder.isReady()) {
            LauncherBackInsertPlaceholder.show(this, this::finish);
        } else {
            finish();
        }
    }

    private static final class LauncherFaqAdapter extends RecyclerView.Adapter<LauncherFaqAdapter.Holder> {

        interface OnItemClick {
            void onClick(@NonNull FaqItem item);
        }

        private final FaqItem[] items = FaqItem.values();
        private final OnItemClick click;

        LauncherFaqAdapter(@NonNull OnItemClick click) {
            this.click = click;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_launcher_faq, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            FaqItem item = items[position];
            holder.title.setText(item.getQuestionRes());
            holder.itemView.setOnClickListener(v -> click.onClick(item));
        }

        @Override
        public int getItemCount() {
            return items.length;
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final TextView title;

            Holder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.item_launcher_faq_title);
            }
        }
    }
}
