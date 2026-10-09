package com.demo.app.launcher.ui.activity.folder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.R;
import com.demo.app.databinding.ActivityFolderBinding;
import com.demo.app.launcher.ui.view.folder.FolderPermissionDialog;

import java.util.ArrayList;
import java.util.List;

/** 文件管理首页：分类、容量和最近文件。 */
public class FolderActivity extends FolderPageActivity<ActivityFolderBinding> {

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> renderPermission());

    private List<FolderEntry> files = new ArrayList<>();

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, FolderActivity.class);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityFolderBinding getViewBinding() {
        return ActivityFolderBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initViews() {
        bindVolume(binding.folderPhoneSize, binding.folderPhoneProgress, binding.folderPhonePercent,
                new FolderRepository.Volume(0, 0));
        bindVolume(binding.folderSdSize, binding.folderSdProgress, binding.folderSdPercent,
                new FolderRepository.Volume(0, 0));
        renderPermission();
    }

    @Override
    protected void initEvents() {
        View.OnClickListener openSearch = v -> FolderSearchActivity.start(this);
        binding.folderToolbarSearch.setOnClickListener(openSearch);
        binding.folderSearchBar.setOnClickListener(openSearch);
        binding.folderPermission.getRoot().setOnClickListener(v ->
                FolderPermissionDialog.open(getSupportFragmentManager()));
        binding.folderCatImages.setOnClickListener(v -> openKind(FolderKind.IMAGE));
        binding.folderCatVideo.setOnClickListener(v -> openKind(FolderKind.VIDEO));
        binding.folderCatMusic.setOnClickListener(v -> openKind(FolderKind.AUDIO));
        binding.folderCatFiles.setOnClickListener(v -> openKind(FolderKind.DOCUMENT));
        binding.folderCatDownload.setOnClickListener(v -> openKind(FolderKind.DOWNLOAD));
        binding.folderCatApk.setOnClickListener(v -> openKind(FolderKind.APK));
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderPermission();
        FolderRepository.load(this, snapshot -> {
            if (isFinishing()) {
                return;
            }
            files = snapshot.files;
            bindVolume(binding.folderPhoneSize, binding.folderPhoneProgress, binding.folderPhonePercent, snapshot.phone);
            bindVolume(binding.folderSdSize, binding.folderSdProgress, binding.folderSdPercent, snapshot.sd);
            renderRecent();
        });
    }

    public void requestStorage() {
        permissionLauncher.launch(FolderAccess.required());
    }

    private void openKind(@NonNull FolderKind kind) {
        FolderCategoryActivity.start(this, kind);
    }

    private void renderPermission() {
        boolean granted = FolderAccess.granted(this);
        binding.folderPermission.getRoot().setVisibility(granted ? View.GONE : View.VISIBLE);
        binding.folderRecentTitle.setVisibility(View.VISIBLE);
        if (!granted) {
            binding.folderRecentList.removeAllViews();
        }
    }

    private void renderRecent() {
        if (!FolderAccess.granted(this)) {
            return;
        }
        binding.folderRecentList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        int shown = 0;
        for (FolderEntry entry : files) {
            if (shown >= 8) {
                break;
            }
            View row = inflater.inflate(R.layout.item_folder_file, binding.folderRecentList, false);
            FolderFileAdapter.bindRow(row, entry, item -> FolderOpener.open(this, item));
            binding.folderRecentList.addView(row);
            shown++;
        }
    }

    private void bindVolume(@NonNull android.widget.TextView size,
                            @NonNull android.widget.ProgressBar progress,
                            @NonNull android.widget.TextView percent,
                            @NonNull FolderRepository.Volume volume) {
        size.setText(getString(R.string.folder_storage_used,
                FolderEntry.sizeText(volume.used), FolderEntry.sizeText(volume.total)));
        int value = volume.total <= 0 ? 0 : (int) Math.min(100L, volume.used * 100L / volume.total);
        progress.setProgress(value);
        percent.setText(getString(R.string.folder_percent, value));
    }
}
