package com.demo.app.launcher.ui.view.quickplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Play Hub 顶栏：底边为笑脸形（左右低、中间向上汇成一点，白底从中间「顶」上来）。
 */
public class QuickPlayWaveHeaderLayout extends FrameLayout {

    private final Path smileClip = new Path();
    private int lastWidth;
    private int lastHeight;

    public QuickPlayWaveHeaderLayout(@NonNull Context context) {
        this(context, null);
    }

    public QuickPlayWaveHeaderLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickPlayWaveHeaderLayout(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != lastWidth || h != lastHeight) {
            lastWidth = w;
            lastHeight = h;
            rebuildSmileClip(w, h);
        }
    }

    /**
     * 底边：左侧 → 中间最高点（白底探入）→ 右侧，对称笑脸弧。
     */
    private void rebuildSmileClip(float w, float h) {
        smileClip.reset();
        if (w <= 0 || h <= 0) {
            return;
        }
        float sideY = h - dp(4f);
        float peakY = h - dp(44f);

        smileClip.moveTo(0f, 0f);
        smileClip.lineTo(w, 0f);
        smileClip.lineTo(w, sideY);
        smileClip.quadTo(w * 0.5f, peakY, 0f, sideY);
        smileClip.close();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(smileClip);
        super.draw(canvas);
        canvas.restoreToCount(save);
    }

    private float dp(float value) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}
