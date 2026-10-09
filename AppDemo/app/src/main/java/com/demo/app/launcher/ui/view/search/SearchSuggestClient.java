package com.demo.app.launcher.ui.view.search;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONArray;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Google 搜索建议。返回格式是 ["query", ["s1", "s2"]]。 */
final class SearchSuggestClient {

    interface Callback {
        void onResult(@NonNull List<String> suggestions);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private SearchSuggestClient() {
    }

    static void fetch(@NonNull String query, @NonNull Callback callback) {
        EXECUTOR.execute(() -> {
            List<String> suggestions = request(query);
            MAIN.post(() -> callback.onResult(suggestions));
        });
    }

    @NonNull
    private static List<String> request(@NonNull String query) {
        List<String> suggestions = new ArrayList<>();
        HttpURLConnection connection = null;
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
            URL url = new URL("https://suggestqueries.google.com/complete/search?client=firefox&q=" + encoded);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(12000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept", "application/json");
            try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
                scanner.useDelimiter("\\A");
                String body = scanner.hasNext() ? scanner.next() : "";
                JSONArray root = new JSONArray(body);
                JSONArray items = root.optJSONArray(1);
                if (items == null) {
                    return suggestions;
                }
                for (int i = 0; i < items.length(); i++) {
                    String item = items.optString(i, "");
                    if (!item.isEmpty()) {
                        suggestions.add(item);
                    }
                }
            }
        } catch (Exception ignored) {
            suggestions.clear();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return suggestions;
    }
}
