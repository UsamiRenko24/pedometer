package com.demo.app.launcher.note;

import androidx.annotation.NonNull;

import com.demo.app.utils.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/** 桌面小组件实例对应哪一条笔记。 */
public final class NoteWidgetStore {

    private static final String KEY_MAP = "note_widget_map";
    private static final String KEY_PENDING = "note_widget_pending";
    private static final Gson GSON = new Gson();
    private static final Type MAP = new TypeToken<Map<String, Long>>() {
    }.getType();

    private NoteWidgetStore() {
    }

    public static void setPending(long noteId) {
        SPUtils.getInstance().put(KEY_PENDING, noteId);
    }

    public static long noteIdOf(int widgetId) {
        Long id = map().get(String.valueOf(widgetId));
        return id == null ? 0L : id;
    }

    /** 新出现、还没有绑定笔记的实例，用上一次请求添加时的笔记。 */
    public static void bindIfNew(int widgetId) {
        Map<String, Long> map = map();
        String key = String.valueOf(widgetId);
        if (map.containsKey(key)) {
            return;
        }
        long pending = SPUtils.getInstance().getLong(KEY_PENDING, 0L);
        if (pending <= 0L) {
            return;
        }
        map.put(key, pending);
        SPUtils.getInstance().put(KEY_PENDING, 0L);
        persist(map);
    }

    public static void remove(@NonNull int[] widgetIds) {
        Map<String, Long> map = map();
        boolean changed = false;
        for (int widgetId : widgetIds) {
            if (map.remove(String.valueOf(widgetId)) != null) {
                changed = true;
            }
        }
        if (changed) {
            persist(map);
        }
    }

    @NonNull
    private static Map<String, Long> map() {
        String json = SPUtils.getInstance().getString(KEY_MAP, "");
        if (json.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, Long> map = GSON.fromJson(json, MAP);
        return map == null ? new HashMap<>() : new HashMap<>(map);
    }

    private static void persist(@NonNull Map<String, Long> map) {
        SPUtils.getInstance().put(KEY_MAP, GSON.toJson(map));
    }
}
