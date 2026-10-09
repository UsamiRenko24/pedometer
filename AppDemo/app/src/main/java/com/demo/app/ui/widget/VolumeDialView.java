package com.demo.app.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Canvas 绘制的音量旋钮（Figma 金属盘 + 9→3 半环刻度）。
 * 与 {@link VolumeKnobView} API 对齐，不依赖 PNG。
 */
public class VolumeDialView extends View {

    public interface OnProgressChangeListener {
        void onProgressChanged(int progress, boolean fromUser);
    }

    public interface OnTrackingListener {
        void onTrackingStarted();

        void onTrackingStopped();
    }

    /** 9 点(0) → 12 点(100) → 3 点(200) */
    private static final float START_ANGLE = 180f;
    private static final float SWEEP_ANGLE = 180f;
    private static final int MIN_PROGRESS = 0;
    private static final int MAX_PROGRESS = 200;
    /** 每 5% 一根刻度 → 共 41 根（0…200） */
    private static final int TICK_COUNT = 41;

    private final Paint discPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF discRect = new RectF();
    private final RectF ringRect = new RectF();
    private final PorterDuffXfermode multiplyMode = new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);
    private final PorterDuffXfermode overlayMode = new PorterDuffXfermode(PorterDuff.Mode.OVERLAY);

    private float cx;
    private float cy;
    private float discRadius;
    private float tickInnerRadius;
    private float tickOuterRadius;
    private float majorOuterRadius;
    private float labelRadius;

    private int progress = 0;
    private boolean tracking;
    private OnProgressChangeListener listener;
    private OnTrackingListener trackingListener;

    public VolumeDialView(Context context) {
        super(context);
        init();
    }

    public VolumeDialView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VolumeDialView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);

        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);

        labelPaint.setColor(0xFFD0D0D6);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setFakeBoldText(true);
    }

    public void setProgress(int value) {
        int clamped = clamp(value);
        if (clamped == progress) {
            return;
        }
        progress = clamped;
        invalidate();
        if (listener != null) {
            listener.onProgressChanged(progress, false);
        }
    }

    public int getProgress() {
        return progress;
    }

    public void setOnProgressChangeListener(@Nullable OnProgressChangeListener listener) {
        this.listener = listener;
    }

    public void setOnTrackingListener(@Nullable OnTrackingListener trackingListener) {
        this.trackingListener = trackingListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (tracking || w <= 0 || h <= 0) {
            return;
        }
        updateGeometry(w, h);
    }

    private void updateGeometry(int w, int h) {
        cx = w / 2f;
        cy = h * 0.48f;
        float size = Math.min(w, h);
        discRadius = size * 0.28f;
        tickInnerRadius = discRadius * 1.18f;
        tickOuterRadius = discRadius * 1.38f;
        majorOuterRadius = discRadius * 1.48f;
        labelRadius = discRadius * 1.68f;

        discRect.set(cx - discRadius, cy - discRadius, cx + discRadius, cy + discRadius);
        float ringR = (tickInnerRadius + tickOuterRadius) / 2f;
        float ringStroke = tickOuterRadius - tickInnerRadius;
        ringRect.set(cx - ringR, cy - ringR, cx + ringR, cy + ringR);
        ringPaint.setStrokeWidth(ringStroke);
        glowPaint.setStrokeWidth(ringStroke * 1.35f);

        rebuildShaders();
    }

    private void rebuildShaders() {
        // Figma Angular / SweepGradient — 金属盘
        int[] sweepColors = new int[]{
                0xFFFDFEFF,
                0xCCEDEDED,
                0xFF465066,
                0xFF444B5F,
                0xFF212530,
                0xFF31353F,
                0xFF6A6E77,
                0xCCEDEDED,
                0xFFEDEDED,
                0xCCEDEDED,
                0xFFFDFEFF
        };
        float[] sweepPos = new float[]{
                0f, 0.08f, 0.18f, 0.28f, 0.40f, 0.52f, 0.64f, 0.78f, 0.88f, 0.95f, 1f
        };
        discPaint.setShader(new SweepGradient(cx, cy, sweepColors, sweepPos));

        overlayPaint.setShader(null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (discRadius <= 0f) {
            updateGeometry(getWidth(), getHeight());
            if (discRadius <= 0f) {
                return;
            }
        }

        drawRingTrack(canvas);
        drawTicks(canvas);
        drawProgressGlow(canvas);
        drawMetalDisc(canvas);
        drawLabels(canvas);
    }

    private void drawRingTrack(Canvas canvas) {
        ringPaint.setShader(null);
        ringPaint.setColor(0xFF2A2E39);
        canvas.drawArc(ringRect, START_ANGLE, SWEEP_ANGLE, false, ringPaint);

        ringPaint.setColor(0xFF343A4B);
        ringPaint.setStrokeWidth(ringPaint.getStrokeWidth() * 0.55f);
        canvas.drawArc(ringRect, START_ANGLE, SWEEP_ANGLE, false, ringPaint);
        ringPaint.setStrokeWidth(tickOuterRadius - tickInnerRadius);

        // Overlay 高光近似
        int save = canvas.saveLayer(ringRect.left - 20, ringRect.top - 20,
                ringRect.right + 20, ringRect.bottom + 20, null);
        ringPaint.setShader(new LinearGradient(
                ringRect.left, ringRect.top, ringRect.right, ringRect.bottom,
                new int[]{0xFFFFFFFF, 0x00FFFFFF, 0x80DEEBFF},
                new float[]{0f, 0.45f, 1f},
                Shader.TileMode.CLAMP));
        ringPaint.setXfermode(overlayMode);
        canvas.drawArc(ringRect, START_ANGLE, SWEEP_ANGLE, false, ringPaint);
        ringPaint.setXfermode(null);
        ringPaint.setShader(null);
        canvas.restoreToCount(save);
    }

    private void drawTicks(Canvas canvas) {
        float progressAngle = START_ANGLE + (progress / (float) MAX_PROGRESS) * SWEEP_ANGLE;

        for (int i = 0; i < TICK_COUNT; i++) {
            float t = i / (float) (TICK_COUNT - 1);
            float angle = START_ANGLE + t * SWEEP_ANGLE;
            boolean major = i % 5 == 0;
            boolean active = progress > 0 && angle <= progressAngle + 0.5f;

            float outer = major ? majorOuterRadius : tickOuterRadius;
            float stroke = major ? Math.max(3.5f, discRadius * 0.035f)
                    : Math.max(2f, discRadius * 0.018f);

            if (active) {
                tickPaint.setColor(0xFF9FD9F1);
            } else if (major) {
                tickPaint.setColor(0xCCEDEDED);
            } else {
                tickPaint.setColor(0x336A6E77);
            }
            tickPaint.setStrokeWidth(stroke);

            double rad = Math.toRadians(angle);
            float cos = (float) Math.cos(rad);
            float sin = (float) Math.sin(rad);
            float x0 = cx + cos * tickInnerRadius;
            float y0 = cy + sin * tickInnerRadius;
            float x1 = cx + cos * outer;
            float y1 = cy + sin * outer;
            canvas.drawLine(x0, y0, x1, y1, tickPaint);
        }
    }

    private void drawProgressGlow(Canvas canvas) {
        if (progress <= 0) {
            return;
        }
        float sweep = progress / (float) MAX_PROGRESS * SWEEP_ANGLE;
        glowPaint.setShader(new RadialGradient(
                cx, cy, majorOuterRadius,
                new int[]{0x669FD9F1, 0x339FD9F1, 0x009FD9F1},
                new float[]{0.55f, 0.82f, 1f},
                Shader.TileMode.CLAMP));
        glowPaint.setColor(0xFF9FD9F1);
        canvas.drawArc(ringRect, START_ANGLE, sweep, false, glowPaint);
        glowPaint.setShader(null);

        ringPaint.setColor(0xFF9FD9F1);
        ringPaint.setStrokeWidth((tickOuterRadius - tickInnerRadius) * 0.75f);
        canvas.drawArc(ringRect, START_ANGLE, sweep, false, ringPaint);
        ringPaint.setStrokeWidth(tickOuterRadius - tickInnerRadius);
    }

    private void drawMetalDisc(Canvas canvas) {
        int save = canvas.saveLayer(discRect.left - 4, discRect.top - 4,
                discRect.right + 4, discRect.bottom + 4, null);

        // Angular 金属底
        canvas.drawCircle(cx, cy, discRadius, discPaint);

        // #232323 → #000 线性叠加
        overlayPaint.setXfermode(null);
        overlayPaint.setShader(new LinearGradient(
                discRect.left, discRect.top, discRect.right, discRect.bottom,
                0xFF232323, 0xFF000000, Shader.TileMode.CLAMP));
        overlayPaint.setAlpha(140);
        canvas.drawCircle(cx, cy, discRadius, overlayPaint);

        // #000 @80% Multiply
        overlayPaint.setShader(null);
        overlayPaint.setColor(0xCC000000);
        overlayPaint.setAlpha(255);
        overlayPaint.setXfermode(multiplyMode);
        canvas.drawCircle(cx, cy, discRadius, overlayPaint);
        overlayPaint.setXfermode(null);

        // 顶部暗边线性 24%
        overlayPaint.setShader(new LinearGradient(
                cx, discRect.top, cx, discRect.bottom,
                0x3D000000, 0xFF000000, Shader.TileMode.CLAMP));
        overlayPaint.setAlpha(60);
        canvas.drawCircle(cx, cy, discRadius, overlayPaint);
        overlayPaint.setAlpha(255);
        overlayPaint.setShader(null);

        // 外圈描边
        overlayPaint.setStyle(Paint.Style.STROKE);
        overlayPaint.setStrokeWidth(Math.max(2f, discRadius * 0.03f));
        overlayPaint.setColor(0xFF212530);
        canvas.drawCircle(cx, cy, discRadius - overlayPaint.getStrokeWidth() / 2f, overlayPaint);

        // 内亮环
        overlayPaint.setColor(0x66EDEDED);
        overlayPaint.setStrokeWidth(Math.max(1.5f, discRadius * 0.018f));
        canvas.drawCircle(cx, cy, discRadius * 0.92f, overlayPaint);
        overlayPaint.setStyle(Paint.Style.FILL);

        // 径向淡蓝高光
        overlayPaint.setShader(new RadialGradient(
                cx - discRadius * 0.25f, cy - discRadius * 0.35f, discRadius * 0.9f,
                new int[]{0x559FD9F1, 0x009FD9F1},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, discRadius, overlayPaint);
        overlayPaint.setShader(null);

        // 中心暗核，贴近原图凹面感
        overlayPaint.setShader(new RadialGradient(
                cx, cy, discRadius * 0.55f,
                new int[]{0x00000000, 0x66000000},
                new float[]{0.4f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, cy, discRadius * 0.7f, overlayPaint);
        overlayPaint.setShader(null);

        canvas.restoreToCount(save);
    }

    private void drawLabels(Canvas canvas) {
        labelPaint.setTextSize(Math.min(getWidth(), getHeight()) * 0.036f);
        Paint.FontMetrics fm = labelPaint.getFontMetrics();
        for (int i = 0; i <= 8; i++) {
            float angle = START_ANGLE + (i / 8f) * SWEEP_ANGLE;
            float[] pos = polar(cx, cy, labelRadius, angle);
            float textY = pos[1] - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(String.valueOf(i * 25), pos[0], textY, labelPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (discRadius <= 0f) {
            updateGeometry(getWidth(), getHeight());
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                tracking = true;
                getParent().requestDisallowInterceptTouchEvent(true);
                if (trackingListener != null) {
                    trackingListener.onTrackingStarted();
                }
                updateFromTouch(event.getX(), event.getY());
                return true;
            case MotionEvent.ACTION_MOVE:
                if (tracking) {
                    updateFromTouch(event.getX(), event.getY());
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                tracking = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                updateGeometry(getWidth(), getHeight());
                if (trackingListener != null) {
                    trackingListener.onTrackingStopped();
                }
                return true;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void updateFromTouch(float x, float y) {
        float dx = x - cx;
        float dy = y - cy;
        float touchAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (touchAngle < 0f) {
            touchAngle += 360f;
        }
        float offset = touchAngle - START_ANGLE;
        if (offset < 0f) {
            offset += 360f;
        }
        if (offset > SWEEP_ANGLE) {
            if (offset - SWEEP_ANGLE < (360f - SWEEP_ANGLE) / 2f) {
                offset = SWEEP_ANGLE;
            } else {
                offset = 0f;
            }
        }
        int next = Math.round(MIN_PROGRESS + (offset / SWEEP_ANGLE) * (MAX_PROGRESS - MIN_PROGRESS));
        next = clamp(next);
        if (next != progress) {
            progress = next;
            invalidate();
            if (listener != null) {
                listener.onProgressChanged(progress, true);
            }
        }
    }

    private float[] polar(float centerX, float centerY, float radius, float angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        return new float[]{
                (float) (centerX + Math.cos(radians) * radius),
                (float) (centerY + Math.sin(radians) * radius)
        };
    }

    private int clamp(int value) {
        return Math.max(MIN_PROGRESS, Math.min(MAX_PROGRESS, value));
    }
}
