package com.demo.app.launcher.workspace;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.app.R;
import com.demo.app.launcher.ui.view.search.LauncherSearchBackLayout;
import com.demo.app.launcher.ui.view.search.LauncherSearchHomeLayout;
import com.demo.app.launcher.ui.view.search.LauncherSearchResultLayout;
import com.demo.app.launcher.ui.view.search.LauncherSearchShortcuts;
import com.demo.app.launcher.ui.view.search.SearchVoiceActivity;

import java.util.Calendar;

/** 顶部搜索的三个板块：首页、搜索结果、返回新闻。 */
public final class TopPanelSearchController {

    private static final long BACK_EXIT_INTERVAL_MS = 1500L;

    private enum ContentMode {
        HOME, BACK, RESULT
    }

    private final Context context;
    private final Runnable requestClosePanel;

    private View panelRoot;
    private View backButton;
    private View greeting;
    private View shortcuts;
    private EditText input;
    private FrameLayout content;
    private final Runnable showIme = this::showKeyboardNow;
    private LauncherSearchHomeLayout homeLayout;
    private LauncherSearchResultLayout resultLayout;
    private LauncherSearchBackLayout backLayout;
    private ContentMode mode = ContentMode.HOME;
    private long lastBackPressTime;
    private boolean closing;

    public TopPanelSearchController(@NonNull Context context, @NonNull Runnable requestClosePanel) {
        this.context = context;
        this.requestClosePanel = requestClosePanel;
    }

    public void bind(@NonNull View root) {
        panelRoot = root;
        applyStatusBarPadding(root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            applyStatusBarPadding(view);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
        backButton = root.findViewById(R.id.globalSearch_ivBack);
        greeting = root.findViewById(R.id.globalSearch_greeting);
        shortcuts = root.findViewById(R.id.globalSearch_shortcuts);
        input = root.findViewById(R.id.globalSearch_etInput);
        content = root.findViewById(R.id.globalSearch_flView);
        RecyclerView shortcutList = root.findViewById(R.id.globalSearch_shortcuts);
        if (shortcutList != null) {
            LauncherSearchShortcuts.attach(shortcutList);
        }
        TextView greetingTitle = root.findViewById(R.id.globalSearch_tvGreeting);
        greetingTitle.setText(greetingRes());
        backButton.setOnClickListener(v -> onBackIcon());
        root.findViewById(R.id.globalSearch_ivVoice).setOnClickListener(v -> {
            hideKeyboard();
            SearchVoiceActivity.start(context, text -> {
                if (input != null) {
                    input.setText(text);
                    input.setSelection(text.length());
                }
            });
        });
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && mode == ContentMode.BACK && !closing) {
                switchToHomeMode();
            }
        });
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (closing || input == null) {
                    return;
                }
                String query = s == null ? "" : s.toString().trim();
                if (!query.isEmpty()) {
                    switchToResultMode(query);
                } else if (mode == ContentMode.RESULT) {
                    if (resultLayout != null) {
                        resultLayout.setSearchQuery("");
                    }
                    switchToHomeMode();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        switchToHomeMode();
    }

    public void onOpened() {
        lastBackPressTime = 0L;
        if (panelRoot != null) {
            applyStatusBarPadding(panelRoot);
        }
        switchToHomeMode();
        showKeyboard();
    }

    public void onClosed() {
        lastBackPressTime = 0L;
        closing = true;
        if (input != null) {
            input.removeCallbacks(showIme);
        }
        hideKeyboard();
        if (input != null) {
            input.setText("");
            input.clearFocus();
        }
        releaseLayouts();
        mode = ContentMode.HOME;
        closing = false;
    }

    public boolean onBackPressed() {
        if (input == null) {
            return false;
        }
        if (mode == ContentMode.HOME || mode == ContentMode.RESULT) {
            switchToBackMode();
            return true;
        }
        long now = System.currentTimeMillis();
        if (now - lastBackPressTime < BACK_EXIT_INTERVAL_MS) {
            requestClosePanel.run();
        } else {
            lastBackPressTime = now;
            Toast.makeText(context, R.string.screen0_press_back_again, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void onResume() {
    }

    public void onPause() {
        hideKeyboard();
    }

    public void destroy() {
        onClosed();
        if (panelRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(panelRoot, null);
        }
        panelRoot = null;
        backButton = null;
        greeting = null;
        shortcuts = null;
        input = null;
        content = null;
    }

    private void onBackIcon() {
        if (mode == ContentMode.RESULT) {
            if (input != null) {
                input.setText("");
            }
            switchToHomeMode();
        } else if (mode == ContentMode.HOME) {
            switchToBackMode();
        }
    }

    private void switchToHomeMode() {
        mode = ContentMode.HOME;
        applyChrome();
        showOnly(home());
        homeLayout.loadNewsList();
    }

    private void switchToResultMode(@NonNull String query) {
        mode = ContentMode.RESULT;
        applyChrome();
        showOnly(result());
        resultLayout.setSearchQuery(query);
    }

    private void switchToBackMode() {
        mode = ContentMode.BACK;
        if (input != null) {
            input.removeCallbacks(showIme);
        }
        hideKeyboard();
        closing = true;
        if (input != null) {
            input.setText("");
            input.clearFocus();
        }
        closing = false;
        applyChrome();
        showOnly(back());
        backLayout.loadNewsDataIfNeeded();
    }

    private void applyChrome() {
        if (greeting != null) {
            greeting.setVisibility(mode == ContentMode.HOME ? View.VISIBLE : View.GONE);
        }
        if (shortcuts != null) {
            shortcuts.setVisibility(mode == ContentMode.HOME ? View.VISIBLE : View.GONE);
        }
        if (backButton != null) {
            backButton.setVisibility(mode == ContentMode.RESULT ? View.VISIBLE : View.GONE);
        }
    }

    private int greetingRes() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            return R.string.search_greeting_morning;
        }
        if (hour < 18) {
            return R.string.search_greeting_afternoon;
        }
        return R.string.search_greeting_evening;
    }

    private void showOnly(@NonNull View page) {
        if (content == null) {
            return;
        }
        if (homeLayout != null) {
            homeLayout.setVisibility(homeLayout == page ? View.VISIBLE : View.GONE);
        }
        if (resultLayout != null) {
            resultLayout.setVisibility(resultLayout == page ? View.VISIBLE : View.GONE);
        }
        if (backLayout != null) {
            backLayout.setVisibility(backLayout == page ? View.VISIBLE : View.GONE);
        }
        page.bringToFront();
    }

    @NonNull
    private LauncherSearchHomeLayout home() {
        if (homeLayout == null) {
            homeLayout = new LauncherSearchHomeLayout(context);
            content.addView(homeLayout, match());
        }
        return homeLayout;
    }

    @NonNull
    private LauncherSearchResultLayout result() {
        if (resultLayout == null) {
            resultLayout = new LauncherSearchResultLayout(context);
            content.addView(resultLayout, match());
        }
        return resultLayout;
    }

    @NonNull
    private LauncherSearchBackLayout back() {
        if (backLayout == null) {
            backLayout = new LauncherSearchBackLayout(context);
            content.addView(backLayout, match());
        }
        return backLayout;
    }

    private void releaseLayouts() {
        if (resultLayout != null) {
            resultLayout.release();
        }
        if (content != null) {
            content.removeAllViews();
        }
        homeLayout = null;
        resultLayout = null;
        backLayout = null;
    }

    private void showKeyboard() {
        if (input == null || mode == ContentMode.BACK) {
            return;
        }
        input.removeCallbacks(showIme);
        input.post(showIme);
    }

    private void showKeyboardNow() {
        if (input == null || mode == ContentMode.BACK) {
            return;
        }
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void applyStatusBarPadding(@NonNull View view) {
        int top = statusBarHeight(view);
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(view);
        if (insets != null) {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout());
            top = Math.max(top, bars.top);
        }
        view.setPadding(view.getPaddingLeft(), top, view.getPaddingRight(), view.getPaddingBottom());
    }

    private static int statusBarHeight(@NonNull View view) {
        int id = view.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (id <= 0) {
            return 0;
        }
        return view.getResources().getDimensionPixelSize(id);
    }

    private void hideKeyboard() {
        if (input == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        }
    }

    @NonNull
    private static FrameLayout.LayoutParams match() {
        return new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
    }
}
