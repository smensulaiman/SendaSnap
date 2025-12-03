package com.sendajapan.sendasnap.utils;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sendajapan.sendasnap.MyApplication;
import com.sendajapan.sendasnap.activities.ChatActivity;
import com.sendajapan.sendasnap.models.Chat;
import com.sendajapan.sendasnap.models.UserData;

import java.util.HashMap;
import java.util.Map;

public class ChatMessageListener {

    private static final String PATH_USER_CHATS = "userChats";
    private static final String PATH_CHATS = "chats";
    private static final String PATH_LAST_MESSAGE = "lastMessage";
    private static final String FIELD_LAST_MESSAGE_TIME = "lastMessageTime";
    private static final String FIELD_SENDER_ID = "senderId";
    private static final long MESSAGE_TIME_LIMIT_MS = 60000;

    private static ValueEventListener chatListener;
    private static DatabaseReference currentChatRef;
    private static final Map<String, Long> lastProcessedTimestamps = new HashMap<>();

    public static void setupChatMessageListener(Context context) {
        if (context == null) {
            return;
        }

        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
        UserData user = prefsManager.getUser();

        if (user == null || user.getEmail() == null) {
            return;
        }

        String userId = FirebaseUtils.sanitizeEmailForKey(user.getEmail());
        if (userId.isEmpty()) {
            return;
        }

        if (currentChatRef != null && chatListener != null) {
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userChatsRef = database.getReference(PATH_USER_CHATS).child(userId);

        chatListener = createChatMessageListener(context, userId);
        userChatsRef.addValueEventListener(chatListener);
        currentChatRef = userChatsRef;
    }

    public static void removeChatMessageListener() {
        removeExistingListener();
    }

    private static void removeExistingListener() {
        if (currentChatRef != null && chatListener != null) {
            currentChatRef.removeEventListener(chatListener);
            currentChatRef = null;
            chatListener = null;
        }
    }

    public static void clearProcessedTimestamps() {
        lastProcessedTimestamps.clear();
    }

    private static ValueEventListener createChatMessageListener(Context context, String currentUserId) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }

                long currentTime = System.currentTimeMillis();
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    processChatMessage(context, chatSnapshot, currentUserId, currentTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        };
    }

    private static void processChatMessage(Context context, DataSnapshot chatSnapshot,
                                          String currentUserId, long currentTime) {
        try {
            Chat chat = chatSnapshot.getValue(Chat.class);
            if (chat == null) {
                return;
            }

            String chatId = chat.getChatId();
            if (chatId == null || chatId.isEmpty()) {
                return;
            }

            long lastMessageTime = chat.getLastMessageTime();
            if (lastMessageTime <= 0) {
                return;
            }

            String lastProcessedKey = chatId;
            Long lastProcessedTime = lastProcessedTimestamps.get(lastProcessedKey);

            if (lastProcessedTime != null && lastMessageTime <= lastProcessedTime) {
                return;
            }

            long timeDiff = currentTime - lastMessageTime;
            if (timeDiff > MESSAGE_TIME_LIMIT_MS || timeDiff < 0) {
                if (lastProcessedTime == null) {
                    lastProcessedTimestamps.put(lastProcessedKey, lastMessageTime);
                }
                return;
            }

            checkAndTriggerFeedback(context, chatId, currentUserId, lastMessageTime, lastProcessedKey);
        } catch (Exception e) {
        }
    }

    private static void checkAndTriggerFeedback(Context context, String chatId, String currentUserId,
                                               long messageTimestamp, String lastProcessedKey) {
        Long lastProcessedTime = lastProcessedTimestamps.get(lastProcessedKey);
        if (lastProcessedTime != null && messageTimestamp <= lastProcessedTime) {
            return;
        }

        MyApplication app = MyApplication.getInstance();
        if (app != null && app.getCurrentActivity() instanceof ChatActivity) {
            ChatActivity chatActivity = (ChatActivity) app.getCurrentActivity();
            String currentChatId = chatActivity.getChatId();
            if (currentChatId != null && currentChatId.equals(chatId)) {
                lastProcessedTimestamps.put(lastProcessedKey, messageTimestamp);
                return;
            }
        }

        lastProcessedTimestamps.put(lastProcessedKey, messageTimestamp);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference lastMessageRef = database.getReference(PATH_CHATS)
                .child(chatId)
                .child(PATH_LAST_MESSAGE);

        lastMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }

                String senderId = snapshot.child(FIELD_SENDER_ID).getValue(String.class);
                if (senderId == null || senderId.equals(currentUserId)) {
                    return;
                }

                MyApplication app = MyApplication.getInstance();
                if (app != null && app.getCurrentActivity() instanceof ChatActivity) {
                    ChatActivity chatActivity = (ChatActivity) app.getCurrentActivity();
                    String currentChatId = chatActivity.getChatId();
                    if (currentChatId != null && currentChatId.equals(chatId)) {
                        return;
                    }
                }

                SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(context);
                if (prefsManager.isHapticEnabled()) {
                    HapticFeedbackHelper hapticHelper = HapticFeedbackHelper.getInstance(context);
                    hapticHelper.vibrateNotification();
                }

                SoundHelper.playNotificationSound(context);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }
}

