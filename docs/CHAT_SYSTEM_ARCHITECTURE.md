# Chat System Architecture Documentation

## Overview
This document explains how the group chat feature works in the SendaSnap application. The chat system is built on Firebase Realtime Database and supports group chats tied to tasks.

## Firebase Database Structure

### Main Paths:
```
Firebase Realtime Database
├── users/
│   └── {userId}/                    # User profile data
│       ├── userId
│       ├── email
│       ├── name
│       ├── lastSeen
│       └── ... (all UserData fields)
│
├── chats/
│   └── {chatId}/                    # Group chat metadata
│       ├── taskId
│       ├── taskTitle
│       ├── isGroupChat: true
│       ├── participants/
│       │   └── {userId}: true
│       └── messages/
│           └── {messageId}/
│               ├── messageId
│               ├── senderId
│               ├── message
│               ├── messageType
│               ├── timestamp
│               └── seen
│
├── userChats/
│   └── {userId}/                    # User's chat list
│       └── {chatId}/
│           ├── chatId
│           ├── taskId
│           ├── taskTitle
│           ├── isGroupChat: true
│           ├── lastMessage
│           ├── lastMessageTime
│           └── unreadCount          # Real-time unread message count
│
└── taskChats/
    └── {taskId}/                    # Task to chat mapping
        ├── chatId
        ├── taskTitle
        └── createdAt
```

## Chat ID Format
- Group chats: `"task_" + taskId`
- Example: Task ID 123 → Chat ID: `"task_123"`

## User ID Format
- User IDs are sanitized email addresses
- Email: `user@example.com` → User ID: `user_example_com`
- Sanitization handled by `FirebaseUtils.sanitizeEmailForKey()`

## Key Components

### 1. ChatService (Singleton)
**Location:** `services/ChatService.java`

Main service class for all chat operations. Handles:
- User initialization in Firebase
- Group chat creation and management
- Message sending (text, image, file)
- Unread count tracking
- UserChats entry management

**Key Methods:**
- `initializeUserData(UserData)` - Initialize user in Firebase `users` table
- `createOrGetGroupChatWithParticipants()` - Create or get group chat, initialize all participants
- `sendGroupMessage()` - Send message to group chat
- `updateGroupChatUserChats()` - Update unread counts for all participants
- `addUnreadCountListener()` - Add real-time listener for unread count changes
- `markGroupChatAsSeen()` - Reset unread count when user views chat

### 2. ChatActivity
**Location:** `activities/ChatActivity.java`

UI activity for displaying and interacting with chat messages.

**Responsibilities:**
- Display messages in RecyclerView
- Send new messages
- Handle image/file uploads
- Mark messages as seen when chat is opened
- Initialize current user in Firebase

**Lifecycle:**
- `onCreate()`: Initialize user, load messages, set up listeners
- `onResume()`: Mark messages as seen, refresh unread count
- `onDestroy()`: Clean up listeners

### 3. ScheduleDetailActivity
**Location:** `activities/schedule/ScheduleDetailActivity.java`

Task detail page that contains the chat icon in toolbar.

**Responsibilities:**
- Display unread count badge on chat icon
- Open chat when icon is clicked
- Collect task participants (creator + assignees)
- Initialize all participants in Firebase when chat opens

**Key Methods:**
- `setupChatIcon()` - Set up chat icon with badge
- `loadUnreadCount()` - Load and listen to unread count changes
- `openTaskChat()` - Open chat activity with participants
- `getTaskParticipantsAsUserData()` - Collect all participants as UserData objects

### 4. TaskAdapter
**Location:** `adapters/TaskAdapter.java`

Adapter for displaying tasks in schedule list with unread badges.

**Responsibilities:**
- Display task items
- Show unread message count badge on each task
- Listen to real-time unread count updates per task
- Clean up listeners when views are recycled

### 5. MessageAdapter
**Location:** `adapters/MessageAdapter.java`

Adapter for displaying chat messages in RecyclerView.

**Responsibilities:**
- Display sent messages (right-aligned)
- Display received messages (left-aligned)
- Show message time and status
- Handle different message types (text, image, file)

## Chat Flow

### 1. Opening a Chat (From Task Detail Page)

```
User clicks chat icon
    ↓
ScheduleDetailActivity.openTaskChat()
    ↓
getTaskParticipantsAsUserData()
    - Collects: creator + all assignees + current user
    ↓
ChatService.createOrGetGroupChatWithParticipants()
    ↓
For each participant:
    - initializeUserData() → Creates/updates in Firebase users table
    ↓
createOrGetGroupChat()
    ↓
Check if chat exists:
    ├─ NO: Create new chat
    │   ├─ Create chat in chats/{chatId}
    │   ├─ Create userChats entries for all participants
    │   └─ Store mapping in taskChats/{taskId}
    │
    └─ YES: Ensure userChats entries exist
        ├─ Add new participants if any
        └─ Create missing userChats entries
    ↓
Open ChatActivity
```

### 2. Sending a Message

```
User types message and clicks send
    ↓
ChatActivity.sendMessage()
    ↓
ChatService.sendGroupMessage()
    ↓
1. Save message to chats/{chatId}/messages/{messageId}
    ↓
2. Update last message in chat
    ↓
3. updateGroupChatUserChats()
    ↓
For each participant (except sender):
    updateGroupChatUserChatEntry()
    ├─ Read current unreadCount from server
    ├─ Increment unreadCount by 1
    └─ Write updated count to userChats/{userId}/{chatId}/unreadCount
    ↓
Real-time listeners fire:
    ├─ ScheduleDetailActivity badge updates
    └─ TaskAdapter badges update
```

### 3. Unread Count Tracking

**How it works:**
1. When a message is sent, `unreadCount` is incremented for all participants except the sender
2. Each user has a `userChats/{userId}/{chatId}/unreadCount` field
3. Real-time listeners (`addValueEventListener`) are attached to this path
4. When `unreadCount` changes, listeners automatically fire
5. UI badges update in real-time

**Key Points:**
- Sender's unreadCount is NOT incremented (they sent the message)
- When user opens ChatActivity, `markGroupChatAsSeen()` resets unreadCount to 0
- Badges are shown on:
  - Task Detail page toolbar (chat icon)
  - Task items in schedule list

**Implementation:**
- Uses `get()` to read from server (bypasses cache)
- Uses `setValue()` with `CompletionListener` to write to server
- Uses `addValueEventListener` for persistent real-time updates
- Database is set to online mode (`goOnline()`) to ensure server writes

### 4. Marking Messages as Seen

```
User opens ChatActivity
    ↓
ChatActivity.onCreate() / onResume()
    ↓
ChatService.markGroupChatAsSeen()
    ↓
Set userChats/{userId}/{chatId}/unreadCount = 0
    ↓
Real-time listener fires
    ↓
Badge updates to show 0 (hidden)
```

## Data Initialization

### When Participants are Initialized:

1. **When chat icon is clicked:**
   - All participants (creator + assignees + current user) are initialized in Firebase `users` table
   - This ensures all user data is available even if they haven't visited the chat yet

2. **When ChatActivity opens:**
   - Current user is initialized (if not already done)
   - This ensures the current user's data is always up-to-date

### User Data Stored:
All fields from `UserData` model are stored in Firebase:
- id, name, email, role, phone
- avis_id, avatar, avatar_url
- created_at, updated_at
- lastSeen (timestamp)

## Real-time Updates

### Unread Count Badge Updates:
- **ScheduleDetailActivity**: Listens to `userChats/{currentUserId}/{chatId}`
- **TaskAdapter**: Each task item listens to its own `userChats/{currentUserId}/{chatId}`
- Both use `addValueEventListener` for persistent real-time updates
- Listeners are properly removed in `onDestroy()` / `onViewRecycled()` to prevent memory leaks

### Message Updates:
- **ChatActivity**: Listens to `chats/{chatId}/messages` for new messages
- Messages are displayed in real-time as they arrive
- RecyclerView automatically scrolls to bottom when new message arrives

## Error Handling

### Firebase Write Failures:
- Primary: `setValue()` with `CompletionListener`
- Fallback: `updateChildren()` if `setValue()` fails
- All errors are logged for debugging

### Null Safety:
- Extensive null checks in all Firebase callbacks
- Activity lifecycle checks before UI updates
- Prevents crashes when activity is destroyed

## Best Practices

1. **Always initialize users before creating chats** - Ensures user data exists in Firebase
2. **Use persistent listeners for real-time updates** - `addValueEventListener` not `addListenerForSingleValueEvent`
3. **Clean up listeners** - Remove listeners in `onDestroy()` / `onViewRecycled()`
4. **Read from server for increments** - Use `get()` instead of cached reads for unreadCount
5. **Write to server explicitly** - Use `goOnline()` and `CompletionListener` to ensure server writes
6. **Handle activity lifecycle** - Check `isFinishing()` / `isDestroyed()` before UI updates

## Testing Checklist

- [ ] Chat icon opens chat with correct participants
- [ ] Messages are sent and received in real-time
- [ ] Unread count increments when message is sent (except sender)
- [ ] Badge updates in real-time on Task Detail page
- [ ] Badge updates in real-time on task list items
- [ ] Unread count resets to 0 when chat is opened
- [ ] All participants are initialized in Firebase users table
- [ ] userChats entries are created for all participants
- [ ] New assignees are added to existing chats
- [ ] Listeners are cleaned up properly (no memory leaks)

## Future Enhancements

- Message read receipts (who has read the message)
- Typing indicators
- Push notifications for new messages
- Message search functionality
- File/image previews
- Message reactions/emojis

