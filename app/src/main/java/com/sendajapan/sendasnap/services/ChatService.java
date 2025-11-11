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
import com.sendajapan.sendasnap.utils.FirebaseUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatService {

    private static final String TAG = "ChatService";
    private static ChatService instance;
    private final FirebaseDatabase database;
    
    private ChatService() {
        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
    }
    
    public static synchronized ChatService getInstance() {
        if (instance == null) {
            instance = new ChatService();
        }
        return instance;
    }
    
    /**
     * Initialize current user in Firebase
     */
    public void initializeUser(Context context) {
        String userId = FirebaseUtils.getCurrentUserId(context);
        if (userId.isEmpty()) {
            return;
        }
        
        String username = FirebaseUtils.getCurrentUsername(context);
        String email = FirebaseUtils.getCurrentUserEmail(context);
        
        DatabaseReference userRef = database.getReference("users").child(userId);
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("username", username);
        userData.put("email", email);
        userData.put("lastSeen", System.currentTimeMillis());
        
        userRef.setValue(userData).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "User initialized in Firebase");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to initialize user", e);
        });
    }
    
    /**
     * Update user's last seen timestamp
     */
    public void updateLastSeen(Context context) {
        String userId = FirebaseUtils.getCurrentUserId(context);
        if (userId.isEmpty()) {
            return;
        }
        
        database.getReference("users").child(userId).child("lastSeen")
                .setValue(System.currentTimeMillis());
    }
    
    /**
     * Send text message
     */
    public void sendMessage(String chatId, String receiverId, String messageText, MessageCallback callback) {
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
            // Update last message in chat
            updateLastMessage(chatId, senderId, messageText, "text");
            // Update user chats
            updateUserChats(chatId, senderId, receiverId, messageText, System.currentTimeMillis());
            callback.onSuccess(message);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to send message", e);
            callback.onFailure(e);
        });
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
        DatabaseReference userChatRef = database.getReference("userChats").child(userId).child(chatId);
        
        userChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int unreadCountValue = 0;
                if (snapshot.exists()) {
                    Chat existingChat = snapshot.getValue(Chat.class);
                    if (existingChat != null) {
                        unreadCountValue = existingChat.getUnreadCount() + unreadIncrement;
                    }
                }
                
                // Make final for inner class
                final int finalUnreadCount = unreadCountValue;
                
                // Get other user info
                getUserById(otherUserId, new UserCallback() {
                    @Override
                    public void onSuccess(ChatUser user) {
                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put("chatId", chatId);
                        chatData.put("otherUserId", otherUserId);
                        chatData.put("otherUserName", user.getUsername());
                        chatData.put("otherUserEmail", user.getEmail());
                        chatData.put("lastMessage", lastMessage);
                        chatData.put("lastMessageTime", timestamp);
                        chatData.put("unreadCount", finalUnreadCount);
                        
                        userChatRef.setValue(chatData);
                    }
                    
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to get user info for chat update", e);
                    }
                });
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to update user chat", error.toException());
            }
        });
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
                        participants.put(participantId, true);
                    }
                    chatData.put("participants", participants);
                    
                    chatRef.updateChildren(chatData);
                }
                
                Chat chat = new Chat();
                chat.setChatId(chatId);
                chat.setLastMessage("");
                chat.setLastMessageTime(0);
                chat.setUnreadCount(0);
                
                callback.onSuccess(chat);
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to create/get group chat", error.toException());
                callback.onFailure(error.toException());
            }
        });
    }
    
    /**
     * Send message to group chat
     */
    public void sendGroupMessage(String chatId, String messageText, MessageCallback callback) {
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
        messageRef.setValue(message).addOnSuccessListener(aVoid -> {
            // Update last message in chat
            updateLastMessage(chatId, senderId, messageText, "text");
            // Update all participants' userChats
            updateGroupChatUserChats(chatId, senderId, messageText, System.currentTimeMillis());
            callback.onSuccess(message);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to send group message", e);
            callback.onFailure(e);
        });
    }
    
    /**
     * Get unread count for group chat
     */
    public void getGroupChatUnreadCount(String chatId, String userId, UnreadCountCallback callback) {
        DatabaseReference unreadRef = database.getReference("userChats").child(userId).child(chatId).child("unreadCount");
        unreadRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int unreadCount = 0;
                if (snapshot.exists() && snapshot.getValue() != null) {
                    Object value = snapshot.getValue();
                    if (value instanceof Long) {
                        unreadCount = ((Long) value).intValue();
                    } else if (value instanceof Integer) {
                        unreadCount = (Integer) value;
                    }
                }
                callback.onSuccess(unreadCount);
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to get unread count", error.toException());
                callback.onFailure(error.toException());
            }
        });
    }
    
    /**
     * Mark group chat as seen (reset unread count)
     */
    public void markGroupChatAsSeen(String chatId, String userId) {
        DatabaseReference userChatRef = database.getReference("userChats").child(userId).child(chatId);
        userChatRef.child("unreadCount").setValue(0);
    }
    
    /**
     * Update userChats for all participants in group chat
     */
    private void updateGroupChatUserChats(String chatId, String senderId, String lastMessage, long timestamp) {
        // Get all participants from chat
        DatabaseReference chatRef = database.getReference("chats").child(chatId).child("participants");
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> participantIds = new ArrayList<>();
                for (DataSnapshot participantSnapshot : snapshot.getChildren()) {
                    participantIds.add(participantSnapshot.getKey());
                }
                
                // Get task info from chat
                DatabaseReference taskInfoRef = database.getReference("chats").child(chatId);
                taskInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot taskSnapshot) {
                        String taskId = taskSnapshot.child("taskId").getValue(String.class);
                        String taskTitle = taskSnapshot.child("taskTitle").getValue(String.class);
                        
                        // Update each participant's userChats
                        for (String participantId : participantIds) {
                            int unreadIncrement = participantId.equals(senderId) ? 0 : 1;
                            updateGroupChatUserChatEntry(chatId, participantId, taskId, taskTitle, lastMessage, timestamp, unreadIncrement);
                        }
                    }
                    
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Failed to get task info", error.toException());
                    }
                });
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to get participants", error.toException());
            }
        });
    }
    
    /**
     * Update single user's group chat entry
     */
    private void updateGroupChatUserChatEntry(String chatId, String userId, String taskId, String taskTitle, 
                                               String lastMessage, long timestamp, int unreadIncrement) {
        DatabaseReference userChatRef = database.getReference("userChats").child(userId).child(chatId);
        
        userChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int unreadCountValue = 0;
                if (snapshot.exists()) {
                    Chat existingChat = snapshot.getValue(Chat.class);
                    if (existingChat != null) {
                        unreadCountValue = existingChat.getUnreadCount() + unreadIncrement;
                    }
                }
                
                Map<String, Object> chatData = new HashMap<>();
                chatData.put("chatId", chatId);
                chatData.put("taskId", taskId);
                chatData.put("taskTitle", taskTitle);
                chatData.put("isGroupChat", true);
                chatData.put("lastMessage", lastMessage);
                chatData.put("lastMessageTime", timestamp);
                chatData.put("unreadCount", unreadCountValue);
                
                userChatRef.setValue(chatData);
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to update group chat user entry", error.toException());
            }
        });
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
    
    public interface MessagesCallback {
        void onSuccess(List<Message> messages);
        void onFailure(Exception e);
    }
    
    public interface ChatCallback {
        void onSuccess(Chat chat);
        void onFailure(Exception e);
    }
    
    public interface UserCallback {
        void onSuccess(ChatUser user);
        void onFailure(Exception e);
    }
    
    public interface UsersCallback {
        void onSuccess(List<ChatUser> users);
        void onFailure(Exception e);
    }
    
    public interface GroupChatCallback {
        void onSuccess(Chat chat);
        void onFailure(Exception e);
    }
    
    public interface UnreadCountCallback {
        void onSuccess(int unreadCount);
        void onFailure(Exception e);
    }
}

