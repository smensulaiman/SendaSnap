package com.sendajapan.sendasnap.utils;

import android.app.Activity;
import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sendajapan.sendasnap.MyApplication;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.models.UserData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FcmNotificationSender {

    private static final String FCM_TOKENS_PATH = "fcm_tokens";
    private static final String TASK_NOTIFICATIONS_PATH = "task_notifications";
    private static final long NOTIFICATION_TIME_LIMIT_MS = 600000;
    private static final String TYPE_TASK_ASSIGNMENT = "task_assignment";

    private static ValueEventListener notificationListener;
    private static DatabaseReference currentNotificationRef;

    public static void storeFcmTokenInFirebase(Context context, String fcmToken) {
        if (context == null || fcmToken == null || fcmToken.isEmpty()) {
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
        DatabaseReference tokenRef = database.getReference(FCM_TOKENS_PATH).child(userId);

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcm_token", fcmToken);
        tokenData.put("updated_at", System.currentTimeMillis());
        tokenData.put("user_id", user.getId());
        tokenData.put("user_email", user.getEmail());

        tokenRef.setValue(tokenData);
    }

    public static void sendTaskAssignmentNotifications(Context context, Task task, List<UserData> assignees) {
        if (context == null || task == null || assignees == null || assignees.isEmpty()) {
            return;
        }

        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        UserData currentUser = prefsManager.getUser();
        
        if (currentUser == null) {
            return;
        }

        String creatorName = getCreatorName(currentUser);
        String taskTitle = getTaskTitle(task);
        String taskDescription = getTaskDescription(task);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notificationsRef = database.getReference(TASK_NOTIFICATIONS_PATH);

        for (UserData assignee : assignees) {
            if (!isValidAssignee(assignee, currentUser)) {
                continue;
            }

            String assigneeFirebaseId = getAssigneeFirebaseId(assignee);
            if (assigneeFirebaseId.isEmpty()) {
                continue;
            }

            String notificationId = notificationsRef.push().getKey();
            if (notificationId == null) {
                continue;
            }

            Map<String, Object> notificationData = createNotificationData(
                    task, creatorName, currentUser, assignee, assigneeFirebaseId
            );

            DatabaseReference assigneeNotificationRef = notificationsRef
                    .child(assigneeFirebaseId)
                    .child(notificationId);

            assigneeNotificationRef.setValue(notificationData);
        }
    }

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

        removeExistingListener();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference notificationsRef = database.getReference(TASK_NOTIFICATIONS_PATH).child(userId);

        notificationListener = createNotificationListener(context);
        notificationsRef.addValueEventListener(notificationListener);
        currentNotificationRef = notificationsRef;
    }

    public static void removeNotificationListener() {
        removeExistingListener();
    }

    private static void removeExistingListener() {
        if (currentNotificationRef != null && notificationListener != null) {
            currentNotificationRef.removeEventListener(notificationListener);
            currentNotificationRef = null;
            notificationListener = null;
        }
    }

    private static ValueEventListener createNotificationListener(Context context) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }

                long currentTime = System.currentTimeMillis();
                for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                    if (shouldProcessNotification(notificationSnapshot, currentTime)) {
                        handleNotification(context, notificationSnapshot);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        };
    }

    private static boolean shouldProcessNotification(DataSnapshot notificationSnapshot, long currentTime) {
        try {
            Boolean read = notificationSnapshot.child("read").getValue(Boolean.class);
            Long createdAt = notificationSnapshot.child("created_at").getValue(Long.class);
            
            if (read != null && read) {
                return false;
            }

            if (createdAt == null) {
                return false;
            }

            long timeDiff = currentTime - createdAt;
            return timeDiff < NOTIFICATION_TIME_LIMIT_MS;
        } catch (Exception e) {
            return false;
        }
    }

    private static void handleNotification(Context context, DataSnapshot notificationSnapshot) {
        try {
            String type = notificationSnapshot.child("type").getValue(String.class);
            
            if (!TYPE_TASK_ASSIGNMENT.equals(type)) {
                return;
            }

            Boolean read = notificationSnapshot.child("read").getValue(Boolean.class);
            if (read != null && read) {
                return;
            }

            String taskIdStr = notificationSnapshot.child("task_id").getValue(String.class);
            String taskTitle = notificationSnapshot.child("task_title").getValue(String.class);
            String taskDescription = notificationSnapshot.child("task_description").getValue(String.class);
            String creatorName = notificationSnapshot.child("creator_name").getValue(String.class);

            MyApplication app = MyApplication.getInstance();
            boolean isForeground = app != null && app.isAppInForeground();
            
            if (isForeground) {
                showForegroundNotification(context, app, taskTitle, creatorName);
            } else {
                showSystemNotification(context, taskIdStr, taskTitle, taskDescription, creatorName);
            }
        } catch (Exception e) {
        }
    }

    private static void showForegroundNotification(Context context, MyApplication app, 
                                                   String taskTitle, String creatorName) {
        Activity currentActivity = app.getCurrentActivity();
        if (currentActivity == null || currentActivity.isFinishing() || currentActivity.isDestroyed()) {
            return;
        }

        String title = "New Task Assigned";
        String message = buildNotificationMessage(creatorName, taskTitle);

        currentActivity.runOnUiThread(() -> {
            CookieBarToastHelper.showInfo(
                    currentActivity,
                    title,
                    message,
                    CookieBarToastHelper.LONG_DURATION
            );
        });

        SoundHelper.playNotificationSound(context);
    }

    private static String buildNotificationMessage(String creatorName, String taskTitle) {
        if (creatorName != null && !creatorName.isEmpty()) {
            return creatorName + " assigned you: " + (taskTitle != null ? taskTitle : "a new task");
        }
        return "You have been assigned: " + (taskTitle != null ? taskTitle : "a new task");
    }

    private static void showSystemNotification(Context context, String taskIdStr, String taskTitle, 
                                               String taskDescription, String creatorName) {
        try {
            int taskId = parseTaskId(taskIdStr);
            
            TaskNotificationHelper.sendTaskAssignmentNotificationFromFirebase(
                    context,
                    taskId,
                    taskTitle != null ? taskTitle : "New Task",
                    taskDescription != null ? taskDescription : "",
                    creatorName != null ? creatorName : "Someone"
            );
        } catch (Exception e) {
        }
    }

    private static int parseTaskId(String taskIdStr) {
        if (taskIdStr != null && !taskIdStr.isEmpty()) {
            try {
                return Integer.parseInt(taskIdStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private static String getCreatorName(UserData currentUser) {
        return currentUser.getName() != null ? currentUser.getName() : "Someone";
    }

    private static String getTaskTitle(Task task) {
        return task.getTitle() != null ? task.getTitle() : "New Task";
    }

    private static String getTaskDescription(Task task) {
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            return task.getDescription();
        }
        return "You have been assigned a new task";
    }

    private static boolean isValidAssignee(UserData assignee, UserData currentUser) {
        if (assignee == null || assignee.getId() <= 0) {
            return false;
        }

        if (currentUser.getId() == assignee.getId()) {
            return false;
        }

        String assigneeEmail = assignee.getEmail();
        return assigneeEmail != null && !assigneeEmail.isEmpty();
    }

    private static String getAssigneeFirebaseId(UserData assignee) {
        String assigneeEmail = assignee.getEmail();
        if (assigneeEmail == null || assigneeEmail.isEmpty()) {
            return "";
        }
        return FirebaseUtils.sanitizeEmailForKey(assigneeEmail);
    }

    private static Map<String, Object> createNotificationData(Task task, String creatorName,
                                                               UserData currentUser, UserData assignee,
                                                               String assigneeFirebaseId) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", TYPE_TASK_ASSIGNMENT);
        notificationData.put("task_id", String.valueOf(task.getId()));
        notificationData.put("task_title", getTaskTitle(task));
        notificationData.put("task_description", getTaskDescription(task));
        notificationData.put("creator_name", creatorName);
        notificationData.put("creator_id", currentUser.getId());
        notificationData.put("assignee_id", assignee.getId());
        notificationData.put("assignee_firebase_id", assigneeFirebaseId);
        notificationData.put("created_at", System.currentTimeMillis());
        notificationData.put("read", false);
        return notificationData;
    }
}
