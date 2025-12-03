package com.sendajapan.sendasnap.utils;

import android.Manifest;
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

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.schedule.ScheduleDetailActivity;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.models.UserData;

import java.util.List;

public class TaskNotificationHelper {

    private static final String CHANNEL_ID = "task_assignment_channel";
    private static final String CHANNEL_NAME = "Task Assignments";
    private static final String CHANNEL_DESCRIPTION = "Notifications for new task assignments";
    private static final int NOTIFICATION_ID_BASE = 2000;
    private static final int NOTIFICATION_ID_MODULO = 1000;
    private static final String DEFAULT_CREATOR_NAME = "Someone";
    private static final String DEFAULT_TASK_TITLE = "New Task";
    private static final String DEFAULT_TASK_DESCRIPTION = "You have been assigned a new task";
    private static final String NOTIFICATION_TITLE_PREFIX = "New Task Assigned: ";
    private static final String NOTIFICATION_MESSAGE_PREFIX = " assigned you a task: ";

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

        String creatorName = getCreatorName(currentUser);
        String taskTitle = getTaskTitle(task);
        String taskDescription = getTaskDescription(task);

        for (UserData assignee : assignees) {
            if (!isValidAssignee(assignee, currentUser)) {
                continue;
            }

            sendNotificationToAssignee(context, assignee, task, creatorName, taskTitle, taskDescription);
        }
    }

    public static void sendTaskAssignmentNotificationFromFirebase(Context context, int taskId,
                                                                   String taskTitle, String taskDescription,
                                                                   String creatorName) {
        if (context == null || taskId <= 0) {
            return;
        }

        createNotificationChannel(context);

        if (!areNotificationsEnabled(context)) {
            return;
        }

        Intent intent = createTaskDetailIntent(context, taskId);
        PendingIntent pendingIntent = createPendingIntent(context, taskId, intent);

        String title = NOTIFICATION_TITLE_PREFIX + (taskTitle != null ? taskTitle : DEFAULT_TASK_TITLE);
        String message = (creatorName != null ? creatorName : DEFAULT_CREATOR_NAME) 
                + NOTIFICATION_MESSAGE_PREFIX 
                + (taskDescription != null ? taskDescription : DEFAULT_TASK_DESCRIPTION);

        NotificationCompat.Builder builder = buildNotification(context, title, message, pendingIntent);

        int notificationId = NOTIFICATION_ID_BASE + (taskId % NOTIFICATION_ID_MODULO);
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
        
        SoundHelper.playNotificationSound(context);
    }

    private static void sendNotificationToAssignee(Context context, UserData assignee, Task task,
                                                   String creatorName, String taskTitle, String taskDescription) {
        if (!areNotificationsEnabled(context)) {
            return;
        }

        Intent intent = createTaskDetailIntent(context, task);
        PendingIntent pendingIntent = createPendingIntent(context, task, assignee, intent);

        String title = NOTIFICATION_TITLE_PREFIX + taskTitle;
        String message = creatorName + NOTIFICATION_MESSAGE_PREFIX + taskDescription;

        NotificationCompat.Builder builder = buildNotification(context, title, message, pendingIntent);

        int notificationId = NOTIFICATION_ID_BASE + ((task.getId() + assignee.getId()) % NOTIFICATION_ID_MODULO);
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    private static boolean areNotificationsEnabled(Context context) {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        if (!prefsManager.isNotificationsEnabled()) {
            return false;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (!notificationManager.areNotificationsEnabled()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private static Intent createTaskDetailIntent(Context context, Task task) {
        Intent intent = new Intent(context, ScheduleDetailActivity.class);
        intent.putExtra("task", task);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    private static Intent createTaskDetailIntent(Context context, int taskId) {
        Intent intent = new Intent(context, ScheduleDetailActivity.class);
        intent.putExtra("taskId", taskId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    private static PendingIntent createPendingIntent(Context context, Task task, UserData assignee, Intent intent) {
        return PendingIntent.getActivity(
                context,
                task.getId() + assignee.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static PendingIntent createPendingIntent(Context context, int taskId, Intent intent) {
        return PendingIntent.getActivity(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static NotificationCompat.Builder buildNotification(Context context, String title, 
                                                                 String message, PendingIntent pendingIntent) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
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

    private static String getCreatorName(UserData currentUser) {
        return currentUser.getName() != null ? currentUser.getName() : DEFAULT_CREATOR_NAME;
    }

    private static String getTaskTitle(Task task) {
        return task.getTitle() != null ? task.getTitle() : DEFAULT_TASK_TITLE;
    }

    private static String getTaskDescription(Task task) {
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            return task.getDescription();
        }
        return DEFAULT_TASK_DESCRIPTION;
    }

    private static boolean isValidAssignee(UserData assignee, UserData currentUser) {
        if (assignee == null || assignee.getId() <= 0) {
            return false;
        }
        return currentUser.getId() != assignee.getId();
    }
}



