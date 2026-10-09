package com.demo.app.integration.http.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.LruCache;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.integration.http.GsonUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** 内存加 SharedPreferences。过期条目读到时删掉。 */
public class CacheStorage {
    private static final String CACHE_SP_NAME = "rx_http_cache";
    private static final int MAX_MEMORY_SIZE = 10 * 1024 * 1024;
    private static final int DISK_MAX_ENTRY_COUNT = 300;
    private static final String META_KEY_PREFIX = "__meta__:";
    private static final int MEMORY_ENTRY_OVERHEAD = 128;

    private static volatile CacheStorage instance;

    private final LruCache<String, CacheData> memoryCache;
    private final SharedPreferences diskCache;

    private CacheStorage(@NonNull Context context) {
        this.memoryCache = new LruCache<>(MAX_MEMORY_SIZE) {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull CacheData value) {
                int keySize = key.getBytes(StandardCharsets.UTF_8).length;
                String body = value.getData();
                int dataSize = body == null ? 0 : body.getBytes(StandardCharsets.UTF_8).length;
                return keySize + dataSize + MEMORY_ENTRY_OVERHEAD;
            }
        };
        this.diskCache = context.getApplicationContext()
                .getSharedPreferences(CACHE_SP_NAME, Context.MODE_PRIVATE);
    }

    public static CacheStorage getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (CacheStorage.class) {
                if (instance == null) {
                    instance = new CacheStorage(context);
                }
            }
        }
        return instance;
    }

    public synchronized void put(@NonNull String key, @NonNull CacheData data) {
        try {
            if (data.isExpired() || data.getData() == null) {
                remove(key);
                return;
            }
            memoryCache.put(key, data);
            String json = GsonUtils.toJson(data);
            if (json == null) {
                return;
            }
            CacheMeta meta = new CacheMeta();
            meta.lastAccessAt = System.currentTimeMillis();
            String metaJson = GsonUtils.toJson(meta);
            SharedPreferences.Editor editor = diskCache.edit().putString(key, json);
            if (metaJson != null) {
                editor.putString(metaKey(key), metaJson);
            }
            editor.apply();
            trimDiskCacheIfNeeded();
        } catch (Exception ignored) {
        }
    }

    @Nullable
    public synchronized CacheData get(@NonNull String key) {
        try {
            CacheData data = memoryCache.get(key);
            if (data != null) {
                if (data.isExpired()) {
                    remove(key);
                    return null;
                }
                return data;
            }
            String json = diskCache.getString(key, null);
            if (json == null) {
                return null;
            }
            data = GsonUtils.fromJson(json, CacheData.class);
            if (data == null || data.isExpired()) {
                remove(key);
                return null;
            }
            memoryCache.put(key, data);
            touchDiskMeta(key);
            return data;
        } catch (Exception ignored) {
            return null;
        }
    }

    public synchronized void remove(@NonNull String key) {
        try {
            memoryCache.remove(key);
            diskCache.edit().remove(key).remove(metaKey(key)).apply();
        } catch (Exception ignored) {
        }
    }

    public synchronized void clear() {
        try {
            memoryCache.evictAll();
            diskCache.edit().clear().apply();
        } catch (Exception ignored) {
        }
    }

    @NonNull
    private String metaKey(@NonNull String key) {
        return META_KEY_PREFIX + key;
    }

    private void touchDiskMeta(@NonNull String key) {
        try {
            String rawMeta = diskCache.getString(metaKey(key), null);
            CacheMeta meta = rawMeta == null ? new CacheMeta() : GsonUtils.fromJson(rawMeta, CacheMeta.class);
            if (meta == null) {
                meta = new CacheMeta();
            }
            meta.lastAccessAt = System.currentTimeMillis();
            String json = GsonUtils.toJson(meta);
            if (json != null) {
                diskCache.edit().putString(metaKey(key), json).apply();
            }
        } catch (Exception ignored) {
        }
    }

    private void trimDiskCacheIfNeeded() {
        try {
            Map<String, ?> all = diskCache.getAll();
            List<CacheEntry> entries = new ArrayList<>();
            for (Map.Entry<String, ?> item : all.entrySet()) {
                String key = item.getKey();
                if (key.startsWith(META_KEY_PREFIX)) {
                    continue;
                }
                Object value = item.getValue();
                if (!(value instanceof String)) {
                    entries.add(new CacheEntry(key, 0L));
                    continue;
                }
                CacheData data = GsonUtils.fromJson((String) value, CacheData.class);
                if (data == null || data.isExpired()) {
                    remove(key);
                    continue;
                }
                entries.add(new CacheEntry(key, readLastAccessAt(key, data.getTimestamp())));
            }
            if (entries.size() <= DISK_MAX_ENTRY_COUNT) {
                return;
            }
            entries.sort(Comparator.comparingLong(entry -> entry.lastAccessAt));
            int removeCount = entries.size() - DISK_MAX_ENTRY_COUNT;
            for (int i = 0; i < removeCount; i++) {
                remove(entries.get(i).key);
            }
        } catch (Exception ignored) {
        }
    }

    private long readLastAccessAt(@NonNull String key, long defaultValue) {
        try {
            String rawMeta = diskCache.getString(metaKey(key), null);
            if (rawMeta == null) {
                return defaultValue;
            }
            CacheMeta meta = GsonUtils.fromJson(rawMeta, CacheMeta.class);
            if (meta == null || meta.lastAccessAt <= 0) {
                return defaultValue;
            }
            return meta.lastAccessAt;
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private static final class CacheEntry {
        @NonNull
        final String key;
        final long lastAccessAt;

        CacheEntry(@NonNull String key, long lastAccessAt) {
            this.key = key;
            this.lastAccessAt = lastAccessAt;
        }
    }

    @Keep
    private static class CacheMeta {
        long lastAccessAt;
    }
}
