package com.sendajapan.sendasnap.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class HapticFeedbackHelper {
    private static final int AMPLITUDE_LIGHT = 20;
    private static final int AMPLITUDE_MEDIUM = 100;
    private static final int AMPLITUDE_STRONG = 255;
    private static final int DURATION_TINY = 20;
    private static final int DURATION_LIGHT = 50;
    private static final int DURATION_MEDIUM = 75;
    private static final int DURATION_NOTIFICATION = 100;
    private static final int DURATION_ERROR_START = 0;
    private static final int DURATION_ERROR_PULSE1 = 100;
    private static final int DURATION_ERROR_PAUSE = 50;
    private static final int DURATION_ERROR_PULSE2 = 100;

    private static HapticFeedbackHelper instance;
    private final Vibrator vibrator;

    private HapticFeedbackHelper(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) context
                    .getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vibratorManager != null) {
                vibrator = vibratorManager.getDefaultVibrator();
            } else {
                vibrator = null;
            }
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    public static synchronized HapticFeedbackHelper getInstance(Context context) {
        if (instance == null) {
            instance = new HapticFeedbackHelper(context.getApplicationContext());
        }
        return instance;
    }

    public void vibrateSuccess() {
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createOneShot(DURATION_LIGHT, AMPLITUDE_MEDIUM);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(DURATION_LIGHT);
            }
        } catch (Exception e) {
        }
    }

    public void vibrateError() {
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                long[] pattern = { DURATION_ERROR_START, DURATION_ERROR_PULSE1, DURATION_ERROR_PAUSE, DURATION_ERROR_PULSE2 };
                int[] amplitudes = { 0, AMPLITUDE_STRONG, 0, AMPLITUDE_STRONG };
                VibrationEffect effect = VibrationEffect.createWaveform(pattern, amplitudes, -1);
                vibrator.vibrate(effect);
            } else {
                long[] pattern = { DURATION_ERROR_START, DURATION_ERROR_PULSE1, DURATION_ERROR_PAUSE, DURATION_ERROR_PULSE2 };
                vibrator.vibrate(pattern, -1);
            }
        } catch (Exception e) {
        }
    }

    public void vibrateClick() {
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createOneShot(DURATION_TINY, AMPLITUDE_LIGHT);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(DURATION_TINY);
            }
        } catch (Exception e) {
        }
    }

    public void vibrateWarning() {
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createOneShot(DURATION_MEDIUM, AMPLITUDE_MEDIUM);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(DURATION_MEDIUM);
            }
        } catch (Exception e) {
        }
    }

    public void vibrateNotification() {
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createOneShot(DURATION_NOTIFICATION, AMPLITUDE_STRONG);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(DURATION_NOTIFICATION);
            }
        } catch (Exception e) {
        }
    }
}
