package com.demo.app.integration.http.cache;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

@Keep
public class CacheData {
    @Nullable
    private String data;
    private long timestamp;
    private long expireTime;

    public CacheData() {
    }

    public CacheData(@Nullable String data, long expireTime) {
        this.data = data;
        this.timestamp = System.currentTimeMillis();
        this.expireTime = expireTime;
    }

    public boolean isExpired() {
        if (expireTime <= 0) {
            return false;
        }
        return System.currentTimeMillis() - timestamp > expireTime;
    }

    @Nullable
    public String getData() {
        return data;
    }

    public void setData(@Nullable String data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
}
