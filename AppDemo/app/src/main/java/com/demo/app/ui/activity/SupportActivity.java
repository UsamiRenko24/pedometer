package com.demo.app.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.demo.app.R;
import com.demo.app.databinding.ActivitySupportBinding;
import com.demo.app.utils.AppLog;

import java.util.Objects;

/** 支持页：常见问题、反馈弹窗，以及更换默认桌面。 */
public class SupportActivity extends BaseActivity<ActivitySupportBinding> {

    private static final int FEEDBACK_LIMIT = 300;

    @Nullable
    @Override
    protected ActivitySupportBinding getViewBinding() {
        return ActivitySupportBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initViews() {
    }

    @Override
    protected void initEvents() {
        binding.btnSupportBack.setOnClickListener(v -> finish());
        binding.rowFaq.setOnClickListener(v ->
                startActivity(new Intent(this, FaqActivity.class)));
        binding.rowFeedback.setOnClickListener(v -> showFeedbackDialog());
        binding.rowChangeHome.setOnClickListener(v ->
                startActivity(AboutActivity.forChangeHome(this)));
    }

    private void showFeedbackDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_feedback);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        android.widget.EditText input = dialog.findViewById(R.id.et_feedback);
        android.widget.TextView count = dialog.findViewById(R.id.tv_feedback_count);
        count.setText(getString(R.string.feedback_count, 0));
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s == null ? 0 : Math.min(s.length(), FEEDBACK_LIMIT);
                count.setText(getString(R.string.feedback_count, length));
            }
        });
        dialog.findViewById(R.id.btn_feedback_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_feedback_submit).setOnClickListener(v -> {
            AppLog.i("SupportActivity", "feedback submitted");
            dialog.dismiss();
        });
        dialog.show();
        View dialogRoot = dialog.findViewById(R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(dialogRoot, (v, insets) -> {
            WindowInsetsCompat windowInsets = WindowInsetsCompat.toWindowInsetsCompat(Objects.requireNonNull(insets.toWindowInsets()));
            int imeBottom = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), imeBottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
