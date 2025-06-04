package com.example.cs304project.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class CodeMessage {
    private String roomId;
    private String senderId;
    private String senderName;
    private String content;
    private String language;
    private String timestamp;
    private MessageType type;
    
    public enum MessageType {
        CODE, // 代码消息
        JOIN, // 加入房间
        LEAVE, // 离开房间
        CHAT  // 普通编辑消息
    }

    public CodeMessage(String roomId, String senderId, String senderName, String content, String language, String timestamp, MessageType type) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.language = language;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}