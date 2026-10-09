package com.demo.app.launcher.ui.activity.weather;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.demo.app.R;
import com.demo.app.databinding.ActivityWeatherCityManageBinding;
import com.demo.app.databinding.DialogWeatherLocationPermissionBinding;
import com.demo.app.databinding.ItemWeatherCityBinding;
import com.demo.app.launcher.weather.WeatherLocation;
import com.demo.app.launcher.weather.WeatherRepository;
import com.demo.app.launcher.weather.WeatherStore;
import com.demo.app.ui.activity.BaseActivity;
import com.demo.app.utils.SystemUiHelper;

import java.util.ArrayList;
import java.util.List;

/** 位置管理。没有城市时是空状态；右上角管理进入编辑，可删除城市。 */
public class WeatherCityManageActivity extends BaseActivity<ActivityWeatherCityManageBinding> {

    private final List<CityCard> cities = new ArrayList<>();
    private boolean editing;

    private final ActivityResultLauncher<String[]> locationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    WeatherRepository.locate(this, (location, snapshot) -> reloadCities());
                }
            });

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, WeatherCityManageActivity.class);
        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Nullable
    @Override
    protected ActivityWeatherCityManageBinding getViewBinding() {
        return ActivityWeatherCityManageBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initWindow(View rootView) {
        EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        applyImmersive();
        ViewCompat.setOnApplyWindowInsetsListener(binding.weatherCityManageScroll, (v, insets) -> {
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
        binding.weatherCityManageSearchInput.setFocusable(false);
        binding.weatherCityManageSearchInput.setCursorVisible(false);
        showCities();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!editing) {
            reloadCities();
        }
    }

    @Override
    protected void initEvents() {
        binding.weatherCityManageBack.setOnClickListener(v -> finish());
        binding.weatherCityManageSearch.setOnClickListener(v -> WeatherCitySearchActivity.start(this));
        binding.weatherCityManageSearchInput.setOnClickListener(v -> WeatherCitySearchActivity.start(this));
        binding.weatherCityAddEmpty.setOnClickListener(v -> WeatherCitySearchActivity.start(this));
        binding.weatherCityAddList.setOnClickListener(v -> WeatherCitySearchActivity.start(this));
        binding.weatherCityManageLocate.setOnClickListener(v -> showLocationPermission());
        binding.weatherCityManageAction.setOnClickListener(v -> setEditing(true));
        binding.weatherCityManageDone.setOnClickListener(v -> setEditing(false));
    }

    private void setEditing(boolean editing) {
        this.editing = editing;
        showCities();
    }

    private void showCities() {
        LinearLayout list = binding.weatherCityList;
        list.removeAllViews();
        for (int index = 0; index < cities.size(); index++) {
            CityCard city = cities.get(index);
            ItemWeatherCityBinding row = ItemWeatherCityBinding.inflate(getLayoutInflater(), list, true);
            row.weatherCityName.setText(city.name);
            row.weatherCityIcon.setImageResource(city.iconRes);
            row.weatherCityLow.setText(city.low);
            row.weatherCityHigh.setText(city.high);
            row.weatherCityMinus.setVisibility(editing ? View.VISIBLE : View.GONE);
            row.weatherCityDelete.setVisibility(editing ? View.VISIBLE : View.GONE);
            int cityIndex = index;
            row.weatherCityMinus.setOnClickListener(v -> removeCity(cityIndex));
            row.weatherCityDelete.setOnClickListener(v -> removeCity(cityIndex));
            if (!editing) {
                row.weatherCityCard.setOnClickListener(v -> {
                    WeatherStore.select(city.location);
                    finish();
                });
            }
            if (editing) {
                enableSwipe(row);
            } else {
                row.weatherCityCard.setTranslationX(0f);
                row.weatherCityCard.setOnTouchListener(null);
            }
        }
        applyChrome();
    }

    private void enableSwipe(@NonNull ItemWeatherCityBinding row) {
        View card = row.weatherCityCard;
        float reveal = dp(68);
        card.setOnTouchListener(new View.OnTouchListener() {
            float downX;
            float startX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        startX = v.getTranslationX();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float next = startX + event.getRawX() - downX;
                        v.setTranslationX(Math.max(-reveal, Math.min(reveal, next)));
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        float offset = v.getTranslationX();
                        float target = 0f;
                        if (offset > reveal / 2f) {
                            target = reveal;
                        } else if (offset < -reveal / 2f) {
                            target = -reveal;
                        }
                        v.animate().translationX(target).setDuration(160L).start();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void removeCity(int index) {
        if (index < 0 || index >= cities.size()) {
            return;
        }
        WeatherStore.remove(cities.get(index).location);
        cities.remove(index);
        showCities();
    }

    private void reloadCities() {
        cities.clear();
        for (WeatherLocation location : WeatherStore.cities()) {
            cities.add(new CityCard(location));
        }
        showCities();
        for (int index = 0; index < cities.size(); index++) {
            CityCard card = cities.get(index);
            int cityIndex = index;
            WeatherRepository.loadCity(card.location, (location, snapshot) -> {
                if (editing || cityIndex >= cities.size() || snapshot == null || snapshot.days.isEmpty()) {
                    return;
                }
                CityCard current = cities.get(cityIndex);
                if (location == null || !current.location.sameAs(location)) {
                    return;
                }
                current.iconRes = WeatherVisuals.ofSkycon(snapshot.skycon).icon;
                current.low = snapshot.days.get(0).low + "°";
                current.high = snapshot.days.get(0).high + "°";
                showCities();
            });
        }
    }

    private void applyChrome() {
        int browse = editing ? View.GONE : View.VISIBLE;
        binding.weatherCityManageArt.setVisibility(browse);
        binding.weatherCityManageBack.setVisibility(browse);
        binding.weatherCityManageTitle.setVisibility(browse);
        binding.weatherCityManageSubtitle.setVisibility(browse);
        binding.weatherCityManageLocate.setVisibility(browse);
        binding.weatherCityManageSearch.setVisibility(browse);
        binding.weatherCityManageAction.setVisibility(editing ? View.GONE : View.VISIBLE);
        binding.weatherCityManageDone.setVisibility(editing ? View.VISIBLE : View.GONE);

        boolean hasCities = !cities.isEmpty();
        binding.weatherCityList.setVisibility(hasCities ? View.VISIBLE : View.GONE);
        binding.weatherCityAddEmpty.setVisibility(!editing && !hasCities ? View.VISIBLE : View.GONE);
        binding.weatherCityAddList.setVisibility(!editing && hasCities ? View.VISIBLE : View.GONE);

        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) binding.weatherCityList.getLayoutParams();
        params.addRule(RelativeLayout.BELOW, editing
                ? R.id.weather_city_manage_done
                : R.id.weather_city_manage_search);
        binding.weatherCityList.setLayoutParams(params);
    }

    private void showLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            WeatherRepository.locate(this, (location, snapshot) -> reloadCities());
            return;
        }
        DialogWeatherLocationPermissionBinding sheet =
                DialogWeatherLocationPermissionBinding.inflate(getLayoutInflater());
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(sheet.getRoot());
        sheet.weatherPermissionMessage.setText(getString(
                R.string.weather_permission_message, getString(R.string.guide_app_name)));
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }
        sheet.weatherPermissionCancel.setOnClickListener(v -> dialog.dismiss());
        sheet.weatherPermissionConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            locationPermission.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        });
        dialog.show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    static final class CityCard {
        final WeatherLocation location;
        int iconRes = R.drawable.icon_home_app_weather_unknown;
        String low = "—";
        String high = "—";

        CityCard(WeatherLocation location) {
            this.location = location;
            this.name = location.displayName();
        }

        String name;
    }
}
