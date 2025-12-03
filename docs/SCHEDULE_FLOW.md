# Schedule Flow Documentation

## Overview
This document provides a comprehensive explanation of how the schedule/task management system works in SendaSnap, including the integration with the chat system. This flow covers task creation, display, filtering, status updates, deletion, and chat functionality.

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Key Components](#key-components)
3. [Task Data Model](#task-data-model)
4. [Schedule Fragment Flow](#schedule-fragment-flow)
5. [Task Creation Flow](#task-creation-flow)
6. [Task Detail Flow](#task-detail-flow)
7. [Chat Integration with Schedules](#chat-integration-with-schedules)
8. [Task Status Management](#task-status-management)
9. [Task Filtering and Display](#task-filtering-and-display)
10. [Notifications](#notifications)
11. [API Integration](#api-integration)
12. [User Permissions](#user-permissions)

---

## Architecture Overview

The schedule system consists of:
- **ScheduleFragment**: Main fragment displaying tasks in a calendar view
- **AddScheduleActivity**: Activity for creating/editing tasks
- **ScheduleDetailActivity**: Activity for viewing task details
- **TaskAdapter**: RecyclerView adapter for displaying tasks with chat badges
- **TaskViewModel**: ViewModel handling API calls and data management
- **ChatService**: Service managing chat functionality tied to tasks
- **TaskNotificationHelper**: Utility for sending task assignment notifications

---

## Key Components

### 1. ScheduleFragment
**Location**: `fragments/ScheduleFragment.java`

**Purpose**: Main UI for displaying and managing tasks/schedules

**Key Features**:
- Calendar view for date selection
- Status filter chips (All, In Progress, Pending, Completed, Cancelled)
- Task list with RecyclerView
- FAB for adding new tasks (admin/manager only)
- Long-press on task for status update/delete dialog

**State Management**:
- `allTasks`: Complete list of tasks from API
- `filteredTasks`: Tasks filtered by date and status
- `selectedDate`: Currently selected date (yyyy-MM-dd format)
- `currentFilter`: Currently selected status filter (null = All)

### 2. TaskAdapter
**Location**: `adapters/TaskAdapter.java`

**Purpose**: Displays task items in RecyclerView with real-time chat unread badges

**Key Features**:
- Displays task title, description, time, status
- Shows assignee avatars (max 4, with overlap)
- Shows creator avatar and name
- Real-time unread message count badge per task
- Attachment indicator icon

**Chat Integration**:
- Each task item shows unread count badge
- Badge updates in real-time via Firebase listeners
- Chat ID format: `"task_" + taskId`
- Listener cleanup on view recycling

### 3. AddScheduleActivity
**Location**: `activities/schedule/AddScheduleActivity.java`

**Purpose**: Create or edit tasks

**Features**:
- Title, description, date, time inputs
- Status selection (Pending, Running, Completed, Cancelled)
- Priority selection (Low, Normal, High)
- Multiple assignee selection
- File attachments (images and documents)
- Edit mode with role-based restrictions

**User Permissions**:
- Admin/Manager: Can create tasks, full edit access
- Other users: Can only update task status in edit mode

### 4. ScheduleDetailActivity
**Location**: `activities/schedule/ScheduleDetailActivity.java`

**Purpose**: View task details and open chat

**Features**:
- Display all task information (read-only)
- Chat icon in toolbar with unread count badge
- Edit button (opens AddScheduleActivity in edit mode)
- Real-time unread count updates

**Chat Integration**:
- Chat icon with badge showing unread messages
- Opens ChatActivity when clicked
- Collects participants (creator + assignees + current user)
- Initializes all participants in Firebase before opening chat

### 5. TaskViewModel
**Location**: `viewmodel/TaskViewModel.java`

**Purpose**: Handles API calls for task operations

**Methods**:
- `listTasks(startDate, endDate, callback)`: Fetch tasks for date range
- `getTask(taskId, callback)`: Fetch single task
- `createTask(task, callback)`: Create new task
- `updateTask(task, callback)`: Update existing task
- `updateTaskStatus(taskId, status, callback)`: Update task status
- `deleteTask(taskId, callback)`: Delete task

### 6. TaskNotificationHelper
**Location**: `utils/TaskNotificationHelper.java`

**Purpose**: Sends local notifications to assignees when tasks are created

**Key Features**:
- Sends notifications to all assignees (except creator)
- Creates notification channel ("Task Assignments")
- Handles notification permissions and user preferences
- Notification opens task details when tapped

**Key Methods**:
- `sendTaskAssignmentNotifications(context, task, assignees)`: Send notifications to all assignees

**Note**: This implementation sends local notifications on the current device. For true push notifications across devices, the backend should send FCM (Firebase Cloud Messaging) notifications when a task is created.

---

## Task Data Model

### Task Class
**Location**: `models/Task.java`

**Key Fields**:
```java
- int id                          // Task ID
- String title                    // Task title
- String description              // Task description
- String workDate                 // Date in yyyy-MM-dd format (may include time)
- String workTime                 // Time in HH:mm format
- TaskStatus status               // Enum: RUNNING, PENDING, COMPLETED, CANCELLED
- TaskPriority priority           // Enum: LOW, NORMAL, HIGH
- List<UserData> assignees        // Multiple assignees
- UserData assignee               // Legacy single assignee (for backward compatibility)
- UserData creator                // Task creator
- int createdByUserId             // Creator user ID
- List<TaskAttachment> attachments // File attachments
- String dueDate                  // Due date
- String completedAt              // Completion timestamp
- String createdAt                // Creation timestamp
- String updatedAt                // Last update timestamp
```

**Status Enum**:
- `PENDING`: Task is pending/not started
- `RUNNING`: Task is in progress
- `COMPLETED`: Task is completed
- `CANCELLED`: Task is cancelled

**Priority Enum**:
- `LOW`: Low priority
- `NORMAL`: Normal priority
- `HIGH`: High priority

---

## Schedule Fragment Flow

### Initialization Flow
```
1. onCreateView()
   └─> Inflate FragmentScheduleBinding
   
2. onViewCreated()
   ├─> initializeDependencies()
   │   ├─> SharedPrefsManager
   │   ├─> TaskViewModel
   │   ├─> ApiService
   │   ├─> NetworkUtils
   │   └─> HapticFeedbackHelper
   │
   ├─> setupViews()
   │   ├─> setupRecyclerView()
   │   │   └─> Create TaskAdapter with filteredTasks
   │   ├─> setupCalendar()
   │   │   └─> Set date change listener
   │   ├─> setupFilterChips()
   │   │   └─> Set chip selection listener
   │   └─> setupFAB()
   │       └─> Show/hide based on user role
   │
   ├─> initializeSelectedDate()
   │   └─> Set to today's date
   │
   └─> loadTasksIfConnected()
       └─> Check network → loadTasksFromApi()
```

### Task Loading Flow
```
loadTasksIfConnected()
  ├─> Check network availability
  │   ├─> NO: Show no internet error, hide shimmer
  │   └─> YES: Continue
  │
  └─> loadTasksFromApi()
      ├─> Show shimmer loading
      ├─> taskViewModel.listTasks(selectedDate, selectedDate, callback)
      │   └─> API Call: GET /tasks?start_date={date}&end_date={date}
      │
      ├─> onSuccess(result)
      │   ├─> Clear allTasks
      │   ├─> Add result.getItems() to allTasks
      │   ├─> Hide shimmer
      │   └─> filterTasks()
      │
      └─> onError(message, errorCode)
          ├─> Hide shimmer
          ├─> Clear allTasks
          ├─> filterTasks()
          └─> Show error toast
```

### Filtering Flow
```
filterTasks()
  ├─> Clear filteredTasks
  │
  ├─> For each task in allTasks:
  │   ├─> Extract date from task.getWorkDate()
  │   │   └─> Handle ISO format (yyyy-MM-ddTHH:mm:ss) → extract first 10 chars
  │   │
  │   ├─> Check if task date matches selectedDate
  │   │
  │   └─> If matches:
  │       ├─> Check if currentFilter is null (All) OR task status matches currentFilter
  │       └─> If matches: Add to filteredTasks
  │
  ├─> taskAdapter.notifyDataSetChanged()
  └─> updateEmptyState()
      └─> Show/hide empty state view
```

### Date Selection Flow
```
User selects date in calendar
  └─> CalendarView.OnDateChangeListener
      ├─> Create Calendar with selected date
      ├─> Format to yyyy-MM-dd → selectedDate
      ├─> updateSelectedDateText()
      │   └─> Format to "EEEE, MMMM dd, yyyy" (e.g., "Monday, January 15, 2024")
      └─> loadTasksIfConnected()
```

### Status Filter Flow
```
User selects status chip
  └─> ChipGroup.OnCheckedStateChangeListener
      ├─> Prevent deselection (always keep one chip selected)
      ├─> Map chip text to TaskStatus enum:
      │   ├─> "All" → null
      │   ├─> "In Progress" → RUNNING
      │   ├─> "Pending" → PENDING
      │   ├─> "Completed" → COMPLETED
      │   └─> "Cancelled" → CANCELLED
      │
      ├─> Update currentFilter
      ├─> filterTasks()
      └─> loadTasksIfConnected()
```

### Task Click Flow
```
User clicks task item
  └─> TaskAdapter.OnTaskClickListener.onTaskClick()
      ├─> Haptic feedback
      └─> openTaskDetails(task)
          └─> Start ScheduleDetailActivity with task as Intent extra
```

### Task Long-Press Flow
```
User long-presses task item
  └─> TaskAdapter.OnTaskLongClickListener.onTaskLongClick()
      ├─> Haptic feedback
      └─> showTaskStatusDialog(task)
          ├─> Inflate DialogTaskStatusBinding
          ├─> Show dialog with buttons:
          │   ├─> Pending
          │   ├─> Running
          │   ├─> Completed
          │   ├─> Cancelled
          │   ├─> Delete
          │   └─> Close
          │
          └─> User selects action
              ├─> Status button → updateTaskStatusIfConnected()
              └─> Delete button → showDeleteConfirmationDialog()
```

### Status Update Flow
```
updateTaskStatusIfConnected(task, status, dialog)
  ├─> Check network availability
  │   ├─> NO: Show no internet error
  │   └─> YES: Continue
  │
  └─> updateTaskStatus(task, status, dialog)
      ├─> Show loading dialog
      ├─> Create StatusUpdateRequest(status)
      ├─> apiService.updateTaskStatus(taskId, request)
      │   └─> API Call: PATCH /tasks/{taskId}/status
      │
      ├─> onResponse()
      │   ├─> Parse TaskResponseDto
      │   ├─> Convert to Task domain model
      │   ├─> updateTaskInList(updatedTask)
      │   │   └─> Replace task in allTasks list
      │   ├─> filterTasks() (refreshes display)
      │   ├─> Dismiss dialog
      │   └─> Show success toast
      │
      └─> onFailure()
          └─> Show error toast
```

### Task Deletion Flow
```
deleteTaskIfConnected(task)
  ├─> Check network availability
  │   ├─> NO: Show no internet error
  │   └─> YES: Continue
  │
  └─> deleteTask(task)
      ├─> Show loading dialog
      ├─> apiService.deleteTask(taskId)
      │   └─> API Call: DELETE /tasks/{taskId}
      │
      ├─> onResponse()
      │   ├─> Remove task from allTasks list
      │   ├─> filterTasks() (refreshes display)
      │   └─> Show success toast
      │
      └─> onFailure()
          └─> Show error toast
```

---

## Task Creation Flow

### Opening Add Task
```
User clicks FAB in ScheduleFragment
  └─> openAddTaskActivity()
      └─> Start AddScheduleActivity with request code ADD_TASK_REQUEST_CODE
```

### AddScheduleActivity Flow
```
onCreate()
  ├─> initHelpers()
  │   ├─> HapticFeedbackHelper
  │   ├─> SharedPrefsManager
  │   ├─> TaskViewModel
  │   └─> UserViewModel
  │
  ├─> checkEditMode()
  │   └─> Check if task extra exists → set isEditMode
  │
  ├─> checkUserRole()
  │   └─> If edit mode and not admin/manager → restrict edit access
  │
  ├─> setupRecyclerView()
  │   └─> Setup attachment RecyclerView
  │
  ├─> setupListeners()
  │   ├─> Date picker
  │   ├─> Time picker
  │   ├─> Status chips
  │   ├─> Priority chips
  │   ├─> Assignee selection
  │   ├─> Attachment selection
  │   └─> Save button
  │
  └─> setupInitialValues()
      └─> If edit mode: Populate fields from task
```

### Save Task Flow
```
User clicks Save button
  └─> saveTask()
      ├─> Validate inputs
      │   ├─> Title required
      │   ├─> Date required
      │   └─> Time required
      │
      ├─> Create Task object
      │   ├─> Set title, description
      │   ├─> Set workDate (yyyy-MM-dd format)
      │   ├─> Set workTime (HH:mm format)
      │   ├─> Set status
      │   ├─> Set priority
      │   ├─> Set assignees
      │   └─> Set attachments
      │
      ├─> If edit mode:
      │   └─> taskViewModel.updateTask(task, callback)
      │       └─> API Call: PUT /tasks/{taskId}
      │
      └─> If create mode:
          └─> taskViewModel.createTask(task, callback)
              └─> API Call: POST /tasks
      
      ├─> onSuccess(createdTask)
      │   ├─> AlarmHelper.setTaskAlarm() (set reminder)
      │   │
      │   ├─> FcmNotificationSender.sendTaskAssignmentNotifications()
      │   │   ├─> Verify assignees have emails (use selectedAssignees if API response lacks emails)
      │   │   ├─> For each assignee (except creator):
      │   │   │   ├─> Get assignee's Firebase ID from email
      │   │   │   ├─> Write notification data to Firebase Realtime Database
      │   │   │   ├─> Path: task_notifications/{assigneeFirebaseId}/{notificationId}
      │   │   │   ├─> Data includes: type, task_id, task_title, task_description, creator_name, created_at, read
      │   │   │   └─> Notification will be picked up by assignee's device listener
      │   │   └─> Note: Notifications appear on assignee devices, NOT on creator's device
      │   │
      │   ├─> CookieBarToastHelper.showSuccess()
      │   │   ├─> Title: "Task Created"
      │   │   ├─> Message: "Task \"[Task Title]\" has been created and assigned"
      │   │   ├─> Duration: SHORT_DURATION (2 seconds)
      │   │   └─> Position: Top of screen (only on creator's device)
      │   │
      │   ├─> SoundHelper.playNotificationSound()
      │   │   ├─> Play default notification sound (ting sound)
      │   │   └─> Respects user notification preferences (only on creator's device)
      │   │
      │   ├─> Set result with task
      │   └─> Finish activity
      │
      └─> onError()
          └─> Show error toast
```

### Task Saved Callback
```
AddScheduleActivity finishes with result
  └─> ScheduleFragment.onActivityResult()
      ├─> Check request code == ADD_TASK_REQUEST_CODE
      ├─> Check result code == RESULT_OK
      ├─> Extract task from Intent
      └─> onTaskSaved(task)
          ├─> loadTasksIfConnected() (refresh list)
          └─> Show success toast
```

---

## Task Detail Flow

### Opening Task Details
```
User clicks task in ScheduleFragment
  └─> openTaskDetails(task)
      └─> Start ScheduleDetailActivity with task as Intent extra
```

### ScheduleDetailActivity Flow
```
onCreate()
  ├─> initHelpers()
  │   ├─> HapticFeedbackHelper
  │   ├─> ChatService
  │   ├─> ApiManager
  │   └─> TaskViewModel
  │
  ├─> loadTask()
  │   ├─> Extract task from Intent
  │   └─> If not found: Load from API using taskId
  │
  ├─> setupToolbar()
  │   └─> Setup navigation and menu
  │
  ├─> populateFields()
  │   ├─> Display title, description
  │   ├─> Display date, time
  │   ├─> Display assignees
  │   ├─> Display status chip
  │   ├─> Display priority chip
  │   └─> Display attachments
  │
  ├─> makeFieldsReadOnly()
  │   └─> Disable chip interactions
  │
  ├─> fetchUsers()
  │   └─> Load all users for participant collection
  │
  └─> loadUnreadCount() (after 500ms delay)
      └─> Setup Firebase listener for unread count
```

### Chat Icon Setup
```
setupChatIcon()
  ├─> Inflate menu_chat_badge layout
  ├─> Set as action view for chat menu item
  ├─> Get badge TextView
  ├─> Set click listener
  └─> loadUnreadCount() will update badge
```

### Unread Count Flow
```
loadUnreadCount()
  ├─> Get chatId: "task_" + taskId
  ├─> Get currentUserId from FirebaseUtils
  │
  ├─> Remove previous listener (if exists)
  │
  ├─> Get initial count
  │   └─> chatService.getGroupChatUnreadCount(chatId, userId, callback)
  │       └─> Firebase: userChats/{userId}/{chatId}/unreadCount
  │
  └─> Add real-time listener
      └─> chatService.addUnreadCountListener(chatId, userId, callback)
          └─> Updates badge when unread count changes
```

### Badge Update Flow
```
updateBadge(unreadCount)
  ├─> If unreadCount > 0:
  │   ├─> Set badge text (max "99+")
  │   └─> Show badge
  │
  └─> If unreadCount == 0:
      └─> Hide badge
```

---

## Chat Integration with Schedules

### Chat ID Format
- **Format**: `"task_" + taskId`
- **Example**: Task ID 123 → Chat ID: `"task_123"`

### Participant Collection
When opening chat from a task, participants are collected as follows:

```
getTaskParticipantsAsUserData()
  ├─> Create empty participants list
  ├─> Create Set to track added emails (prevent duplicates)
  │
  ├─> Add creator
  │   ├─> If task.getCreator() exists → add creator
  │   └─> Else: Find creator from allUsers by createdByUserId
  │
  ├─> Add assignees
  │   ├─> If task.getAssignees() exists → add all assignees
  │   └─> Else if task.getAssignee() exists → add assignee
  │
  ├─> Add current user
  │   └─> Get from SharedPrefsManager
  │
  └─> Return participants list (no duplicates)
```

### Opening Chat from Task
```
User clicks chat icon in ScheduleDetailActivity
  └─> openTaskChat()
      ├─> Check if already opening (prevent double-click)
      ├─> Validate task exists
      ├─> Get participants: getTaskParticipantsAsUserData()
      │
      ├─> Set isChatOpening = true
      ├─> Disable chat button
      │
      └─> chatService.createOrGetGroupChatWithParticipants()
          ├─> For each participant:
          │   └─> Initialize user in Firebase (users/{userId})
          │
          ├─> Check if chat exists (chats/{chatId})
          │   ├─> NO: Create new chat
          │   │   ├─> Create chat metadata
          │   │   ├─> Create userChats entries for all participants
          │   │   └─> Store mapping in taskChats/{taskId}
          │   │
          │   └─> YES: Ensure userChats entries exist
          │       └─> Add missing participants
          │
          ├─> onSuccess(chat)
          │   ├─> Set isChatOpening = false
          │   ├─> Enable chat button
          │   └─> Start ChatActivity with:
          │       ├─> chatId
          │       ├─> isGroupChat = true
          │       ├─> taskId
          │       ├─> taskTitle
          │       └─> participants (as Serializable)
          │
          └─> onFailure(exception)
              ├─> Set isChatOpening = false
              ├─> Enable chat button
              └─> Show error toast
```

### Unread Count in Task List
Each task item in ScheduleFragment shows an unread count badge:

```
TaskAdapter.bind(task)
  └─> loadUnreadCount(task)
      ├─> Get chatId: "task_" + taskId
      ├─> Get currentUserId
      │
      ├─> Remove previous listener (if exists)
      │
      ├─> Get initial count
      │   └─> chatService.getGroupChatUnreadCount(chatId, userId, callback)
      │
      └─> Add real-time listener
          └─> chatService.addUnreadCountListener(chatId, userId, callback)
              └─> Updates badge when unread count changes
```

### Listener Cleanup
```
TaskAdapter.onViewRecycled(holder)
  └─> holder.removeUnreadCountListener()
      └─> chatService.removeUnreadCountListener(chatId, userId, listener)
```

---

## Task Status Management

### Status Values
- `PENDING`: Task is pending/not started
- `RUNNING`: Task is in progress
- `COMPLETED`: Task is completed
- `CANCELLED`: Task is cancelled

### Status Update Methods

#### From ScheduleFragment (Long-press)
```
User long-presses task
  └─> showTaskStatusDialog(task)
      └─> User selects status
          └─> updateTaskStatusIfConnected(task, status, dialog)
              └─> API: PATCH /tasks/{taskId}/status
```

#### From ScheduleDetailActivity (Menu)
```
User clicks edit menu item
  └─> openEditMode()
      └─> Start AddScheduleActivity in edit mode
          └─> User can change status in edit form
```

### Status Display
- **ScheduleFragment**: Status shown as colored chip on each task item
- **ScheduleDetailActivity**: Status shown as selected chip (read-only)
- **TaskAdapter**: Status shown with color-coded indicator and chip

### Status Colors
- **RUNNING**: Primary color (blue)
- **PENDING**: Warning color (yellow/orange)
- **COMPLETED**: Success color (green)
- **CANCELLED**: Error color (red)

---

## Task Filtering and Display

### Date Filtering
- Tasks are filtered by `workDate` matching `selectedDate`
- Date format: `yyyy-MM-dd`
- Handles ISO format dates (extracts first 10 characters)

### Status Filtering
- Filter chips: All, In Progress, Pending, Completed, Cancelled
- "All" shows all statuses
- Other chips filter by specific status
- Always one chip selected (deselection prevented)

### Display Order
- Tasks displayed in order received from API
- No explicit sorting applied

### Empty State
- Shows empty state view when `filteredTasks` is empty
- Hides RecyclerView when empty

---

## Notifications

### Task Creation Feedback

When a task is created successfully, the user receives immediate visual and audio feedback:

#### CookieBar Notification
- **Title**: "Task Created"
- **Message**: "Task \"[Task Title]\" has been created and assigned"
- **Type**: Success (green background with check icon)
- **Duration**: 2 seconds (SHORT_DURATION)
- **Position**: Top of screen
- **Features**: Auto-dismiss, swipe to dismiss

#### Sound Feedback
- **Sound**: Default notification sound (ting sound)
- **Implementation**: Uses RingtoneManager to play system default notification sound
- **Respects Settings**: Only plays if notifications are enabled in user preferences
- **Volume**: Respects device notification volume settings

### Task Assignment Notifications

When a task is created, all assignees receive a notification about the new task assignment.

#### Notification Flow (Android-to-Android via Firebase Realtime Database)

**On Creator's Device:**
```
Task Created Successfully
  └─> FcmNotificationSender.sendTaskAssignmentNotifications()
      ├─> Get assignees from createdTask.getAssignees() (verify they have emails)
      │   └─> Fallback to selectedAssignees if API response lacks emails
      │
      ├─> For each assignee (skip creator):
      │   ├─> Get assignee's Firebase ID: FirebaseUtils.sanitizeEmailForKey(assignee.getEmail())
      │   ├─> Write notification data to Firebase:
      │   │   ├─> Path: task_notifications/{assigneeFirebaseId}/{notificationId}
      │   │   ├─> Data:
      │   │   │   ├─> type: "task_assignment"
      │   │   │   ├─> task_id: String.valueOf(task.getId())
      │   │   │   ├─> task_title: task.getTitle()
      │   │   │   ├─> task_description: task.getDescription()
      │   │   │   ├─> creator_name: currentUser.getName()
      │   │   │   ├─> created_at: System.currentTimeMillis()
      │   │   │   └─> read: false
      │   │   └─> Firebase write operation
      │   │
      └─> CookieBarToastHelper.showSuccess() + SoundHelper.playNotificationSound()
      │   └─> Only shown on creator's device as feedback
```

**On Assignee's Device:**
```
MyApplication.onCreate() or Activity.onResume()
  └─> FcmNotificationSender.setupNotificationListener()
      ├─> Get current user's Firebase ID from email
      ├─> Setup ValueEventListener on: task_notifications/{userId}/
      │
      └─> Listener.onDataChange()
          ├─> For each notification in snapshot:
          │   ├─> Check if unread (read == false)
          │   ├─> Check if created within last 10 minutes (prevent old notifications)
          │   ├─> If valid:
          │   │   ├─> Mark as read in Firebase immediately (prevent duplicates when navigating)
          │   │   ├─> Check if app is in foreground
          │   │   │
          │   │   ├─> If foreground:
          │   │   │   ├─> CookieBarToastHelper.showInfo()
          │   │   │   │   ├─> Title: "New Task Assigned" (white text)
          │   │   │   │   ├─> Message: "[Creator Name] assigned you: [Task Title]" (white text)
          │   │   │   │   ├─> Icon: White colored icon
          │   │   │   │   └─> Duration: LONG_DURATION
          │   │   │   └─> SoundHelper.playNotificationSound()
          │   │   │
          │   │   └─> If background:
          │   │       └─> TaskNotificationHelper.sendTaskAssignmentNotificationFromFirebase()
          │   │           ├─> Create notification channel (if needed)
          │   │           ├─> Check notification permissions
          │   │           ├─> Build system notification:
          │   │           │   ├─> Title: "New Task Assigned: [Task Title]"
          │   │           │   ├─> Text: "[Creator Name] assigned you a task: [Description]"
          │   │           │   ├─> Priority: HIGH
          │   │           │   ├─> Intent: Opens ScheduleDetailActivity with taskId extra
          │   │           │   │   └─> ScheduleDetailActivity loads full task from API using taskId
          │   │           │   ├─> Intent Flags: FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP
          │   │           │   └─> Show system notification in notification tray
          │   │           └─> When user clicks notification: Opens task detail activity
```

#### Notification Features

**Creator's Device:**
- CookieBar notification: "Task Created" with success message
- Sound: Ting sound (respects notification preferences)
- **Note**: Creator does NOT receive task assignment notification

**Assignee's Device:**
- **Foreground**: CookieBar notification + sound
- **Background**: System notification with:
  - Title: "New Task Assigned: [Task Title]"
  - Content: "[Creator Name] assigned you a task: [Task Description]"
  - Priority: High (with sound, vibration, and lights)
  - Action: Tapping notification opens ScheduleDetailActivity
    - Intent contains `taskId` extra
    - ScheduleDetailActivity loads full task from API using taskId
    - Displays complete task details to the user
  - Auto-cancel: Notification dismisses when tapped

#### Notification Channel
- **Channel ID**: `task_assignment_channel`
- **Channel Name**: "Task Assignments"
- **Importance**: High
- **Features**: Vibration and lights enabled

#### Permissions and Preferences
- **MainActivity**: Automatically requests POST_NOTIFICATIONS permission (Android 13+)
- Checks if notifications are enabled in system settings (Android 11+)
- Respects user notification preferences from SharedPrefsManager
- Shows rationale dialog if permission was denied before

#### Firebase Realtime Database Structure
```
task_notifications/
  └─> {assigneeFirebaseId}/  (sanitized email)
      └─> {notificationId}/
          ├─> type: "task_assignment"
          ├─> task_id: "123"
          ├─> task_title: "Task Title"
          ├─> task_description: "Task Description"
          ├─> creator_name: "Creator Name"
          ├─> created_at: 1234567890
          └─> read: false/true
```

#### Key Implementation Details
- **FcmNotificationSender**: `utils/FcmNotificationSender.java`
  - `sendTaskAssignmentNotifications()`: Writes notification data to Firebase for assignees
  - `setupNotificationListener()`: Sets up global Firebase listener in MyApplication
  - `removeNotificationListener()`: Cleans up listener on logout
  - `handleNotification()`: Processes incoming notifications and shows appropriate UI (CookieBar or system notification)
    - **Important**: Marks notification as read in Firebase BEFORE showing UI to prevent duplicate notifications when navigating between activities
  - `showForegroundNotification()`: Shows CookieBar notification when app is in foreground
  - `showSystemNotification()`: Shows system notification when app is in background
- **TaskNotificationHelper**: `utils/TaskNotificationHelper.java`
  - `sendTaskAssignmentNotificationFromFirebase()`: Creates and displays system notification
    - Creates Intent with `taskId` extra (not full Task object)
    - Uses `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP` for proper navigation
    - ScheduleDetailActivity receives taskId and loads full task from API
- **MyApplication**: Sets up global notification listener that works across all activities
- **Listener Lifecycle**: 
  - Setup: When app starts (if user logged in) and when any activity resumes
  - Cleanup: When user logs out
- **Duplicate Prevention**: 
  - Notifications marked as read in Firebase **BEFORE** showing UI (prevents duplicates when navigating between activities)
  - Only processes notifications created in last 10 minutes (NOTIFICATION_TIME_LIMIT_MS = 600000ms)
  - Listener prevents duplicates by checking read status before processing
- **Token Storage**: FCM tokens stored in Firebase Realtime Database (`fcm_tokens/{userId}`)
- **Works Offline**: Firebase Realtime Database handles offline sync automatically
- **Notification Click Behavior**:
  - When user clicks system notification (app in background):
    1. Intent opens ScheduleDetailActivity with `taskId` extra
    2. ScheduleDetailActivity.loadTask() receives taskId
    3. If task object not in intent, loads full task from API via TaskViewModel.getTask()
    4. Displays complete task details to user

#### Firebase Database Structure
```
Firebase Realtime Database
├── fcm_tokens/
│   └── {userId}/
│       ├── fcm_token
│       ├── updated_at
│       ├── user_id
│       └── user_email
│
└── task_notifications/
    └── {assigneeFirebaseId}/
        └── {notificationId}/
            ├── type: "task_assignment"
            ├── task_id
            ├── task_title
            ├── task_description
            ├── creator_name
            ├── created_at
            └── read: false
```

#### Implementation Details
- **FcmNotificationSender**: `utils/FcmNotificationSender.java`
  - `storeFcmTokenInFirebase()`: Stores FCM token in Firebase
  - `sendTaskAssignmentNotifications()`: Writes notification data to Firebase
  - `setupNotificationListener()`: Sets up Firebase listener for incoming notifications
- **Token Storage**: FCM tokens stored in Firebase Realtime Database, not backend
- **Notification Listener**: Setup in `MainActivity.onCreate()` to listen for new assignments
- **Works Offline**: Firebase Realtime Database handles offline sync automatically

#### Implementation Details
- **Location**: `utils/TaskNotificationHelper.java`
- **Method**: `sendTaskAssignmentNotifications(context, task, assignees)`
- **Called From**: `AddScheduleActivity.saveTask()` after successful task creation
- **Notification ID**: `2000 + (taskId + assigneeId) % 1000` (unique per task-assignee pair)

### Android-to-Android Notification Implementation
- **FcmNotificationSender Location**: `utils/FcmNotificationSender.java`
- **Main Methods**:
  - `storeFcmTokenInFirebase()`: Stores FCM token in Firebase Realtime Database
  - `sendTaskAssignmentNotifications()`: Writes notification data to Firebase for assignees
  - `setupNotificationListener()`: Sets up Firebase listener for incoming notifications
- **Called From**: 
  - Token storage: `FcmTokenManager.registerToken()` after login
  - Notification sending: `AddScheduleActivity.saveTask()` after task creation
  - Listener setup: `MainActivity.onCreate()` when app starts
- **Firebase Paths**:
  - FCM Tokens: `fcm_tokens/{userId}/`
  - Notifications: `task_notifications/{assigneeFirebaseId}/{notificationId}/`
- **See Also**: `docs/ANDROID_TO_ANDROID_NOTIFICATIONS.md` for detailed documentation

### CookieBar and Sound Feedback Implementation
- **CookieBar Location**: `utils/CookieBarToastHelper.java`
- **Sound Helper Location**: `utils/SoundHelper.java`
- **CookieBar Method**: `showSuccess(context, title, message, duration)`
- **Sound Method**: `playNotificationSound(context)`
- **Called From**: `AddScheduleActivity.saveTask()` after successful task creation and notification sending
- **CookieBar Styling**:
  - All icons are white colored (via drawable tint: `@android:color/white`)
  - Title and message text are white (`Color.WHITE`)
  - Icons used: `ic_check_circle` (success), `ic_error` (error), `ic_info` (info), `ic_warning` (warning), `ic_wifi_off` (no internet)

---

## API Integration

### Endpoints Used

#### List Tasks
```
GET /tasks?start_date={date}&end_date={date}
Headers: Authorization: Bearer {token}
Response: PagedResult<Task>
```

#### Get Task
```
GET /tasks/{taskId}
Headers: Authorization: Bearer {token}
Response: TaskResponseDto
```

#### Create Task
```
POST /tasks
Headers: Authorization: Bearer {token}
Body: TaskCreateRequest
Response: TaskResponseDto
```

#### Update Task
```
PUT /tasks/{taskId}
Headers: Authorization: Bearer {token}
Body: TaskUpdateRequest
Response: TaskResponseDto
```

#### Update Task Status
```
PATCH /tasks/{taskId}/status
Headers: Authorization: Bearer {token}
Body: StatusUpdateRequest { status: "pending" | "running" | "completed" | "cancelled" }
Response: TaskResponseDto
```

#### Delete Task
```
DELETE /tasks/{taskId}
Headers: Authorization: Bearer {token}
Response: ApiResponse<Object>
```

### Error Handling
- **401 Unauthorized**: Authentication required
- **403 Forbidden**: No permission
- **404 Not Found**: Resource not found
- **422 Unprocessable Entity**: Validation error
- **Network Error**: Show network error message

---

## User Permissions

### Task Creation
- **Admin**: Can create tasks
- **Manager**: Can create tasks
- **Other roles**: Cannot create tasks (FAB hidden)

### Task Editing
- **Admin/Manager**: Full edit access (all fields)
- **Other roles**: Restricted edit (can only update status)

### Task Deletion
- Only available via long-press dialog in ScheduleFragment
- No explicit role check (assumed admin/manager only)

### Status Updates
- All users can update task status (via long-press or edit mode)

---

## Data Flow Summary

### Task Creation Flow
```
Creator's Device:
  User → FAB Click → AddScheduleActivity → Fill Form → Save
    → TaskViewModel.createTask() → API POST /tasks
    → Success → FcmNotificationSender.sendTaskAssignmentNotifications()
    │   ├─> Verify assignees have emails
    │   ├─> For each assignee (skip creator):
    │   │   └─> Write notification to Firebase: task_notifications/{assigneeFirebaseId}/{notificationId}
    │   │
    → CookieBarToastHelper.showSuccess()
    │   └─> Show "Task Created" message at top of screen (creator feedback only)
    │
    → SoundHelper.playNotificationSound()
    │   └─> Play ting sound (creator feedback only)
    │
    → Set Task Alarm → Return to ScheduleFragment → Refresh List

Assignee's Device:
  → MyApplication: Global listener active on task_notifications/{assigneeFirebaseId}/
  │
  → Firebase Listener detects new notification
  │   ├─> Check if unread and created within last 10 minutes
  │   ├─> Mark as read in Firebase immediately (prevents duplicates when navigating)
  │   │
  │   ├─> If app in foreground:
  │   │   ├─> CookieBarToastHelper.showInfo()
  │   │   │   ├─> Title: "New Task Assigned" (white text)
  │   │   │   ├─> Message: "[Creator Name] assigned you: [Task Title]" (white text)
  │   │   │   └─> Icon: White colored icon
  │   │   └─> SoundHelper.playNotificationSound()
  │   │
  │   └─> If app in background:
  │       └─> TaskNotificationHelper.sendTaskAssignmentNotificationFromFirebase()
  │           ├─> Create system notification with task details
  │           ├─> Intent: Opens ScheduleDetailActivity with taskId extra
  │           │   └─> ScheduleDetailActivity.loadTask() receives taskId
  │           │       └─> Loads full task from API via TaskViewModel.getTask()
  │           └─> When user clicks notification: Opens task detail activity showing full task information
```

### Task View Flow
```
User → Task Click → ScheduleDetailActivity → Display Task
  → Load Unread Count → Show Badge → User Clicks Chat
  → Collect Participants → Create/Get Chat → Open ChatActivity
```

### Task Update Flow
```
User → Long-press Task → Status Dialog → Select Status
  → API PATCH /tasks/{id}/status → Update Local List → Refresh Display
```

### Chat Integration Flow
```
Task Created → Chat ID: "task_" + taskId
  → User Opens Chat → Collect Participants (creator + assignees + current user)
  → Initialize All Participants in Firebase
  → Create/Get Group Chat → Open ChatActivity
  → Messages Sent → Update Unread Counts → Badge Updates in Real-time
```

---

## Key Files Reference

- **ScheduleFragment**: `app/src/main/java/com/sendajapan/sendasnap/fragments/ScheduleFragment.java`
- **TaskAdapter**: `app/src/main/java/com/sendajapan/sendasnap/adapters/TaskAdapter.java`
- **AddScheduleActivity**: `app/src/main/java/com/sendajapan/sendasnap/activities/schedule/AddScheduleActivity.java`
- **ScheduleDetailActivity**: `app/src/main/java/com/sendajapan/sendasnap/activities/schedule/ScheduleDetailActivity.java`
- **NotificationsActivity**: `app/src/main/java/com/sendajapan/sendasnap/activities/NotificationsActivity.java`
- **NotificationAdapter**: `app/src/main/java/com/sendajapan/sendasnap/adapters/NotificationAdapter.java`
- **NotificationHelper**: `app/src/main/java/com/sendajapan/sendasnap/utils/NotificationHelper.java`
- **Task Model**: `app/src/main/java/com/sendajapan/sendasnap/models/Task.java`
- **TaskViewModel**: `app/src/main/java/com/sendajapan/sendasnap/viewmodel/TaskViewModel.java`
- **ChatService**: `app/src/main/java/com/sendajapan/sendasnap/services/ChatService.java`
- **TaskNotificationHelper**: `app/src/main/java/com/sendajapan/sendasnap/utils/TaskNotificationHelper.java`
- **FcmNotificationSender**: `app/src/main/java/com/sendajapan/sendasnap/utils/FcmNotificationSender.java`
- **FcmTokenManager**: `app/src/main/java/com/sendajapan/sendasnap/utils/FcmTokenManager.java`
- **FcmMessagingService**: `app/src/main/java/com/sendajapan/sendasnap/services/FcmMessagingService.java` (for future backend integration)
- **SoundHelper**: `app/src/main/java/com/sendajapan/sendasnap/utils/SoundHelper.java`

---

## Notes

1. **Date Format**: All dates are stored and transmitted as `yyyy-MM-dd` format
2. **Time Format**: All times are stored and transmitted as `HH:mm` format (24-hour)
3. **Chat ID**: Always follows pattern `"task_" + taskId` for group chats
4. **Unread Count**: Real-time updates via Firebase listeners, cleaned up on view recycling
5. **Network Checks**: All API operations check network availability first
6. **Error Handling**: User-friendly error messages based on HTTP status codes
7. **Loading States**: Shimmer effect for task list, loading dialog for status updates/deletions
8. **Notifications**: Android-to-Android push notifications via Firebase Realtime Database. Notifications appear on assignee devices (not creator's device) when tasks are created. Global listener in MyApplication ensures notifications work across all activities. Users can view all notifications in `NotificationsActivity` with a badge count indicator on the toolbar. Notifications are displayed in a list with unread indicators and can be clicked to open the associated task detail.

---

## Code Quality Guidelines

### Clean Code Principles

When working on this codebase, please follow these clean code principles:

#### 1. **Naming Conventions**
- Use descriptive, meaningful names for classes, methods, and variables
- Follow Java naming conventions:
  - Classes: `PascalCase` (e.g., `TaskNotificationHelper`)
  - Methods: `camelCase` (e.g., `sendTaskAssignmentNotifications()`)
  - Constants: `UPPER_SNAKE_CASE` (e.g., `TASK_NOTIFICATIONS_PATH`)
  - Variables: `camelCase` (e.g., `taskAssignees`)

#### 2. **Method Responsibilities**
- Each method should do ONE thing and do it well
- Keep methods short and focused (ideally < 50 lines)
- Extract complex logic into separate methods
- Use descriptive method names that explain what they do

#### 3. **Code Organization**
- Group related functionality together
- Use appropriate access modifiers (`private`, `protected`, `public`)
- Keep class responsibilities clear and focused
- Avoid god classes (classes that do too much)

#### 4. **Error Handling**
- Always handle exceptions appropriately
- Use try-catch blocks for operations that can fail
- Log errors with meaningful messages
- Provide user-friendly error messages when appropriate

#### 5. **Comments and Documentation**
- Write self-documenting code (code that explains itself)
- Add comments only when code cannot be self-explanatory
- Use JavaDoc for public APIs
- Keep comments up-to-date with code changes

#### 6. **Code Formatting**
- Use consistent indentation (4 spaces for Java)
- Follow Android Studio's default formatting
- Use blank lines to separate logical sections
- Keep line length reasonable (< 120 characters)

### Import Optimization

#### 1. **Organize Imports**
- Always use Android Studio's "Optimize Imports" feature (Ctrl+Alt+O / Cmd+Option+O)
- Remove unused imports
- Organize imports in this order:
  1. Android framework imports
  2. Third-party library imports
  3. Project-specific imports
  4. Static imports (if any)

#### 2. **Import Best Practices**
- Use explicit imports (avoid wildcard imports like `import java.util.*`)
- Import only what you need
- Use static imports sparingly and only for constants or utility methods
- Group imports with blank lines between groups

#### 3. **Before Committing Code**
Always run these checks:
1. **Optimize Imports**: `Code → Optimize Imports` (Ctrl+Alt+O)
2. **Reformat Code**: `Code → Reformat Code` (Ctrl+Alt+L)
3. **Check for unused imports**: Android Studio will show warnings
4. **Remove unused variables/methods**: Clean up dead code

#### 4. **Example of Good Import Organization**
```java
// Android framework imports
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

// Third-party library imports
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

// Project-specific imports
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;
```

#### 5. **Import Optimization Checklist**
- [ ] Run "Optimize Imports" before committing
- [ ] Remove all unused imports
- [ ] Group imports logically (Android → Libraries → Project)
- [ ] Use explicit imports (no wildcards)
- [ ] Check for duplicate imports
- [ ] Verify no circular dependencies

### Code Review Guidelines

When reviewing code, check for:
1. **Clean Code**: Methods are focused, names are descriptive
2. **Import Optimization**: No unused imports, properly organized
3. **Error Handling**: Exceptions are handled appropriately
4. **Logging**: Appropriate use of Log.d(), Log.e(), etc.
5. **Performance**: No unnecessary operations or memory leaks
6. **Documentation**: Complex logic is documented
7. **Testing**: Code is testable and tested (if applicable)

---

*Last Updated: [Current Date]*
*This document should be updated whenever the schedule flow is modified.*

