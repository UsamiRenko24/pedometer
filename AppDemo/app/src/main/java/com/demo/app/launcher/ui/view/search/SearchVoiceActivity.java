package com.demo.app.launcher.ui.view.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;

/** 把系统语音识别结果交回桌面搜索框。 */
public final class SearchVoiceActivity extends Activity {

    public interface Callback {
        void onText(@NonNull String text);
    }

    @Nullable
    private static Callback callback;

    public static void start(@NonNull Context context, @NonNull Callback listener) {
        callback = listener;
        context.startActivity(new Intent(context, SearchVoiceActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, 1);
        } catch (RuntimeException ignored) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && callback != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                callback.onText(results.get(0));
            }
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        callback = null;
        super.onDestroy();
    }
}
