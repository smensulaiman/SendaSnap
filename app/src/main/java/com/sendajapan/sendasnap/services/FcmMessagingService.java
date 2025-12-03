package com.sendajapan.sendasnap.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.schedule.ScheduleDetailActivity;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.utils.SoundHelper;

import java.util.Map;

/**
 * Firebase Cloud Messaging Service to handle push notifications
 * This service handles notifications when the app is in background or killed
 */
public class FcmMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FcmMessagingService";
    private static final String CHANNEL_ID = "task_assignment_channel";
    private static final String CHANNEL_NAME = "Task Assignments";
    private static final int NOTIFICATION_ID_BASE = 2000;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Handle data payload
        Map<String, String> data = remoteMessage.getData();
        
        if (data != null && data.containsKey("type")) {
            String type = data.get("type");
            
            if ("task_assignment".equals(type)) {
                handleTaskAssignmentNotification(data);
            }
        }

        // Handle notification payload (when app is in background, this is handled automatically)
        if (remoteMessage.getNotification() != null) {
            // If app is in foreground, we handle it manually
            // If app is in background, Android handles it automatically
            if (isAppInForeground()) {
                handleNotificationPayload(remoteMessage.getNotification(), remoteMessage.getData());
            }
        }
    }

    /**
     * Handle task assignment notification from data payload
     */
    private void handleTaskAssignmentNotification(Map<String, String> data) {
        String taskId = data.get("task_id");
        String taskTitle = data.get("task_title");
        String taskDescription = data.get("task_description");
        String creatorName = data.get("creator_name");
        String assigneeId = data.get("assignee_id");

        if (taskId == null || taskTitle == null) {
            return;
        }

        createNotificationChannel();

        // Create intent to open task details
        Intent intent = new Intent(this, ScheduleDetailActivity.class);
        intent.putExtra("task_id", taskId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (taskId + (assigneeId != null ? assigneeId : "")).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "New Task Assigned: " + taskTitle;
        String message = creatorName != null && !creatorName.isEmpty()
                ? creatorName + " assigned you a task: " + (taskDescription != null ? taskDescription : "")
                : "You have been assigned a new task: " + (taskDescription != null ? taskDescription : "");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        int notificationId = NOTIFICATION_ID_BASE + (taskId.hashCode() % 1000);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        
        // Check permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(notificationId, builder.build());
            
            // Play notification sound
            SoundHelper.playNotificationSound(this);
        }
    }

    /**
     * Handle notification payload (when app is in foreground)
     */
    private void handleNotificationPayload(RemoteMessage.Notification notification, Map<String, String> data) {
        // Create a notification from the notification payload
        createNotificationChannel();

        String taskId = data != null ? data.get("task_id") : null;
        
        Intent intent = new Intent(this, ScheduleDetailActivity.class);
        if (taskId != null) {
            intent.putExtra("task_id", taskId);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                notification.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getBody()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(NOTIFICATION_ID_BASE + notification.hashCode(), builder.build());
            SoundHelper.playNotificationSound(this);
        }
    }

    /**
     * Create notification channel for Android O+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for new task assignments");
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Check if app is in foreground
     */
    private boolean isAppInForeground() {
        // Simple check - in a real implementation, you might want to track this more accurately
        // For now, we'll assume if onMessageReceived is called, we should handle it
        return true;
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Send token to backend server
        sendTokenToServer(token);
    }

    /**
     * Send FCM token to backend server
     * This should be called whenever the token is refreshed
     */
    private void sendTokenToServer(String token) {
        // TODO: Implement API call to send token to backend
        // The backend should store this token associated with the user ID
        // Example: POST /api/users/{userId}/fcm-token
        // Body: { "fcm_token": token }
    }
}

