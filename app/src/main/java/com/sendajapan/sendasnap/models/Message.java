package com.sendajapan.sendasnap.models;

public class Message {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String message;
    private String messageType; // text, image, file
    private String imageUrl;
    private String fileUrl;
    private String fileName;
    private long timestamp;
    private boolean isSeen;
    private long seenAt;

    public Message() {
        this.isSeen = false;
        this.timestamp = System.currentTimeMillis();
    }

    public Message(String messageId, String senderId, String receiverId, String message, String messageType) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.messageType = messageType;
        this.isSeen = false;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public long getSeenAt() {
        return seenAt;
    }

    public void setSeenAt(long seenAt) {
        this.seenAt = seenAt;
    }
}

