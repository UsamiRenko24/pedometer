package com.demo.app.launcher.ui.activity.folder;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.demo.app.databinding.ActivityFolderCategoryBinding;
import com.demo.app.launcher.ui.view.folder.FolderPermissionDialog;

import java.util.ArrayList;
import java.util.List;

/** 分类文件页。没有文件时显示对应的空状态。 */
public class FolderCategoryActivity extends FolderPageActivity<ActivityFolderCategoryBinding> {

    public static final String EXTRA_KIND = "folder_kind";

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> reload());

    private FolderKind kind = FolderKind.IMAGE;
    private FolderFileAdapter adapter;

    public static void start(@NonNull Context context, @NonNull FolderKind kind) {
        Intent intent = new Intent(context, FolderCategoryActivity.class);
        intent.putExtra(EXTRA_KIND, kind.name());
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityFolderCategoryBinding getViewBinding() {
        return ActivityFolderCategoryBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initData(@Nullable android.os.Bundle bundle) {
        if (bundle != null && bundle.containsKey(EXTRA_KIND)) {
            kind = FolderKind.from(bundle.getString(EXTRA_KIND, FolderKind.IMAGE.name()));
        }
    }

    @Override
    protected void initViews() {
        binding.folderCategoryTitle.setText(kind.pageTitle);
        binding.folderCategoryEmptyIcon.setImageResource(kind.emptyIcon);
        binding.folderCategoryEmptyText.setText(kind.emptyText);
        adapter = new FolderFileAdapter(entry -> FolderOpener.open(this, entry));
        binding.folderCategoryList.setLayoutManager(new LinearLayoutManager(this));
        binding.folderCategoryList.setAdapter(adapter);
        renderPermission();
    }

    @Override
    protected void initEvents() {
        binding.folderCategoryBack.setOnClickListener(v -> finish());
        binding.folderPermission.getRoot().setOnClickListener(v ->
                FolderPermissionDialog.open(getSupportFragmentManager()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    public void requestStorage() {
        permissionLauncher.launch(FolderAccess.required());
    }

    private void reload() {
        renderPermission();
        FolderRepository.load(this, snapshot -> {
            if (isFinishing()) {
                return;
            }
            List<FolderEntry> matched = new ArrayList<>();
            for (FolderEntry entry : snapshot.files) {
                if (entry.kind == kind) {
                    matched.add(entry);
                }
            }
            adapter.submit(matched);
            boolean empty = matched.isEmpty();
            binding.folderCategoryEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.folderCategoryList.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    private void renderPermission() {
        binding.folderPermission.getRoot().setVisibility(FolderAccess.granted(this) ? View.GONE : View.VISIBLE);
    }
}
