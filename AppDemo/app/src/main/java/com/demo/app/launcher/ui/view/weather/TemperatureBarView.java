package com.demo.app.launcher.ui.view.weather;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/** 10 天预报里的温度区间条。位置先按占位比例绘制。 */
public class TemperatureBarView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float start = 0.18f;
    private float end = 0.82f;

    public TemperatureBarView(Context context) {
        this(context, null);
    }

    public TemperatureBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TemperatureBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        trackPaint.setColor(0xFFE4EBF3);
    }

    public void setSpan(float start, float end) {
        this.start = start;
        this.end = end;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        float radius = height / 2f;
        canvas.drawRoundRect(0f, 0f, width, height, radius, radius, trackPaint);
        float left = width * start;
        float right = width * end;
        if (right <= left) {
            return;
        }
        fillPaint.setShader(new LinearGradient(
                left, 0f, right, 0f,
                0xFF5BC8FF, 0xFFFFB15A, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(left, 0f, right, height, radius, radius, fillPaint);
    }
}
