package com.demo.app.http;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@Keep
public class HttpResponse<T> {
    private int code = -1;
    @NonNull
    private String msg = "";
    @Nullable
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @NonNull
    public String getMsg() {
        return msg;
    }

    public void setMsg(@NonNull String msg) {
        this.msg = msg;
    }

    @Nullable
    public T getData() {
        return data;
    }

    public void setData(@Nullable T data) {
        this.data = data;
    }
}
