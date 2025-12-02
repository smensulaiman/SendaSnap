package com.sendajapan.sendasnap.utils;

import android.content.Context;
import android.util.Log;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sendajapan.sendasnap.MyApplication;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.utils.CookieBarToastHelper;
import com.sendajapan.sendasnap.utils.SoundHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends push notifications directly from Android app using Firebase Realtime Database
 * Stores FCM tokens in Firebase and triggers notifications via database writes
 */
public class FcmNotificationSender {

    private static final String TAG = "FcmNotificationSender";
    private static final String FCM_TOKENS_PATH = "fcm_tokens";
    private static final String TASK_NOTIFICATIONS_PATH = "task_notifications";

    /**
     * Store FCM token in Firebase Realtime Database
     */
    public static void storeFcmTokenInFirebase(Context context, String fcmToken) {
        if (context == null || fcmToken == null || fcmToken.isEmpty()) {
            return;
        }

        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        UserData user = prefsManager.getUser();
        
        if (user == null || user.getEmail() == null) {
            Log.w(TAG, "User not logged in, cannot store FCM token");
            return;
        }

        String userId = FirebaseUtils.sanitizeEmailForKey(user.getEmail());
        if (userId.isEmpty()) {
            Log.w(TAG, "Invalid user ID, cannot store FCM token");
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference tokenRef = database.getReference(FCM_TOKENS_PATH).child(userId);

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcm_token", fcmToken);
        tokenData.put("updated_at", System.currentTimeMillis());
        tokenData.put("user_id", user.getId());
        tokenData.put("user_email", user.getEmail());

        tokenRef.setValue(tokenData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "FCM token stored in Firebase for user: " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to store FCM token in Firebase", e);
                });
    }

    /**
     * Send task assignment notifications to assignees via Firebase Realtime Database
     * This writes notification data to Firebase, which other devices will listen to
     * IMPORTANT: This only sends notifications to assignees, NOT to the creator
     */
    public static void sendTaskAssignmentNotifications(Context context, Task task, List<UserData> assignees) {
        if (context == null || task == null || assignees == null || assignees.isEmpty()) {
            Log.w(TAG, "Invalid parameters for sendTaskAssignmentNotifications");
            return;
        }

        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        UserData currentUser = prefsManager.getUser();
        
        if (currentUser == null) {
            Log.w(TAG, "User not logged in, cannot send notifications");
            return;
        }

        String creatorName = currentUser.getName() != null ? currentUser.getName() : "Someone";
        String taskTitle = task.getTitle() != null ? task.getTitle() : "New Task";
        String taskDescription = task.getDescription() != null && !task.getDescription().isEmpty()
                ? task.getDescription()
                : "You have been assigned a new task";

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notificationsRef = database.getReference(TASK_NOTIFICATIONS_PATH);

        int notificationCount = 0;
        for (UserData assignee : assignees) {
            if (assignee == null || assignee.getId() <= 0) {
                Log.w(TAG, "Invalid assignee, skipping");
                continue;
            }

            // Skip creator - creator should NOT receive notification
            if (currentUser.getId() == assignee.getId()) {
                Log.d(TAG, "Skipping creator (ID: " + currentUser.getId() + ") - creator should not receive notification");
                continue;
            }

            // Get assignee's Firebase user ID from email
            String assigneeEmail = assignee.getEmail();
            if (assigneeEmail == null || assigneeEmail.isEmpty()) {
                Log.w(TAG, "Assignee email is null or empty for assignee ID: " + assignee.getId() + ", skipping");
                continue;
            }

            String assigneeFirebaseId = FirebaseUtils.sanitizeEmailForKey(assigneeEmail);

            if (assigneeFirebaseId.isEmpty()) {
                Log.w(TAG, "Invalid assignee Firebase ID for email: " + assigneeEmail + ", skipping");
                continue;
            }

            Log.d(TAG, "Preparing notification for assignee: " + assigneeFirebaseId + " (email: " + assigneeEmail + ")");

            // Create notification data
            String notificationId = notificationsRef.push().getKey();
            if (notificationId == null) {
                Log.e(TAG, "Failed to generate notification ID");
                continue;
            }

            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "task_assignment");
            notificationData.put("task_id", String.valueOf(task.getId())); // Store as string for consistency
            notificationData.put("task_title", taskTitle);
            notificationData.put("task_description", taskDescription);
            notificationData.put("creator_name", creatorName);
            notificationData.put("creator_id", currentUser.getId());
            notificationData.put("assignee_id", assignee.getId());
            notificationData.put("assignee_firebase_id", assigneeFirebaseId);
            notificationData.put("created_at", System.currentTimeMillis());
            notificationData.put("read", false);
            
            Log.d(TAG, "Notification data prepared for assignee " + assigneeFirebaseId + 
                  ": task_id=" + task.getId() + ", title=" + taskTitle);

            // Write notification to Firebase under assignee's path
            DatabaseReference assigneeNotificationRef = notificationsRef
                    .child(assigneeFirebaseId)
                    .child(notificationId);

            assigneeNotificationRef.setValue(notificationData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Task assignment notification written to Firebase for: " + assigneeFirebaseId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to write notification to Firebase for: " + assigneeFirebaseId, e);
                    });
        }
    }

    private static ValueEventListener notificationListener;
    private static DatabaseReference currentNotificationRef;

    /**
     * Setup listener for task assignment notifications
     * This should be called when app starts or user logs in
     * Prevents duplicate listeners by removing existing one first
     */
    public static void setupNotificationListener(Context context) {
        if (context == null) {
            return;
        }

        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        UserData user = prefsManager.getUser();
        
        if (user == null || user.getEmail() == null) {
            return;
        }

        String userId = FirebaseUtils.sanitizeEmailForKey(user.getEmail());
        if (userId.isEmpty()) {
            return;
        }

        // Remove existing listener if any
        if (currentNotificationRef != null && notificationListener != null) {
            currentNotificationRef.removeEventListener(notificationListener);
            Log.d(TAG, "Removed existing notification listener");
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notificationsRef = database.getReference(TASK_NOTIFICATIONS_PATH).child(userId);

        // Create new listener that only processes NEW unread notifications
        notificationListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long currentTime = System.currentTimeMillis();
                    for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                        try {
                            // Only process unread notifications created in the last 10 minutes
                            Boolean read = notificationSnapshot.child("read").getValue(Boolean.class);
                            Long createdAt = notificationSnapshot.child("created_at").getValue(Long.class);
                            
                            if ((read == null || !read) && createdAt != null) {
                                // Only process notifications created in the last 10 minutes to avoid showing old ones
                                long timeDiff = currentTime - createdAt;
                                if (timeDiff < 600000) { // 10 minutes
                                    Log.d(TAG, "Processing new notification: " + notificationSnapshot.getKey() + 
                                          ", created " + (timeDiff / 1000) + " seconds ago");
                                    handleNotification(context, notificationSnapshot);
                                } else {
                                    Log.d(TAG, "Skipping old notification: " + notificationSnapshot.getKey() + 
                                          ", created " + (timeDiff / 1000) + " seconds ago");
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing notification snapshot", e);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to listen for notifications", error.toException());
            }
        };

        // Add listener
        notificationsRef.addValueEventListener(notificationListener);
        currentNotificationRef = notificationsRef;
        
        Log.d(TAG, "✓ Notification listener setup for user: " + userId + 
              " (email: " + user.getEmail() + ") at path: " + TASK_NOTIFICATIONS_PATH + "/" + userId);
        
        // Test: Check if there are any existing notifications
        notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "Found " + snapshot.getChildrenCount() + " existing notification(s) for user: " + userId);
                } else {
                    Log.d(TAG, "No existing notifications found for user: " + userId);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error checking existing notifications", error.toException());
            }
        });
    }

    /**
     * Handle incoming notification from Firebase
     */
    private static void handleNotification(Context context, DataSnapshot notificationSnapshot) {
        try {
            String type = notificationSnapshot.child("type").getValue(String.class);
            
            if ("task_assignment".equals(type)) {
                String taskIdStr = notificationSnapshot.child("task_id").getValue(String.class);
                String taskTitle = notificationSnapshot.child("task_title").getValue(String.class);
                String taskDescription = notificationSnapshot.child("task_description").getValue(String.class);
                String creatorName = notificationSnapshot.child("creator_name").getValue(String.class);
                Boolean read = notificationSnapshot.child("read").getValue(Boolean.class);

                if (read == null || !read) {
                    Log.d(TAG, "Handling unread notification: task_id=" + taskIdStr + ", title=" + taskTitle);
                    
                    MyApplication app = MyApplication.getInstance();
                    boolean isForeground = app != null && app.isAppInForeground();
                    
                    String notificationId = notificationSnapshot.getKey();
                    
                    if (isForeground) {
                        // Show cookiebar notification when app is in foreground
                        Activity currentActivity = app.getCurrentActivity();
                        if (currentActivity != null && !currentActivity.isFinishing() && !currentActivity.isDestroyed()) {
                            String title = "New Task Assigned";
                            String message = creatorName != null && !creatorName.isEmpty()
                                    ? creatorName + " assigned you: " + (taskTitle != null ? taskTitle : "a new task")
                                    : "You have been assigned: " + (taskTitle != null ? taskTitle : "a new task");
                            
                            currentActivity.runOnUiThread(() -> {
                                CookieBarToastHelper.showInfo(
                                        currentActivity,
                                        title,
                                        message,
                                        CookieBarToastHelper.LONG_DURATION
                                );
                            });
                            
                            // Play notification sound
                            SoundHelper.playNotificationSound(context);
                            Log.d(TAG, "Cookiebar notification shown for task: " + taskTitle);
                        } else {
                            Log.w(TAG, "Current activity is null or finishing, showing system notification instead");
                            // Fallback to system notification if activity is not available
                            showSystemNotification(context, taskIdStr, taskTitle, taskDescription, creatorName);
                        }
                    } else {
                        // App is in background - show system notification
                        // System notification will appear in notification tray
                        // When user clicks it, it will open ScheduleDetailActivity with the task_id
                        Log.d(TAG, "App is in background, showing system notification for task: " + taskTitle);
                        showSystemNotification(context, taskIdStr, taskTitle, taskDescription, creatorName);
                    }
                } else {
                    Log.d(TAG, "Notification already read, skipping: " + notificationSnapshot.getKey());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling notification", e);
        }
    }
    
    /**
     * Show system notification
     */
    private static void showSystemNotification(Context context, String taskIdStr, String taskTitle, 
                                               String taskDescription, String creatorName) {
        try {
            int taskId = 0;
            if (taskIdStr != null && !taskIdStr.isEmpty()) {
                taskId = Integer.parseInt(taskIdStr);
            }
            
            TaskNotificationHelper.sendTaskAssignmentNotificationFromFirebase(
                    context,
                    taskId,
                    taskTitle != null ? taskTitle : "New Task",
                    taskDescription != null ? taskDescription : "",
                    creatorName != null ? creatorName : "Someone"
            );
            Log.d(TAG, "System notification shown for task: " + taskTitle);
        } catch (Exception e) {
            Log.e(TAG, "Error showing system notification", e);
        }
    }

    /**
     * Remove notification listener (call when user logs out)
     */
    public static void removeNotificationListener() {
        if (currentNotificationRef != null && notificationListener != null) {
            currentNotificationRef.removeEventListener(notificationListener);
            currentNotificationRef = null;
            notificationListener = null;
            Log.d(TAG, "Notification listener removed");
        }
    }
}

