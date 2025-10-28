package com.sendajapan.sendasnap.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class HapticFeedbackHelper {
    private static HapticFeedbackHelper instance;
    private final Vibrator vibrator;

    private HapticFeedbackHelper(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) context
                    .getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
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
        if (vibrator != null && vibrator.hasVibrator()) {
            // Light vibration pattern for success
            VibrationEffect effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(effect);
        }
    }

    public void vibrateError() {
        if (vibrator != null && vibrator.hasVibrator()) {
            {
                // Strong vibration pattern for error
                long[] pattern = { 0, 100, 50, 100 };
                VibrationEffect effect = VibrationEffect.createWaveform(pattern, -1);
                vibrator.vibrate(effect);
            }
        }
    }

    public void vibrateClick() {
        if (vibrator != null && vibrator.hasVibrator()) {
            // Tiny vibration for click feedback
            VibrationEffect effect = VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(effect);
        }
    }

    public void vibrateWarning() {
        if (vibrator != null && vibrator.hasVibrator()) {
            // Medium vibration for warning
            VibrationEffect effect = VibrationEffect.createOneShot(75, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(effect);
        }
    }
}
