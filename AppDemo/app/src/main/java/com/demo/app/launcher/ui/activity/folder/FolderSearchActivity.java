package com.demo.app.launcher.ui.activity.folder;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.demo.app.databinding.ActivityFolderSearchBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** 搜索和筛选。 */
public class FolderSearchActivity extends FolderPageActivity<ActivityFolderSearchBinding> {

    private final List<FolderEntry> files = new ArrayList<>();
    private FolderFileAdapter adapter;
    private boolean filterOpen = true;

    private final ActivityResultLauncher<Intent> voiceLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null) {
                    return;
                }
                ArrayList<String> matches = result.getData()
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (matches != null && !matches.isEmpty()) {
                    binding.folderSearchInput.setText(matches.get(0));
                    binding.folderSearchInput.setSelection(binding.folderSearchInput.length());
                }
            });

    public static void start(@NonNull android.content.Context context) {
        context.startActivity(new Intent(context, FolderSearchActivity.class));
    }

    @Nullable
    @Override
    protected ActivityFolderSearchBinding getViewBinding() {
        return ActivityFolderSearchBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initViews() {
        adapter = new FolderFileAdapter(entry -> FolderOpener.open(this, entry));
        binding.folderSearchList.setLayoutManager(new LinearLayoutManager(this));
        binding.folderSearchList.setAdapter(adapter);
    }

    @Override
    protected void initEvents() {
        binding.folderSearchBack.setOnClickListener(v -> finish());
        binding.folderFilterHeader.setOnClickListener(v -> toggleFilter());
        binding.folderSearchVoice.setOnClickListener(v -> startVoice());
        binding.folderSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        android.widget.CompoundButton.OnCheckedChangeListener refresh =
                (button, checked) -> applyFilter();
        binding.folderFilterYesterday.setOnCheckedChangeListener(refresh);
        binding.folderFilterWeek.setOnCheckedChangeListener(refresh);
        binding.folderFilterPicture.setOnCheckedChangeListener(refresh);
        binding.folderFilterVideo.setOnCheckedChangeListener(refresh);
        binding.folderFilterAudio.setOnCheckedChangeListener(refresh);
        binding.folderFilterInstall.setOnCheckedChangeListener(refresh);
        binding.folderFilterCompressed.setOnCheckedChangeListener(refresh);
    }

    @Override
    protected void loadData() {
        FolderRepository.load(this, snapshot -> {
            if (isFinishing()) {
                return;
            }
            files.clear();
            files.addAll(snapshot.files);
            applyFilter();
        });
    }

    private void toggleFilter() {
        filterOpen = !filterOpen;
        binding.folderFilterCard.setVisibility(filterOpen ? View.VISIBLE : View.GONE);
        binding.folderFilterArrow.setRotation(filterOpen ? 0f : -90f);
    }

    private void startVoice() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            voiceLauncher.launch(intent);
        } catch (RuntimeException ignored) {
            // 没有语音输入时保留键盘搜索。
        }
    }

    private void applyFilter() {
        String query = binding.folderSearchInput.getText().toString().trim().toLowerCase(Locale.US);
        long start = timeStart();
        List<FolderEntry> matched = new ArrayList<>();
        for (FolderEntry entry : files) {
            if (!query.isEmpty() && !entry.name.toLowerCase(Locale.US).contains(query)) {
                continue;
            }
            if (start > 0 && (entry.modifiedMillis < start || entry.modifiedMillis > timeEnd())) {
                continue;
            }
            if (!typeMatches(entry.kind)) {
                continue;
            }
            matched.add(entry);
        }
        adapter.submit(matched);
        boolean empty = matched.isEmpty();
        binding.folderSearchEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.folderSearchList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private boolean typeMatches(@NonNull FolderKind kind) {
        boolean any = binding.folderFilterPicture.isChecked()
                || binding.folderFilterVideo.isChecked()
                || binding.folderFilterAudio.isChecked()
                || binding.folderFilterInstall.isChecked()
                || binding.folderFilterCompressed.isChecked();
        if (!any) {
            return true;
        }
        if (binding.folderFilterPicture.isChecked() && kind == FolderKind.IMAGE) {
            return true;
        }
        if (binding.folderFilterVideo.isChecked() && kind == FolderKind.VIDEO) {
            return true;
        }
        if (binding.folderFilterAudio.isChecked() && kind == FolderKind.AUDIO) {
            return true;
        }
        if (binding.folderFilterInstall.isChecked() && kind == FolderKind.APK) {
            return true;
        }
        return binding.folderFilterCompressed.isChecked() && kind == FolderKind.COMPRESSED;
    }

    private long timeStart() {
        Calendar calendar = Calendar.getInstance();
        if (binding.folderFilterYesterday.isChecked()) {
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTimeInMillis();
        }
        if (binding.folderFilterWeek.isChecked()) {
            return System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L;
        }
        return 0L;
    }

    private long timeEnd() {
        if (!binding.folderFilterYesterday.isChecked()) {
            return Long.MAX_VALUE;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
