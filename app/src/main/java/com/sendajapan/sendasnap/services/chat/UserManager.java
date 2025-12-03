package com.sendajapan.sendasnap.services.chat;

import android.content.Context;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.utils.FirebaseUtils;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages user data initialization and updates in Firebase.
 * Handles all operations related to the Firebase 'users' table.
 */
public class UserManager {
    
    private static final String TAG = "UserManager";
    private final FirebaseDatabase database;
    
    public UserManager(FirebaseDatabase database) {
        this.database = database;
    }
    
    /**
     * Initialize current user in Firebase with all UserData fields.
     * Retrieves user data from SharedPreferences and saves to Firebase.
     * 
     * @param context Application context
     */
    public void initializeCurrentUser(Context context) {
        String userId = FirebaseUtils.getCurrentUserId(context);
        if (userId.isEmpty()) {
            return;
        }
        
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        UserData userData = prefsManager.getUser();
        
        if (userData == null) {
            return;
        }
        
        initializeUserData(userData);
    }
    
    /**
     * Initialize/update user data in Firebase from UserData object.
     * This method can be used for any user, not just the current user.
     * Saves all UserData fields to Firebase users table.
     * 
     * @param userData UserData object containing all user information
     */
    public void initializeUserData(UserData userData) {
        if (userData == null || userData.getEmail() == null || userData.getEmail().isEmpty()) {
            return;
        }
        
        String userId = FirebaseUtils.sanitizeEmailForKey(userData.getEmail());
        if (userId.isEmpty()) {
            return;
        }
        
        DatabaseReference userRef = database.getReference("users").child(userId);
        
        // Prepare all user data fields for Firebase
        Map<String, Object> firebaseUserData = buildUserDataMap(userData, userId);
        
        // Save to Firebase
        userRef.setValue(firebaseUserData)
            .addOnSuccessListener(aVoid -> {
            })
            .addOnFailureListener(e -> {
            });
    }
    
    /**
     * Update user's last seen timestamp in Firebase.
     * 
     * @param context Application context
     */
    public void updateLastSeen(Context context) {
        String userId = FirebaseUtils.getCurrentUserId(context);
        if (userId.isEmpty()) {
            return;
        }
        
        database.getReference("users")
            .child(userId)
            .child("lastSeen")
            .setValue(System.currentTimeMillis());
    }
    
    /**
     * Build a map of user data fields for Firebase storage.
     * Includes all fields from UserData model.
     * 
     * @param userData UserData object
     * @param userId Sanitized user ID
     * @return Map of user data fields
     */
    private Map<String, Object> buildUserDataMap(UserData userData, String userId) {
        Map<String, Object> firebaseUserData = new HashMap<>();
        
        // Basic user info
        firebaseUserData.put("userId", userId);
        firebaseUserData.put("username", userData.getName() != null ? userData.getName() : "");
        firebaseUserData.put("email", userData.getEmail() != null ? userData.getEmail() : "");
        firebaseUserData.put("lastSeen", System.currentTimeMillis());
        
        // All UserData fields
        firebaseUserData.put("id", userData.getId());
        firebaseUserData.put("name", userData.getName() != null ? userData.getName() : "");
        firebaseUserData.put("email_verified_at", userData.getEmailVerifiedAt() != null ? userData.getEmailVerifiedAt() : "");
        firebaseUserData.put("role", userData.getRole() != null ? userData.getRole() : "");
        firebaseUserData.put("phone", userData.getPhone() != null ? userData.getPhone() : "");
        firebaseUserData.put("avis_id", userData.getAvisId() != null ? userData.getAvisId() : "");
        firebaseUserData.put("avatar", userData.getAvatar() != null ? userData.getAvatar() : "");
        firebaseUserData.put("avatar_url", userData.getAvatarUrl() != null ? userData.getAvatarUrl() : "");
        firebaseUserData.put("profileImageUrl", userData.getAvatarUrl() != null ? userData.getAvatarUrl() : ""); // For ChatUser compatibility
        firebaseUserData.put("created_at", userData.getCreatedAt() != null ? userData.getCreatedAt() : "");
        firebaseUserData.put("updated_at", userData.getUpdatedAt() != null ? userData.getUpdatedAt() : "");
        
        return firebaseUserData;
    }
}

