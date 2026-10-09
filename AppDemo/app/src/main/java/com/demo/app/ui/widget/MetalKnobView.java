package com.demo.app.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 按 Figma SVG（node 133-555）自绘：底座 + 金属盘 + 8→4 点刻度环 + 指示点。
 */
public class MetalKnobView extends View {

    public interface OnAngleChangeListener {
        void onAngleChanged(float angle, float percent);
    }

    public interface OnProgressChangeListener {
        void onProgressChanged(int progress, boolean fromUser);
    }

    public interface OnTrackingListener {
        void onTrackingStarted();

        void onTrackingStopped();
    }

    /** 8 点(0) → 4 点，默认 0–200。 */
    private static final float START_ANGLE = 150f;
    private static final float SWEEP_ANGLE = 240f;
    private static final int MIN_PROGRESS = 0;
    private static final int DEFAULT_MAX_PROGRESS = 200;
    private static final int TOTAL_TICK_COUNT = 41;

    /** SVG conic：暗蓝 → #009DFF → #FF8800 → #FF0000 → #1B2326 */
    private static final int COLOR_TICK_START = Color.rgb(17, 77, 113);
    private static final int COLOR_AT_100 = Color.parseColor("#009DFF");
    private static final int COLOR_AT_150 = Color.parseColor("#FF8800");
    private static final int COLOR_AT_200 = Color.parseColor("#FF0000");
    private static final int COLOR_INACTIVE = Color.parseColor("#1B2326");
    private static final int COLOR_DOT = Color.parseColor("#9FD9F1");
    /** 指示点固定在金属盘右上方（约 1:30，对应 SVG） */
    private static final float INDICATOR_ANGLE = -46f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final PorterDuffXfermode overlayMode = new PorterDuffXfermode(PorterDuff.Mode.OVERLAY);
    private final PorterDuffXfermode multiplyMode = new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY);

    private float centerX;
    private float centerY;
    /** 金属盘半径（对应 SVG r≈90.6） */
    private float metalR;
    /** 底座半径（对应 SVG r≈100） */
    private float baseR;
    private float tickInnerR;
    private float tickOuterR;
    private float labelR;

    private SweepGradient metalSweep;
    private LinearGradient baseGradient;
    private LinearGradient rimOverlayGradient;
    private final Matrix metalMatrix = new Matrix();

    private int progress;
    private int maxProgress = DEFAULT_MAX_PROGRESS;
    private boolean tracking;

    private OnAngleChangeListener angleChangeListener;
    private OnProgressChangeListener progressChangeListener;
    private OnTrackingListener trackingListener;

    public MetalKnobView(Context context) {
        super(context);
        init();
    }

    public MetalKnobView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MetalKnobView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        shadowPaint.setShadowLayer(18f, 0f, 14f, Color.argb(120, 0, 0, 0));
        labelPaint.setColor(0xFFD0D0D6);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setFakeBoldText(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // SVG 圆心偏上：cy/height ≈ 122/295
        centerX = w / 2f;
        centerY = h * 0.42f;
        float size = Math.min(w, h);
        // 251 宽、金属 r=90.6 → 约占半宽的 0.72；收窄到 0.27 给两侧刻度文字留边距
        metalR = size * 0.27f;
        baseR = metalR * (100.146f / 90.6433f);
        // 刻度等长，整体再往外一点
        tickInnerR = metalR * 1.24f;
        tickOuterR = metalR * 1.52f;
        labelR = metalR * 1.66f;
        rebuildShaders();
    }

    private void rebuildShaders() {
        // 白高光放在 0 / 0.5，再旋转 -90° → 落在 12 点与 6 点
        int[] metalColors = new int[]{
                Color.argb(255, 251, 252, 252),
                Color.argb(220, 237, 237, 237),
                Color.rgb(86, 93, 113),
                Color.rgb(42, 46, 57),
                Color.rgb(33, 37, 48),
                Color.rgb(49, 53, 63),
                Color.rgb(70, 80, 102),
                Color.argb(220, 237, 237, 237),
                Color.argb(255, 251, 252, 252),
                Color.argb(220, 237, 237, 237),
                Color.rgb(67, 74, 94),
                Color.rgb(33, 37, 48),
                Color.rgb(42, 46, 57),
                Color.rgb(86, 89, 97),
                Color.rgb(106, 110, 119),
                Color.argb(220, 237, 237, 237),
                Color.argb(255, 251, 252, 252)
        };
        float[] metalPos = new float[]{
                0f, 0.04f, 0.10f, 0.16f, 0.22f, 0.30f, 0.38f,
                0.46f, 0.50f, 0.54f, 0.62f, 0.70f, 0.78f,
                0.86f, 0.92f, 0.97f, 1f
        };
        metalSweep = new SweepGradient(centerX, centerY, metalColors, metalPos);
        metalMatrix.reset();
        metalMatrix.setRotate(-90f, centerX, centerY);
        metalSweep.setLocalMatrix(metalMatrix);

        baseGradient = new LinearGradient(
                centerX, centerY - baseR,
                centerX, centerY + baseR,
                new int[]{Color.parseColor("#232323"), Color.BLACK},
                new float[]{0.46f, 1f},
                Shader.TileMode.CLAMP);

        rimOverlayGradient = new LinearGradient(
                centerX, centerY - metalR,
                centerX, centerY + metalR,
                new int[]{Color.WHITE, Color.TRANSPARENT, Color.argb(128, 222, 235, 255)},
                new float[]{0f, 0.345f, 0.735f},
                Shader.TileMode.CLAMP);
    }

    private float angleForProgress(float p) {
        return START_ANGLE + (p / maxProgress) * SWEEP_ANGLE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (metalR <= 0f) {
            return;
        }

        drawBase(canvas);
        drawTicks(canvas);
        drawMetalDisc(canvas);
        drawPointer(canvas);
        drawLabels(canvas);
    }

    private void drawBase(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(baseGradient);
        canvas.drawCircle(centerX, centerY, baseR, shadowPaint);
        canvas.drawCircle(centerX, centerY, baseR, paint);
        paint.setShader(null);

        // 白边 Screen 近似：低透明白描边
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, metalR * 0.008f));
        paint.setColor(Color.argb(51, 255, 255, 255));
        canvas.drawCircle(centerX, centerY, baseR, paint);
    }

    private void drawMetalDisc(Canvas canvas) {
        int save = canvas.saveLayer(
                centerX - metalR - 4, centerY - metalR - 4,
                centerX + metalR + 4, centerY + metalR + 4, null);

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(metalSweep);
        canvas.drawCircle(centerX, centerY, metalR, paint);
        paint.setShader(null);

        // Overlay 描边（SVG paint2）
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.5f, metalR * 0.012f));
        paint.setShader(rimOverlayGradient);
        paint.setXfermode(overlayMode);
        canvas.drawCircle(centerX, centerY, metalR, paint);
        paint.setXfermode(null);
        paint.setShader(null);

        // 内圈细环（SVG 半透明环）
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2.5f, metalR * 0.05f));
        paint.setColor(Color.argb(40, 0, 0, 0));
        canvas.drawCircle(centerX, centerY, metalR * (80.4f / 90.64f), paint);

        // 内银环
        paint.setColor(Color.argb(80, 237, 237, 237));
        paint.setStrokeWidth(Math.max(2f, metalR * 0.018f));
        canvas.drawCircle(centerX, centerY, metalR * 0.82f, paint);

        canvas.restoreToCount(save);
    }

    /** 从圆心指向当前进度，长度为金属盘半径的一半 */
    private void drawPointer(Canvas canvas) {
        float angle = angleForProgress(progress);
        double rad = Math.toRadians(angle);
        float len = metalR * 0.75f;
        float endX = centerX + len * (float) Math.cos(rad);
        float endY = centerY + len * (float) Math.sin(rad);

        paint.setShader(null);
        paint.setXfermode(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(Math.max(4f, metalR * 0.035f));
        paint.setColor(Color.parseColor("#E8EEF5"));
        canvas.drawLine(centerX, centerY, endX, endY, paint);

        // 中心轴点
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#C8D0DC"));
        canvas.drawCircle(centerX, centerY, Math.max(5f, metalR * 0.045f), paint);
        paint.setColor(Color.parseColor("#2A2E39"));
        canvas.drawCircle(centerX, centerY, Math.max(2.5f, metalR * 0.02f), paint);
    }

    private void drawTicks(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setShader(null);
        paint.setXfermode(null);

        for (int i = 0; i < TOTAL_TICK_COUNT; i++) {
            float value = i / (float) (TOTAL_TICK_COUNT - 1) * maxProgress;
            float angle = angleForProgress(value);
            boolean major = i % 5 == 0;
            boolean active = progress > 0 && value <= progress + 0.01f;

            // 长度一致；0/25/50… 等主刻度加粗
            paint.setStrokeWidth(major ? Math.max(5.5f, metalR * 0.048f)
                    : Math.max(2.4f, metalR * 0.02f));
            paint.setColor(active ? colorForValue(value) : COLOR_INACTIVE);

            double rad = Math.toRadians(angle);
            float cos = (float) Math.cos(rad);
            float sin = (float) Math.sin(rad);
            canvas.drawLine(
                    centerX + cos * tickInnerR, centerY + sin * tickInnerR,
                    centerX + cos * tickOuterR, centerY + sin * tickOuterR,
                    paint);
        }
    }

    /** 颜色比刻度数值提前 25：100 处用原先 125 的颜色。 */
    private static int colorForValue(float value) {
        float v = Math.max(0f, Math.min(DEFAULT_MAX_PROGRESS, value + 25f));
        if (v <= 100f) {
            return lerpColor(COLOR_TICK_START, COLOR_AT_100, v / 100f);
        }
        if (v <= 150f) {
            return lerpColor(COLOR_AT_100, COLOR_AT_150, (v - 100f) / 50f);
        }
        return lerpColor(COLOR_AT_150, COLOR_AT_200, (v - 150f) / 50f);
    }

    private static int lerpColor(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = Color.alpha(from) + Math.round((Color.alpha(to) - Color.alpha(from)) * t);
        int r = Color.red(from) + Math.round((Color.red(to) - Color.red(from)) * t);
        int g = Color.green(from) + Math.round((Color.green(to) - Color.green(from)) * t);
        int b = Color.blue(from) + Math.round((Color.blue(to) - Color.blue(from)) * t);
        return Color.argb(a, r, g, b);
    }

    private void drawIndicatorDot(Canvas canvas) {
        // 固定在右上方，不随进度移动
        float dist = metalR * 0.92f;
        double rad = Math.toRadians(INDICATOR_ANGLE);
        float dx = centerX + dist * (float) Math.cos(rad);
        float dy = centerY + dist * (float) Math.sin(rad);
        float dotR = metalR * 0.028f;

        // 底色 + 径向光
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(
                dx, dy - dotR * 0.4f, dotR * 1.2f,
                new int[]{Color.WHITE, COLOR_DOT, Color.argb(0, 159, 217, 241)},
                new float[]{0f, 0.55f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawCircle(dx, dy, dotR, paint);
        paint.setShader(null);
        paint.setColor(COLOR_DOT);
        canvas.drawCircle(dx, dy, dotR * 0.85f, paint);

        // Multiply 黑描边
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, dotR * 0.25f));
        paint.setColor(Color.argb(204, 0, 0, 0));
        paint.setXfermode(multiplyMode);
        canvas.drawCircle(dx, dy, dotR, paint);
        paint.setXfermode(null);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawLabels(Canvas canvas) {
        labelPaint.setTextSize(Math.min(getWidth(), getHeight()) * 0.032f);
        Paint.FontMetrics fm = labelPaint.getFontMetrics();
        for (int i = 0; i <= 8; i++) {
            float value = i * 25f;
            float angle = angleForProgress(value);
            double rad = Math.toRadians(angle);
            float x = centerX + labelR * (float) Math.cos(rad);
            float y = centerY + labelR * (float) Math.sin(rad);
            canvas.drawText(String.valueOf(Math.round(i * maxProgress / 8f)), x, y - (fm.ascent + fm.descent) / 2f, labelPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return true;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                tracking = true;
                getParent().requestDisallowInterceptTouchEvent(true);
                if (trackingListener != null) {
                    trackingListener.onTrackingStarted();
                }
                updateFromTouch(event.getX(), event.getY(), true, false);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (tracking) {
                    updateFromTouch(event.getX(), event.getY(), true, true);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                tracking = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                if (trackingListener != null) {
                    trackingListener.onTrackingStopped();
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void updateFromTouch(float x, float y, boolean fromUser, boolean blockWrap) {
        float dx = x - centerX;
        float dy = y - centerY;
        float touchAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
        if (touchAngle < 0f) {
            touchAngle += 360f;
        }
        float offset = touchAngle - START_ANGLE;
        if (offset < 0f) {
            offset += 360f;
        }
        if (offset > SWEEP_ANGLE) {
            int end;
            if (blockWrap) {
                end = progress >= maxProgress / 2 ? maxProgress : MIN_PROGRESS;
            } else if (offset - SWEEP_ANGLE < (360f - SWEEP_ANGLE) / 2f) {
                end = maxProgress;
            } else {
                end = MIN_PROGRESS;
            }
            setProgressInternal(end, fromUser);
            return;
        }
        int next = Math.round(MIN_PROGRESS + (offset / SWEEP_ANGLE) * maxProgress);
        if (blockWrap && Math.abs(next - progress) > maxProgress / 2) {
            next = progress >= maxProgress / 2 ? maxProgress : MIN_PROGRESS;
        }
        setProgressInternal(next, fromUser);
    }

    private void setProgressInternal(int value, boolean fromUser) {
        int clamped = Math.max(MIN_PROGRESS, Math.min(maxProgress, value));
        progress = clamped;
        float angle = angleForProgress(progress);
        float percent = progress / (float) maxProgress;
        if (angleChangeListener != null) {
            angleChangeListener.onAngleChanged(angle, percent);
        }
        if (progressChangeListener != null) {
            progressChangeListener.onProgressChanged(progress, fromUser);
        }
        invalidate();
    }

    public void setAngle(float absoluteCanvasAngle) {
        float offset = absoluteCanvasAngle - START_ANGLE;
        if (offset < 0f) {
            offset += 360f;
        }
        offset = Math.max(0f, Math.min(SWEEP_ANGLE, offset));
        setProgressInternal(Math.round(offset / SWEEP_ANGLE * maxProgress), false);
    }

    public float getAngle() {
        return angleForProgress(progress);
    }

    public float getPercent() {
        return progress / (float) maxProgress;
    }

    public void setMaxProgress(int max) {
        maxProgress = Math.max(1, max);
        setProgress(progress);
    }

    public void setProgress(int value) {
        setProgressInternal(value, false);
    }

    public int getProgress() {
        return progress;
    }

    public void setOnAngleChangeListener(@Nullable OnAngleChangeListener listener) {
        this.angleChangeListener = listener;
    }

    public void setAngleChangeListener(@Nullable OnAngleChangeListener listener) {
        setOnAngleChangeListener(listener);
    }

    public void setOnProgressChangeListener(@Nullable OnProgressChangeListener listener) {
        this.progressChangeListener = listener;
    }

    public void setOnTrackingListener(@Nullable OnTrackingListener listener) {
        this.trackingListener = listener;
    }
}
