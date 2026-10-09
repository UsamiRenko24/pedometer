package com.demo.app.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.demo.app.R;
import com.demo.app.data.enum2.Language;
import com.demo.app.databinding.FragmentGuideLanguageBinding;
import com.demo.app.integration.language.MultiLanguages;
import com.demo.app.ui.guide.GuideNavigator;
import com.demo.app.utils.CacheUtils;
import com.demo.app.utils.AppLog;

/** 引导第 5 页。选择语言并确认后应用到全局，再进入下一页。 */
public class GuideLanguageFragment extends Fragment {

    private FragmentGuideLanguageBinding binding;
    private GuideNavigator navigator;
    private Language selected = Language.ENGLISH;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof GuideNavigator) {
            navigator = (GuideNavigator) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGuideLanguageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.itemEn.setOnClickListener(v -> select(Language.ENGLISH));
        binding.itemZh.setOnClickListener(v -> select(Language.CHINESE));
        binding.itemJa.setOnClickListener(v -> select(Language.JAPANESE));
        binding.itemEs.setOnClickListener(v -> select(Language.SPANISH));
        binding.itemFr.setOnClickListener(v -> select(Language.FRENCH));
        binding.btnConfirm.setOnClickListener(v -> confirm());
        refreshSelectionUi();
    }

    private void select(Language language) {
        selected = language;
        refreshSelectionUi();
    }

    private void refreshSelectionUi() {
        binding.itemEn.setBackgroundResource(
                selected == Language.ENGLISH ? R.drawable.bg_guide_lang_selected : R.drawable.bg_guide_lang_normal);
        binding.itemZh.setBackgroundResource(
                selected == Language.CHINESE ? R.drawable.bg_guide_lang_selected : R.drawable.bg_guide_lang_normal);
        binding.itemJa.setBackgroundResource(
                selected == Language.JAPANESE ? R.drawable.bg_guide_lang_selected : R.drawable.bg_guide_lang_normal);
        binding.itemEs.setBackgroundResource(
                selected == Language.SPANISH ? R.drawable.bg_guide_lang_selected : R.drawable.bg_guide_lang_normal);
        binding.itemFr.setBackgroundResource(
                selected == Language.FRENCH ? R.drawable.bg_guide_lang_selected : R.drawable.bg_guide_lang_normal);
    }

    private void confirm() {
        Context context = requireContext();
        CacheUtils.setCurLanguageName(selected.name());
        AppLog.i("GuideLanguageFragment", "confirm " + selected.name());
        CacheUtils.setFirstSelectLanguage(true);
        CacheUtils.updateFirstLanguageShow(false);
        CacheUtils.isFirstLanguageDoneUse();
        if (selected == Language.DEFAULT) {
            MultiLanguages.clearAppLanguage(context);
        } else {
            MultiLanguages.setAppLanguage(context, selected.getLocale());
        }
        if (navigator != null) {
            navigator.reloadGuideAfterLanguage();
        }
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        navigator = null;
        super.onDetach();
    }
}
