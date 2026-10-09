package com.demo.app.ui.activity;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.MainActivity;
import com.demo.app.data.enum2.FaqItem;
import com.demo.app.databinding.ActivityAboutBinding;

/** 关于页。从更换默认桌面进来时，继续进入恢复布局说明。 */
public class AboutActivity extends BaseActivity<ActivityAboutBinding> {

    private static final String EXTRA_CONTINUE_RESTORE = "about_continue_restore";

    @NonNull
    public static Intent forChangeHome(@NonNull Context context) {
        return new Intent(context, AboutActivity.class).putExtra(EXTRA_CONTINUE_RESTORE, true);
    }

    @Nullable
    @Override
    protected ActivityAboutBinding getViewBinding() {
        return ActivityAboutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initViews() {
    }

    @Override
    protected void initEvents() {
        boolean restore = getIntent().getBooleanExtra(EXTRA_CONTINUE_RESTORE, false);
        binding.btnAboutBack.setOnClickListener(v -> leave(restore));
        binding.btnAboutBottom.setOnClickListener(v -> leave(restore));
        binding.btnAboutContinue.setOnClickListener(v -> {
            if (restore) {
                startActivity(FaqDetailActivity.intent(this, FaqItem.RESTORE_LAYOUT));
                return;
            }
            startActivity(new Intent(this, FaqActivity.class));
        });
    }

    private void leave(boolean restore) {
        if (restore) {
            finish();
            return;
        }
        returnToSettings();
    }

    private void returnToSettings() {
        Intent main = new Intent(this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        main.putExtra(MainActivity.EXTRA_OPEN_SETTINGS, true);
        startActivity(main);
        finish();
    }
}
