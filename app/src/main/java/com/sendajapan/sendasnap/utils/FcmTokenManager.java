package com.sendajapan.sendasnap.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.utils.FcmNotificationSender;

import androidx.annotation.NonNull;

/**
 * Helper class to manage FCM token registration and retrieval
 */
public class FcmTokenManager {

    private static final String TAG = "FcmTokenManager";

    /**
     * Get the current FCM token and send it to the backend
     */
    public static void registerToken(Context context) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.d(TAG, "FCM Registration Token: " + token);

                        // Store token in Firebase Realtime Database (for direct Android-to-Android notifications)
                        FcmNotificationSender.storeFcmTokenInFirebase(context, token);
                    }
                });
    }

    /**
     * Send FCM token to backend server
     * This should be called after user login or when token is refreshed
     * 
     * Backend endpoint: POST /api/users/{userId}/fcm-token
     * Headers: Authorization: Bearer {auth_token}
     * Body: { "fcm_token": token, "device_id": deviceId }
     * 
     * Note: Backend should use Firebase Admin SDK with Service Account JSON
     * to send push notifications using the V1 API.
     */
    private static void sendTokenToBackend(Context context, String token) {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        UserData user = prefsManager.getUser();
        
        if (user == null || user.getId() <= 0) {
            Log.w(TAG, "User not logged in, cannot send FCM token to backend");
            return;
        }
        
        // TODO: Implement API call to send token to backend
        // Example implementation:
        // 
        // ApiService apiService = RetrofitClient.getInstance(context).getApiService();
        // FcmTokenRequest request = new FcmTokenRequest(token, getDeviceId(context));
        // Call<ApiResponse> call = apiService.updateFcmToken(user.getId(), request);
        // call.enqueue(new Callback<ApiResponse>() {
        //     @Override
        //     public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
        //         if (response.isSuccessful()) {
        //             Log.d(TAG, "FCM token sent to backend successfully");
        //         } else {
        //             Log.e(TAG, "Failed to send FCM token to backend: " + response.code());
        //         }
        //     }
        //     @Override
        //     public void onFailure(Call<ApiResponse> call, Throwable t) {
        //         Log.e(TAG, "Error sending FCM token to backend", t);
        //     }
        // });
        
        // For now, just log it
        Log.d(TAG, "FCM Token to send to backend for user " + user.getId() + ": " + token);
        Log.d(TAG, "Backend should store this token and use Firebase Admin SDK (V1 API) with Service Account to send notifications");
    }
    
    /**
     * Get unique device ID (optional, for tracking multiple devices per user)
     */
    private static String getDeviceId(Context context) {
        return android.provider.Settings.Secure.getString(
            context.getContentResolver(),
            android.provider.Settings.Secure.ANDROID_ID
        );
    }

    /**
     * Subscribe to a topic (optional, for broadcast notifications)
     */
    public static void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed to topic: " + topic;
                        if (!task.isSuccessful()) {
                            msg = "Failed to subscribe to topic: " + topic;
                        }
                        Log.d(TAG, msg);
                    }
                });
    }

    /**
     * Unsubscribe from a topic
     */
    public static void unsubscribeFromTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Unsubscribed from topic: " + topic;
                        if (!task.isSuccessful()) {
                            msg = "Failed to unsubscribe from topic: " + topic;
                        }
                        Log.d(TAG, msg);
                    }
                });
    }
}

