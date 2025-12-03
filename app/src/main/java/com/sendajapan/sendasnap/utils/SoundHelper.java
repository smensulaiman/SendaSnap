package com.sendajapan.sendasnap.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

public class SoundHelper {

    public static void playNotificationSound(Context context) {
        if (context == null) {
            return;
        }

        try {
            SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
            if (!prefsManager.isNotificationsEnabled()) {
                return;
            }

            Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (notificationUri == null) {
                notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }

            if (notificationUri != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(context, notificationUri);
                if (ringtone != null) {
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager != null) {
                        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                        if (currentVolume > 0) {
                            ringtone.play();
                        }
                    } else {
                        ringtone.play();
                    }
                }
            }
        } catch (Exception e) {
        }
    }
}
