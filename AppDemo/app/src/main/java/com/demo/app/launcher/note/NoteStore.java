package com.demo.app.launcher.note;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.app.utils.SPUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** 笔记保存在本地，按更新时间新的在前。最近一次删除可以撤销。 */
public final class NoteStore {

    private static final String KEY = "note_entries";
    private static final Gson GSON = new Gson();
    private static final Type LIST = new TypeToken<List<NoteEntry>>() {
    }.getType();

    private static List<NoteEntry> lastDeleted = new ArrayList<>();

    private NoteStore() {
    }

    @NonNull
    public static List<NoteEntry> load() {
        String json = SPUtils.getInstance().getString(KEY, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        List<NoteEntry> list = GSON.fromJson(json, LIST);
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    @Nullable
    public static NoteEntry find(long id) {
        if (id <= 0) {
            return null;
        }
        for (NoteEntry note : load()) {
            if (note.id == id) {
                return note;
            }
        }
        return null;
    }

    public static void save(@NonNull NoteEntry entry) {
        List<NoteEntry> notes = load();
        if (entry.id <= 0) {
            entry.id = System.currentTimeMillis();
        }
        entry.updatedMillis = System.currentTimeMillis();
        boolean replaced = false;
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).id == entry.id) {
                notes.set(i, entry);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            notes.add(0, entry);
        }
        sort(notes);
        persist(notes);
    }

    /** 删掉选中的笔记，并记住它们，供紧接着的撤销使用。 */
    public static void delete(@NonNull List<Long> ids) {
        List<NoteEntry> notes = load();
        List<NoteEntry> removed = new ArrayList<>();
        notes.removeIf(note -> {
            if (ids.contains(note.id)) {
                removed.add(note);
                return true;
            }
            return false;
        });
        lastDeleted = removed;
        persist(notes);
    }

    public static boolean undoDelete() {
        if (lastDeleted.isEmpty()) {
            return false;
        }
        List<NoteEntry> notes = load();
        notes.addAll(lastDeleted);
        lastDeleted = new ArrayList<>();
        sort(notes);
        persist(notes);
        return true;
    }

    private static void sort(@NonNull List<NoteEntry> notes) {
        notes.sort((left, right) -> Long.compare(right.updatedMillis, left.updatedMillis));
    }

    private static void persist(@NonNull List<NoteEntry> notes) {
        SPUtils.getInstance().put(KEY, GSON.toJson(notes));
    }
}
