package com.demo.app.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.demo.app.databinding.FragmentGuideSoundBinding;
import com.demo.app.ui.guide.GuideNavigator;
import com.demo.app.utils.AppLog;

/** 引导第 6 页。四条音量柱自动上下滑动，点击进入下一页。 */
public class GuideSoundFragment extends Fragment {

    private FragmentGuideSoundBinding binding;
    private GuideNavigator navigator;

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
        binding = FragmentGuideSoundBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnNext.setOnClickListener(v -> {
            AppLog.i("GuideSoundFragment", "next");
            if (navigator != null) {
                navigator.goNextGuidePage();
            }
        });
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
