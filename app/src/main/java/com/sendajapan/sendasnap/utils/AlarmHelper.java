package com.sendajapan.sendasnap.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.receivers.TaskAlarmReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AlarmHelper {
    private static final String ACTION_TASK_REMINDER = "com.sendajapan.sendasnap.TASK_REMINDER";
    
    public static void setTaskAlarm(Context context, Task task) {
        if (task == null || task.getWorkDate() == null || task.getWorkTime() == null) {
            return;
        }

        try {
            // Parse date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            
            Calendar taskDateTime = Calendar.getInstance();
            taskDateTime.setTime(dateFormat.parse(task.getWorkDate()));
            
            String[] timeParts = task.getWorkTime().split(":");
            if (timeParts.length == 2) {
                taskDateTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                taskDateTime.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                taskDateTime.set(Calendar.SECOND, 0);
                taskDateTime.set(Calendar.MILLISECOND, 0);
            }

            // Don't set alarm if the date/time is in the past
            if (taskDateTime.before(Calendar.getInstance())) {
                return;
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                return;
            }

            // Create intent for alarm
            Intent intent = new Intent(context, TaskAlarmReceiver.class);
            intent.setAction(ACTION_TASK_REMINDER);
            intent.putExtra("task_id", task.getId());
            intent.putExtra("task_title", task.getTitle());
            intent.putExtra("task_description", task.getDescription());

            // Use task ID as request code to ensure unique alarms
            int requestCode;
            try {
                requestCode = (int) (Long.parseLong(task.getId()) % Integer.MAX_VALUE);
            } catch (NumberFormatException e) {
                // If ID is not a number, use hash code
                requestCode = task.getId().hashCode() & 0x7FFFFFFF;
            }
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Set alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        taskDateTime.getTimeInMillis(),
                        pendingIntent
                );
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        taskDateTime.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        taskDateTime.getTimeInMillis(),
                        pendingIntent
                );
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void cancelTaskAlarm(Context context, Task task) {
        if (task == null) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        intent.setAction(ACTION_TASK_REMINDER);
        
        int requestCode;
        try {
            requestCode = (int) (Long.parseLong(task.getId()) % Integer.MAX_VALUE);
        } catch (NumberFormatException e) {
            // If ID is not a number, use hash code
            requestCode = task.getId().hashCode() & 0x7FFFFFFF;
        }
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }
}

