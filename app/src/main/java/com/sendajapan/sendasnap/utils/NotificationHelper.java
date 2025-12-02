package com.sendajapan.sendasnap.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sendajapan.sendasnap.models.Notification;
import com.sendajapan.sendasnap.models.UserData;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to manage notification badge count and retrieve notifications
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private static final String TASK_NOTIFICATIONS_PATH = "task_notifications";

    /**
     * Interface for unread count callbacks
     */
    public interface UnreadCountCallback {
        void onSuccess(int unreadCount);
        void onFailure(Exception e);
    }

    /**
     * Interface for notifications list callbacks
     */
    public interface NotificationsCallback {
        void onSuccess(List<Notification> notifications);
        void onFailure(Exception e);
    }

    /**
     * Get unread notification count for current user
     */
    public static void getUnreadNotificationCount(Context context, UnreadCountCallback callback) {
        if (context == null || callback == null) {
            return;
        }

        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        UserData user = prefsManager.getUser();

        if (user == null || user.getEmail() == null) {
            callback.onSuccess(0);
            return;
        }

        String userId = FirebaseUtils.sanitizeEmailForKey(user.getEmail());
        if (userId.isEmpty()) {
            callback.onSuccess(0);
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notificationsRef = database.getReference(TASK_NOTIFICATIONS_PATH).child(userId);

        // Query for unread notifications
        Query unreadQuery = notificationsRef.orderByChild("read").equalTo(false);

        unreadQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int unreadCount = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                        Boolean read = notificationSnapshot.child("read").getValue(Boolean.class);
                        if (read == null || !read) {
                            Integer creatorId = notificationSnapshot.child("creator_id").getValue(Integer.class);
                            Integer assigneeId = notificationSnapshot.child("assignee_id").getValue(Integer.class);
                            
                            if (creatorId != null && creatorId.equals(user.getId())) {
                                unreadCount++;
                            } else if (assigneeId != null && assigneeId.equals(user.getId())) {
                                unreadCount++;
                            }
                        }
                    }
                }
                Log.d(TAG, "Unread notification count: " + unreadCount + " for user: " + user.getEmail());
                callback.onSuccess(unreadCount);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to get unread notification count", error.toException());
                callback.onFailure(error.toException());
            }
        });
    }

    /**
     * Add listener for real-time unread count updates
     * Returns the listener so it can be removed later
     */
    public static ValueEventListener addUnreadCountListener(Context context, UnreadCountCallback callback) {
        if (context == null || callback == null) {
            return null;
        }

        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        UserData user = prefsManager.getUser();

        if (user == null || user.getEmail() == null) {
            callback.onSuccess(0);
            return null;
        }

        String userId = FirebaseUtils.sanitizeEmailForKey(user.getEmail());
        if (userId.isEmpty()) {
            callback.onSuccess(0);
            return null;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notificationsRef = database.getReference(TASK_NOTIFICATIONS_PATH).child(userId);

        // Query for unread notifications
        Query unreadQuery = notificationsRef.orderByChild("read").equalTo(false);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int unreadCount = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                        Boolean read = notificationSnapshot.child("read").getValue(Boolean.class);
                        if (read == null || !read) {
                            // Check if user is creator or assignee
                            Integer creatorId = notificationSnapshot.child("creator_id").getValue(Integer.class);
                            Integer assigneeId = notificationSnapshot.child("assignee_id").getValue(Integer.class);
                            
                            if (creatorId != null && creatorId.equals(user.getId())) {
                                // User is creator
                                unreadCount++;
                            } else if (assigneeId != null && assigneeId.equals(user.getId())) {
                                // User is assignee
                                unreadCount++;
                            }
                        }
                    }
                }
                callback.onSuccess(unreadCount);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to listen for unread notification count", error.toException());
                callback.onFailure(error.toException());
            }
        };

        unreadQuery.addValueEventListener(listener);
        return listener;
    }

    /**
     * Remove unread count listener
     */
    public static void removeUnreadCountListener(Context context, ValueEventListener listener) {
        if (context == null || listener == null) {
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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notificationsRef = database.getReference(TASK_NOTIFICATIONS_PATH).child(userId);
        Query unreadQuery = notificationsRef.orderByChild("read").equalTo(false);
        unreadQuery.removeEventListener(listener);
    }

    /**
     * Get all notifications for current user (both read and unread)
     * Only returns notifications where user is creator or assignee
     */
    public static void getAllNotifications(Context context, NotificationsCallback callback) {
        if (context == null || callback == null) {
            return;
        }

        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        UserData user = prefsManager.getUser();

        if (user == null || user.getEmail() == null) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        String userId = FirebaseUtils.sanitizeEmailForKey(user.getEmail());
        if (userId.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notificationsRef = database.getReference(TASK_NOTIFICATIONS_PATH).child(userId);

        // Get all notifications (we'll sort them in code since Firebase doesn't support descending order easily)
        Query query = notificationsRef;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Notification> notifications = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                        try {
                            // Check if user is creator or assignee
                            Integer creatorId = notificationSnapshot.child("creator_id").getValue(Integer.class);
                            Integer assigneeId = notificationSnapshot.child("assignee_id").getValue(Integer.class);
                            
                            boolean isRelevant = false;
                            if (creatorId != null && creatorId.equals(user.getId())) {
                                isRelevant = true;
                            } else if (assigneeId != null && assigneeId.equals(user.getId())) {
                                isRelevant = true;
                            }

                            if (isRelevant) {
                                Notification notification = new Notification();
                                notification.setNotificationId(notificationSnapshot.getKey());
                                notification.setType(notificationSnapshot.child("type").getValue(String.class));
                                
                                String taskIdStr = notificationSnapshot.child("task_id").getValue(String.class);
                                if (taskIdStr != null) {
                                    try {
                                        notification.setTaskId(Integer.parseInt(taskIdStr));
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, "Invalid task_id: " + taskIdStr);
                                    }
                                }
                                
                                notification.setTaskTitle(notificationSnapshot.child("task_title").getValue(String.class));
                                notification.setTaskDescription(notificationSnapshot.child("task_description").getValue(String.class));
                                notification.setCreatorName(notificationSnapshot.child("creator_name").getValue(String.class));
                                
                                Integer creatorIdValue = notificationSnapshot.child("creator_id").getValue(Integer.class);
                                if (creatorIdValue != null) {
                                    notification.setCreatorId(creatorIdValue);
                                }
                                
                                Integer assigneeIdValue = notificationSnapshot.child("assignee_id").getValue(Integer.class);
                                if (assigneeIdValue != null) {
                                    notification.setAssigneeId(assigneeIdValue);
                                }
                                
                                notification.setAssigneeFirebaseId(notificationSnapshot.child("assignee_firebase_id").getValue(String.class));
                                
                                Long createdAt = notificationSnapshot.child("created_at").getValue(Long.class);
                                if (createdAt != null) {
                                    notification.setCreatedAt(createdAt);
                                }
                                
                                Boolean read = notificationSnapshot.child("read").getValue(Boolean.class);
                                notification.setRead(read != null && read);

                                notifications.add(notification);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing notification", e);
                        }
                    }
                }
                
                // Sort by created_at descending (newest first)
                notifications.sort((n1, n2) -> Long.compare(n2.getCreatedAt(), n1.getCreatedAt()));
                
                callback.onSuccess(notifications);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to get notifications", error.toException());
                callback.onFailure(error.toException());
            }
        });
    }

    /**
     * Mark notification as read
     */
    public static void markNotificationAsRead(Context context, String notificationId) {
        if (context == null || notificationId == null || notificationId.isEmpty()) {
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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notificationRef = database.getReference(TASK_NOTIFICATIONS_PATH)
                .child(userId)
                .child(notificationId)
                .child("read");

        notificationRef.setValue(true)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification marked as read: " + notificationId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to mark notification as read: " + notificationId, e));
    }
}

