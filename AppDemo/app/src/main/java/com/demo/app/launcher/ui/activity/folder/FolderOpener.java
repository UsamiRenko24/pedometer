package com.demo.app.launcher.ui.activity.folder;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.demo.app.R;

/** 用系统应用打开一条文件。 */
public final class FolderOpener {

    private FolderOpener() {
    }

    public static void open(@NonNull Context context, @NonNull FolderEntry entry) {
        String mime = entry.mime.isEmpty() ? "*/*" : entry.mime;
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(entry.uri, mime)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
            Toast.makeText(context, R.string.folder_open_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
