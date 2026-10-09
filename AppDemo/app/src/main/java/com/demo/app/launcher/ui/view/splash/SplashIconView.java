package com.demo.app.launcher.ui.view.splash;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/** 启动页中间图标。按短边裁成圆角，背景大图不走这里。 */
public class SplashIconView extends AppCompatImageView {

    public SplashIconView(Context context) {
        this(context, null);
    }

    public SplashIconView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SplashIconView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int width = view.getWidth();
                int height = view.getHeight();
                outline.setRoundRect(0, 0, width, height, Math.min(width, height) * 0.22f);
            }
        });
    }
}
