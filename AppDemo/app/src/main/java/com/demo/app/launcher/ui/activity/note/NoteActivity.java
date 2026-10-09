package com.demo.app.launcher.ui.activity.note;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.databinding.ActivityNoteBinding;
import com.demo.app.launcher.ads.LauncherBackInsertPlaceholder;
import com.demo.app.launcher.ads.LauncherBackPressAds;
import com.demo.app.launcher.ads.LauncherBuiltInAds;
import com.demo.app.launcher.note.NoteEntry;
import com.demo.app.launcher.note.NoteStore;
import com.demo.app.launcher.note.NoteWidgetProvider;
import com.demo.app.launcher.ui.adapter.note.NoteAdapter;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** 笔记列表。支持搜索、多选删除和撤销。 */
public class NoteActivity extends BaseActivity<ActivityNoteBinding> {

    private final List<NoteEntry> allNotes = new ArrayList<>();
    private final List<NoteEntry> notes = new ArrayList<>();
    private final Set<Long> selected = new HashSet<>();
    private final NoteAdapter adapter = new NoteAdapter(new NoteAdapter.Listener() {
        @Override
        public void onOpen(long noteId) {
            NoteEditActivity.start(NoteActivity.this, noteId);
        }

        @Override
        public void onToggle(long noteId) {
            if (!selected.add(noteId)) {
                selected.remove(noteId);
            }
            binding.noteDelete.setVisibility(selected.isEmpty() ? View.GONE : View.VISIBLE);
            adapter.submit(notes, selected);
        }
    });

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, NoteActivity.class);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityNoteBinding getViewBinding() {
        return ActivityNoteBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.noteRoot, (v, insets) -> {
            Insets bars = insets.getInsets(
                    SystemUiHelper.contentInsetTypes());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow(), true, true);
    }

    @Override
    protected void initViews() {
        binding.noteList.setLayoutManager(new GridLayoutManager(this, 2));
        binding.noteList.setAdapter(adapter);
        LauncherBuiltInAds.show(binding.noteBottomAd.getRoot());
        LauncherBackInsertPlaceholder.preload(this);
        LauncherBackPressAds.install(this, this::finish);
    }

    @Override
    protected void initEvents() {
        binding.noteAdd.setOnClickListener(v -> NoteEditActivity.start(this, 0));
        binding.noteCreate.setOnClickListener(v -> NoteEditActivity.start(this, 0));
        binding.noteDelete.setOnClickListener(v -> deleteSelected());
        binding.noteSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilter();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    private void deleteSelected() {
        if (selected.isEmpty()) {
            return;
        }
        NoteStore.delete(new ArrayList<>(selected));
        selected.clear();
        NoteWidgetProvider.refresh(this);
        reload();
        Snackbar.make(binding.noteRoot, R.string.note_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.note_undo, v -> {
                    if (NoteStore.undoDelete()) {
                        NoteWidgetProvider.refresh(NoteActivity.this);
                        reload();
                    }
                })
                .show();
    }

    private void reload() {
        allNotes.clear();
        allNotes.addAll(NoteStore.load());
        selected.retainAll(idsOf(allNotes));
        applyFilter();
    }

    private void applyFilter() {
        String query = binding.noteSearch.getText().toString().trim().toLowerCase(Locale.getDefault());
        notes.clear();
        for (NoteEntry note : allNotes) {
            if (query.isEmpty()
                    || note.title.toLowerCase(Locale.getDefault()).contains(query)
                    || note.body.toLowerCase(Locale.getDefault()).contains(query)) {
                notes.add(note);
            }
        }
        boolean hasNotes = !allNotes.isEmpty();
        boolean searching = !query.isEmpty();
        boolean showList = !notes.isEmpty();
        binding.noteSearch.setVisibility(hasNotes ? View.VISIBLE : View.GONE);
        binding.noteEmpty.setVisibility(!hasNotes && !searching ? View.VISIBLE : View.GONE);
        binding.noteSearchEmpty.setVisibility(hasNotes && searching && !showList ? View.VISIBLE : View.GONE);
        binding.noteList.setVisibility(showList ? View.VISIBLE : View.GONE);
        binding.noteCount.setVisibility(hasNotes ? View.VISIBLE : View.GONE);
        if (hasNotes) {
            binding.noteCount.setText(allNotes.size() == 1
                    ? getString(R.string.note_count_one)
                    : getString(R.string.note_count, allNotes.size()));
        }
        binding.noteDelete.setVisibility(selected.isEmpty() ? View.GONE : View.VISIBLE);
        adapter.submit(notes, selected);
    }

    @NonNull
    private static Set<Long> idsOf(@NonNull List<NoteEntry> source) {
        Set<Long> ids = new HashSet<>();
        for (NoteEntry note : source) {
            ids.add(note.id);
        }
        return ids;
    }
}
