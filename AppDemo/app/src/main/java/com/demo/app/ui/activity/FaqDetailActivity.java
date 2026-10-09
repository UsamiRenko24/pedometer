package com.demo.app.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.demo.app.data.enum2.FaqItem;
import com.demo.app.databinding.ActivityFaqDetailBinding;
import com.demo.app.utils.AppLog;
import com.demo.app.utils.DefaultLauncher;

/** FAQ 详情。第 1、2 题底部可打开系统默认桌面设置。 */
public class FaqDetailActivity extends BaseActivity<ActivityFaqDetailBinding> {

    public static final String EXTRA_ITEM = "faq_item";

    /** 打开某一题的详情页。 */
    public static Intent intent(Context context, FaqItem item) {
        return new Intent(context, FaqDetailActivity.class).putExtra(EXTRA_ITEM, item.name());
    }

    private final ActivityResultLauncher<Intent> defaultHomeRequest =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
                    AppLog.i("FaqDetailActivity", "default home result " + result.getResultCode()));

    /** 创建详情页布局。 */
    @Nullable
    @Override
    protected ActivityFaqDetailBinding getViewBinding() {
        return ActivityFaqDetailBinding.inflate(getLayoutInflater());
    }

    /** 显示当前题目的问题和答案，并按题目决定是否显示底部入口。 */
    @Override
    protected void initViews() {
        FaqItem item = readItem();
        AppLog.i("FaqDetailActivity", "show " + item.name() + ", homeBottom=" + item.showHomeBottom());
        binding.tvQuestion.setText(item.getQuestionRes());
        binding.tvAnswer.setText(item.getAnswerRes());
        binding.homeBottom.setVisibility(item.showHomeBottom() ? View.VISIBLE : View.GONE);
    }

    /** 从跳转参数取出题目，参数缺失或无效时回到第一题。 */
    private FaqItem readItem() {
        String name = getIntent().getStringExtra(EXTRA_ITEM);
        if (name == null) {
            return FaqItem.DISORGANIZED;
        }
        try {
            return FaqItem.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return FaqItem.DISORGANIZED;
        }
    }

    /** 返回关闭详情；Default Home Screen 打开系统默认桌面设置。 */
    @Override
    protected void initEvents() {
        binding.btnFaqBack.setOnClickListener(v -> finish());
        binding.btnDefaultHome.setOnClickListener(v -> openDefaultLauncherSettings());
    }

    /** 用带返回结果的方式打开默认桌面设置；打不开时再试主屏幕应用设置。 */
    private void openDefaultLauncherSettings() {
        boolean alreadyDefault = DefaultLauncher.isDefault(this);
        Intent intent = DefaultLauncher.requestIntent(this);
        AppLog.i("FaqDetailActivity", "open default home, alreadyDefault=" + alreadyDefault
                + ", intent=" + intent);
        if (intent == null) {
            intent = new Intent(Settings.ACTION_HOME_SETTINGS);
        }
        try {
            defaultHomeRequest.launch(intent);
        } catch (ActivityNotFoundException first) {
            AppLog.i("FaqDetailActivity", "role request unavailable, open home settings");
            try {
                startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
            } catch (ActivityNotFoundException second) {
                AppLog.i("FaqDetailActivity", "home settings not found");
            }
        }
    }
}
