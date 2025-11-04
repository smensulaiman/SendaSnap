package com.sendajapan.sendasnap.utils;

import android.content.Context;
import com.sendajapan.sendasnap.models.User;
import java.util.Arrays;

public class FirebaseUtils {
    
    /**
     * Sanitize email to be used as Firebase key
     * Replace special characters with underscores
     */
    public static String sanitizeEmailForKey(String email) {
        if (email == null || email.isEmpty()) {
            return "";
        }
        return email.replaceAll("[@.]", "_").replaceAll("[^a-zA-Z0-9_]", "");
    }

    /**
     * Generate chat ID from two user IDs (sorted alphabetically)
     */
    public static String generateChatId(String userId1, String userId2) {
        String[] ids = {userId1, userId2};
        Arrays.sort(ids);
        return ids[0] + "_" + ids[1];
    }

    /**
     * Get current user ID from SharedPreferences
     */
    public static String getCurrentUserId(Context context) {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        User user = prefsManager.getUser();
        if (user != null && user.getEmail() != null) {
            return sanitizeEmailForKey(user.getEmail());
        }
        return "";
    }

    /**
     * Get current user email
     */
    public static String getCurrentUserEmail(Context context) {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        User user = prefsManager.getUser();
        if (user != null && user.getEmail() != null) {
            return user.getEmail();
        }
        return "";
    }

    /**
     * Get current username
     */
    public static String getCurrentUsername(Context context) {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        User user = prefsManager.getUser();
        if (user != null && user.getUsername() != null) {
            return user.getUsername();
        }
        return "";
    }
}

