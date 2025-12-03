package com.sendajapan.sendasnap.services;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatService {

    private static final String PATH_CHATS = "chats";
    private static final String PATH_MESSAGES = "messages";
    private static final String PATH_USER_CHATS = "userChats";
    private static final String PATH_USERS = "users";
    private static final String PATH_PARTICIPANTS = "participants";
    private static final String PATH_TASK_CHATS = "taskChats";
    private static final String PATH_LAST_MESSAGE = "lastMessage";
    private static final String MESSAGE_TYPE_TEXT = "text";
    private static final String MESSAGE_TYPE_IMAGE = "image";
    private static final String MESSAGE_TYPE_FILE = "file";
    private static final String MESSAGE_TEXT_IMAGE = "Image";
    private static final String MESSAGE_TEXT_FILE_PREFIX = "File: ";
    private static final String CHAT_ID_PREFIX_TASK = "task_";
    private static final String FIELD_CHAT_ID = "chatId";
    private static final String FIELD_OTHER_USER_ID = "otherUserId";
    private static final String FIELD_OTHER_USER_NAME = "otherUserName";
    private static final String FIELD_OTHER_USER_EMAIL = "otherUserEmail";
    private static final String FIELD_LAST_MESSAGE = "lastMessage";
    private static final String FIELD_LAST_MESSAGE_TIME = "lastMessageTime";
    private static final String FIELD_UNREAD_COUNT = "unreadCount";
    private static final String FIELD_IS_GROUP_CHAT = "isGroupChat";
    private static final String FIELD_TASK_ID = "taskId";
    private static final String FIELD_TASK_TITLE = "taskTitle";
    private static final String FIELD_IS_SEEN = "isSeen";
    private static final String FIELD_SEEN_AT = "seenAt";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_MESSAGE = "message";
    private static final String FIELD_SENDER_ID = "senderId";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_MESSAGE_TYPE = "messageType";
    private static final String DEFAULT_USER_NAME = "User";
    private static final String EMPTY_STRING = "";
    private static final int DEFAULT_UNREAD_COUNT = 0;
    private static final long DEFAULT_TIMESTAMP = 0L;

    private static ChatService instance;
    private final FirebaseDatabase database;
    private final UserManager userManager;
    private final UnreadCountManager unreadCountManager;

    private ChatService() {
        database = FirebaseDatabase.getInstance();
        userManager = new UserManager(database);
        unreadCountManager = new UnreadCountManager(database);
    }

    public static synchronized ChatService getInstance() {
        if (instance == null) {
            instance = new ChatService();
        }
        return instance;
    }

    public void initializeUser(Context context) {
        userManager.initializeCurrentUser(context);
    }

    public void initializeUserData(UserData userData) {
        userManager.initializeUserData(userData);
    }

    public void updateLastSeen(Context context) {
        userManager.updateLastSeen(context);
    }

    public void sendMessage(String chatId, String receiverId, String messageText, MessageCallback callback) {
        try {
            if (!isValidChatId(chatId)) {
                callback.onFailure(new Exception("Invalid chat ID"));
                return;
            }

            if (isMessageTextEmpty(messageText)) {
                callback.onFailure(new Exception("Message text cannot be empty"));
                return;
            }

            String senderId = FirebaseUtils.getCurrentUserId(null);
            if (senderId.isEmpty()) {
                callback.onFailure(new Exception("User not logged in"));
                return;
            }

            String messageId = generateMessageId(chatId);
            if (messageId == null) {
                callback.onFailure(new Exception("Failed to generate message ID"));
                return;
            }

            Message message = createTextMessage(messageId, senderId, receiverId, messageText);
            DatabaseReference messageRef = getMessageReference(chatId, messageId);
            messageRef.setValue(message).addOnSuccessListener(aVoid -> {
                try {
                    updateLastMessage(chatId, senderId, messageText, MESSAGE_TYPE_TEXT);
                    updateUserChats(chatId, senderId, receiverId, messageText, System.currentTimeMillis());
                    callback.onSuccess(message);
                } catch (Exception e) {
                    callback.onSuccess(message);
                }
            }).addOnFailureListener(callback::onFailure);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    public void sendImageMessage(String chatId, String receiverId, String imageUrl, MessageCallback callback) {
        String senderId = FirebaseUtils.getCurrentUserId(null);
        if (senderId.isEmpty()) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String messageId = generateMessageId(chatId);
        if (messageId == null) {
            callback.onFailure(new Exception("Failed to generate message ID"));
            return;
        }

        Message message = createImageMessage(messageId, senderId, receiverId, imageUrl);
        DatabaseReference messageRef = getMessageReference(chatId, messageId);
        messageRef.setValue(message).addOnSuccessListener(aVoid -> {
            updateLastMessage(chatId, senderId, MESSAGE_TEXT_IMAGE, MESSAGE_TYPE_IMAGE);
            updateUserChats(chatId, senderId, receiverId, MESSAGE_TEXT_IMAGE, System.currentTimeMillis());
            callback.onSuccess(message);
        }).addOnFailureListener(callback::onFailure);
    }

    public void sendFileMessage(String chatId, String receiverId, String fileUrl, String fileName, MessageCallback callback) {
        String senderId = FirebaseUtils.getCurrentUserId(null);
        if (senderId.isEmpty()) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String messageId = generateMessageId(chatId);
        if (messageId == null) {
            callback.onFailure(new Exception("Failed to generate message ID"));
            return;
        }

        String fileMessageText = MESSAGE_TEXT_FILE_PREFIX + fileName;
        Message message = createFileMessage(messageId, senderId, receiverId, fileUrl, fileName, fileMessageText);
        DatabaseReference messageRef = getMessageReference(chatId, messageId);
        messageRef.setValue(message).addOnSuccessListener(aVoid -> {
            updateLastMessage(chatId, senderId, fileMessageText, MESSAGE_TYPE_FILE);
            updateUserChats(chatId, senderId, receiverId, fileMessageText, System.currentTimeMillis());
            callback.onSuccess(message);
        }).addOnFailureListener(callback::onFailure);
    }

    public void markAsSeen(String chatId, String currentUserId) {
        DatabaseReference messagesRef = database.getReference(PATH_CHATS).child(chatId).child(PATH_MESSAGES);
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null &&
                            message.getReceiverId().equals(currentUserId) &&
                            !message.isSeen()) {
                        messageSnapshot.getRef().child(FIELD_IS_SEEN).setValue(true);
                        messageSnapshot.getRef().child(FIELD_SEEN_AT).setValue(System.currentTimeMillis());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    public void getRecentChats(Context context, RecentChatsCallback callback) {
        String userId = FirebaseUtils.getCurrentUserId(context);
        if (userId.isEmpty()) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        DatabaseReference userChatsRef = database.getReference(PATH_USER_CHATS).child(userId);
        userChatsRef.orderByChild(FIELD_LAST_MESSAGE_TIME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Chat> chats = new ArrayList<>();
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    Chat chat = chatSnapshot.getValue(Chat.class);
                    if (chat != null) {
                        chats.add(0, chat);
                    }
                }
                callback.onSuccess(chats);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    public void getChatMessages(String chatId, MessagesCallback callback) {
        DatabaseReference messagesRef = database.getReference(PATH_CHATS).child(chatId).child(PATH_MESSAGES);
        messagesRef.orderByChild(FIELD_TIMESTAMP).addValueEventListener(new ValueEventListener() {
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
                callback.onFailure(error.toException());
            }
        });
    }

    public void createChat(String otherUserId, String otherUserName, String otherUserEmail, ChatCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId(null);
        if (currentUserId.isEmpty()) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String chatId = FirebaseUtils.generateChatId(currentUserId, otherUserId);
        DatabaseReference chatRef = database.getReference(PATH_CHATS).child(chatId);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put(PATH_PARTICIPANTS + "/" + currentUserId, true);
                    chatData.put(PATH_PARTICIPANTS + "/" + otherUserId, true);
                    chatRef.updateChildren(chatData);
                }

                Chat chat = createChatObject(chatId, otherUserId, otherUserName, otherUserEmail);
                callback.onSuccess(chat);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    public void getUserById(String userId, UserCallback callback) {
        database.getReference(PATH_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
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
                callback.onFailure(error.toException());
            }
        });
    }

    public void getAllUsers(Context context, UsersCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId(context);
        database.getReference(PATH_USERS).addListenerForSingleValueEvent(new ValueEventListener() {
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
                callback.onFailure(error.toException());
            }
        });
    }

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

        for (UserData participant : participants) {
            if (participant != null && participant.getEmail() != null && !participant.getEmail().isEmpty()) {
                initializeUserData(participant);
            }
        }

        List<String> participantIds = extractParticipantIds(participants);

        if (!currentUserId.isEmpty() && !participantIds.contains(currentUserId)) {
            participantIds.add(currentUserId);
        }

        createOrGetGroupChat(taskId, taskTitle, participantIds, callback);
    }

    public void createOrGetGroupChat(String taskId, String taskTitle, List<String> participantIds, GroupChatCallback callback) {
        String currentUserId = FirebaseUtils.getCurrentUserId(null);
        if (currentUserId.isEmpty()) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String chatId = CHAT_ID_PREFIX_TASK + taskId;
        DatabaseReference chatRef = database.getReference(PATH_CHATS).child(chatId);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    createNewGroupChat(chatRef, chatId, taskId, taskTitle, participantIds, callback);
                } else {
                    handleExistingGroupChat(chatRef, snapshot, chatId, taskId, taskTitle, participantIds, callback);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    public void sendGroupMessage(String chatId, String messageText, MessageCallback callback) {
        try {
            if (!isValidChatId(chatId)) {
                callback.onFailure(new Exception("Invalid chat ID"));
                return;
            }

            if (isMessageTextEmpty(messageText)) {
                callback.onFailure(new Exception("Message text cannot be empty"));
                return;
            }

            String senderId = FirebaseUtils.getCurrentUserId(null);
            if (senderId.isEmpty()) {
                callback.onFailure(new Exception("User not logged in"));
                return;
            }

            String messageId = generateMessageId(chatId);
            if (messageId == null) {
                callback.onFailure(new Exception("Failed to generate message ID"));
                return;
            }

            Message message = createGroupTextMessage(messageId, senderId, messageText);
            DatabaseReference messageRef = getMessageReference(chatId, messageId);
            messageRef.setValue(message).addOnSuccessListener(aVoid -> {
                try {
                    updateLastMessage(chatId, senderId, messageText, MESSAGE_TYPE_TEXT);
                    updateGroupChatUserChats(chatId, senderId, messageText, System.currentTimeMillis());
                    callback.onSuccess(message);
                } catch (Exception e) {
                    callback.onSuccess(message);
                }
            }).addOnFailureListener(callback::onFailure);
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

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

    public void removeUnreadCountListener(String chatId, String userId, ValueEventListener listener) {
        unreadCountManager.removeUnreadCountListener(chatId, userId, listener);
    }

    public void markGroupChatAsSeen(String chatId, String userId) {
        unreadCountManager.markAsSeen(chatId, userId);
    }

    private void updateLastMessage(String chatId, String senderId, String messageText, String messageType) {
        Map<String, Object> lastMessage = new HashMap<>();
        lastMessage.put(FIELD_MESSAGE, messageText);
        lastMessage.put(FIELD_SENDER_ID, senderId);
        lastMessage.put(FIELD_TIMESTAMP, System.currentTimeMillis());
        lastMessage.put(FIELD_MESSAGE_TYPE, messageType);

        database.getReference(PATH_CHATS).child(chatId).child(PATH_LAST_MESSAGE).setValue(lastMessage);
    }

    private void updateUserChats(String chatId, String senderId, String receiverId, String lastMessage, long timestamp) {
        updateUserChatEntry(chatId, senderId, receiverId, lastMessage, timestamp, 0);
        updateUserChatEntry(chatId, receiverId, senderId, lastMessage, timestamp, 1);
    }

    private void updateUserChatEntry(String chatId, String userId, String otherUserId, String lastMessage, long timestamp, int unreadIncrement) {
        if (!isValidUserChatEntryParams(chatId, userId, otherUserId)) {
            return;
        }

        try {
            DatabaseReference userChatRef = database.getReference(PATH_USER_CHATS).child(userId).child(chatId);
            userChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        int unreadCountValue = getUnreadCountFromSnapshot(snapshot, unreadIncrement);
                        final int finalUnreadCount = unreadCountValue;

                        getUserById(otherUserId, new UserCallback() {
                            @Override
                            public void onSuccess(ChatUser user) {
                                try {
                                    if (user == null) {
                                        Map<String, Object> chatData = createUserChatData(
                                                chatId, otherUserId, DEFAULT_USER_NAME, EMPTY_STRING,
                                                lastMessage, timestamp, finalUnreadCount);
                                        userChatRef.setValue(chatData);
                                        return;
                                    }

                                    String userName = user.getUsername() != null ? user.getUsername() : DEFAULT_USER_NAME;
                                    String userEmail = user.getEmail() != null ? user.getEmail() : EMPTY_STRING;
                                    Map<String, Object> chatData = createUserChatData(
                                            chatId, otherUserId, userName, userEmail,
                                            lastMessage, timestamp, finalUnreadCount);
                                    userChatRef.setValue(chatData);
                                } catch (Exception e) {
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                try {
                                    Map<String, Object> chatData = createUserChatData(
                                            chatId, otherUserId, DEFAULT_USER_NAME, EMPTY_STRING,
                                            lastMessage, timestamp, finalUnreadCount);
                                    userChatRef.setValue(chatData);
                                } catch (Exception ex) {
                                }
                            }
                        });
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });
        } catch (Exception e) {
        }
    }

    private void updateGroupChatUserChats(String chatId, String senderId, String lastMessage, long timestamp) {
        if (!isValidChatId(chatId) || !isValidUserId(senderId)) {
            return;
        }

        DatabaseReference chatRef = database.getReference(PATH_CHATS).child(chatId).child(PATH_PARTICIPANTS);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }

                List<String> participantIds = extractParticipantIds(snapshot);
                if (participantIds.isEmpty()) {
                    return;
                }

                DatabaseReference taskInfoRef = database.getReference(PATH_CHATS).child(chatId);
                taskInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot taskSnapshot) {
                        if (!taskSnapshot.exists()) {
                            return;
                        }

                        String taskId = taskSnapshot.child(FIELD_TASK_ID).getValue(String.class);
                        String taskTitle = taskSnapshot.child(FIELD_TASK_TITLE).getValue(String.class);

                        for (String participantId : participantIds) {
                            int unreadIncrement = participantId.equals(senderId) ? 0 : 1;
                            updateGroupChatUserChatEntry(chatId, participantId, taskId, taskTitle, lastMessage, timestamp, unreadIncrement);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void updateGroupChatUserChatEntry(String chatId, String userId, String taskId, String taskTitle,
                                              String lastMessage, long timestamp, int unreadIncrement) {
        if (chatId == null || userId == null) {
            return;
        }

        if (unreadIncrement <= 0) {
            updateGroupChatUserChatEntryFieldsOnly(chatId, userId, taskId, taskTitle, lastMessage, timestamp);
            return;
        }

        DatabaseReference userChatRef = database.getReference(PATH_USER_CHATS).child(userId).child(chatId);
        database.goOnline();

        userChatRef.get().addOnSuccessListener(snapshot -> {
            try {
                int currentUnreadCount = getUnreadCountFromSnapshot(snapshot);
                int newUnreadCount = Math.max(0, currentUnreadCount + unreadIncrement);
                final int finalUnreadCount = newUnreadCount;

                Map<String, Object> updates = createGroupChatUserChatUpdates(
                        chatId, taskId, taskTitle, lastMessage, timestamp, finalUnreadCount);

                userChatRef.setValue(updates, (error, ref) -> {
                    if (error != null) {
                        Map<String, Object> fallbackUpdates = createUnreadCountFallbackUpdates(
                                finalUnreadCount, lastMessage, timestamp);
                        userChatRef.updateChildren(fallbackUpdates);
                    }
                });
            } catch (Exception e) {
            }
        }).addOnFailureListener(e -> {
            Map<String, Object> updates = createGroupChatUserChatUpdates(
                    chatId, taskId, taskTitle, lastMessage, timestamp, unreadIncrement);
            userChatRef.setValue(updates);
        });
    }

    private void updateGroupChatUserChatEntryFieldsOnly(String chatId, String userId, String taskId, String taskTitle,
                                                        String lastMessage, long timestamp) {
        try {
            DatabaseReference userChatRef = database.getReference(PATH_USER_CHATS).child(userId).child(chatId);
            Map<String, Object> updates = createGroupChatUserChatFieldsOnly(
                    chatId, taskId, taskTitle, lastMessage, timestamp);
            userChatRef.updateChildren(updates);
        } catch (Exception e) {
        }
    }

    private void createNewGroupChat(DatabaseReference chatRef, String chatId, String taskId, String taskTitle,
                                    List<String> participantIds, GroupChatCallback callback) {
        Map<String, Object> chatData = createGroupChatData(taskId, taskTitle, participantIds);

        chatRef.setValue(chatData).addOnSuccessListener(aVoid -> {
            storeTaskChatMapping(taskId, chatId, taskTitle);
            initializeUserChatsForParticipants(chatId, taskId, taskTitle, participantIds);
            Chat chat = createEmptyChatObject(chatId);
            callback.onSuccess(chat);
        }).addOnFailureListener(callback::onFailure);
    }

    private void handleExistingGroupChat(DatabaseReference chatRef, DataSnapshot snapshot, String chatId,
                                         String taskId, String taskTitle, List<String> participantIds,
                                         GroupChatCallback callback) {
        DataSnapshot participantsSnapshot = snapshot.child(PATH_PARTICIPANTS);
        List<String> existingParticipantIds = extractParticipantIds(participantsSnapshot);

        updateChatParticipants(chatRef, participantIds, existingParticipantIds);

        String existingTaskId = snapshot.child(FIELD_TASK_ID).getValue(String.class);
        String existingTaskTitle = snapshot.child(FIELD_TASK_TITLE).getValue(String.class);

        ensureUserChatsEntries(chatId, existingParticipantIds, existingTaskId, existingTaskTitle);

        Chat chat = createEmptyChatObject(chatId);
        callback.onSuccess(chat);
    }

    private List<String> extractParticipantIds(List<UserData> participants) {
        List<String> participantIds = new ArrayList<>();
        for (UserData participant : participants) {
            if (participant != null && participant.getEmail() != null && !participant.getEmail().isEmpty()) {
                String userId = FirebaseUtils.sanitizeEmailForKey(participant.getEmail());
                if (!userId.isEmpty()) {
                    participantIds.add(userId);
                }
            }
        }
        return participantIds;
    }

    private List<String> extractParticipantIds(DataSnapshot snapshot) {
        List<String> participantIds = new ArrayList<>();
        for (DataSnapshot participantSnapshot : snapshot.getChildren()) {
            String participantId = participantSnapshot.getKey();
            if (participantId != null && !participantId.isEmpty()) {
                participantIds.add(participantId);
            }
        }
        return participantIds;
    }

    private void updateChatParticipants(DatabaseReference chatRef, List<String> participantIds,
                                         List<String> existingParticipantIds) {
        boolean hasNewParticipants = false;
        Map<String, Object> participantsUpdate = new HashMap<>();
        for (String participantId : participantIds) {
            if (participantId != null && !participantId.isEmpty() && !existingParticipantIds.contains(participantId)) {
                existingParticipantIds.add(participantId);
                participantsUpdate.put(participantId, true);
                hasNewParticipants = true;
            }
        }

        if (hasNewParticipants) {
            DatabaseReference participantsRef = chatRef.child(PATH_PARTICIPANTS);
            participantsRef.updateChildren(participantsUpdate);
        }
    }

    private void ensureUserChatsEntries(String chatId, List<String> participantIds, String taskId, String taskTitle) {
        for (String participantId : participantIds) {
            if (participantId != null && !participantId.isEmpty()) {
                DatabaseReference userChatRef = database.getReference(PATH_USER_CHATS)
                        .child(participantId).child(chatId);
                userChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot userChatSnapshot) {
                        if (!userChatSnapshot.exists()) {
                            Map<String, Object> userChatData = createGroupChatUserChatData(
                                    chatId, taskId, taskTitle, DEFAULT_UNREAD_COUNT);
                            userChatRef.setValue(userChatData);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }
        }
    }

    private void storeTaskChatMapping(String taskId, String chatId, String taskTitle) {
        Map<String, Object> taskChatMapping = new HashMap<>();
        taskChatMapping.put(FIELD_CHAT_ID, chatId);
        taskChatMapping.put(FIELD_TASK_TITLE, taskTitle);
        taskChatMapping.put(FIELD_CREATED_AT, System.currentTimeMillis());
        database.getReference(PATH_TASK_CHATS).child(taskId).setValue(taskChatMapping);
    }

    private void initializeUserChatsForParticipants(String chatId, String taskId, String taskTitle,
                                                      List<String> participantIds) {
        for (String participantId : participantIds) {
            if (participantId != null && !participantId.isEmpty()) {
                DatabaseReference userChatRef = database.getReference(PATH_USER_CHATS)
                        .child(participantId).child(chatId);
                Map<String, Object> userChatData = createGroupChatUserChatData(
                        chatId, taskId, taskTitle, DEFAULT_UNREAD_COUNT);
                userChatRef.setValue(userChatData);
            }
        }
    }

    private boolean isValidChatId(String chatId) {
        return chatId != null && !chatId.isEmpty();
    }

    private boolean isValidUserId(String userId) {
        return userId != null && !userId.isEmpty();
    }

    private boolean isValidUserChatEntryParams(String chatId, String userId, String otherUserId) {
        return chatId != null && userId != null && otherUserId != null;
    }

    private boolean isMessageTextEmpty(String messageText) {
        return messageText == null || messageText.trim().isEmpty();
    }

    private String generateMessageId(String chatId) {
        return database.getReference(PATH_CHATS).child(chatId).child(PATH_MESSAGES).push().getKey();
    }

    private DatabaseReference getMessageReference(String chatId, String messageId) {
        return database.getReference(PATH_CHATS).child(chatId).child(PATH_MESSAGES).child(messageId);
    }

    private Message createTextMessage(String messageId, String senderId, String receiverId, String messageText) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessage(messageText);
        message.setMessageType(MESSAGE_TYPE_TEXT);
        message.setTimestamp(System.currentTimeMillis());
        message.setSeen(false);
        return message;
    }

    private Message createImageMessage(String messageId, String senderId, String receiverId, String imageUrl) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessage(MESSAGE_TEXT_IMAGE);
        message.setMessageType(MESSAGE_TYPE_IMAGE);
        message.setImageUrl(imageUrl);
        message.setTimestamp(System.currentTimeMillis());
        message.setSeen(false);
        return message;
    }

    private Message createFileMessage(String messageId, String senderId, String receiverId,
                                      String fileUrl, String fileName, String fileMessageText) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessage(fileMessageText);
        message.setMessageType(MESSAGE_TYPE_FILE);
        message.setFileUrl(fileUrl);
        message.setFileName(fileName);
        message.setTimestamp(System.currentTimeMillis());
        message.setSeen(false);
        return message;
    }

    private Message createGroupTextMessage(String messageId, String senderId, String messageText) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setMessage(messageText);
        message.setMessageType(MESSAGE_TYPE_TEXT);
        message.setTimestamp(System.currentTimeMillis());
        message.setSeen(false);
        return message;
    }

    private Chat createChatObject(String chatId, String otherUserId, String otherUserName, String otherUserEmail) {
        Chat chat = new Chat();
        chat.setChatId(chatId);
        chat.setOtherUserId(otherUserId);
        chat.setOtherUserName(otherUserName);
        chat.setOtherUserEmail(otherUserEmail);
        chat.setLastMessage(EMPTY_STRING);
        chat.setLastMessageTime(DEFAULT_TIMESTAMP);
        chat.setUnreadCount(DEFAULT_UNREAD_COUNT);
        return chat;
    }

    private Chat createEmptyChatObject(String chatId) {
        Chat chat = new Chat();
        chat.setChatId(chatId);
        chat.setLastMessage(EMPTY_STRING);
        chat.setLastMessageTime(DEFAULT_TIMESTAMP);
        chat.setUnreadCount(DEFAULT_UNREAD_COUNT);
        return chat;
    }

    private Map<String, Object> createGroupChatData(String taskId, String taskTitle, List<String> participantIds) {
        Map<String, Object> chatData = new HashMap<>();
        chatData.put(FIELD_TASK_ID, taskId);
        chatData.put(FIELD_TASK_TITLE, taskTitle);
        chatData.put(FIELD_IS_GROUP_CHAT, true);

        Map<String, Object> participants = new HashMap<>();
        for (String participantId : participantIds) {
            if (participantId != null && !participantId.isEmpty()) {
                participants.put(participantId, true);
            }
        }
        chatData.put(PATH_PARTICIPANTS, participants);
        return chatData;
    }

    private Map<String, Object> createUserChatData(String chatId, String otherUserId, String otherUserName,
                                                    String otherUserEmail, String lastMessage,
                                                    long timestamp, int unreadCount) {
        Map<String, Object> chatData = new HashMap<>();
        chatData.put(FIELD_CHAT_ID, chatId);
        chatData.put(FIELD_OTHER_USER_ID, otherUserId);
        chatData.put(FIELD_OTHER_USER_NAME, otherUserName);
        chatData.put(FIELD_OTHER_USER_EMAIL, otherUserEmail);
        chatData.put(FIELD_LAST_MESSAGE, lastMessage);
        chatData.put(FIELD_LAST_MESSAGE_TIME, timestamp);
        chatData.put(FIELD_UNREAD_COUNT, unreadCount);
        return chatData;
    }

    private Map<String, Object> createGroupChatUserChatData(String chatId, String taskId,
                                                              String taskTitle, int unreadCount) {
        Map<String, Object> userChatData = new HashMap<>();
        userChatData.put(FIELD_CHAT_ID, chatId);
        if (taskId != null) {
            userChatData.put(FIELD_TASK_ID, taskId);
        }
        if (taskTitle != null) {
            userChatData.put(FIELD_TASK_TITLE, taskTitle);
        }
        userChatData.put(FIELD_IS_GROUP_CHAT, true);
        userChatData.put(FIELD_LAST_MESSAGE, EMPTY_STRING);
        userChatData.put(FIELD_LAST_MESSAGE_TIME, DEFAULT_TIMESTAMP);
        userChatData.put(FIELD_UNREAD_COUNT, unreadCount);
        return userChatData;
    }

    private Map<String, Object> createGroupChatUserChatUpdates(String chatId, String taskId, String taskTitle,
                                                                 String lastMessage, long timestamp, int unreadCount) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_CHAT_ID, chatId);
        if (taskId != null) {
            updates.put(FIELD_TASK_ID, taskId);
        }
        if (taskTitle != null) {
            updates.put(FIELD_TASK_TITLE, taskTitle);
        }
        updates.put(FIELD_IS_GROUP_CHAT, true);
        updates.put(FIELD_LAST_MESSAGE, lastMessage != null ? lastMessage : EMPTY_STRING);
        updates.put(FIELD_LAST_MESSAGE_TIME, timestamp);
        updates.put(FIELD_UNREAD_COUNT, unreadCount);
        return updates;
    }

    private Map<String, Object> createUnreadCountFallbackUpdates(int unreadCount, String lastMessage, long timestamp) {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put(FIELD_UNREAD_COUNT, unreadCount);
        updateMap.put(FIELD_LAST_MESSAGE, lastMessage != null ? lastMessage : EMPTY_STRING);
        updateMap.put(FIELD_LAST_MESSAGE_TIME, timestamp);
        return updateMap;
    }

    private Map<String, Object> createGroupChatUserChatFieldsOnly(String chatId, String taskId, String taskTitle,
                                                                    String lastMessage, long timestamp) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_CHAT_ID, chatId);
        if (taskId != null) {
            updates.put(FIELD_TASK_ID, taskId);
        }
        if (taskTitle != null) {
            updates.put(FIELD_TASK_TITLE, taskTitle);
        }
        updates.put(FIELD_IS_GROUP_CHAT, true);
        updates.put(FIELD_LAST_MESSAGE, lastMessage != null ? lastMessage : EMPTY_STRING);
        updates.put(FIELD_LAST_MESSAGE_TIME, timestamp);
        return updates;
    }

    private int getUnreadCountFromSnapshot(DataSnapshot snapshot) {
        if (!snapshot.exists()) {
            return 0;
        }

        Object unreadObj = snapshot.child(FIELD_UNREAD_COUNT).getValue();
        if (unreadObj instanceof Long) {
            return ((Long) unreadObj).intValue();
        } else if (unreadObj instanceof Integer) {
            return (Integer) unreadObj;
        }
        return 0;
    }

    private int getUnreadCountFromSnapshot(DataSnapshot snapshot, int unreadIncrement) {
        int unreadCountValue = 0;
        if (snapshot.exists()) {
            Chat existingChat = snapshot.getValue(Chat.class);
            if (existingChat != null) {
                unreadCountValue = existingChat.getUnreadCount() + unreadIncrement;
            }
        }
        return unreadCountValue;
    }

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
