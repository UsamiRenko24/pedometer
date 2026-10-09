package com.demo.app.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.demo.app.databinding.FragmentGuidePlaceholderBinding;
import com.demo.app.utils.AppLog;

/** 引导占位页。正式页面都已接入后，这个类不再被使用。 */
public class GuidePlaceholderFragment extends Fragment {

    private static final String ARG_INDEX = "guide_index";

    public static GuidePlaceholderFragment newInstance(int index) {
        GuidePlaceholderFragment f = new GuidePlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INDEX, index);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        AppLog.i("GuidePlaceholderFragment", "create placeholder");
        return FragmentGuidePlaceholderBinding.inflate(inflater, container, false).getRoot();
    }
}
