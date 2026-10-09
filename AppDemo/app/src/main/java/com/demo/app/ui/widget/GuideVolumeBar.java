package com.demo.app.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import com.demo.app.R;

/**
 * 引导页竖向音量条。外观取自 guide_page6_view，圆点与填充在轨道内上下滑动。
 */
public class GuideVolumeBar extends View {

    private static final float SRC_W = 120f;
    private static final float SRC_H = 474f;
    private static final float THUMB_SRC = 72f;
    private static final float FILL_SRC_W = 42f;
    private static final float FILL_BOTTOM_INSET = 28f;

    private static Bitmap thumbSprite;
    private static Bitmap fillSprite;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF pillRect = new RectF();
    private final RectF fillRect = new RectF();
    private final RectF thumbRect = new RectF();
    private final Matrix fillMatrix = new Matrix();

    private float level = 0.65f;
    private float levelLow = 0.42f;
    private float levelHigh = 0.82f;
    private int fillStart = 0xFF60DBFC;
    private int fillEnd = 0xFF5C7AFE;
    private int trackColor = 0xFF17479E;
    private int lineColor = 0xFF2756AA;
    private int slideDelay;
    private boolean useImageFill = true;

    private ValueAnimator slideAnimator;

    public GuideVolumeBar(Context context) {
        this(context, null);
    }

    public GuideVolumeBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuideVolumeBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ensureSprites(context);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GuideVolumeBar);
            level = a.getFloat(R.styleable.GuideVolumeBar_gvbLevel, level);
            fillStart = a.getColor(R.styleable.GuideVolumeBar_gvbFillStart, fillStart);
            fillEnd = a.getColor(R.styleable.GuideVolumeBar_gvbFillEnd, fillEnd);
            int trackStart = a.getColor(R.styleable.GuideVolumeBar_gvbTrackStart, trackColor);
            trackColor = a.getColor(R.styleable.GuideVolumeBar_gvbTrackEnd, trackStart);
            slideDelay = a.getInt(R.styleable.GuideVolumeBar_gvbDelay, 0);
            a.recycle();
        }
        level = clamp(level);
        levelLow = clamp(level - 0.22f);
        levelHigh = clamp(level + 0.16f);
        if (levelHigh - levelLow < 0.24f) {
            levelLow = 0.28f;
            levelHigh = 0.86f;
        }
        useImageFill = fillStart == 0xFF60DBFC && fillEnd == 0xFF5C7AFE;
        trackPaint.setColor(trackColor);
        linePaint.setColor(lineColor);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startSlide();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopSlide();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        if (w <= 0f || h <= 0f || thumbSprite == null) {
            return;
        }

        float radius = w / 2f;
        pillRect.set(0f, 0f, w, h);
        canvas.drawRoundRect(pillRect, radius, radius, trackPaint);

        float thumbSize = w * (THUMB_SRC / SRC_W);
        float thumbR = thumbSize / 2f;
        float travelTop = thumbR + w * 0.08f;
        float travelBottom = h - thumbR - w * 0.04f;
        float cy = travelBottom - (travelBottom - travelTop) * level;

        float lineWidth = Math.max(2f, w * (4f / SRC_W));
        linePaint.setStrokeWidth(lineWidth);
        float lineTop = radius * 0.55f;
        canvas.drawLine(w / 2f, lineTop, w / 2f, cy, linePaint);
        canvas.drawCircle(w / 2f, lineTop, lineWidth * 0.85f, linePaint);

        float fillW = w * (FILL_SRC_W / SRC_W);
        float fillBottom = h - h * (FILL_BOTTOM_INSET / SRC_H);
        float fillTop = Math.min(cy + thumbR * 0.2f, fillBottom - fillW);
        fillRect.set((w - fillW) / 2f, fillTop, (w + fillW) / 2f, fillBottom);
        if (useImageFill && fillSprite != null) {
            BitmapShader shader = new BitmapShader(fillSprite, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            fillMatrix.setRectToRect(new RectF(0f, 0f, fillSprite.getWidth(), fillSprite.getHeight()),
                    fillRect, Matrix.ScaleToFit.FILL);
            shader.setLocalMatrix(fillMatrix);
            fillPaint.setShader(shader);
        } else {
            fillPaint.setShader(new LinearGradient(
                    0f, fillRect.top, 0f, fillRect.bottom, fillEnd, fillStart, Shader.TileMode.CLAMP));
        }
        canvas.drawRoundRect(fillRect, fillW / 2f, fillW / 2f, fillPaint);

        thumbRect.set(w / 2f - thumbR, cy - thumbR, w / 2f + thumbR, cy + thumbR);
        canvas.drawBitmap(thumbSprite, null, thumbRect, thumbPaint);
    }

    private void startSlide() {
        if (slideAnimator != null) {
            return;
        }
        slideAnimator = ValueAnimator.ofFloat(levelLow, levelHigh);
        slideAnimator.setDuration(1700L);
        slideAnimator.setStartDelay(slideDelay);
        slideAnimator.setRepeatMode(ValueAnimator.REVERSE);
        slideAnimator.setRepeatCount(ValueAnimator.INFINITE);
        slideAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        slideAnimator.addUpdateListener(animation -> {
            level = (float) animation.getAnimatedValue();
            invalidate();
        });
        slideAnimator.start();
    }

    private void stopSlide() {
        if (slideAnimator != null) {
            slideAnimator.cancel();
            slideAnimator = null;
        }
    }

    private static void ensureSprites(Context context) {
        if (thumbSprite != null && fillSprite != null) {
            return;
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            Bitmap src = BitmapFactory.decodeResource(context.getResources(), R.drawable.guide_page6_view, options);
            if (src == null || src.getWidth() < 120 || src.getHeight() < 450) {
                return;
            }
            thumbSprite = extractThumb(src);
            fillSprite = Bitmap.createBitmap(src, 39, 244, 42, 202);
        } catch (RuntimeException ignored) {
            thumbSprite = null;
            fillSprite = null;
        }
    }

    private static Bitmap extractThumb(Bitmap src) {
        int left = 18;
        int top = 168;
        int sizeW = Math.min(84, src.getWidth() - left);
        int sizeH = Math.min(78, src.getHeight() - top);
        Bitmap crop = Bitmap.createBitmap(src, left, top, Math.max(1, sizeW), Math.max(1, sizeH));
        Bitmap out = crop.copy(Bitmap.Config.ARGB_8888, true);
        for (int y = 0; y < out.getHeight(); y++) {
            for (int x = 0; x < out.getWidth(); x++) {
                int pixel = out.getPixel(x, y);
                int lum = ((pixel >> 16) & 0xFF) + ((pixel >> 8) & 0xFF) + (pixel & 0xFF);
                lum /= 3;
                if (lum < 176) {
                    out.setPixel(x, y, 0);
                } else {
                    int alpha = Math.min(255, (lum - 176) * 255 / 79);
                    out.setPixel(x, y, (alpha << 24) | 0x00FFFFFF);
                }
            }
        }
        return out;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
