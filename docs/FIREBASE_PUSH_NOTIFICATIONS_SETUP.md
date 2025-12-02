# Firebase Cloud Messaging (FCM) Push Notifications Setup Guide

## Overview

This guide explains how Firebase Cloud Messaging (FCM) push notifications are implemented for task assignments and what needs to be configured.

**Current Configuration**: ✅ Firebase Cloud Messaging API (V1) is enabled  
**Current Configuration**: ❌ Cloud Messaging API (Legacy) is disabled (correct for new projects)

This setup uses the **V1 API** which requires **Service Account JSON credentials** instead of server keys.

## Implementation Status

### ✅ Completed (Android App)

**Note**: The app currently uses **Android-to-Android notifications via Firebase Realtime Database** (no backend required). See `docs/ANDROID_TO_ANDROID_NOTIFICATIONS.md` for details.

For backend-based FCM push notifications, the following components are available:

1. **FcmMessagingService** (`app/src/main/java/com/sendajapan/sendasnap/services/FcmMessagingService.java`)
   - Handles incoming FCM push notifications from backend
   - Works when app is in foreground, background, or killed
   - Creates notifications with proper channels
   - Plays notification sound
   - Opens task details when notification is tapped

2. **FcmTokenManager** (`app/src/main/java/com/sendajapan/sendasnap/utils/FcmTokenManager.java`)
   - Manages FCM token registration
   - Currently stores tokens in Firebase Realtime Database (for Android-to-Android notifications)
   - Can be extended to send tokens to backend
   - Handles token refresh automatically

3. **FcmNotificationSender** (`app/src/main/java/com/sendajapan/sendasnap/utils/FcmNotificationSender.java`)
   - **Current Implementation**: Sends notifications via Firebase Realtime Database
   - Stores FCM tokens in Firebase
   - Writes notification data to Firebase for real-time delivery

4. **AndroidManifest Configuration**
   - FCM service registered
   - Proper permissions configured

5. **Token Registration**
   - Automatically registers FCM token after user login
   - Token stored in Firebase Realtime Database
   - Token refresh handled automatically

## Firebase Console Configuration

### Step 1: Enable Cloud Messaging API (V1) ✅

**Status**: Firebase Cloud Messaging API (V1) is enabled ✅  
**Status**: Cloud Messaging API (Legacy) is disabled (this is correct for new projects)

The V1 API is the recommended approach and provides better security and features.

### Step 2: Get Service Account Credentials (V1 API)

Since you're using V1 API, you need a Service Account JSON key file:

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project: `sendasnap-85e3d`
3. Navigate to **IAM & Admin** → **Service Accounts**
4. If no service account exists:
   - Click **Create Service Account**
   - Name it (e.g., "fcm-notification-service")
   - Grant role: **Firebase Cloud Messaging Admin** or **Firebase Admin SDK Administrator Service Agent**
   - Click **Done**
5. Click on the service account you created
6. Go to **Keys** tab
7. Click **Add Key** → **Create new key**
8. Select **JSON** format
9. Download the JSON key file
10. **Keep this file secure** - it provides full access to your Firebase project
11. Backend will use this JSON file for authentication

**Important**: 
- Do NOT commit this JSON file to version control
- Store it securely on your backend server
- Use environment variables or secure secret management

### Step 3: Verify App Configuration

1. Ensure `google-services.json` is in `app/` directory
2. Verify package name matches: `com.sendajapan.sendasnap`
3. Check that Firebase Messaging dependency is included in `build.gradle`

## Backend Implementation Required

### 1. Store FCM Tokens

When a user logs in, the app sends the FCM token to the backend. You need to:

**Endpoint**: `POST /api/users/{userId}/fcm-token`

**Request Body**:
```json
{
  "fcm_token": "fcm_token_string",
  "device_id": "unique_device_id" // optional
}
```

**Implementation**:
- Store FCM token associated with user ID
- Update token when it changes (token refresh)
- Support multiple devices per user (store array of tokens)

### 2. Send Push Notifications on Task Creation

When a task is created via `POST /tasks`, the backend should:

1. Extract assignee user IDs from the request
2. Get FCM tokens for all assignees (except creator)
3. Send FCM push notifications to each assignee

**FCM Notification Payload**:

```json
{
  "to": "fcm_token_here",
  "notification": {
    "title": "New Task Assigned: [Task Title]",
    "body": "[Creator Name] assigned you a task: [Task Description]"
  },
  "data": {
    "type": "task_assignment",
    "task_id": "123",
    "task_title": "Task Title",
    "task_description": "Task Description",
    "creator_name": "Creator Name",
    "assignee_id": "456"
  },
  "priority": "high",
  "android": {
    "priority": "high",
    "notification": {
      "sound": "default",
      "channel_id": "task_assignment_channel"
    }
  }
}
```

### 3. Backend Code Example (Node.js/Express) - V1 API

**Install Firebase Admin SDK:**
```bash
npm install firebase-admin
```

**Initialize Firebase Admin SDK with Service Account:**
```javascript
const admin = require('firebase-admin');

// Initialize Firebase Admin SDK with service account JSON
// Option 1: Load from file (for development)
const serviceAccount = require('./path/to/serviceAccountKey.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: 'sendasnap-85e3d'
});

// Option 2: Load from environment variable (for production)
// const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
// admin.initializeApp({
//   credential: admin.credential.cert(serviceAccount),
//   projectId: 'sendasnap-85e3d'
// });

// Function to send task assignment notification
async function sendTaskAssignmentNotification(assigneeToken, task, creatorName) {
  const message = {
    token: assigneeToken,
    notification: {
      title: `New Task Assigned: ${task.title}`,
      body: `${creatorName} assigned you a task: ${task.description || 'New task'}`
    },
    data: {
      type: 'task_assignment',
      task_id: task.id.toString(),
      task_title: task.title,
      task_description: task.description || '',
      creator_name: creatorName,
      assignee_id: task.assigneeId.toString()
    },
    android: {
      priority: 'high',
      notification: {
        sound: 'default',
        channelId: 'task_assignment_channel'
      }
    },
    apns: {
      headers: {
        'apns-priority': '10'
      },
      payload: {
        aps: {
          sound: 'default'
        }
      }
    }
  };

  try {
    const response = await admin.messaging().send(message);
    console.log('Successfully sent message:', response);
    return response;
  } catch (error) {
    console.error('Error sending message:', error);
    throw error;
  }
}

// In your task creation endpoint
app.post('/api/tasks', async (req, res) => {
  // ... create task logic ...
  
  // After task is created, send notifications to assignees
  const assignees = task.assignedUsers; // Array of user IDs
  const creator = req.user; // Current user creating the task
  
  for (const assigneeId of assignees) {
    if (assigneeId !== creator.id) {
      // Get FCM token for assignee
      const fcmToken = await getFcmTokenForUser(assigneeId);
      
      if (fcmToken) {
        await sendTaskAssignmentNotification(fcmToken, task, creator.name);
      }
    }
  }
  
  res.json(task);
});
```

### 4. Backend Code Example (Python/Django) - V1 API

**Install Firebase Admin SDK:**
```bash
pip install firebase-admin
```

**Initialize Firebase Admin SDK with Service Account:**
```python
import os
from firebase_admin import messaging, credentials, initialize_app

# Initialize Firebase Admin SDK with service account JSON
# Option 1: Load from file (for development)
cred = credentials.Certificate("path/to/serviceAccountKey.json")
initialize_app(cred, {
    'projectId': 'sendasnap-85e3d'
})

# Option 2: Load from environment variable (for production)
# import json
# service_account_info = json.loads(os.environ.get('FIREBASE_SERVICE_ACCOUNT'))
# cred = credentials.Certificate(service_account_info)
# initialize_app(cred, {'projectId': 'sendasnap-85e3d'})

def send_task_assignment_notification(assignee_token, task, creator_name):
    message = messaging.Message(
        token=assignee_token,
        notification=messaging.Notification(
            title=f"New Task Assigned: {task.title}",
            body=f"{creator_name} assigned you a task: {task.description or 'New task'}"
        ),
        data={
            'type': 'task_assignment',
            'task_id': str(task.id),
            'task_title': task.title,
            'task_description': task.description or '',
            'creator_name': creator_name,
            'assignee_id': str(task.assignee_id)
        },
        android=messaging.AndroidConfig(
            priority='high',
            notification=messaging.AndroidNotification(
                sound='default',
                channel_id='task_assignment_channel'
            )
        ),
        apns=messaging.APNSConfig(
            headers={'apns-priority': '10'},
            payload=messaging.APNSPayload(
                aps=messaging.Aps(sound='default')
            )
        )
    )
    
    try:
        response = messaging.send(message)
        print(f'Successfully sent message: {response}')
        return response
    except Exception as e:
        print(f'Error sending message: {e}')
        raise

# In your task creation view
@api_view(['POST'])
def create_task(request):
    # ... create task logic ...
    
    # After task is created, send notifications
    assignees = task.assigned_users.all()
    creator = request.user
    
    for assignee in assignees:
        if assignee.id != creator.id:
            fcm_token = get_fcm_token_for_user(assignee.id)
            if fcm_token:
                send_task_assignment_notification(fcm_token, task, creator.name)
    
    return Response(task_data)
```

## Testing Push Notifications

### 1. Test from Firebase Console

1. Go to Firebase Console → **Cloud Messaging**
2. Click **Send test message** (or **Send your first message**)
3. For V1 API, you can:
   - Enter FCM token (get from app logs: `FcmTokenManager`)
   - Enter title and message
   - Click **Test**
   
**Note**: The Firebase Console test message feature works with both V1 and Legacy APIs. However, for production use, you must use the V1 API with service account credentials in your backend.

### 2. Test from Backend

Use the backend code examples above to send test notifications.

### 3. Test Scenarios

- ✅ App in foreground: Notification should appear
- ✅ App in background: Notification should appear
- ✅ App killed: Notification should appear
- ✅ Tap notification: Should open task details
- ✅ Multiple assignees: Each should receive notification
- ✅ Creator excluded: Creator should not receive notification

## Troubleshooting

### Notifications Not Appearing

1. **Check FCM Token Registration**
   - Verify token is being sent to backend
   - Check app logs for token registration
   - Ensure token is stored in backend database

2. **Check Backend Implementation**
   - Verify backend is sending notifications
   - Check backend logs for errors
   - Ensure FCM credentials are correct

3. **Check Device Settings**
   - Ensure notifications are enabled in app settings
   - Check Android notification permissions (Android 13+)
   - Verify notification channel is created

4. **Check Firebase Configuration**
   - Verify `google-services.json` is correct
   - Ensure Cloud Messaging API is enabled
   - Check Firebase project settings

### Common Issues

**Issue**: Notifications work in foreground but not in background
- **Solution**: Ensure backend sends both `notification` and `data` payloads

**Issue**: Notifications not appearing when app is killed
- **Solution**: Ensure `data` payload is included (Android handles `notification` payload automatically when app is killed)

**Issue**: Token refresh not working
- **Solution**: Implement `onNewToken` callback in backend to update stored tokens

**Issue**: "Permission denied" or authentication errors when sending notifications
- **Solution**: 
  - Verify service account JSON file is correct
  - Ensure service account has **Firebase Cloud Messaging Admin** role
  - Check that project ID matches: `sendasnap-85e3d`
  - Verify V1 API is enabled (which it is ✅)

**Issue**: Legacy API errors (if accidentally using old code)
- **Solution**: Make sure you're using Firebase Admin SDK with service account, not the legacy server key

## Next Steps

1. ✅ Implement FCM token storage endpoint in backend
2. ✅ Implement push notification sending in task creation endpoint
3. ✅ Test notifications in all scenarios (foreground, background, killed)
4. ✅ Handle token refresh on backend
5. ✅ Add error handling and retry logic
6. ✅ Monitor notification delivery rates

## Additional Resources

- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [FCM HTTP v1 API](https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages)
- [Android Notification Channels](https://developer.android.com/develop/ui/views/notifications/channels)

