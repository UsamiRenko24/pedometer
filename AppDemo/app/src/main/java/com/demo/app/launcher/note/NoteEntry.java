package com.demo.app.launcher.note;

/** 一条本地笔记。bgIndex 0 是默认米色，textColor 0 用默认深色。 */
public final class NoteEntry {
    public long id;
    public String title = "";
    public String body = "";
    public long updatedMillis;
    public int textColor;
    public int bgIndex;
}
