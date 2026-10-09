package com.demo.app.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.demo.app.R;

public class VolumeKnobView extends View {

    public interface OnProgressChangeListener {
        void onProgressChanged(int progress, boolean fromUser);
    }

    public interface OnTrackingListener {
        void onTrackingStarted();

        void onTrackingStopped();
    }

    // 刻度弧：9 点(0) → 12 点(100) → 3 点(200)
    private static final float START_ANGLE = 180f;
    private static final float SWEEP_ANGLE = 180f;
    private static final int MIN_PROGRESS = 0;
    private static final int MAX_PROGRESS = 200;
    private static final float KNOB_CX_RATIO = 386f / 751f;
    private static final float KNOB_CY_RATIO = 319f / 883f;
    private static final float TICK_RADIUS_RATIO = 300f / 751f;

    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF imageDest = new RectF();
    private final RectF clipOval = new RectF();
    private final Path clipPath = new Path();

    private static volatile Bitmap cachedGaugeOff;
    private static volatile Bitmap cachedGaugeOn;

    private Bitmap gaugeOff;
    private Bitmap gaugeOn;
    private float knobCx;
    private float knobCy;
    private float tickRadius;
    private int progress = 0;
    private boolean tracking;
    private OnProgressChangeListener listener;
    private OnTrackingListener trackingListener;

    public VolumeKnobView(Context context) {
        super(context);
        init();
    }

    public VolumeKnobView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VolumeKnobView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public static void preload(Context context) {
        if (cachedGaugeOff != null && cachedGaugeOn != null) {
            return;
        }
        synchronized (VolumeKnobView.class) {
            Context app = context.getApplicationContext();
            if (cachedGaugeOff == null) {
                cachedGaugeOff = BitmapFactory.decodeResource(app.getResources(), R.drawable.voice_adjust_no);
            }
            if (cachedGaugeOn == null) {
                cachedGaugeOn = BitmapFactory.decodeResource(app.getResources(), R.drawable.voice_adjust);
            }
        }
    }

    private void init() {
        setClickable(true);
        preload(getContext());
        gaugeOff = cachedGaugeOff;
        gaugeOn = cachedGaugeOn;

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
        if (tracking) {
            return;
        }
        updateGeometry(w, h);
    }

    private void updateGeometry(int viewW, int viewH) {
        Bitmap src = gaugeOn != null ? gaugeOn : gaugeOff;
        if (src == null || viewW <= 0 || viewH <= 0) {
            return;
        }
        float bw = src.getWidth();
        float bh = src.getHeight();
        float scale = Math.min(viewW / bw, viewH / bh) * 0.80f;
        float dw = bw * scale;
        float dh = bh * scale;
        float left = (viewW - dw) / 2f;
        float top = (viewH - dh) / 2f;
        imageDest.set(left, top, left + dw, top + dh);
        knobCx = left + dw * KNOB_CX_RATIO;
        knobCy = top + dh * KNOB_CY_RATIO;
        tickRadius = dw * TICK_RADIUS_RATIO;
        float clipRadius = tickRadius * 1.2f;
        clipOval.set(knobCx - clipRadius, knobCy - clipRadius, knobCx + clipRadius, knobCy + clipRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (imageDest.isEmpty()) {
            updateGeometry(getWidth(), getHeight());
        }

        if (gaugeOff != null) {
            canvas.drawBitmap(gaugeOff, null, imageDest, bitmapPaint);
        }

        if (progress > 0 && gaugeOn != null) {
            float sweep = progress / (float) MAX_PROGRESS * SWEEP_ANGLE;
            clipPath.reset();
            clipPath.moveTo(knobCx, knobCy);
            clipPath.arcTo(clipOval, START_ANGLE, sweep, false);
            clipPath.close();
            canvas.save();
            canvas.clipPath(clipPath);
            canvas.drawBitmap(gaugeOn, null, imageDest, bitmapPaint);
            canvas.restore();
        }

        labelPaint.setTextSize(Math.min(getWidth(), getHeight()) * 0.036f);
        drawLabels(canvas);
    }

    private void drawLabels(Canvas canvas) {
        // 数字对准每 25 的粗刻度，放在刻度外侧不遮挡
        Paint.FontMetrics fm = labelPaint.getFontMetrics();
        float radius = tickRadius * 1.30f;
        for (int i = 0; i <= 8; i++) {
            float angle = START_ANGLE + (i / 8f) * SWEEP_ANGLE;
            float[] pos = polar(knobCx, knobCy, radius, angle);
            float textY = pos[1] - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(String.valueOf(i * 25), pos[0], textY, labelPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (imageDest.isEmpty()) {
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
        float dx = x - knobCx;
        float dy = y - knobCy;
        float touchAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (touchAngle < 0) {
            touchAngle += 360f;
        }
        float offset = touchAngle - START_ANGLE;
        if (offset < 0) {
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

    private float[] polar(float cx, float cy, float radius, float angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        return new float[]{
                (float) (cx + Math.cos(radians) * radius),
                (float) (cy + Math.sin(radians) * radius)
        };
    }

    private int clamp(int value) {
        return Math.max(MIN_PROGRESS, Math.min(MAX_PROGRESS, value));
    }
}
