package com.sendajapan.sendasnap.models;

public class Chat {
    private String chatId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserEmail;
    private String lastMessage;
    private long lastMessageTime;
    private int unreadCount;
    private String otherUserProfileImageUrl;

    public Chat() {
    }

    public Chat(String chatId, String otherUserId, String otherUserName, String otherUserEmail,
                String lastMessage, long lastMessageTime, int unreadCount) {
        this.chatId = chatId;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.otherUserEmail = otherUserEmail;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserEmail() {
        return otherUserEmail;
    }

    public void setOtherUserEmail(String otherUserEmail) {
        this.otherUserEmail = otherUserEmail;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getOtherUserProfileImageUrl() {
        return otherUserProfileImageUrl;
    }

    public void setOtherUserProfileImageUrl(String otherUserProfileImageUrl) {
        this.otherUserProfileImageUrl = otherUserProfileImageUrl;
    }
}

