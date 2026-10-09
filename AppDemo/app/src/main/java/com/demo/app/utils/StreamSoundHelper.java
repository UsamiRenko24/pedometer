package com.demo.app.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

public final class StreamSoundHelper {

    private static final long SYSTEM_BEEP_INTERVAL_MS = 420L;

    private final Context appContext;
    private final AudioManager audioManager;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Ringtone ringtone;
    private ToneGenerator toneGenerator;
    private int playingStream = Integer.MIN_VALUE;

    private final Runnable systemBeep = new Runnable() {
        @Override
        public void run() {
            if (toneGenerator == null) {
                return;
            }
            try {
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 160);
            } catch (Throwable ignored) {
            }
            handler.postDelayed(this, SYSTEM_BEEP_INTERVAL_MS);
        }
    };

    public StreamSoundHelper(Context context) {
        appContext = context.getApplicationContext();
        audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public int getPercent(int streamType) {
        if (audioManager == null) {
            return 0;
        }
        int max = Math.max(audioManager.getStreamMaxVolume(streamType), 1);
        return Math.round(audioManager.getStreamVolume(streamType) * 100f / max);
    }

    public void setPercent(int streamType, int percent) {
        if (audioManager == null) {
            return;
        }
        int max = audioManager.getStreamMaxVolume(streamType);
        int index = Math.round(Math.max(0, Math.min(100, percent)) / 100f * max);
        try {
            audioManager.setStreamVolume(streamType, index, 0);
        } catch (Throwable ignored) {
        }
    }

    public void startPreview(int streamType) {
        if (playingStream == streamType && isPlaying()) {
            return;
        }
        stopPreview();
        if (getPercent(streamType) <= 0) {
            return;
        }
        playingStream = streamType;
        if (streamType == AudioManager.STREAM_SYSTEM) {
            startSystemBeep();
            return;
        }
        Uri uri = uriFor(streamType);
        if (uri == null || !isUriPlayable(uri)) {
            startSystemBeep();
            return;
        }
        try {
            ringtone = RingtoneManager.getRingtone(appContext, uri);
            if (ringtone == null) {
                startSystemBeep();
                return;
            }
            ringtone.setAudioAttributes(attributesFor(streamType));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone.setLooping(true);
            }
            ringtone.play();
        } catch (Throwable ignored) {
            startSystemBeep();
        }
    }

    public void stopPreview() {
        handler.removeCallbacks(systemBeep);
        playingStream = Integer.MIN_VALUE;
        if (ringtone != null) {
            try {
                ringtone.stop();
            } catch (Throwable ignored) {
            }
            ringtone = null;
        }
        if (toneGenerator != null) {
            try {
                toneGenerator.stopTone();
                toneGenerator.release();
            } catch (Throwable ignored) {
            }
            toneGenerator = null;
        }
    }

    private boolean isPlaying() {
        if (toneGenerator != null) {
            return true;
        }
        try {
            return ringtone != null && ringtone.isPlaying();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void startSystemBeep() {
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, 80);
            systemBeep.run();
        } catch (Throwable ignored) {
        }
    }

    private boolean isUriPlayable(Uri uri) {
        try {
            AssetFileDescriptor afd = appContext.getContentResolver().openAssetFileDescriptor(uri, "r");
            if (afd != null) {
                afd.close();
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static Uri uriFor(int streamType) {
        int type;
        switch (streamType) {
            case AudioManager.STREAM_ALARM:
                type = RingtoneManager.TYPE_ALARM;
                break;
            case AudioManager.STREAM_NOTIFICATION:
                type = RingtoneManager.TYPE_NOTIFICATION;
                break;
            case AudioManager.STREAM_MUSIC:
                type = RingtoneManager.TYPE_RINGTONE;
                break;
            case AudioManager.STREAM_RING:
            default:
                type = RingtoneManager.TYPE_RINGTONE;
                break;
        }
        return RingtoneManager.getDefaultUri(type);
    }

    private static AudioAttributes attributesFor(int streamType) {
        int usage;
        int contentType = AudioAttributes.CONTENT_TYPE_SONIFICATION;
        switch (streamType) {
            case AudioManager.STREAM_ALARM:
                usage = AudioAttributes.USAGE_ALARM;
                break;
            case AudioManager.STREAM_NOTIFICATION:
                usage = AudioAttributes.USAGE_NOTIFICATION;
                break;
            case AudioManager.STREAM_MUSIC:
                usage = AudioAttributes.USAGE_MEDIA;
                contentType = AudioAttributes.CONTENT_TYPE_MUSIC;
                break;
            case AudioManager.STREAM_RING:
                usage = AudioAttributes.USAGE_NOTIFICATION_RINGTONE;
                break;
            default:
                usage = AudioAttributes.USAGE_ASSISTANCE_SONIFICATION;
                break;
        }
        return new AudioAttributes.Builder()
                .setLegacyStreamType(streamType)
                .setUsage(usage)
                .setContentType(contentType)
                .build();
    }
}
