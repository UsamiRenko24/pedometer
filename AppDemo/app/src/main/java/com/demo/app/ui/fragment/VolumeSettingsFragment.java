package com.demo.app.ui.fragment;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.demo.app.R;
import com.demo.app.databinding.FragmentVolumeSettingsBinding;
import com.demo.app.databinding.ItemVolumeStreamBinding;
import com.demo.app.launcher.ads.LauncherAds;
import com.demo.app.utils.AppLog;
import com.demo.app.utils.StreamSoundHelper;

/** 声音设置页。拖动系统音量时，铃声和通知一起变化。铃声为 0 时进入静音。 */
public class VolumeSettingsFragment extends Fragment {

    private FragmentVolumeSettingsBinding binding;
    private StreamSoundHelper soundHelper;
    private StreamRow ringtoneRow;
    private StreamRow notificationsRow;
    private StreamRow systemRow;
    private boolean syncing;
    private boolean silent;
    @Nullable
    private Boolean pendingHeaderMuted;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentVolumeSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        soundHelper = new StreamSoundHelper(requireContext());
        ringtoneRow = bindRow(binding.rowRingtone, R.drawable.volume_small, R.drawable.volume_small_silent,
                R.string.stream_ringtone, AudioManager.STREAM_RING);
        bindRow(binding.rowAlarm, R.drawable.alarm_clock, R.drawable.alarm_clock_silent,
                R.string.stream_alarm, AudioManager.STREAM_ALARM);
        bindRow(binding.rowMedia, R.drawable.media, R.drawable.media_silent,
                R.string.stream_media, AudioManager.STREAM_MUSIC);
        notificationsRow = bindRow(binding.rowNotifications, R.drawable.remind, R.drawable.remind_silent,
                R.string.stream_notifications, AudioManager.STREAM_NOTIFICATION);
        systemRow = bindRow(binding.rowSystem, R.drawable.setting_config, R.drawable.setting_config_silent,
                R.string.stream_system, AudioManager.STREAM_SYSTEM);
        LauncherAds.setVisible(binding.adPlaceholder.getRoot(), true);
        silent = ringtoneRow.item.seekVolume.getProgress() <= 0;
        applyLock();
        if (pendingHeaderMuted != null) {
            applyHeaderMuted(pendingHeaderMuted);
            pendingHeaderMuted = null;
        }
    }

    public void setHeaderMuted(boolean muted) {
        if (binding == null || ringtoneRow == null) {
            pendingHeaderMuted = muted;
            return;
        }
        applyHeaderMuted(muted);
    }

    private void applyHeaderMuted(boolean muted) {
        boolean wasSilent = silent;
        setRingtoneLevel(muted ? 0 : ringtoneRow.lastProgress, false);
        setRowMuted(systemRow, muted, false);
        if (wasSilent != silent) {
            showSilentToast(silent);
        }
        if (soundHelper != null) {
            soundHelper.stopPreview();
        }
    }

    private StreamRow bindRow(ItemVolumeStreamBinding item, @DrawableRes int icon,
                              @DrawableRes int silentIcon, @StringRes int name, int streamType) {
        int progress = soundHelper.getPercent(streamType);
        StreamRow row = new StreamRow(item, icon, silentIcon, streamType, progress);
        item.tvStreamName.setText(name);
        item.seekVolume.setProgress(progress);
        applyMuted(row, progress <= 0);

        item.btnMute.setOnClickListener(v -> {
            if (silent && (row == notificationsRow || row == systemRow)) {
                return;
            }
            setRowMuted(row, !row.item.btnMute.isSelected(), true);
        });
        item.ivStreamIcon.setOnClickListener(null);
        item.ivStreamIcon.setClickable(false);

        item.seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                if (syncing || !fromUser) {
                    return;
                }
                if (row == systemRow) {
                    if (silent) {
                        syncing = true;
                        seekBar.setProgress(systemRow.item.seekVolume.getProgress());
                        syncing = false;
                        return;
                    }
                    onSystemChanged(value);
                    return;
                }
                if (row == ringtoneRow) {
                    onRingtoneChanged(value);
                    return;
                }
                if (silent && row == notificationsRow) {
                    syncing = true;
                    seekBar.setProgress(notificationsRow.item.seekVolume.getProgress());
                    syncing = false;
                    return;
                }
                onSingleRowChanged(row, value, true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (soundHelper != null && seekBar.getProgress() > 0 && !isLocked(row)) {
                    soundHelper.startPreview(row.streamType);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AppLog.i("VolumeSettingsFragment", "stream " + row.streamType + " progress " + seekBar.getProgress());
                if (soundHelper != null) {
                    soundHelper.stopPreview();
                }
            }
        });
        return row;
    }

    private boolean isLocked(StreamRow row) {
        return silent && (row == notificationsRow || row == systemRow);
    }

    /** 拖动系统音量时，铃声和通知使用同一个音量。 */
    private void onSystemChanged(int value) {
        boolean wasSilent = silent;
        syncing = true;
        setRowProgress(systemRow, value);
        setRowProgress(ringtoneRow, value);
        setRowProgress(notificationsRow, value);
        syncing = false;
        silent = value <= 0;
        applyLock();
        if (wasSilent != silent) {
            showSilentToast(silent);
        }
        if (soundHelper != null) {
            if (value <= 0) {
                soundHelper.stopPreview();
            } else {
                soundHelper.startPreview(AudioManager.STREAM_SYSTEM);
            }
        }
    }

    /** 拖动铃声时，通知使用同一个音量。 */
    private void onRingtoneChanged(int value) {
        boolean wasSilent = silent;
        setRingtoneLevel(value, true);
        if (wasSilent != silent) {
            showSilentToast(silent);
        }
    }

    private void setRingtoneLevel(int value, boolean fromUser) {
        syncing = true;
        setRowProgress(ringtoneRow, value);
        setRowProgress(notificationsRow, value);
        syncing = false;
        silent = value <= 0;
        applyLock();
        if (fromUser && soundHelper != null) {
            if (value <= 0) {
                soundHelper.stopPreview();
            } else {
                soundHelper.startPreview(AudioManager.STREAM_RING);
            }
        }
    }

    private void onSingleRowChanged(StreamRow row, int value, boolean playPreview) {
        syncing = true;
        setRowProgress(row, value);
        syncing = false;
        if (playPreview && soundHelper != null) {
            if (value <= 0) {
                soundHelper.stopPreview();
            } else {
                soundHelper.startPreview(row.streamType);
            }
        }
    }

    private void setRowProgress(StreamRow row, int value) {
        row.item.seekVolume.setProgress(value);
        applyStreamVolume(row, value, true);
    }

    private void applyLock() {
        setLocked(notificationsRow, silent);
        setLocked(systemRow, silent);
    }

    private void setLocked(StreamRow row, boolean locked) {
        row.item.seekVolume.setEnabled(!locked);
        row.item.btnMute.setEnabled(!locked);
        row.item.seekVolume.setAlpha(locked ? 0.4f : 1f);
        row.item.btnMute.setAlpha(locked ? 0.4f : 1f);
    }

    private void showSilentToast(boolean enabled) {
        if (!isAdded()) {
            return;
        }
        Toast toast = Toast.makeText(
                requireContext(),
                enabled ? R.string.silent_mode_on : R.string.silent_mode_off,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, Math.round(72 * getResources().getDisplayMetrics().density));
        toast.show();
    }

    private void applyStreamVolume(StreamRow row, int value, boolean updateUiMute) {
        if (value > 0) {
            row.lastProgress = value;
            if (updateUiMute) {
                applyMuted(row, false);
            }
        } else if (updateUiMute) {
            applyMuted(row, true);
        }
        if (soundHelper != null) {
            soundHelper.setPercent(row.streamType, value);
        }
    }

    private void setRowMuted(StreamRow row, boolean muted, boolean fromUser) {
        if (row == null) {
            return;
        }
        if (fromUser) {
            AppLog.i("VolumeSettingsFragment", "mute stream " + row.streamType + " = " + muted);
        }
        if (row == systemRow && !silent) {
            int current = row.item.seekVolume.getProgress();
            if (muted && current > 0) {
                row.lastProgress = current;
            }
            onSystemChanged(muted ? 0 : row.lastProgress);
            if (soundHelper != null) {
                soundHelper.stopPreview();
            }
            return;
        }
        if (row == ringtoneRow) {
            int current = row.item.seekVolume.getProgress();
            if (muted && current > 0) {
                row.lastProgress = current;
            }
            onRingtoneChanged(muted ? 0 : row.lastProgress);
            if (soundHelper != null) {
                soundHelper.stopPreview();
            }
            return;
        }
        if (fromUser && silent && row == notificationsRow) {
            return;
        }
        syncing = true;
        int target = muted ? 0 : row.lastProgress;
        if (muted) {
            int current = row.item.seekVolume.getProgress();
            if (current > 0) {
                row.lastProgress = current;
            }
            target = 0;
        }
        setRowProgress(row, target);
        syncing = false;
        if (fromUser && soundHelper != null) {
            soundHelper.stopPreview();
        }
    }

    private void applyMuted(StreamRow row, boolean muted) {
        row.item.btnMute.setSelected(muted);
        row.item.ivStreamIcon.setImageResource(muted ? row.silentIcon : row.icon);
        row.item.btnMute.setImageResource(muted
                ? R.drawable.volume_small_silent
                : R.drawable.volume_small);
    }

    @Override
    public void onPause() {
        if (soundHelper != null) {
            soundHelper.stopPreview();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (soundHelper != null) {
            soundHelper.stopPreview();
            soundHelper = null;
        }
        super.onDestroyView();
        binding = null;
        ringtoneRow = null;
        notificationsRow = null;
        systemRow = null;
    }

    private static final class StreamRow {
        final ItemVolumeStreamBinding item;
        @DrawableRes
        final int icon;
        @DrawableRes
        final int silentIcon;
        final int streamType;
        int lastProgress;

        StreamRow(ItemVolumeStreamBinding item, @DrawableRes int icon, @DrawableRes int silentIcon,
                  int streamType, int progress) {
            this.item = item;
            this.icon = icon;
            this.silentIcon = silentIcon;
            this.streamType = streamType;
            this.lastProgress = Math.max(progress, 1);
        }
    }
}
