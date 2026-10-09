package com.demo.app.utils;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

public final class LifecycleUtils {

    public static boolean isAlive(@NonNull LifecycleOwner lifecycleOwner) {
        if (lifecycleOwner instanceof FragmentActivity
                && !ActivityUtils.isActivityAlive((Activity) lifecycleOwner)
        ) {
            return false;
        }

        return lifecycleOwner.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.INITIALIZED);
    }

    public static void launch(
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull Runnable runnable
    ) {
        getViewLifecycleOwner(lifecycleOwner, owner -> {
            if (!isAlive(owner)) {
                return;
            }
            runnable.run();
        });
    }

    public static void launchWhenResume(
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull Runnable resume
    ) {
        getViewLifecycleOwner(lifecycleOwner, owner -> owner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onResume(@NonNull LifecycleOwner owner) {
                owner.getLifecycle().removeObserver(this);
                resume.run();
            }
        }));
    }

    public static void launchWhenPause(
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull Runnable pause
    ) {
        getViewLifecycleOwner(lifecycleOwner, owner -> owner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onPause(@NonNull LifecycleOwner owner) {
                owner.getLifecycle().removeObserver(this);
                pause.run();
            }
        }));
    }

    public static void launchWhenDestroy(
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull Runnable destroy
    ) {
        getViewLifecycleOwner(lifecycleOwner, owner -> owner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                owner.getLifecycle().removeObserver(this);
                destroy.run();
            }
        }));
    }

    public static void getViewLifecycleOwner(
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull Consumer<LifecycleOwner> ownerConsumer
    ) {
        ThreadUtils.runOnUiThread(() -> {
            if (lifecycleOwner instanceof Fragment) {
                ((Fragment) lifecycleOwner).getViewLifecycleOwnerLiveData().observe(lifecycleOwner, owner -> {
                    if (owner instanceof Fragment) {
                        ownerConsumer.accept(((Fragment) owner).getViewLifecycleOwner());
                    } else {
                        ownerConsumer.accept(owner);
                    }
                });
            } else {
                ownerConsumer.accept(lifecycleOwner);
            }
        });
    }
}