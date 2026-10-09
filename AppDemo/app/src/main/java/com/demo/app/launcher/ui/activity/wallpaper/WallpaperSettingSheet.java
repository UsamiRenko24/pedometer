package com.demo.app.launcher.ui.activity.wallpaper;

import android.app.Dialog;
import android.app.WallpaperManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.demo.app.R;
import com.demo.app.databinding.DialogWallpaperSettingBinding;
import com.demo.app.launcher.wallpaper.WallpaperActions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/** 壁纸设定：锁屏、桌面，或两者一起。 */
public class WallpaperSettingSheet extends BottomSheetDialogFragment {

    private static final String ARG_IMAGE_URL = "image_url";

    private DialogWallpaperSettingBinding binding;
    private String imageUrl = "";
    private boolean working;

    public static void open(@NonNull FragmentManager manager, @NonNull String imageUrl) {
        WallpaperSettingSheet sheet = new WallpaperSettingSheet();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        sheet.setArguments(args);
        sheet.show(manager, "wallpaper_setting");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(shown -> {
            FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (sheet != null) {
                sheet.setBackgroundColor(Color.TRANSPARENT);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogWallpaperSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        imageUrl = args == null ? "" : args.getString(ARG_IMAGE_URL, "");
        clipCard(binding.wallpaperSettingLock);
        clipCard(binding.wallpaperSettingHome);
        Glide.with(this).load(imageUrl).centerCrop().into(binding.wallpaperSettingLock);
        Glide.with(this).load(imageUrl).centerCrop().into(binding.wallpaperSettingHome);
        binding.wallpaperSettingClose.setOnClickListener(v -> dismiss());
        binding.wallpaperSettingLockAction.setOnClickListener(v ->
                apply(WallpaperManager.FLAG_LOCK));
        binding.wallpaperSettingDesktopAction.setOnClickListener(v ->
                apply(WallpaperManager.FLAG_SYSTEM));
        binding.wallpaperSettingBothAction.setOnClickListener(v ->
                apply(WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK));
    }

    private static void clipCard(@NonNull View preview) {
        if (preview.getParent() instanceof View) {
            View card = (View) preview.getParent();
            card.setClipToOutline(true);
        }
    }

    private void apply(int flags) {
        if (working || imageUrl.isEmpty()) {
            return;
        }
        working = true;
        binding.wallpaperSettingProgress.setVisibility(View.VISIBLE);
        WallpaperActions.apply(requireContext(), imageUrl, flags, success -> {
            working = false;
            if (!isAdded() || binding == null) {
                return;
            }
            binding.wallpaperSettingProgress.setVisibility(View.GONE);
            Toast.makeText(requireContext(),
                    success ? R.string.wallpaper_set_done : R.string.wallpaper_set_failed,
                    Toast.LENGTH_SHORT).show();
            if (success) {
                dismiss();
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
