package com.demo.app.ui.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import com.demo.app.R;

/**
 * 引导页表盘。中心按钮用 guide_page7_voice2，
 * 弧条、刻度、100%/200% 和右上角气泡自行绘制。弧条和刻度在 100% 与 200% 之间来回。
 */
public class GuideBoostDialView extends View {

    private static final float SWEEP = 150f;
    private static final float START_ANGLE = 270f - SWEEP / 2f;
    private static final float MIN_FILL = 0.06f;
    private static final int TICK_COUNT = 22;
    private static final int ARC_LEFT = 0xFF0477FC;
    private static final int ARC_RIGHT = 0xFF31F1FC;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubbleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF arcRect = new RectF();
    private final RectF bubbleRect = new RectF();
    private final Path tail = new Path();

    private Bitmap button;
    private float progress;
    private ValueAnimator animator;

    public GuideBoostDialView(Context context) {
        this(context, null);
    }

    public GuideBoostDialView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuideBoostDialView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        button = BitmapFactory.decodeResource(getResources(), R.drawable.guide_page7_voice2, options);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.BUTT);
        trackPaint.setColor(0xFF123F86);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.BUTT);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeCap(Paint.Cap.BUTT);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeCap(Paint.Cap.BUTT);
        tickPaint.setColor(0xFF7AE7FF);
        labelPaint.setColor(0xFFFFFFFF);
        labelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setShadowLayer(10f, 0f, 0f, 0xFF1A8CFF);
        bubbleTextPaint.setColor(0xFFFFFFFF);
        bubbleTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        bubbleTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startMotion();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopMotion();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        if (width <= 0f || height <= 0f) {
            return;
        }
        float visualRadius = Math.min(width * 0.24f, height * 0.195f);
        float gauge = visualRadius * 1.2f;
        float cx = width * 0.5f;
        float cy = height * 0.62f;
        float buttonSize = gauge / 0.46f;
        float stroke = gauge * 0.24f;
        float arcRadius = gauge * 1.18f;
        float filled = MIN_FILL + (1f - MIN_FILL) * progress;

        arcRect.set(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius);
        trackPaint.setStrokeWidth(stroke);
        arcPaint.setStrokeWidth(stroke);
        canvas.drawArc(arcRect, START_ANGLE, SWEEP, false, trackPaint);

        @SuppressLint("DrawAllocation") LinearGradient fillShader = new LinearGradient(
                arcRect.left, cy, arcRect.right, cy,
                ARC_LEFT, ARC_RIGHT, Shader.TileMode.CLAMP);
        float filledSweep = SWEEP * filled;
        glowPaint.setShader(fillShader);
        glowPaint.setStrokeWidth(stroke * 1.25f);
        glowPaint.setAlpha(165);
        canvas.drawArc(arcRect, START_ANGLE, filledSweep, false, glowPaint);
        arcPaint.setShader(fillShader);
        canvas.drawArc(arcRect, START_ANGLE, filledSweep, false, arcPaint);

        float tickGap = gauge * 0.09f;
        float tickInner = arcRadius + stroke * 0.5f + tickGap;
        float tickOuter = tickInner + gauge * 0.14f;
        tickPaint.setStrokeWidth(gauge * 0.035f);
        for (int i = 0; i < TICK_COUNT; i++) {
            float t = i / (float) (TICK_COUNT - 1);
            float angle = (float) Math.toRadians(START_ANGLE + SWEEP * t);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            tickPaint.setColor(lerpColor(ARC_LEFT, ARC_RIGHT, t));
            canvas.drawLine(cx + tickInner * cos, cy + tickInner * sin,
                    cx + tickOuter * cos, cy + tickOuter * sin, tickPaint);
        }

        labelPaint.setTextSize(gauge * 0.16f);
        drawLabelBelow(canvas, "100%", cx, cy, arcRadius, stroke, START_ANGLE);
        drawLabelBelow(canvas, "200%", cx, cy, arcRadius, stroke, START_ANGLE + SWEEP);
        drawBubble(canvas, cx, cy, visualRadius, tickOuter);
        drawButton(canvas, cx, cy, buttonSize);
    }

    private static int lerpColor(int start, int end, float amount) {
        float t = Math.max(0f, Math.min(1f, amount));
        int sr = (start >> 16) & 0xFF;
        int sg = (start >> 8) & 0xFF;
        int sb = start & 0xFF;
        int er = (end >> 16) & 0xFF;
        int eg = (end >> 8) & 0xFF;
        int eb = end & 0xFF;
        int r = (int) (sr + (er - sr) * t);
        int g = (int) (sg + (eg - sg) * t);
        int b = (int) (sb + (eb - sb) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private void drawLabelBelow(Canvas canvas, String text, float cx, float cy,
                                 float arcRadius, float stroke, float degrees) {
        float angle = (float) Math.toRadians(degrees);
        float x = cx + arcRadius * (float) Math.cos(angle);
        float trackBottom = cy + arcRadius * (float) Math.sin(angle) + stroke * 0.5f;
        Paint.FontMetrics metrics = labelPaint.getFontMetrics();
        float textY = trackBottom + labelPaint.getTextSize() * 0.35f - metrics.ascent;
        labelPaint.setStyle(Paint.Style.STROKE);
        labelPaint.setStrokeWidth(labelPaint.getTextSize() * 0.16f);
        labelPaint.setColor(0xFF2E9BFF);
        canvas.drawText(text, x, textY, labelPaint);
        labelPaint.setStyle(Paint.Style.FILL);
        labelPaint.setColor(0xFFFFFFFF);
        canvas.drawText(text, x, textY, labelPaint);
    }

    private void drawBubble(Canvas canvas, float cx, float cy, float visualRadius, float tickOuter) {
        float bubbleW = visualRadius * 1.15f;
        float bubbleH = visualRadius * 0.62f;
        float left = cx + visualRadius * 0.82f;
        float tailDrop = bubbleH * 0.34f;
        float top = cy - tickOuter - bubbleH - tailDrop + visualRadius * 0.28f;
        bubbleRect.set(left, top, left + bubbleW, top + bubbleH);
        bubblePaint.setShader(new LinearGradient(
                bubbleRect.left, bubbleRect.centerY(), bubbleRect.right, bubbleRect.centerY(),
                0xFF30BEFC, 0xFF0873FC, Shader.TileMode.CLAMP));
        float corner = bubbleH * 0.38f;
        canvas.drawRoundRect(bubbleRect, corner, corner, bubblePaint);

        float tailX = bubbleRect.left + bubbleW * 0.34f;
        tail.rewind();
        tail.moveTo(tailX, bubbleRect.bottom - bubbleH * 0.12f);
        tail.lineTo(tailX + bubbleW * 0.16f, bubbleRect.bottom - bubbleH * 0.12f);
        tail.lineTo(tailX + bubbleW * 0.02f, bubbleRect.bottom + bubbleH * 0.34f);
        tail.close();
        canvas.drawPath(tail, bubblePaint);

        bubbleTextPaint.setTextSize(visualRadius * 0.26f);
        Paint.FontMetrics metrics = bubbleTextPaint.getFontMetrics();
        float textY = bubbleRect.centerY() - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText("200%", bubbleRect.centerX(), textY, bubbleTextPaint);
    }

    private void drawButton(Canvas canvas, float cx, float cy, float size) {
        if (button == null) {
            return;
        }
        float left = cx - size / 2f;
        float top = cy - size / 2f;
        canvas.drawBitmap(button, null, new RectF(left, top, left + size, top + size), bitmapPaint);
    }

    private void startMotion() {
        if (animator != null) {
            return;
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1600L);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private void stopMotion() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
}
