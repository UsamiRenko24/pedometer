package com.demo.app.launcher.ui.activity.weather;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.demo.app.databinding.ActivityWeatherCitySearchBinding;
import com.demo.app.databinding.ItemWeatherCitySearchBinding;
import com.demo.app.launcher.weather.WeatherLocation;
import com.demo.app.launcher.weather.WeatherRepository;
import com.demo.app.launcher.weather.WeatherStore;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 城市搜索。未输入时展示热门地区，搜索后展示结果、无结果、加载失败或网络错误。 */
public class WeatherCitySearchActivity extends BaseActivity<ActivityWeatherCitySearchBinding> {

    private static final int MATCH_COLOR = 0xFF216AFF;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private int searchToken;

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, WeatherCitySearchActivity.class);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityWeatherCitySearchBinding getViewBinding() {
        return ActivityWeatherCitySearchBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.weatherSearchRoot, (v, insets) -> {
            Insets bars = insets.getInsets(
                    SystemUiHelper.contentInsetTypes());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void applyImmersive() {
        SystemUiHelper.applyVolumeControlImmersive(getWindow(), true, true);
    }

    @Override
    protected void initViews() {
        showRecommend();
    }

    @Override
    protected void initEvents() {
        binding.weatherSearchBack.setOnClickListener(v -> finish());
        binding.weatherSearchIcon.setOnClickListener(v -> request(currentQuery()));
        binding.weatherSearchReload.setOnClickListener(v -> request(currentQuery()));
        binding.weatherSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                request(currentQuery());
                return true;
            }
            return false;
        });
        binding.weatherSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);
                String query = s == null ? "" : s.toString();
                searchHandler.postDelayed(() -> request(query), query.trim().isEmpty() ? 0 : 350);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void request(@NonNull String query) {
        String text = query.trim();
        if (!isOnline()) {
            setPage(Page.NETWORK);
            return;
        }
        int token = ++searchToken;
        if (text.isEmpty()) {
            WeatherRepository.hotCities((success, cities) -> applyCities(token, "", success, cities));
        } else {
            WeatherRepository.search(text, (success, cities) -> applyCities(token, text, success, cities));
        }
    }

    private void applyCities(int token, @NonNull String query, boolean success,
                             @NonNull List<WeatherLocation> cities) {
        if (token != searchToken) {
            return;
        }
        if (!success) {
            setPage(Page.FAIL);
            return;
        }
        if (cities.isEmpty()) {
            setPage(query.isEmpty() ? Page.RECOMMEND : Page.EMPTY);
            if (query.isEmpty()) {
                fillList(binding.weatherSearchRecommendList, new ArrayList<>(), "");
            }
            return;
        }
        if (query.isEmpty()) {
            fillList(binding.weatherSearchRecommendList, cities, "");
            setPage(Page.RECOMMEND);
        } else {
            fillList(binding.weatherSearchResults, cities, query);
            setPage(Page.RESULTS);
        }
    }

    private void showLoadFail() {
        setPage(Page.FAIL);
    }

    private void showRecommend() {
        request("");
    }

    private void fillList(@NonNull LinearLayout parent, @NonNull List<WeatherLocation> items,
                          @NonNull String query) {
        parent.removeAllViews();
        for (int index = 0; index < items.size(); index++) {
            WeatherLocation location = items.get(index);
            ItemWeatherCitySearchBinding row = ItemWeatherCitySearchBinding.inflate(
                    getLayoutInflater(), parent, true);
            row.weatherSearchResultText.setText(highlight(location.detailLine(), query));
            row.getRoot().setOnClickListener(v -> {
                WeatherStore.select(location);
                finish();
            });
            if (index < items.size() - 1) {
                View divider = new View(this);
                divider.setBackgroundColor(0xFFE6EBF2);
                parent.addView(divider, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) divider.getLayoutParams();
                params.setMarginStart(dp(20));
                params.setMarginEnd(dp(20));
            }
        }
    }

    private SpannableString highlight(@NonNull String source, @NonNull String query) {
        SpannableString text = new SpannableString(source);
        if (query.isEmpty()) {
            return text;
        }
        String lower = source.toLowerCase(Locale.US);
        String needle = query.toLowerCase(Locale.US);
        int from = 0;
        while (from < lower.length()) {
            int found = lower.indexOf(needle, from);
            if (found < 0) {
                break;
            }
            text.setSpan(new ForegroundColorSpan(MATCH_COLOR), found, found + needle.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            from = found + needle.length();
        }
        return text;
    }

    private void setPage(@NonNull Page page) {
        binding.weatherSearchRecommend.setVisibility(page == Page.RECOMMEND ? View.VISIBLE : View.GONE);
        binding.weatherSearchResults.setVisibility(page == Page.RESULTS ? View.VISIBLE : View.GONE);
        binding.weatherSearchFail.setVisibility(page == Page.FAIL ? View.VISIBLE : View.GONE);
        binding.weatherSearchEmpty.setVisibility(page == Page.EMPTY ? View.VISIBLE : View.GONE);
        binding.weatherSearchNetwork.setVisibility(page == Page.NETWORK ? View.VISIBLE : View.GONE);
    }

    @NonNull
    private String currentQuery() {
        return binding.weatherSearchInput.getText() == null
                ? ""
                : binding.weatherSearchInput.getText().toString();
    }

    private boolean isOnline() {
        ConnectivityManager manager = getSystemService(ConnectivityManager.class);
        if (manager == null) {
            return false;
        }
        Network network = manager.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private enum Page {
        RECOMMEND, RESULTS, FAIL, EMPTY, NETWORK
    }
}
