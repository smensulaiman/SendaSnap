package com.sendajapan.sendasnap.services.chat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Manages unread message count tracking and updates.
 * Handles real-time unread count listeners and updates for group chats.
 */
public class UnreadCountManager {
    
    private static final String TAG = "UnreadCountManager";
    private final FirebaseDatabase database;
    
    public UnreadCountManager(FirebaseDatabase database) {
        this.database = database;
    }
    
    /**
     * Get current unread count for a group chat (one-time read).
     * 
     * @param chatId Chat ID
     * @param userId User ID
     * @param callback Callback with unread count
     */
    public void getUnreadCount(String chatId, String userId, UnreadCountCallback callback) {
        DatabaseReference unreadRef = database.getReference("userChats")
            .child(userId)
            .child(chatId)
            .child("unreadCount");
            
        unreadRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                callback.onFailure(error.toException());
            }
        });
    }
    
    /**
     * Add persistent listener for unread count updates.
     * This listener fires on every change to the unread count.
     * 
     * @param chatId Chat ID
     * @param userId User ID
     * @param callback Callback that fires on every unread count change
     * @return ValueEventListener that can be removed later
     */
    public ValueEventListener addUnreadCountListener(String chatId, String userId, UnreadCountCallback callback) {
        // Listen to the parent node to catch updates even if unreadCount doesn't exist initially
        DatabaseReference userChatRef = database.getReference("userChats")
            .child(userId)
            .child(chatId);
            
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int unreadCount = 0;
                if (snapshot.exists()) {
                    DataSnapshot unreadSnapshot = snapshot.child("unreadCount");
                    if (unreadSnapshot.exists() && unreadSnapshot.getValue() != null) {
                        Object value = unreadSnapshot.getValue();
                        if (value instanceof Long) {
                            unreadCount = ((Long) value).intValue();
                        } else if (value instanceof Integer) {
                            unreadCount = (Integer) value;
                        }
                    }
                }
                callback.onSuccess(unreadCount);
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.toException());
            }
        };
        
        userChatRef.addValueEventListener(listener);
        return listener;
    }
    
    /**
     * Remove unread count listener.
     * 
     * @param chatId Chat ID
     * @param userId User ID
     * @param listener Listener to remove
     */
    public void removeUnreadCountListener(String chatId, String userId, ValueEventListener listener) {
        if (listener == null) {
            return;
        }
        
        DatabaseReference userChatRef = database.getReference("userChats")
            .child(userId)
            .child(chatId);
            
        userChatRef.removeEventListener(listener);
    }
    
    /**
     * Mark group chat as seen (reset unread count to 0).
     * 
     * @param chatId Chat ID
     * @param userId User ID
     */
    public void markAsSeen(String chatId, String userId) {
        DatabaseReference unreadRef = database.getReference("userChats")
            .child(userId)
            .child(chatId)
            .child("unreadCount");
            
        unreadRef.setValue(0)
            .addOnSuccessListener(aVoid -> {
            })
            .addOnFailureListener(e -> {
            });
    }
    
    /**
     * Callback interface for unread count operations.
     */
    public interface UnreadCountCallback {
        void onSuccess(int unreadCount);
        void onFailure(Exception e);
    }
}

