package com.demo.app.ui.activity;

import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.demo.app.data.enum2.Language;
import com.demo.app.databinding.ActivityLanguageBinding;
import com.demo.app.integration.language.MultiLanguages;
import com.demo.app.ui.adapter.LanguageAdapter;
import com.demo.app.utils.AppLog;
import com.demo.app.utils.CacheUtils;

import java.util.Objects;

/** 设置里的语言页。点某一项立刻切换。 */
public class LanguageActivity extends BaseActivity<ActivityLanguageBinding> {

    private LanguageAdapter adapter;

    @Nullable
    @Override
    protected ActivityLanguageBinding getViewBinding() {
        return ActivityLanguageBinding.inflate(getLayoutInflater());
    }

    /** 列出全部语言，并勾上上次保存的那一项。 */
    @Override
    protected void initViews() {
        adapter = new LanguageAdapter(CacheUtils.getCurLanguageName());
        binding.languageList.setLayoutManager(new LinearLayoutManager(this));
        binding.languageList.setAdapter(adapter);
        RecyclerView.ItemAnimator animator = binding.languageList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    /** 点到哪一种语言就立刻写入，并刷新当前页。 */
    @Override
    protected void initEvents() {
        binding.btnBack.setOnClickListener(v->finish());
        adapter.setOnPickListener(this::applyLanguage);
    }

    private void applyLanguage(Language language) {
        AppLog.i("LanguageActivity", "apply " + language.name());
        if (Objects.equals(CacheUtils.getCurLanguageName(), language.name())) {
            return;
        }
        CacheUtils.setCurLanguageName(language.name());
        if (language == Language.DEFAULT) {
            MultiLanguages.clearAppLanguage(this);
        } else {
            MultiLanguages.setAppLanguage(this, language.getLocale());
        }
        setResult(Activity.RESULT_OK);
        recreate();
    }
}
