package com.demo.app.launcher.wallpaper;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.demo.app.R;

/** 接口分类 id 对应抽屉里的文案和图标。 */
public final class WallpaperCategories {

    private WallpaperCategories() {
    }

    @StringRes
    public static int label(String categoryId) {
        if (categoryId == null) {
            return R.string.wallpaper_cat_wallpaper;
        }
        switch (categoryId) {
            case "Nature":
                return R.string.wallpaper_cat_nature;
            case "ArtCulture":
                return R.string.wallpaper_cat_art;
            case "FoodDrink":
                return R.string.wallpaper_cat_food;
            case "Architecture":
                return R.string.wallpaper_cat_architecture;
            case "Spirituality":
                return R.string.wallpaper_cat_abstract;
            case "TexturesPatterns":
                return R.string.wallpaper_cat_particle;
            case "Travel":
                return R.string.wallpaper_cat_travel;
            case "Animals":
                return R.string.wallpaper_cat_animal;
            case "Wallpaper":
            default:
                return R.string.wallpaper_cat_wallpaper;
        }
    }

    @DrawableRes
    public static int icon(String categoryId) {
        if (categoryId == null) {
            return R.drawable.icon_home_app_wallpaper_wallpaper;
        }
        switch (categoryId) {
            case "Nature":
                return R.drawable.icon_home_app_wallpaper_nature;
            case "ArtCulture":
                return R.drawable.icon_home_app_wallpaper_art;
            case "FoodDrink":
                return R.drawable.icon_home_app_wallpaper_food;
            case "Architecture":
                return R.drawable.icon_home_app_wallpaper_architecture;
            case "Spirituality":
                return R.drawable.icon_home_app_wallpaper_abstract;
            case "TexturesPatterns":
                return R.drawable.icon_home_app_wallpaper_particle;
            case "Travel":
                return R.drawable.icon_home_app_wallpaper_travel;
            case "Animals":
                return R.drawable.icon_home_app_wallpaper_animal;
            case "Wallpaper":
            default:
                return R.drawable.icon_home_app_wallpaper_wallpaper;
        }
    }
}
