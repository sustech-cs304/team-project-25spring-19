package com.example.cs304project.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
} 