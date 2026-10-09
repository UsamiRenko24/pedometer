package com.demo.app.launcher.ui.activity.wallpaper;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.demo.app.R;
import com.demo.app.databinding.DialogWallpaperDesktopPreviewBinding;
import com.demo.app.utils.SystemUiHelper;

/** 壁纸详情：全屏桌面效果预览（Launcher 桌面四段布局）。 */
public class WallpaperDesktopPreviewDialog extends DialogFragment {

    private static final String ARG_IMAGE_URL = "image_url";
    private static final String TAG = "wallpaper_desktop_preview";

    private DialogWallpaperDesktopPreviewBinding binding;

    public static void open(@NonNull FragmentManager manager, @NonNull String imageUrl) {
        if (imageUrl.isEmpty() || manager.findFragmentByTag(TAG) != null) {
            return;
        }
        WallpaperDesktopPreviewDialog dialog = new WallpaperDesktopPreviewDialog();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        dialog.setArguments(args);
        dialog.show(manager, TAG);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogWallpaperDesktopPreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        String imageUrl = args == null ? "" : args.getString(ARG_IMAGE_URL, "");
        if (!imageUrl.isEmpty()) {
            Glide.with(binding.wallpaperDesktopPreviewImage)
                    .load(imageUrl)
                    .centerCrop()
                    .into(binding.wallpaperDesktopPreviewImage);
        }
        bindDesktopApps();
        binding.wallpaperDesktopPreviewClose.setOnClickListener(v -> dismiss());
        ViewCompat.setOnApplyWindowInsetsListener(binding.wallpaperDesktopPreviewRoot, (v, insets) -> {
            Insets bars = insets.getInsets(
                    SystemUiHelper.contentInsetTypes());
            ViewGroup.MarginLayoutParams closeLp =
                    (ViewGroup.MarginLayoutParams) binding.wallpaperDesktopPreviewClose.getLayoutParams();
            closeLp.topMargin = bars.top + 12;
            closeLp.setMarginEnd(bars.right + 12);
            binding.wallpaperDesktopPreviewClose.setLayoutParams(closeLp);

            ViewGroup.MarginLayoutParams adLp =
                    (ViewGroup.MarginLayoutParams) binding.wallpaperDesktopPreviewAd.getLayoutParams();
            adLp.topMargin = bars.top + 16;
            binding.wallpaperDesktopPreviewAd.setLayoutParams(adLp);

            ViewGroup.LayoutParams bottomParams = binding.wallpaperDesktopPreviewBottomAd.getLayoutParams();
            if (bottomParams instanceof ConstraintLayout.LayoutParams) {
                ConstraintLayout.LayoutParams bottomLp = (ConstraintLayout.LayoutParams) bottomParams;
                bottomLp.bottomMargin = bars.bottom;
                bottomLp.setMarginStart(bars.left);
                bottomLp.setMarginEnd(bars.right);
                binding.wallpaperDesktopPreviewBottomAd.setLayoutParams(bottomLp);
            }
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(binding.wallpaperDesktopPreviewRoot);
    }

    private void bindDesktopApps() {
        bindApp(binding.wallpaperDesktopPreviewAppVolume.getRoot(),
                R.drawable.spalsh_round,
                R.string.guide_app_name);
        bindApp(binding.wallpaperDesktopPreviewAppNews.getRoot(),
                R.drawable.icon_home_app_news,
                R.string.screen0_news);
        bindApp(binding.wallpaperDesktopPreviewAppWeather.getRoot(),
                R.drawable.icon_home_app_weather,
                R.string.screen0_weather);
        bindApp(binding.wallpaperDesktopPreviewAppGoogle.getRoot(),
                R.drawable.icon_home_app_folder,
                R.string.screen0_folder_google);
    }

    private void bindApp(@NonNull View slotRoot, int iconRes, @StringRes int labelRes) {
        ImageView icon = slotRoot.findViewById(R.id.wallpaper_desktop_preview_app_icon);
        TextView label = slotRoot.findViewById(R.id.wallpaper_desktop_preview_app_label);
        icon.setImageResource(iconRes);
        label.setText(labelRes);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        SystemUiHelper.applyVolumeControlImmersive(window, true, false);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
