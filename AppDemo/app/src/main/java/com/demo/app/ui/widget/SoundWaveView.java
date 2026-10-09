package com.demo.app.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class SoundWaveView extends View {

    private static final int WAVE_COUNT = 10;
    private static final float BASE_WIDTH = 384f;

    private final Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path wavePath = new Path();

    private long lastFrameNs;
    private float timeSec;
    private boolean running = true;
    private LinearGradient waveGradient;

    private final Runnable frame = new Runnable() {
        @Override
        public void run() {
            if (!running) {
                return;
            }
            invalidate();
            postOnAnimation(this);
        }
    };

    public SoundWaveView(Context context) {
        super(context);
        init();
    }

    public SoundWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SoundWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeJoin(Paint.Join.ROUND);
        wavePaint.setStrokeCap(Paint.Cap.ROUND);
        particlePaint.setStyle(Paint.Style.FILL);
    }

    public void setRunning(boolean running) {
        this.running = running;
        lastFrameNs = 0L;
        if (running) {
            postOnAnimation(frame);
        } else {
            removeCallbacks(frame);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        lastFrameNs = 0L;
        setRunning(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        setRunning(false);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0) {
            return;
        }
        waveGradient = new LinearGradient(0, 0, w, 0, new int[]{
                0x003D9BFF,
                0xE600A6FF,
                0xFF4EC2FF,
                0xCC7A4CFF,
                0x002E7AFF
        }, new float[]{0f, 0.28f, 0.5f, 0.72f, 1f}, Shader.TileMode.CLAMP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        advanceTime();
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        float scale = width / BASE_WIDTH;
        float cy = height / 2f;
        float beat = (float) Math.pow((1f + Math.cos(timeSec * Math.PI * 3.6f)) / 2f, 7);

        for (int k = 0; k < WAVE_COUNT; k++) {
            wavePath.reset();
            boolean first = true;
            float amp = (16f + k * 2.8f) * (1f + beat * 0.18f) * scale;
            for (int x = -8; x <= width + 8; x += 2) {
                float e = 0.4f + 0.6f * (float) Math.pow(Math.sin(Math.PI * x / (double) width), 2);
                float y = cy
                        + (float) Math.sin(x * 0.018 - timeSec * (1.7 + k * 0.11) + k * 0.42) * amp * e
                        + (float) Math.sin(x * 0.034 + timeSec * 1.2 + k) * 8f * scale;
                if (first) {
                    wavePath.moveTo(x, y);
                    first = false;
                } else {
                    wavePath.lineTo(x, y);
                }
            }
            boolean main = k == 0;
            wavePaint.setStrokeWidth((main ? 1.7f : 0.75f) * scale);
            wavePaint.setShadowLayer((main ? 10f : 3f) * scale, 0f, 0f, 0xFF007FFF);
            if (main) {
                wavePaint.setShader(waveGradient);
                wavePaint.setAlpha(255);
            } else {
                wavePaint.setShader(waveGradient);
                wavePaint.setAlpha(55);
            }
            canvas.drawPath(wavePath, wavePaint);
        }

        wavePaint.setShader(null);
        wavePaint.clearShadowLayer();
        for (int i = 0; i < 6; i++) {
            float x = (i * 83.7f + timeSec * (4 + i % 3) * 40f) % Math.max(width, 1);
            if (x < 0) {
                x += width;
            }
            float y = cy - 40f * scale + ((float) Math.sin(i * 3.7 + timeSec * 0.4) + 1f) * 38f * scale;
            float alpha = 0.15f + 0.45f * (1f + (float) Math.sin(timeSec * 2 + i)) / 2f;
            particlePaint.setColor(0xFF43BCFF);
            particlePaint.setAlpha(Math.round(alpha * 255f));
            canvas.drawCircle(x, y, (i % 4 == 0 ? 1.3f : 0.7f) * scale * 1.6f, particlePaint);
        }
    }

    private void advanceTime() {
        long now = System.nanoTime();
        if (lastFrameNs == 0L) {
            lastFrameNs = now;
            return;
        }
        float dt = Math.min((now - lastFrameNs) / 1_000_000_000f, 0.05f);
        lastFrameNs = now;
        timeSec += dt;
    }
}
