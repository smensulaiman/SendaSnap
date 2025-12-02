package com.sendajapan.sendasnap.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * Helper class for playing notification sounds.
 */
public class SoundHelper {

    /**
     * Play the default notification sound (ting sound)
     */
    public static void playNotificationSound(Context context) {
        if (context == null) {
            return;
        }

        try {
            // Check if sound is enabled in user preferences
            SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
            if (!prefsManager.isNotificationsEnabled()) {
                return;
            }

            // Get the default notification sound URI
            Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (notificationUri == null) {
                // Fallback to default alarm sound if notification sound is not available
                notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }

            if (notificationUri != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(context, notificationUri);
                if (ringtone != null) {
                    // Set volume based on current audio stream
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
            // Silently fail if sound cannot be played
            e.printStackTrace();
        }
    }
}

