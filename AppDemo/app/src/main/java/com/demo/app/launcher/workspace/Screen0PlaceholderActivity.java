package com.demo.app.launcher.workspace;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.demo.app.R;

/** 0 屏里还没做完的入口。先显示图标和名称。 */
public class Screen0PlaceholderActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "screen0_title";
    public static final String EXTRA_ICON = "screen0_icon";

    public static void start(@Nullable Context context, @StringRes int titleRes, @DrawableRes int iconRes) {
        if (context == null) {
            return;
        }
        context.startActivity(new Intent(context, Screen0PlaceholderActivity.class)
                .putExtra(EXTRA_TITLE, context.getString(titleRes))
                .putExtra(EXTRA_ICON, iconRes)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen0_placeholder);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        int icon = getIntent().getIntExtra(EXTRA_ICON, R.drawable.icon_home_app_folder);
        TextView titleView = findViewById(R.id.screen0_placeholder_title);
        ImageView iconView = findViewById(R.id.screen0_placeholder_icon);
        titleView.setText(title == null ? "" : title);
        iconView.setImageResource(icon);
        findViewById(R.id.screen0_placeholder_back).setOnClickListener(v -> finish());
    }
}
