package com.demo.app.launcher.ui.activity.note;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.demo.app.R;
import com.demo.app.databinding.ActivityNoteEditBinding;
import com.demo.app.launcher.note.NoteEntry;
import com.demo.app.launcher.note.NoteStore;
import com.demo.app.launcher.note.NoteStyles;
import com.demo.app.launcher.note.NoteWidgetProvider;
import com.demo.app.launcher.note.NoteWidgetStore;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;

/** 新建或编辑一条笔记，可选文字颜色、背景，并添加到桌面。 */
public class NoteEditActivity extends BaseActivity<ActivityNoteEditBinding> {

    public static final String EXTRA_ID = "note_id";

    private long noteId;
    private int textColor;
    private int bgIndex;

    public static void start(@NonNull Context context, long noteId) {
        Intent intent = new Intent(context, NoteEditActivity.class);
        intent.putExtra(EXTRA_ID, noteId);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityNoteEditBinding getViewBinding() {
        return ActivityNoteEditBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.noteEditRoot, (v, insets) -> {
            Insets bars = insets.getInsets(
                    SystemUiHelper.contentInsetTypesWithIme()
                            | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow(), true, true);
    }

    @Override
    protected void initData(@Nullable Bundle bundle) {
        noteId = getIntent().getLongExtra(EXTRA_ID, 0);
        NoteEntry note = NoteStore.find(noteId);
        if (note == null) {
            noteId = 0;
            textColor = 0;
            bgIndex = 0;
            return;
        }
        textColor = note.textColor;
        bgIndex = note.bgIndex;
        binding.noteEditTitle.setText(note.title);
        binding.noteEditBody.setText(note.body);
    }

    @Override
    protected void initViews() {
        applyStyle();
        buildColorChips();
        buildBackgroundChips();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initData(intent.getExtras());
        initViews();
    }

    @Override
    protected void initEvents() {
        binding.noteEditSave.setOnClickListener(v -> {
            if (persist() != null) {
                finish();
            }
        });
        binding.noteEditPin.setOnClickListener(v -> pin());
    }

    private void buildColorChips() {
        binding.noteEditColors.removeAllViews();
        int selected = NoteStyles.textColorIndex(this, textColor);
        int size = dp(28);
        int gap = dp(8);
        for (int i = 0; i < NoteStyles.textColorCount(); i++) {
            View chip = new View(this);
            int color = NoteStyles.textColor(this, i);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            if (color == Color.WHITE) {
                drawable.setStroke(dp(1), getColor(R.color.color_E0E0E0));
            }
            if (i == selected) {
                drawable.setStroke(dp(2), getColor(R.color.color_8E300E));
            }
            chip.setBackground(drawable);
            LinearParams(chip, size, size, i == 0 ? 0 : gap);
            int index = i;
            chip.setOnClickListener(v -> {
                textColor = NoteStyles.textColor(this, index);
                applyStyle();
                buildColorChips();
            });
            binding.noteEditColors.addView(chip);
        }
    }

    private void buildBackgroundChips() {
        binding.noteEditBackgrounds.removeAllViews();
        int size = dp(36);
        int gap = dp(8);
        for (int i = 0; i < NoteStyles.backgroundCount(); i++) {
            FrameLayout chip = new FrameLayout(this);
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setImageResource(NoteStyles.backgroundRes(i));
            chip.addView(image, new FrameLayout.LayoutParams(size, size));
            if (i == bgIndex) {
                View mark = new View(this);
                GradientDrawable ring = new GradientDrawable();
                ring.setShape(GradientDrawable.OVAL);
                ring.setColor(getColor(R.color.color_8E300E));
                mark.setBackground(ring);
                FrameLayout.LayoutParams markParams = new FrameLayout.LayoutParams(dp(10), dp(10));
                markParams.gravity = Gravity.CENTER;
                chip.addView(mark, markParams);
            }
            LinearParams(chip, size, size, i == 0 ? 0 : gap);
            int index = i;
            chip.setOnClickListener(v -> {
                bgIndex = index;
                applyStyle();
                buildBackgroundChips();
            });
            binding.noteEditBackgrounds.addView(chip);
        }
    }

    private void applyStyle() {
        int color = NoteStyles.resolvedTextColor(this, textColor);
        binding.noteEditTitle.setTextColor(color);
        binding.noteEditBody.setTextColor(color);
        binding.noteEditSheet.setBackgroundResource(NoteStyles.backgroundRes(bgIndex));
    }

    private void pin() {
        NoteEntry entry = persist();
        if (entry == null) {
            return;
        }
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        if (!manager.isRequestPinAppWidgetSupported()) {
            Toast.makeText(this, R.string.note_pin_unsupported, Toast.LENGTH_SHORT).show();
            return;
        }
        NoteWidgetStore.setPending(entry.id);
        manager.requestPinAppWidget(
                new ComponentName(this, NoteWidgetProvider.class),
                null,
                null);
    }

    @Nullable
    private NoteEntry persist() {
        String title = binding.noteEditTitle.getText().toString().trim();
        String body = binding.noteEditBody.getText().toString().trim();
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(body)) {
            Toast.makeText(this, R.string.note_body_hint, Toast.LENGTH_SHORT).show();
            return null;
        }
        NoteEntry entry = new NoteEntry();
        entry.id = noteId;
        entry.title = title.isEmpty() ? body : title;
        entry.body = title.isEmpty() ? "" : body;
        entry.textColor = textColor;
        entry.bgIndex = bgIndex;
        NoteStore.save(entry);
        noteId = entry.id;
        NoteWidgetProvider.refresh(this);
        return entry;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static void LinearParams(@NonNull View view, int width, int height, int marginStart) {
        android.widget.LinearLayout.LayoutParams params =
                new android.widget.LinearLayout.LayoutParams(width, height);
        params.setMarginStart(marginStart);
        view.setLayoutParams(params);
    }
}
