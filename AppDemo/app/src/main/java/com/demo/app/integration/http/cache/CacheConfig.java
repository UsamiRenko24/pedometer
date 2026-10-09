package com.demo.app.integration.http.cache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CacheConfig {
    @NonNull
    private final String key;
    private final long expireTime;
    @NonNull
    private final CacheStrategy strategy;
    @Nullable
    private final CacheValidator validator;

    private CacheConfig(@NonNull Builder builder) {
        this.key = builder.key;
        this.expireTime = builder.expireTime;
        this.strategy = builder.strategy;
        this.validator = builder.validator;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    public long getExpireTime() {
        return expireTime;
    }

    @NonNull
    public CacheStrategy getStrategy() {
        return strategy;
    }

    @Nullable
    public CacheValidator getValidator() {
        return validator;
    }

    public static class Builder {
        private String key;
        private long expireTime = 5 * 60 * 1000;
        @NonNull
        private CacheStrategy strategy = CacheStrategy.CACHE_FIRST;
        @Nullable
        private CacheValidator validator;

        @NonNull
        public Builder key(@NonNull String key) {
            this.key = key;
            return this;
        }

        @NonNull
        public Builder expireTime(long expireTime) {
            this.expireTime = expireTime;
            return this;
        }

        @NonNull
        public Builder strategy(@NonNull CacheStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        @NonNull
        public Builder validator(@Nullable CacheValidator validator) {
            this.validator = validator;
            return this;
        }

        @NonNull
        public CacheConfig build() {
            if (key == null || key.isEmpty()) {
                throw new IllegalArgumentException("The cache key cannot be empty.");
            }
            return new CacheConfig(this);
        }
    }
}
