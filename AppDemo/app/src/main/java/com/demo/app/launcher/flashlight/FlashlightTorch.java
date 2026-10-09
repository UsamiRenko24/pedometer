package com.demo.app.launcher.flashlight;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** 用后置闪光灯开关手电筒。 */
public final class FlashlightTorch {

    private final CameraManager cameraManager;
    @Nullable
    private String cameraId;
    private boolean on;

    public FlashlightTorch(@NonNull Context context) {
        cameraManager = context.getApplicationContext().getSystemService(CameraManager.class);
    }

    public void prepare() {
        if (cameraManager == null) {
            return;
        }
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Boolean flash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (flash == null || !flash) {
                    continue;
                }
                cameraId = id;
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return;
                }
            }
        } catch (CameraAccessException ignored) {
            cameraId = null;
        }
    }

    public boolean hasFlash() {
        return cameraId != null;
    }

    public boolean isOn() {
        return on;
    }

    public void turnOn() throws CameraAccessException {
        if (cameraId == null || cameraManager == null) {
            throw new CameraAccessException(CameraAccessException.CAMERA_ERROR);
        }
        cameraManager.setTorchMode(cameraId, true);
        on = true;
    }

    public void turnOff() {
        if (cameraId == null || cameraManager == null || !on) {
            on = false;
            return;
        }
        try {
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException ignored) {
            // 离开页面时关闭失败不再打断界面。
        }
        on = false;
    }
}
