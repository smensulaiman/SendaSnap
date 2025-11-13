package com.sendajapan.sendasnap.services;

import android.content.Context;
import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sendajapan.sendasnap.models.Chat;
import com.sendajapan.sendasnap.models.ChatUser;
import com.sendajapan.sendasnap.models.Message;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.services.chat.UnreadCountManager;
import com.sendajapan.sendasnap.services.chat.UserManager;
import com.sendajapan.sendasnap.utils.FirebaseUtils;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main service class for chat operations.
 * 
 * This service handles:
 * - User initialization in Firebase
 * - Group chat creation and management
 * - Message sending (text, image, file)
 * - Unread count tracking
 * - UserChats entry management
 * 
 * For detailed architecture documentation, see: docs/CHAT_SYSTEM_ARCHITECTURE.md
 * 
 * @see com.sendajapan.sendasnap.services.chat.UserManager
 * @see com.sendajapan.sendasnap.services.chat.UnreadCountManager
 */
public class ChatService {

    private static final String TAG = "ChatService";
    private static ChatService instance;
    private final FirebaseDatabase database;
    
    // Manager classes for better code organization
    private final UserManager userManager;
    private final UnreadCountManager unreadCountManager;
    
    private ChatService() {
        // Firebase is initialized in MyApplication
        database = FirebaseDatabase.getInstance();
        
        // Initialize manager classes
        userManager = new UserManager(database);
        unreadCountManager = new UnreadCountManager(database);
    }
    
    public static synchronized ChatService getInstance() {
        if (instance == null) {
            instance = new ChatService();
        }
        return instance;
    }
    
    // ============================================================================
    // USER MANAGEMENT METHODS
    // ============================================================================
    
    /**
     * Initialize current user in Firebase with all UserData fields.
     * Delegates to UserManager.
     * 
     * @param context Application context
     */
    public void initializeUser(Context context) {
        userManager.initializeCurrentUser(context);
    }
    
    /**
     * Initialize/update user data in Firebase from UserData object.
     * This method can be used for any user, not just the current user.
     * Delegates to UserManager.
     * 
     * @param userData UserData object containing all user information
     */
    public void initializeUserData(UserData userData) {
        userManager.initializeUserData(userData);
    }
    
    /**
     * Update user's last seen timestamp.
     * Delegates to UserManager.
     * 
     * @param context Application context
     */
    public void updateLastSeen(Context context) {
        userManager.updateLastSeen(context);
    }
    
    /**
     * Send text message
     */
    public void sendMessage(String chatId, String receiverId, String messageText, MessageCallback callback) {
        try {
            if (chatId == null || chatId.isEmpty()) {
                callback.onFailure(new Exception("Invalid chat ID"));
                return;
            }
            
            if (messageText == null || messageText.trim().isEmpty()) {
                callback.onFailure(new Exception("Message text cannot be empty"));
                return;
            }
            
        String senderId = FirebaseUtils.getCurrentUserId(null);
        if (senderId.isEmpty()) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        
        String messageId = database.getReference("chats").child(chatId).child("messages").push().getKey();
        if (messageId == null) {
            callback.onFailure(new Exception("Failed to generate message ID"));
            return;
        }
        
        Message message = new Message();
        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessage(messageText);
        message.setMessageType("text");
        message.setTimestamp(System.currentTimeMillis());
        message.setSeen(false);
        
        DatabaseReference messageRef = database.getReference("chats").child(chatId).child("messages").child(messageId);
        messageRef.setValue(message).addOnSuccessListener(aVoid -> {
                try {
            // Update last message in chat
            updateLastMessage(chatId, senderId, messageText, "text");
            // Update user chats
            updateUserChats(chatId, senderId, receiverId, messageText, System.currentTimeMillis());
            callback.onSuccess(message);
                } catch (Exception e) {
                    Log.e(TAG, "Error updating chat after sending message", e);
                    // Still call success since message was sent
                    callback.onSuccess(message);
                }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to send message", e);
            callback.onFailure(e);
        });
        } catch (Exception e) {
            Log.e(TAG, "Error in sendMessage", e);
            callback.onFailure(e);
        }
    }
    
    /**
     * Send image message
     */
    public void sendImageMessage(String chatId, String receiverId, String imageUrl, MessageCallback callback) {
        String senderId = FirebaseUtils.getCurrentUserId(null);
        if (senderId.isEmpty()) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        
        String messageId = database.getReference("chats").child(chatId).child("messages").push().getKey();
        if (messageId == null) {
            callback.onFailure(new Exception("Failed to generate message ID"));
            return;
        }
        
        Message message = new Message();
        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessage("Image");
        message.setMessageType("image");
        message.setImageUrl(imageUrl);
        message.setTimestamp(System.currentTimeMillis());
        message.setSeen(false);
        
        DatabaseReference messageRef = database.getReference("chats").child(chatId).child("messages").child(messageId);
        messageRef.setValue(message).addOnSuccessListener(aVoid -> {
            updateLastMessage(chatId, senderId, "Image", "image");
            updateUserChats(chatId, senderId, receiverId, "Image", System.currentTimeMillis());
            callback.onSuccess(message);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to send image message", e);
            callback.onFailure(e);
        });
    }
    
    /**
     * Send file message
     */
    public void sendFileMessage(String chatId, String receiverId, String fileUrl, String fileName, MessageCallback callback) {
        String senderId = FirebaseUtils.getCurrentUserId(null);
        if (senderId.isEmpty()) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        
        String messageId = database.getReference("chats").child(chatId).child("messages").push().getKey();
        if (messageId == null) {
            callback.onFailure(new Exception("Failed to generate message ID"));
            return;
        }
        
        Message message = new Message();
        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessage("File: " + fileName);
        message.setMessageType("file");
        message.setFileUrl(fileUrl);
        message.setFileName(fileName);
        message.setTimestamp(System.currentTimeMillis());
        message.setSeen(false);
        
        DatabaseReference messageRef = database.getReference("chats").child(chatId).child("messages").child(messageId);
        messageRef.setValue(message).addOnSuccessListener(aVoid -> {
            updateLastMessage(chatId, senderId, "File: " + fileName, "file");
            updateUserChats(chatId, senderId, receiverId, "File: " + fileName, System.currentTimeMillis());
            callback.onSuccess(message);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to send file message", e);
            callback.onFailure(e);
        });
    }
    
    /**
     * Mark messages as seen
     */
    public void markAsSeen(String chatId, String currentUserId) {
        DatabaseReference messagesRef = database.getReference("chats").child(chatId).child("messages");
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null && 
                        message.getReceiverId().equals(currentUserId) && 
                        !message.isSeen()) {
                        
                        messageSnapshot.getRef().child("isSeen").setValue(true);
                        messageSnapshot.getRef().child("seenAt").setValue(System.currentTimeMillis());
                    }
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to mark messages as seen", error.toException());
            }
        });
    }
    
    /**
     * Get recent chats for current user
     */
    public void getRecentChats(Context context, RecentChatsCallback callback) {
        String userId = FirebaseUtils.getCurrentUserId(context);
        if (userId.isEmpty()) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        
        DatabaseReference userChatsRef = database.getReference("userChats").child(userId);
        userChatsRef.orderByChild("lastMessageTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Chat> chats = new ArrayList<>();
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    Chat chat = chatSnapshot.getValue(Chat.class);
                    if (chat != null) {
                        chats.add(0, chat); // Add to beginning to reverse order
                    }
                }
                callback.onSuccess(chats);
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to get recent chats", error.toException());
                callback.onFailure(error.toException());
            }
        });
    }
    
    /**
     * Get messages for a chat
     */
    public void getChatMessages(String chatId, MessagesCallback callback) {
        DatabaseReference messagesRef = database.getReference("chats").child(chatId).child("messages");
        messagesRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        messages.add(message);
                    }
                }
                callback.onSuccess(messages);
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to get messages", error.toException());
                callback.onFailure(error.toException());
            }
        });
    }
    
    /**
     * Create or get chat between two users
     */
    public void createChat(String otherUserId, String otherUserName, String otherUserEmail, ChatCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId(null);
        if (currentUserId.isEmpty()) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        
        String chatId = FirebaseUtils.generateChatId(currentUserId, otherUserId);
        
        DatabaseReference chatRef = database.getReference("chats").child(chatId);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Create new chat
                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put("participants/" + currentUserId, true);
                    chatData.put("participants/" + otherUserId, true);
                    chatRef.updateChildren(chatData);
                }
                
                Chat chat = new Chat();
                chat.setChatId(chatId);
                chat.setOtherUserId(otherUserId);
                chat.setOtherUserName(otherUserName);
                chat.setOtherUserEmail(otherUserEmail);
                chat.setLastMessage("");
                chat.setLastMessageTime(0);
                chat.setUnreadCount(0);
                
                callback.onSuccess(chat);
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to create chat", error.toException());
                callback.onFailure(error.toException());
            }
        });
    }
    
    /**
     * Get user by ID
     */
    public void getUserById(String userId, UserCallback callback) {
        database.getReference("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ChatUser user = snapshot.getValue(ChatUser.class);
                    callback.onSuccess(user);
                } else {
                    callback.onFailure(new Exception("User not found"));
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to get user", error.toException());
                callback.onFailure(error.toException());
            }
        });
    }
    
    /**
     * Get all users (for contacts list)
     */
    public void getAllUsers(Context context, UsersCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId(context);
        database.getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<ChatUser> users = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    ChatUser user = userSnapshot.getValue(ChatUser.class);
                    if (user != null && !user.getUserId().equals(currentUserId)) {
                        users.add(user);
                    }
                }
                callback.onSuccess(users);
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to get users", error.toException());
                callback.onFailure(error.toException());
            }
        });
    }
    
    private void updateLastMessage(String chatId, String senderId, String messageText, String messageType) {
        Map<String, Object> lastMessage = new HashMap<>();
        lastMessage.put("message", messageText);
        lastMessage.put("senderId", senderId);
        lastMessage.put("timestamp", System.currentTimeMillis());
        lastMessage.put("messageType", messageType);
        
        database.getReference("chats").child(chatId).child("lastMessage").setValue(lastMessage);
    }
    
    private void updateUserChats(String chatId, String senderId, String receiverId, String lastMessage, long timestamp) {
        // Update sender's userChats
        updateUserChatEntry(chatId, senderId, receiverId, lastMessage, timestamp, 0);
        
        // Update receiver's userChats (with unread count)
        updateUserChatEntry(chatId, receiverId, senderId, lastMessage, timestamp, 1);
    }
    
    private void updateUserChatEntry(String chatId, String userId, String otherUserId, String lastMessage, long timestamp, int unreadIncrement) {
        if (chatId == null || userId == null || otherUserId == null) {
            Log.e(TAG, "Invalid parameters for updateUserChatEntry");
            return;
        }
        
        try {
        DatabaseReference userChatRef = database.getReference("userChats").child(userId).child(chatId);
        
        userChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                    try {
                int unreadCountValue = 0;
                if (snapshot.exists()) {
                    Chat existingChat = snapshot.getValue(Chat.class);
                    if (existingChat != null) {
                        unreadCountValue = existingChat.getUnreadCount() + unreadIncrement;
                    }
                }
                
                // Make final for inner class
                final int finalUnreadCount = unreadCountValue;
                
                        // Get other user info with error handling
                getUserById(otherUserId, new UserCallback() {
                    @Override
                    public void onSuccess(ChatUser user) {
                                try {
                                    if (user == null) {
                                        Log.w(TAG, "User is null for userId: " + otherUserId);
                                        // Still update chat with basic info
                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put("chatId", chatId);
                        chatData.put("otherUserId", otherUserId);
                                        chatData.put("otherUserName", "User");
                                        chatData.put("otherUserEmail", "");
                        chatData.put("lastMessage", lastMessage);
                        chatData.put("lastMessageTime", timestamp);
                        chatData.put("unreadCount", finalUnreadCount);
                        userChatRef.setValue(chatData);
                                        return;
                                    }
                                    
                                    Map<String, Object> chatData = new HashMap<>();
                                    chatData.put("chatId", chatId);
                                    chatData.put("otherUserId", otherUserId);
                                    chatData.put("otherUserName", user.getUsername() != null ? user.getUsername() : "User");
                                    chatData.put("otherUserEmail", user.getEmail() != null ? user.getEmail() : "");
                                    chatData.put("lastMessage", lastMessage);
                                    chatData.put("lastMessageTime", timestamp);
                                    chatData.put("unreadCount", finalUnreadCount);
                                    
                                    userChatRef.setValue(chatData).addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to update user chat entry", e);
                                    });
                                } catch (Exception e) {
                                    Log.e(TAG, "Error updating user chat entry", e);
                                }
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                                Log.e(TAG, "Failed to get user info for chat update, using fallback", e);
                                // Fallback: update chat without user info
                                try {
                                    Map<String, Object> chatData = new HashMap<>();
                                    chatData.put("chatId", chatId);
                                    chatData.put("otherUserId", otherUserId);
                                    chatData.put("otherUserName", "User");
                                    chatData.put("otherUserEmail", "");
                                    chatData.put("lastMessage", lastMessage);
                                    chatData.put("lastMessageTime", timestamp);
                                    chatData.put("unreadCount", finalUnreadCount);
                                    userChatRef.setValue(chatData);
                                } catch (Exception ex) {
                                    Log.e(TAG, "Failed to update user chat entry with fallback", ex);
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onDataChange for updateUserChatEntry", e);
                    }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to update user chat", error.toException());
            }
        });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up updateUserChatEntry listener", e);
        }
    }
    
    // ============================================================================
    // GROUP CHAT MANAGEMENT METHODS
    // ============================================================================
    
    /**
     * Create or get group chat for a task with UserData objects.
     * 
     * This method:
     * 1. Initializes ALL participants' data in Firebase users table
     * 2. Creates or retrieves the group chat
     * 3. Ensures userChats entries exist for all participants
     * 
     * This ensures all assignees are available in Firebase when chat is opened,
     * even if they haven't visited the chat activity yet.
     * 
     * @param taskId Task ID
     * @param taskTitle Task title (used as chat name)
     * @param participants List of UserData objects for all participants (creator + assignees + current user)
     * @param callback Callback with created/retrieved Chat object
     */
    public void createOrGetGroupChatWithParticipants(String taskId, String taskTitle, List<UserData> participants, GroupChatCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId(null);
        if (currentUserId.isEmpty()) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        
        if (participants == null || participants.isEmpty()) {
            callback.onFailure(new Exception("No participants provided"));
            return;
        }
        
        Log.d(TAG, "Initializing all participants in Firebase (users and userChats): " + participants.size() + " participants");
        
        // Initialize ALL participants' data in Firebase (both users and userChats)
        // This ensures all assignees are available in Firebase when chat is opened
        for (UserData participant : participants) {
            if (participant != null && participant.getEmail() != null && !participant.getEmail().isEmpty()) {
                // Initialize user data in users table
                initializeUserData(participant);
                Log.d(TAG, "Initialized user data for: " + participant.getEmail());
            }
        }
        
        // Extract participant IDs
        List<String> participantIds = new ArrayList<>();
        for (UserData participant : participants) {
            if (participant != null && participant.getEmail() != null && !participant.getEmail().isEmpty()) {
                String userId = FirebaseUtils.sanitizeEmailForKey(participant.getEmail());
                if (!userId.isEmpty()) {
                    participantIds.add(userId);
                }
            }
        }
        
        // Add current user if not already in the list
        if (!currentUserId.isEmpty() && !participantIds.contains(currentUserId)) {
            participantIds.add(currentUserId);
        }
        
        Log.d(TAG, "Total participant IDs: " + participantIds.size());
        
        // Call the existing method with participant IDs
        createOrGetGroupChat(taskId, taskTitle, participantIds, callback);
    }
    
    /**
     * Create or get group chat for a task
     */
    public void createOrGetGroupChat(String taskId, String taskTitle, List<String> participantIds, GroupChatCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId(null);
        if (currentUserId.isEmpty()) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        
        String chatId = "task_" + taskId;
        
        DatabaseReference chatRef = database.getReference("chats").child(chatId);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Create new group chat
                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put("taskId", taskId);
                    chatData.put("taskTitle", taskTitle);
                    chatData.put("isGroupChat", true);
                    
                    // Add all participants
                    Map<String, Object> participants = new HashMap<>();
                    for (String participantId : participantIds) {
                        if (participantId != null && !participantId.isEmpty()) {
                        participants.put(participantId, true);
                        }
                    }
                    chatData.put("participants", participants);
                    
                    // Create the chat
                    chatRef.setValue(chatData).addOnSuccessListener(aVoid -> {
                        // Store mapping in taskChats for reference
                        Map<String, Object> taskChatMapping = new HashMap<>();
                        taskChatMapping.put("chatId", chatId);
                        taskChatMapping.put("taskTitle", taskTitle);
                        taskChatMapping.put("createdAt", System.currentTimeMillis());
                        
                        database.getReference("taskChats").child(taskId).setValue(taskChatMapping)
                                .addOnSuccessListener(aVoid2 -> {
                                    Log.d(TAG, "Group chat created and mapping stored for task: " + taskId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to store task chat mapping", e);
                                });
                        
                        // Initialize userChats entries for all participants with unreadCount = 0
                        for (String participantId : participantIds) {
                            if (participantId != null && !participantId.isEmpty()) {
                                DatabaseReference userChatRef = database.getReference("userChats").child(participantId).child(chatId);
                                Map<String, Object> userChatData = new HashMap<>();
                                userChatData.put("chatId", chatId);
                                userChatData.put("taskId", taskId);
                                userChatData.put("taskTitle", taskTitle);
                                userChatData.put("isGroupChat", true);
                                userChatData.put("lastMessage", "");
                                userChatData.put("lastMessageTime", 0);
                                userChatData.put("unreadCount", 0);
                                userChatRef.setValue(userChatData);
                            }
                        }
                        
                        // Return chat object
                Chat chat = new Chat();
                chat.setChatId(chatId);
                chat.setLastMessage("");
                chat.setLastMessageTime(0);
                chat.setUnreadCount(0);
                        callback.onSuccess(chat);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to create group chat", e);
                        callback.onFailure(e);
                    });
                } else {
                    // Chat already exists, ensure userChats entries exist for all participants
                    Log.d(TAG, "Chat already exists: " + chatId + ", ensuring userChats entries exist for all participants");
                    
                    // Get participants from existing chat
                    DataSnapshot participantsSnapshot = snapshot.child("participants");
                    List<String> existingParticipantIds = new ArrayList<>();
                    for (DataSnapshot participantSnapshot : participantsSnapshot.getChildren()) {
                        String participantId = participantSnapshot.getKey();
                        if (participantId != null && !participantId.isEmpty()) {
                            existingParticipantIds.add(participantId);
                        }
                    }
                    
                    // Also add new participants from the provided list
                    boolean hasNewParticipants = false;
                    Map<String, Object> participantsUpdate = new HashMap<>();
                    for (String participantId : participantIds) {
                        if (participantId != null && !participantId.isEmpty() && !existingParticipantIds.contains(participantId)) {
                            existingParticipantIds.add(participantId);
                            participantsUpdate.put(participantId, true);
                            hasNewParticipants = true;
                            Log.d(TAG, "Adding new participant to existing chat: " + participantId);
                        }
                    }
                    
                    // Update chat participants if new ones were added
                    if (hasNewParticipants) {
                        DatabaseReference participantsRef = chatRef.child("participants");
                        participantsRef.updateChildren(participantsUpdate).addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Updated chat participants list with new members");
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to update chat participants list", e);
                        });
                    }
                    
                    // Ensure userChats entries exist for all participants
                    String existingTaskId = snapshot.child("taskId").getValue(String.class);
                    String existingTaskTitle = snapshot.child("taskTitle").getValue(String.class);
                    
                    for (String participantId : existingParticipantIds) {
                        if (participantId != null && !participantId.isEmpty()) {
                            DatabaseReference userChatRef = database.getReference("userChats").child(participantId).child(chatId);
                            userChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot userChatSnapshot) {
                                    if (!userChatSnapshot.exists()) {
                                        // Create missing userChats entry
                                        Map<String, Object> userChatData = new HashMap<>();
                                        userChatData.put("chatId", chatId);
                                        if (existingTaskId != null) {
                                            userChatData.put("taskId", existingTaskId);
                                        }
                                        if (existingTaskTitle != null) {
                                            userChatData.put("taskTitle", existingTaskTitle);
                                        }
                                        userChatData.put("isGroupChat", true);
                                        userChatData.put("lastMessage", "");
                                        userChatData.put("lastMessageTime", 0);
                                        userChatData.put("unreadCount", 0);
                                        userChatRef.setValue(userChatData).addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Created missing userChats entry for participant: " + participantId);
                                        }).addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to create userChats entry for participant: " + participantId, e);
                                        });
                                    }
                                }
                                
                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Log.e(TAG, "Failed to check userChats entry for participant: " + participantId, error.toException());
                                }
                            });
                        }
                    }
                    
                    // Return chat object
                    Chat chat = new Chat();
                    chat.setChatId(chatId);
                    chat.setLastMessage("");
                    chat.setLastMessageTime(0);
                    chat.setUnreadCount(0);
                callback.onSuccess(chat);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to create/get group chat", error.toException());
                callback.onFailure(error.toException());
            }
        });
    }
    
    /**
     * Send message to group chat.
     * 
     * This method:
     * 1. Saves the message to Firebase
     * 2. Updates the last message in the chat
     * 3. Updates unread counts for all participants (except sender)
     * 
     * @param chatId Group chat ID (format: "task_{taskId}")
     * @param messageText Message text
     * @param callback Callback with sent Message object
     */
    public void sendGroupMessage(String chatId, String messageText, MessageCallback callback) {
        try {
            if (chatId == null || chatId.isEmpty()) {
                callback.onFailure(new Exception("Invalid chat ID"));
                return;
            }
            
            if (messageText == null || messageText.trim().isEmpty()) {
                callback.onFailure(new Exception("Message text cannot be empty"));
                return;
            }
            
        String senderId = FirebaseUtils.getCurrentUserId(null);
        if (senderId.isEmpty()) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        
        String messageId = database.getReference("chats").child(chatId).child("messages").push().getKey();
        if (messageId == null) {
            callback.onFailure(new Exception("Failed to generate message ID"));
            return;
        }
        
        Message message = new Message();
        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setMessage(messageText);
        message.setMessageType("text");
        message.setTimestamp(System.currentTimeMillis());
        message.setSeen(false);
        
        DatabaseReference messageRef = database.getReference("chats").child(chatId).child("messages").child(messageId);
        Log.d(TAG, "Sending group message to chat: " + chatId + ", sender: " + senderId);
        messageRef.setValue(message).addOnSuccessListener(aVoid -> {
            try {
                Log.d(TAG, "Message saved successfully, now updating userChats for all participants");
                // Update last message in chat
                updateLastMessage(chatId, senderId, messageText, "text");
                // Update all participants' userChats
                updateGroupChatUserChats(chatId, senderId, messageText, System.currentTimeMillis());
                callback.onSuccess(message);
            } catch (Exception e) {
                Log.e(TAG, "Error updating chat after sending group message", e);
                // Still call success since message was sent
                callback.onSuccess(message);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to send group message", e);
            callback.onFailure(e);
        });
        } catch (Exception e) {
            Log.e(TAG, "Error in sendGroupMessage", e);
            callback.onFailure(e);
        }
    }
    
    // ============================================================================
    // UNREAD COUNT MANAGEMENT METHODS
    // ============================================================================
    
    /**
     * Get current unread count for a group chat (one-time read).
     * Delegates to UnreadCountManager.
     * 
     * @param chatId Chat ID
     * @param userId User ID
     * @param callback Callback with unread count
     */
    public void getGroupChatUnreadCount(String chatId, String userId, UnreadCountCallback callback) {
        unreadCountManager.getUnreadCount(chatId, userId, new UnreadCountManager.UnreadCountCallback() {
            @Override
            public void onSuccess(int unreadCount) {
                callback.onSuccess(unreadCount);
            }
            
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Add persistent listener for unread count updates.
     * Returns the listener so it can be removed later.
     * Delegates to UnreadCountManager.
     * 
     * @param chatId Chat ID
     * @param userId User ID
     * @param callback Callback that fires on every unread count change
     * @return ValueEventListener that can be removed later
     */
    public ValueEventListener addUnreadCountListener(String chatId, String userId, UnreadCountCallback callback) {
        return unreadCountManager.addUnreadCountListener(chatId, userId, new UnreadCountManager.UnreadCountCallback() {
            @Override
            public void onSuccess(int unreadCount) {
                callback.onSuccess(unreadCount);
            }
            
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
    
    /**
     * Remove unread count listener.
     * Delegates to UnreadCountManager.
     * 
     * @param chatId Chat ID
     * @param userId User ID
     * @param listener Listener to remove
     */
    public void removeUnreadCountListener(String chatId, String userId, ValueEventListener listener) {
        unreadCountManager.removeUnreadCountListener(chatId, userId, listener);
    }
    
    /**
     * Mark group chat as seen (reset unread count to 0).
     * Delegates to UnreadCountManager.
     * 
     * @param chatId Chat ID
     * @param userId User ID
     */
    public void markGroupChatAsSeen(String chatId, String userId) {
        unreadCountManager.markAsSeen(chatId, userId);
    }
    
    /**
     * Update userChats for all participants in group chat
     */
    private void updateGroupChatUserChats(String chatId, String senderId, String lastMessage, long timestamp) {
        Log.d(TAG, "updateGroupChatUserChats called for chat: " + chatId + ", sender: " + senderId);
        
        if (chatId == null || chatId.isEmpty()) {
            Log.e(TAG, "Invalid chatId in updateGroupChatUserChats");
            return;
        }
        
        if (senderId == null || senderId.isEmpty()) {
            Log.e(TAG, "Invalid senderId in updateGroupChatUserChats");
            return;
        }
        
        // Get all participants from chat
        DatabaseReference chatRef = database.getReference("chats").child(chatId).child("participants");
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.e(TAG, "Participants node does not exist for chat: " + chatId);
                    return;
                }
                
                List<String> participantIds = new ArrayList<>();
                for (DataSnapshot participantSnapshot : snapshot.getChildren()) {
                    String participantId = participantSnapshot.getKey();
                    if (participantId != null && !participantId.isEmpty()) {
                        participantIds.add(participantId);
                    }
                }
                
                if (participantIds.isEmpty()) {
                    Log.e(TAG, "No participants found for chat: " + chatId);
                    return;
                }
                
                Log.d(TAG, "Found " + participantIds.size() + " participants for chat: " + chatId + ": " + participantIds);
                
                // Get task info from chat
                DatabaseReference taskInfoRef = database.getReference("chats").child(chatId);
                taskInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot taskSnapshot) {
                        if (!taskSnapshot.exists()) {
                            Log.e(TAG, "Chat does not exist: " + chatId);
                            return;
                        }
                        
                        String taskId = taskSnapshot.child("taskId").getValue(String.class);
                        String taskTitle = taskSnapshot.child("taskTitle").getValue(String.class);
                        
                        Log.d(TAG, "Updating userChats for " + participantIds.size() + " participants, sender: " + senderId + ", taskId: " + taskId + ", taskTitle: " + taskTitle);
                        
                        // Update each participant's userChats
                        Log.d(TAG, "About to update userChats for " + participantIds.size() + " participants");
                        for (String participantId : participantIds) {
                            int unreadIncrement = participantId.equals(senderId) ? 0 : 1;
                            Log.d(TAG, "Calling updateGroupChatUserChatEntry for participant: " + participantId + ", unreadIncrement: " + unreadIncrement + ", senderId: " + senderId);
                            updateGroupChatUserChatEntry(chatId, participantId, taskId, taskTitle, lastMessage, timestamp, unreadIncrement);
                        }
                        Log.d(TAG, "Finished calling updateGroupChatUserChatEntry for all participants");
                    }
                    
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Failed to get task info for chat: " + chatId, error.toException());
                    }
                });
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to get participants for chat: " + chatId, error.toException());
            }
        });
    }
    
    /**
     * Update single user's group chat entry
     */
    private void updateGroupChatUserChatEntry(String chatId, String userId, String taskId, String taskTitle, 
                                               String lastMessage, long timestamp, int unreadIncrement) {
        if (chatId == null || userId == null) {
            Log.e(TAG, "Invalid parameters for updateGroupChatUserChatEntry");
            return;
        }
        
        if (unreadIncrement <= 0) {
            // No need to increment, just update other fields
            Log.d(TAG, "Skipping unread count increment for user: " + userId + " (increment is 0 or negative)");
            updateGroupChatUserChatEntryFieldsOnly(chatId, userId, taskId, taskTitle, lastMessage, timestamp);
            return;
        }
        
        Log.d(TAG, "Starting unread count update for user: " + userId + ", chat: " + chatId + ", increment: " + unreadIncrement);
        
        DatabaseReference userChatRef = database.getReference("userChats").child(userId).child(chatId);
        
        // Ensure database is online to force immediate server write
        database.goOnline();
        
        // Use get() to read from server (bypasses cache) instead of addListenerForSingleValueEvent
        userChatRef.get().addOnSuccessListener(snapshot -> {
            try {
                int currentUnreadCount = 0;
                boolean entryExists = snapshot.exists();
                
                if (entryExists) {
                    Object unreadObj = snapshot.child("unreadCount").getValue();
                    if (unreadObj instanceof Long) {
                        currentUnreadCount = ((Long) unreadObj).intValue();
                    } else if (unreadObj instanceof Integer) {
                        currentUnreadCount = ((Integer) unreadObj);
                    }
                }
                
                int newUnreadCount = currentUnreadCount + unreadIncrement;
                if (newUnreadCount < 0) {
                    newUnreadCount = 0;
                }
                
                final int finalUnreadCount = newUnreadCount;
                
                Log.d(TAG, "Read current unread count from SERVER: " + currentUnreadCount + ", calculating new: " + finalUnreadCount + " for user: " + userId);
                
                // Prepare updates - update all fields including unreadCount
                Map<String, Object> updates = new HashMap<>();
                updates.put("chatId", chatId);
                if (taskId != null) {
                    updates.put("taskId", taskId);
                }
                if (taskTitle != null) {
                    updates.put("taskTitle", taskTitle);
                }
                updates.put("isGroupChat", true);
                updates.put("lastMessage", lastMessage != null ? lastMessage : "");
                updates.put("lastMessageTime", timestamp);
                updates.put("unreadCount", finalUnreadCount);
                
                Log.d(TAG, "Writing updates to Firebase SERVER for user: " + userId + ", unreadCount: " + finalUnreadCount + ", path: userChats/" + userId + "/" + chatId);
                
                // Use setValue with CompletionListener to ensure write completes
                // This writes to server even with persistence enabled
                userChatRef.setValue(updates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        if (error != null) {
                            Log.e(TAG, "setValue failed for user: " + userId + ", error: " + error.getMessage() + ", code: " + error.getCode());
                            // Try updateChildren as fallback - this also writes to server
                            Map<String, Object> updateMap = new HashMap<>();
                            updateMap.put("unreadCount", finalUnreadCount);
                            updateMap.put("lastMessage", lastMessage != null ? lastMessage : "");
                            updateMap.put("lastMessageTime", timestamp);
                            userChatRef.updateChildren(updateMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError updateError, DatabaseReference updateRef) {
                                    if (updateError != null) {
                                        Log.e(TAG, "updateChildren also failed for user: " + userId + ", error: " + updateError.getMessage());
                                    } else {
                                        Log.d(TAG, "updateChildren succeeded: unreadCount = " + finalUnreadCount + " for user: " + userId);
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "setValue succeeded: unreadCount = " + finalUnreadCount + " for user: " + userId + ", chat: " + chatId);
                        }
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Exception processing unread count update for user: " + userId, e);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to read userChats entry from server for user: " + userId + ", chat: " + chatId, e);
            // Fallback: try to update anyway with increment of 1
            Map<String, Object> updates = new HashMap<>();
            updates.put("chatId", chatId);
            if (taskId != null) {
                updates.put("taskId", taskId);
            }
            if (taskTitle != null) {
                updates.put("taskTitle", taskTitle);
            }
            updates.put("isGroupChat", true);
            updates.put("lastMessage", lastMessage != null ? lastMessage : "");
            updates.put("lastMessageTime", timestamp);
            updates.put("unreadCount", unreadIncrement); // Assume starting from 0
            Log.d(TAG, "Fallback: Writing unreadCount = " + unreadIncrement + " for user: " + userId);
            userChatRef.setValue(updates);
        });
    }
    
    /**
     * Direct update method as fallback when transaction fails
     */
    private void updateGroupChatUserChatEntryDirect(String chatId, String userId, String taskId, String taskTitle, 
                                                    String lastMessage, long timestamp, int unreadIncrement) {
        Log.d(TAG, "Using direct update fallback for user: " + userId + ", chat: " + chatId);
        DatabaseReference userChatRef = database.getReference("userChats").child(userId).child(chatId);
        
        userChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    int currentUnreadCount = 0;
                    boolean entryExists = snapshot.exists();
                    
                    if (entryExists) {
                        Object unreadObj = snapshot.child("unreadCount").getValue();
                        if (unreadObj instanceof Long) {
                            currentUnreadCount = ((Long) unreadObj).intValue();
                        } else if (unreadObj instanceof Integer) {
                            currentUnreadCount = ((Integer) unreadObj);
                        }
                    }
                    
                    int newUnreadCount = currentUnreadCount + unreadIncrement;
                    if (newUnreadCount < 0) {
                        newUnreadCount = 0;
                    }
                    
                    final int finalUnreadCount = newUnreadCount;
                    
                    Log.d(TAG, "Direct update: current=" + currentUnreadCount + ", new=" + finalUnreadCount + " for user: " + userId);
                    
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("chatId", chatId);
                    if (taskId != null) {
                        updates.put("taskId", taskId);
                    }
                    if (taskTitle != null) {
                        updates.put("taskTitle", taskTitle);
                    }
                    updates.put("isGroupChat", true);
                    updates.put("lastMessage", lastMessage != null ? lastMessage : "");
                    updates.put("lastMessageTime", timestamp);
                    updates.put("unreadCount", finalUnreadCount);
                    
                    userChatRef.setValue(updates, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError error, DatabaseReference ref) {
                            if (error != null) {
                                Log.e(TAG, "Direct update failed for user: " + userId + ", error: " + error.getMessage());
                            } else {
                                Log.d(TAG, "Direct update succeeded: unreadCount = " + finalUnreadCount + " for user: " + userId);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error in direct update", e);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to read for direct update", error.toException());
            }
        });
    }
    
    /**
     * Update only the fields (not unread count) for a group chat user entry
     */
    private void updateGroupChatUserChatEntryFieldsOnly(String chatId, String userId, String taskId, String taskTitle, 
                                                        String lastMessage, long timestamp) {
        try {
            DatabaseReference userChatRef = database.getReference("userChats").child(userId).child(chatId);
            Map<String, Object> updates = new HashMap<>();
            updates.put("chatId", chatId);
            if (taskId != null) {
                updates.put("taskId", taskId);
            }
            if (taskTitle != null) {
                updates.put("taskTitle", taskTitle);
            }
            updates.put("isGroupChat", true);
            updates.put("lastMessage", lastMessage != null ? lastMessage : "");
            updates.put("lastMessageTime", timestamp);
            
            userChatRef.updateChildren(updates).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to update group chat user entry fields", e);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error updating group chat user entry fields", e);
        }
    }
    
    /**
     * Fallback method to update unread count if primary method fails
     * Uses get() to read from server (bypass cache) then updates
     */
    private void updateGroupChatUserChatEntryFallback(String chatId, String userId, String taskId, String taskTitle, 
                                                      String lastMessage, long timestamp, int unreadIncrement) {
        try {
            DatabaseReference userChatRef = database.getReference("userChats").child(userId).child(chatId);
            
            // Use get() to read from server (bypass cache)
            userChatRef.get().addOnSuccessListener(snapshot -> {
                try {
                    int currentUnreadCount = 0;
                    boolean entryExists = snapshot.exists();
                    
                    if (entryExists) {
                        // Get current unread count
                        Object unreadObj = snapshot.child("unreadCount").getValue();
                        if (unreadObj instanceof Long) {
                            currentUnreadCount = ((Long) unreadObj).intValue();
                        } else if (unreadObj instanceof Integer) {
                            currentUnreadCount = ((Integer) unreadObj);
                        }
                    }
                    
                    // Calculate new unread count
                    int newUnreadCount = currentUnreadCount + unreadIncrement;
                    if (newUnreadCount < 0) {
                        newUnreadCount = 0;
                    }
                    
                    // Make final for lambda
                    final int finalUnreadCount = newUnreadCount;
                    
                    // Update or create the entry
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("chatId", chatId);
                    if (taskId != null) {
                        updates.put("taskId", taskId);
                    }
                    if (taskTitle != null) {
                        updates.put("taskTitle", taskTitle);
                    }
                    updates.put("isGroupChat", true);
                    updates.put("lastMessage", lastMessage != null ? lastMessage : "");
                    updates.put("lastMessageTime", timestamp);
                    updates.put("unreadCount", finalUnreadCount);
                    
                    Log.d(TAG, "Updating userChats entry (fallback) for user: " + userId + ", chat: " + chatId + ", currentCount: " + currentUnreadCount + ", increment: " + unreadIncrement + ", newCount: " + finalUnreadCount);
                    
                    // Use setValue to ensure the entry is created/updated on server
                    userChatRef.setValue(updates, (error, ref) -> {
                        if (error != null) {
                            Log.e(TAG, "Failed to update unread count (fallback) for user: " + userId + ", chat: " + chatId + ", error: " + error.getMessage());
                            // Last resort: try updateChildren
                            userChatRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Unread count updated via updateChildren (last resort) for user: " + userId);
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "All update methods failed for user: " + userId + ", chat: " + chatId, e);
                            });
                        } else {
                            Log.d(TAG, "Unread count " + (entryExists ? "updated" : "created") + " successfully (fallback) for user: " + userId + ", chat: " + chatId + ", count: " + finalUnreadCount);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error in fallback updateGroupChatUserChatEntry", e);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to read userChats entry (fallback) for user: " + userId + ", chat: " + chatId, e);
                // Last resort: set unreadCount directly without reading
                Map<String, Object> directUpdate = new HashMap<>();
                directUpdate.put("unreadCount", unreadIncrement);
                directUpdate.put("chatId", chatId);
                if (taskId != null) {
                    directUpdate.put("taskId", taskId);
                }
                if (taskTitle != null) {
                    directUpdate.put("taskTitle", taskTitle);
                }
                directUpdate.put("isGroupChat", true);
                directUpdate.put("lastMessage", lastMessage != null ? lastMessage : "");
                directUpdate.put("lastMessageTime", timestamp);
                userChatRef.setValue(directUpdate, (error, ref) -> {
                    if (error == null) {
                        Log.d(TAG, "Unread count set directly (last resort) for user: " + userId);
                    } else {
                        Log.e(TAG, "Failed to set unread count directly (last resort)", error.toException());
                    }
                });
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up fallback updateGroupChatUserChatEntry", e);
        }
    }
    
    private void updateGroupChatUserChatEntryFields(String chatId, String userId, String taskId, String taskTitle, 
                                                    String lastMessage, long timestamp) {
        try {
            DatabaseReference userChatRef = database.getReference("userChats").child(userId).child(chatId);
            Map<String, Object> updates = new HashMap<>();
            updates.put("chatId", chatId);
            if (taskId != null) {
                updates.put("taskId", taskId);
            }
            if (taskTitle != null) {
                updates.put("taskTitle", taskTitle);
            }
            updates.put("isGroupChat", true);
            updates.put("lastMessage", lastMessage != null ? lastMessage : "");
            updates.put("lastMessageTime", timestamp);
            
            userChatRef.updateChildren(updates).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to update group chat user entry fields", e);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error updating group chat user entry fields", e);
        }
    }
    
    
    // Callback interfaces
    public interface MessageCallback {
        void onSuccess(Message message);
        void onFailure(Exception e);
    }
    
    public interface RecentChatsCallback {
        void onSuccess(List<Chat> chats);
        void onFailure(Exception e);
    }
    
    /**
     * Callback for retrieving chat messages.
     */
    public interface MessagesCallback {
        void onSuccess(List<Message> messages);
        void onFailure(Exception e);
    }
    
    /**
     * Callback for chat creation/retrieval operations.
     */
    public interface ChatCallback {
        void onSuccess(Chat chat);
        void onFailure(Exception e);
    }
    
    /**
     * Callback for retrieving a single user.
     */
    public interface UserCallback {
        void onSuccess(ChatUser user);
        void onFailure(Exception e);
    }
    
    /**
     * Callback for retrieving multiple users.
     */
    public interface UsersCallback {
        void onSuccess(List<ChatUser> users);
        void onFailure(Exception e);
    }
    
    /**
     * Callback for group chat operations.
     */
    public interface GroupChatCallback {
        void onSuccess(Chat chat);
        void onFailure(Exception e);
    }
    
    /**
     * Callback for unread count operations.
     */
    public interface UnreadCountCallback {
        void onSuccess(int unreadCount);
        void onFailure(Exception e);
    }
}

