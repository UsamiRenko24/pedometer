package com.demo.app.ui.fragment;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.demo.app.R;
import com.demo.app.databinding.FragmentVolumeControlBinding;
import com.demo.app.launcher.ads.LauncherAds;
import com.demo.app.ui.widget.MetalKnobView;
import com.demo.app.utils.AppLog;
import com.demo.app.utils.MediaOutputBoost;

/** 声音控制页。旋钮写入手机媒体音量，松手后按该音量试听。 */
public class VolumeControlFragment extends Fragment {

    /** 桌面原曲：media_Xylophone_loop nepaul.mp3（铃儿响叮当） */
    private static final String ASSET_NAME = "media_xylophone_loop.mp3";

    private FragmentVolumeControlBinding binding;
    private AudioManager audioManager;
    private MediaPlayer previewPlayer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentVolumeControlBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
        LauncherAds.setVisible(binding.adPlaceholder.getRoot(), true);
        int systemPercent = readSystemPercent();
        int saved = MediaOutputBoost.savedProgress(requireContext());
        int progress = saved > 100 && systemPercent >= 100 ? saved : systemPercent;
        binding.volumeKnob.setProgress(progress);
        updatePercent(progress);
        MediaOutputBoost.apply(requireContext(), progress);

        binding.volumeKnob.setOnProgressChangeListener((voiceProgress, fromUser) -> {
            updatePercent(voiceProgress);
            if (!fromUser) {
                return;
            }
            applySystemVolume(voiceProgress);
            if (isPlaying()) {
                applyVolume(voiceProgress);
            }
        });
        binding.volumeKnob.setOnTrackingListener(new MetalKnobView.OnTrackingListener() {
            @Override
            public void onTrackingStarted() {
                stopPlayback();
            }

            @Override
            public void onTrackingStopped() {
                playFromStart(binding.volumeKnob.getProgress());
            }
        });
    }

    public void stopPlayback() {
        AppLog.i("VolumeControlFragment", "stop playback");
        if (previewPlayer == null) {
            return;
        }
        try {
            if (previewPlayer.isPlaying()) {
                previewPlayer.stop();
            }
        } catch (Throwable ignored) {
        }
        releasePlayer();
    }

    private boolean isPlaying() {
        try {
            return previewPlayer != null && previewPlayer.isPlaying();
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void playFromStart(int progress) {
        AppLog.i("VolumeControlFragment", "play progress " + progress);
        if (!isAdded() || progress <= 0) {
            stopPlayback();
            return;
        }
        stopPlayback();
        try {
            applySystemVolume(progress);
            previewPlayer = new MediaPlayer();
            previewPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            previewPlayer.setLooping(false);
            previewPlayer.setOnCompletionListener(mp -> releasePlayer());
            if (!setDataSourceFromAsset(previewPlayer) && !setDataSourceFromRaw(previewPlayer)) {
                releasePlayer();
                return;
            }
            previewPlayer.prepare();
            applyVolume(progress);
            previewPlayer.start();
        } catch (Throwable ignored) {
            releasePlayer();
        }
    }

    /** 0–100 写入系统媒体音量。超过 100 的部分加到整机输出。 */
    private void applySystemVolume(int progress) {
        if (audioManager == null) {
            return;
        }
        int systemPercent = Math.max(0, Math.min(100, progress));
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 感知响度补偿：幂曲线（指数 1/1.6≈0.625），把低半程(0~50)映射到更高的系统档位，听感更线性
        float ratio = (float) Math.pow(systemPercent / 100f, 1.0f / 1.6f);
        int index = Math.round(ratio * max);
        index = Math.max(0, Math.min(max, index));
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && index > 0) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
            }
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        } catch (Throwable ignored) {
        }
        MediaOutputBoost.apply(requireContext(), progress);
    }

    private int readSystemPercent() {
        if (audioManager == null) {
            return 0;
        }
        int max = Math.max(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 1);
        return Math.round(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100f / max);
    }

    private void applyVolume(int progress) {
        if (previewPlayer == null) {
            return;
        }
        float playerVolume = progress <= 0 ? 0f : 1f;
        try {
            previewPlayer.setVolume(playerVolume, playerVolume);
        } catch (Throwable ignored) {
        }
    }

    private boolean setDataSourceFromAsset(MediaPlayer player) {
        try (AssetFileDescriptor afd = requireContext().getAssets().openFd(ASSET_NAME)) {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean setDataSourceFromRaw(MediaPlayer player) {
        try (AssetFileDescriptor afd =
                     requireContext().getResources().openRawResourceFd(R.raw.media_xylophone_loop)) {
            if (afd == null) {
                return false;
            }
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void releasePlayer() {
        try {
            if (previewPlayer != null) {
                previewPlayer.reset();
                previewPlayer.release();
            }
        } catch (Throwable ignored) {
        }
        previewPlayer = null;
    }

    private void updatePercent(int progress) {
        binding.tvPercent.setText(getString(R.string.volume_percent, progress));
        int colorRes;
        if (progress <= 100) {
            colorRes = R.color.progress_blue;
        } else if (progress < 150) {
            colorRes = R.color.progress_orange;
        } else {
            colorRes = R.color.progress_red;
        }
        binding.tvPercent.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        binding.groupWarning.setVisibility(progress >= 150 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPause() {
        stopPlayback();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        stopPlayback();
        super.onDestroyView();
        binding = null;
        audioManager = null;
    }
}
