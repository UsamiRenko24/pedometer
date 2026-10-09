package com.demo.app.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewbinding.ViewBinding;

import com.demo.app.integration.language.MultiLanguages;
import com.demo.app.utils.AppLog;
import com.demo.app.utils.SystemUiHelper;

/** 所有页面的基类：沉浸式窗口、跟随应用语言，并按固定顺序完成初始化。 */
public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {
    protected VB binding;

    @Nullable
    protected abstract VB getViewBinding();

    protected void initData(@Nullable Bundle bundle) {
    }

    protected abstract void initViews();

    protected void initEvents() {
    }

    protected void loadData() {
    }

    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            final Insets cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout());
            v.setPadding(cutout.left, cutout.top, cutout.right, 0);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyImmersive();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        try {
            super.attachBaseContext((base != null) ? MultiLanguages.attach(base) : null);
        } catch (Throwable exception) {
            super.attachBaseContext(base);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultiLanguages.updateAppLanguage(this);
        AppLog.i(getClass().getSimpleName(), "onCreate");
        binding = getViewBinding();
        if (binding != null) {
            setContentView(binding.getRoot());
            initWindow(binding.getRoot());
        }
        initData(getIntent().getExtras());
        initViews();
        initEvents();
        loadData();
    }
}
