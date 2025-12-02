package com.sendajapan.sendasnap.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.schedule.ScheduleDetailActivity;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.utils.SoundHelper;

import java.util.List;

/**
 * Helper class for sending task assignment notifications.
 * 
 * Note: This implementation sends local notifications on the current device.
 * For true push notifications to all assignees across different devices,
 * the backend API should send FCM (Firebase Cloud Messaging) push notifications
 * when a task is created. The backend endpoint should:
 * 1. Receive task creation event
 * 2. Get FCM tokens for all assignees
 * 3. Send push notifications via FCM
 */
public class TaskNotificationHelper {

    private static final String CHANNEL_ID = "task_assignment_channel";
    private static final String CHANNEL_NAME = "Task Assignments";
    private static final String CHANNEL_DESCRIPTION = "Notifications for new task assignments";
    private static final int NOTIFICATION_ID_BASE = 2000;

    public static void sendTaskAssignmentNotifications(Context context, Task task, List<UserData> assignees) {
        if (context == null || task == null || assignees == null || assignees.isEmpty()) {
            return;
        }

        createNotificationChannel(context);

        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        UserData currentUser = prefsManager.getUser();
        if (currentUser == null) {
            return;
        }

        String creatorName = currentUser.getName() != null ? currentUser.getName() : "Someone";
        String taskTitle = task.getTitle() != null ? task.getTitle() : "New Task";
        String taskDescription = task.getDescription() != null && !task.getDescription().isEmpty()
                ? task.getDescription()
                : "You have been assigned a new task";

        for (UserData assignee : assignees) {
            if (assignee == null || assignee.getId() <= 0) {
                continue;
            }

            if (currentUser.getId() == assignee.getId()) {
                continue;
            }

            sendNotificationToAssignee(context, assignee, task, creatorName, taskTitle, taskDescription);
        }
    }

    private static void sendNotificationToAssignee(Context context, UserData assignee, Task task,
                                                   String creatorName, String taskTitle, String taskDescription) {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        if (!prefsManager.isNotificationsEnabled()) {
            return;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (!notificationManager.areNotificationsEnabled()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(context, ScheduleDetailActivity.class);
        intent.putExtra("task", task);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                context,
                task.getId() + assignee.getId(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("New Task Assigned: " + taskTitle)
                .setContentText(creatorName + " assigned you a task: " + taskDescription)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(creatorName + " assigned you a task: " + taskDescription))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        int notificationId = NOTIFICATION_ID_BASE + (task.getId() + assignee.getId()) % 1000;
        notificationManager.notify(notificationId, builder.build());
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Send notification from Firebase Realtime Database trigger
     * This is called when a notification is received via Firebase listener
     */
    public static void sendTaskAssignmentNotificationFromFirebase(Context context, int taskId,
                                                                   String taskTitle, String taskDescription,
                                                                   String creatorName) {
        if (context == null || taskId <= 0) {
            return;
        }

        createNotificationChannel(context);

        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        if (!prefsManager.isNotificationsEnabled()) {
            return;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (!notificationManager.areNotificationsEnabled()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        // Create intent to open task detail activity
        // Pass taskId so ScheduleDetailActivity can load the full task from API
        Intent intent = new Intent(context, ScheduleDetailActivity.class);
        intent.putExtra("taskId", taskId);
        // FLAG_ACTIVITY_NEW_TASK: Required when starting activity from non-Activity context (notification)
        // FLAG_ACTIVITY_CLEAR_TOP: If activity is already running, bring it to front instead of creating new instance
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                context,
                taskId, // Use taskId as request code for unique pending intent per task
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        String title = "New Task Assigned: " + taskTitle;
        String message = creatorName + " assigned you a task: " + taskDescription;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        int notificationId = NOTIFICATION_ID_BASE + (taskId % 1000);
        notificationManager.notify(notificationId, builder.build());
        
        // Play notification sound
        SoundHelper.playNotificationSound(context);
    }
}

