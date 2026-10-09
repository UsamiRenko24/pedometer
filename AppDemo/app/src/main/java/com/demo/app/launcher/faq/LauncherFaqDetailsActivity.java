package com.demo.app.launcher.faq;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.data.enum2.FaqItem;
import com.demo.app.databinding.ActivityLauncherFaqDetailsBinding;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.DefaultLauncher;

/** 桌面常见问题详情。第 1、2 题可以打开系统默认桌面设置。 */
public class LauncherFaqDetailsActivity extends BaseActivity<ActivityLauncherFaqDetailsBinding> {

    private static final String EXTRA_ITEM = "launcher_faq_item";

    private FaqItem item = FaqItem.DISORGANIZED;

    public static void start(@NonNull Context context, @NonNull FaqItem item) {
        context.startActivity(new Intent(context, LauncherFaqDetailsActivity.class)
                .putExtra(EXTRA_ITEM, item.name()));
    }

    private final ActivityResultLauncher<Intent> defaultHomeRequest =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            });

    @Nullable
    @Override
    protected ActivityLauncherFaqDetailsBinding getViewBinding() {
        return ActivityLauncherFaqDetailsBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initViews() {
        String name = getIntent().getStringExtra(EXTRA_ITEM);
        if (name != null) {
            try {
                item = FaqItem.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                finish();
                return;
            }
        }
        binding.launcherFaqDetailTitle.setText(item.getQuestionRes());
        binding.launcherFaqDetailContent.setText(item.getAnswerRes());
        binding.launcherFaqDetailButton.setVisibility(
                item.showHomeBottom() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void initEvents() {
        binding.launcherFaqDetailBack.setOnClickListener(v -> finish());
        binding.launcherFaqDetailButton.setOnClickListener(v -> openDefaultHome());
    }

    private void openDefaultHome() {
        Intent intent = DefaultLauncher.requestIntent(this);
        if (intent == null) {
            intent = new Intent(Settings.ACTION_HOME_SETTINGS);
        }
        try {
            defaultHomeRequest.launch(intent);
        } catch (ActivityNotFoundException first) {
            try {
                startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
            } catch (ActivityNotFoundException ignored) {
            }
        }
    }
}
