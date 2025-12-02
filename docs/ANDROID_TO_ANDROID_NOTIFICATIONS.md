# Android-to-Android Push Notifications via Firebase Realtime Database

## Overview

This document explains how push notifications are sent directly from Android app to Android app using Firebase Realtime Database, without requiring a backend server.

## Architecture

### How It Works

1. **FCM Token Storage**: When a user logs in, their FCM token is stored in Firebase Realtime Database
2. **Notification Writing**: When a task is created, notification data is written to Firebase under each assignee's path
3. **Real-time Listening**: Each device listens to Firebase for new notifications assigned to the current user
4. **Local Notification**: When a new notification is detected, a local notification is shown on the device

### Key Components

1. **FcmTokenManager** (`utils/FcmTokenManager.java`)
   - Manages FCM token registration
   - Stores FCM token in Firebase Realtime Database after login
   - Handles token refresh automatically

2. **FcmNotificationSender** (`utils/FcmNotificationSender.java`)
   - Stores FCM tokens in Firebase
   - Writes task assignment notifications to Firebase
   - Sets up Firebase listeners for incoming notifications

3. **TaskNotificationHelper** (`utils/TaskNotificationHelper.java`)
   - Shows local notifications when triggered by Firebase listeners
   - Handles notification display and sound

4. **MainActivity** (`activities/MainActivity.java`)
   - Sets up notification listener when app starts
   - Listens for new task assignments in real-time

## Firebase Database Structure

### FCM Tokens Storage

```
fcm_tokens/
└── {userId}/                    # User's Firebase ID (sanitized email)
    ├── fcm_token                # FCM registration token
    ├── updated_at               # Timestamp when token was last updated
    ├── user_id                  # User's database ID
    └── user_email               # User's email address
```

**Example:**
```
fcm_tokens/
└── user_example_com/
    ├── fcm_token: "dK3jH9fL2mN..."
    ├── updated_at: 1704067200000
    ├── user_id: 123
    └── user_email: "user@example.com"
```

### Task Notifications Storage

```
task_notifications/
└── {assigneeFirebaseId}/        # Assignee's Firebase ID
    └── {notificationId}/        # Auto-generated notification ID
        ├── type                  # "task_assignment"
        ├── task_id               # Task ID from database
        ├── task_title            # Task title
        ├── task_description      # Task description
        ├── creator_name          # Creator's name
        ├── creator_id            # Creator's user ID
        ├── assignee_id           # Assignee's user ID
        ├── assignee_firebase_id  # Assignee's Firebase ID
        ├── created_at            # Timestamp when notification was created
        └── read                  # Boolean: false initially, true after notification shown
```

**Example:**
```
task_notifications/
└── assignee_example_com/
    └── -NxYz123abc/
        ├── type: "task_assignment"
        ├── task_id: 456
        ├── task_title: "Complete project review"
        ├── task_description: "Please review the project documentation"
        ├── creator_name: "John Doe"
        ├── creator_id: 123
        ├── assignee_id: 789
        ├── assignee_firebase_id: "assignee_example_com"
        ├── created_at: 1704067200000
        └── read: false
```

## Implementation Flow

### 1. User Login & Token Registration

```
User logs in
  └─> LoginActivity.onSuccess()
      ├─> FcmTokenManager.registerToken()
      │   └─> FirebaseMessaging.getInstance().getToken()
      │       └─> FcmNotificationSender.storeFcmTokenInFirebase()
      │           └─> Write to: fcm_tokens/{userId}/
      │
      └─> MainActivity.onCreate()
          └─> FcmNotificationSender.setupNotificationListener()
              └─> Listen to: task_notifications/{currentUserId}/
```

### 2. Task Creation & Notification Sending

```
User creates task
  └─> AddScheduleActivity.saveTask()
      └─> TaskViewModel.createTask()
          └─> onSuccess(createdTask)
              ├─> FcmNotificationSender.sendTaskAssignmentNotifications()
              │   ├─> For each assignee:
              │   │   ├─> Get assignee's Firebase ID
              │   │   └─> Write notification to: task_notifications/{assigneeId}/{notificationId}
              │   │
              │   └─> Notification data includes:
              │       ├─> task_id, task_title, task_description
              │       ├─> creator_name, creator_id
              │       ├─> assignee_id, assignee_firebase_id
              │       └─> created_at, read: false
              │
              └─> TaskNotificationHelper.sendTaskAssignmentNotifications()
                  └─> Show local notification on creator's device
```

### 3. Notification Reception

```
Assignee's device (any state: foreground, background, or killed)
  └─> Firebase Listener (setupNotificationListener)
      └─> Detects new notification in task_notifications/{assigneeId}/
          └─> handleNotification()
              ├─> Check if notification is unread (read == false)
              ├─> Check if notification is recent (< 5 minutes old)
              │
              ├─> TaskNotificationHelper.sendTaskAssignmentNotificationFromFirebase()
              │   ├─> Create local notification
              │   ├─> Set title: "New Task Assigned: [Task Title]"
              │   ├─> Set message: "[Creator Name] assigned you a task: [Description]"
              │   ├─> Set intent to open ScheduleDetailActivity
              │   └─> Show notification
              │
              ├─> SoundHelper.playNotificationSound()
              │   └─> Play notification sound
              │
              └─> Mark notification as read
                  └─> Update: task_notifications/{assigneeId}/{notificationId}/read = true
```

## Code Examples

### Storing FCM Token

```java
// Called automatically after login
FcmTokenManager.registerToken(context);

// Internally calls:
FcmNotificationSender.storeFcmTokenInFirebase(context, fcmToken);
```

### Sending Notification

```java
// Called when task is created
FcmNotificationSender.sendTaskAssignmentNotifications(
    context,
    createdTask,
    taskAssignees
);
```

### Setting Up Listener

```java
// Called in MainActivity.onCreate()
FcmNotificationSender.setupNotificationListener(context);
```

## Features

### ✅ Advantages

1. **No Backend Required**: Everything works directly from Android app
2. **Real-time**: Firebase listeners trigger instantly when data changes
3. **Works Offline**: Firebase Realtime Database handles offline sync
4. **Works in Background**: Firebase listeners work even when app is closed
5. **Automatic Sync**: Firebase handles synchronization across devices
6. **Secure**: Uses Firebase Realtime Database security rules
7. **Scalable**: Firebase handles multiple concurrent connections

### ⚠️ Considerations

1. **Firebase Security Rules**: Must be configured to allow read/write access
2. **Data Cleanup**: Old notifications should be cleaned up periodically
3. **Network Dependency**: Requires internet connection (Firebase handles offline queue)
4. **Firebase Costs**: Firebase Realtime Database has usage limits (free tier available)

## Firebase Security Rules

To make this work, you need to configure Firebase Realtime Database security rules:

```json
{
  "rules": {
    "fcm_tokens": {
      "$userId": {
        ".read": "$userId === auth.uid || auth != null",
        ".write": "$userId === auth.uid || auth != null"
      }
    },
    "task_notifications": {
      "$userId": {
        ".read": "$userId === auth.uid || auth != null",
        ".write": "auth != null",
        "$notificationId": {
          ".validate": "newData.hasChildren(['type', 'task_id', 'task_title', 'created_at'])"
        }
      }
    }
  }
}
```

**Note**: Adjust security rules based on your authentication setup. The above assumes Firebase Authentication is used.

## Testing

### Test Scenario 1: Basic Notification Flow

1. **Device A (Creator)**:
   - Login as User A
   - Create a task with User B as assignee
   - Verify notification data is written to Firebase

2. **Device B (Assignee)**:
   - Login as User B
   - Wait for notification to appear
   - Verify notification shows correct task details
   - Tap notification → Should open task details

### Test Scenario 2: Background Notification

1. **Device B**:
   - Login as User B
   - Put app in background (press home button)
   - **Device A**: Create task with User B as assignee
   - **Device B**: Should receive notification even in background

### Test Scenario 3: Multiple Assignees

1. **Device A**:
   - Create task with multiple assignees (User B, User C, User D)
   - Verify notification data written for each assignee in Firebase

2. **Device B, C, D**:
   - Each should receive notification independently
   - Verify notifications are not duplicated

### Test Scenario 4: Token Refresh

1. **Device A**:
   - Login
   - Verify FCM token stored in Firebase
   - Force token refresh (uninstall/reinstall app)
   - Verify new token is stored in Firebase

## Troubleshooting

### Notifications Not Appearing

1. **Check Firebase Connection**:
   - Verify internet connection
   - Check Firebase Realtime Database is accessible
   - Verify security rules allow read/write

2. **Check Token Storage**:
   - Verify FCM token is stored in `fcm_tokens/{userId}/`
   - Check token is not null or empty

3. **Check Notification Writing**:
   - Verify notification data is written to `task_notifications/{assigneeId}/`
   - Check Firebase console for data

4. **Check Listener Setup**:
   - Verify `setupNotificationListener()` is called in MainActivity
   - Check listener is not removed prematurely

5. **Check Notification Permissions**:
   - Verify notification permissions are granted (Android 13+)
   - Check app notification settings

### Duplicate Notifications

- **Cause**: Listener triggering multiple times for same notification
- **Solution**: Notification is marked as `read: true` after showing to prevent duplicates

### Old Notifications Showing

- **Cause**: Listener processing old unread notifications
- **Solution**: Code checks `created_at` timestamp and only shows notifications < 5 minutes old

## Data Cleanup

Consider implementing periodic cleanup of old notifications:

```java
// Example: Clean up notifications older than 7 days
public static void cleanupOldNotifications(String userId) {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference notificationsRef = database.getReference("task_notifications").child(userId);
    
    long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
    
    notificationsRef.orderByChild("created_at")
        .endAt(sevenDaysAgo)
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot notification : snapshot.getChildren()) {
                    notification.getRef().removeValue();
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Error cleaning up notifications", error.toException());
            }
        });
}
```

## Future Enhancements

1. **Notification Categories**: Support different notification types (task updates, comments, etc.)
2. **Notification Preferences**: Allow users to configure notification settings per category
3. **Notification History**: Store notification history for users to view
4. **Batch Notifications**: Group multiple notifications together
5. **Rich Notifications**: Add images, action buttons, etc.

## Related Documentation

- [Schedule Flow Documentation](SCHEDULE_FLOW.md)
- [Firebase Push Notifications Setup](FIREBASE_PUSH_NOTIFICATIONS_SETUP.md) (for backend integration)
- [Chat System Architecture](CHAT_SYSTEM_ARCHITECTURE.md)

