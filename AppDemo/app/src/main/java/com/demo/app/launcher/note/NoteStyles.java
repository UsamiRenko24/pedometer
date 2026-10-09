package com.demo.app.launcher.note;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.demo.app.R;

/** 编辑页可选的文字颜色和卡片背景。 */
public final class NoteStyles {

    private static final int[] TEXT_COLORS = {
            R.color.color_white,
            R.color.color_000000,
            R.color.color_FF7D7D,
            R.color.color_F06292,
            R.color.color_FF2D6F,
            R.color.color_D500F9,
            R.color.color_AA00FF,
            R.color.color_651FFF,
            R.color.color_304FFE,
            R.color.color_00B0FF,
            R.color.color_00E5FF,
            R.color.color_1DE9B6,
            R.color.color_12C700,
            R.color.color_AEEA00,
            R.color.color_FFEA00,
            R.color.color_FFAB00,
            R.color.color_FF9100,
            R.color.color_FF8A65,
            R.color.color_A1887F,
            R.color.color_E0E0E0,
    };

    private static final int[] BACKGROUNDS = {
            R.drawable.bg_note_card,
            R.drawable.bg_note_bg_01,
            R.drawable.bg_note_bg_02,
            R.drawable.bg_note_bg_03,
            R.drawable.bg_note_bg_04,
            R.drawable.bg_note_bg_05,
            R.drawable.bg_note_bg_06,
            R.drawable.bg_note_bg_07,
            R.drawable.bg_note_bg_08,
            R.drawable.bg_note_bg_09,
            R.drawable.bg_note_bg_10,
            R.drawable.bg_note_bg_11,
            R.drawable.bg_note_bg_12,
    };

    private NoteStyles() {
    }

    public static int textColorCount() {
        return TEXT_COLORS.length;
    }

    public static int backgroundCount() {
        return BACKGROUNDS.length;
    }

    @ColorInt
    public static int textColor(@NonNull Context context, int index) {
        if (index < 0 || index >= TEXT_COLORS.length) {
            return ContextCompat.getColor(context, R.color.color_1C1C1E);
        }
        return ContextCompat.getColor(context, TEXT_COLORS[index]);
    }

    @ColorInt
    public static int resolvedTextColor(@NonNull Context context, int storedColor) {
        if (storedColor == 0) {
            return ContextCompat.getColor(context, R.color.color_1C1C1E);
        }
        return storedColor;
    }

    public static int textColorIndex(@NonNull Context context, int storedColor) {
        if (storedColor == 0) {
            return -1;
        }
        for (int i = 0; i < TEXT_COLORS.length; i++) {
            if (textColor(context, i) == storedColor) {
                return i;
            }
        }
        return -1;
    }

    @DrawableRes
    public static int backgroundRes(int index) {
        if (index < 0 || index >= BACKGROUNDS.length) {
            return R.drawable.bg_note_card;
        }
        return BACKGROUNDS[index];
    }
}
