package com.demo.app.launcher.ui.view.folder;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.demo.app.databinding.DialogFolderPermissionBinding;
import com.demo.app.launcher.ui.activity.folder.FolderActivity;
import com.demo.app.launcher.ui.activity.folder.FolderCategoryActivity;

/** 存储权限说明。确认后由当前页面发起系统授权。 */
public class FolderPermissionDialog extends DialogFragment {

    public static void open(@NonNull FragmentManager manager) {
        if (manager.findFragmentByTag("folder_permission") != null) {
            return;
        }
        new FolderPermissionDialog().show(manager, "folder_permission");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        DialogFolderPermissionBinding binding = DialogFolderPermissionBinding.inflate(inflater, container, false);
        binding.folderPermissionClose.setOnClickListener(v -> dismiss());
        binding.folderPermissionCancel.setOnClickListener(v -> dismiss());
        binding.folderPermissionConfirm.setOnClickListener(v -> {
            dismiss();
            if (getActivity() instanceof FolderActivity home) {
                home.requestStorage();
            } else if (getActivity() instanceof FolderCategoryActivity category) {
                category.requestStorage();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) {
            return;
        }
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
    }
}
