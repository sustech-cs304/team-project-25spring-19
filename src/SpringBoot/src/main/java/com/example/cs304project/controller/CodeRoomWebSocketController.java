package com.example.cs304project.controller;

import com.example.cs304project.model.CodeMessage;
import com.example.cs304project.service.CodeRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class CodeRoomWebSocketController {

    private final CodeRoomService codeRoomService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public CodeRoomWebSocketController(CodeRoomService codeRoomService) {
        this.codeRoomService = codeRoomService;
    }

    @MessageMapping("/room/{roomId}/code")
    @SendTo("/topic/room/{roomId}")
    public CodeMessage handleCodeMessage(@DestinationVariable String roomId, @Payload CodeMessage message) {
        message.setTimestamp(LocalDateTime.now().format(formatter));
        
        // 如果是代码消息，更新房间的当前代码状态
        if (message.getType() == CodeMessage.MessageType.CODE) {
            codeRoomService.updateCode(Long.valueOf(roomId), message.getContent());
        }
        
        return message;
    }
    
    @MessageMapping("/room/{roomId}/join")
    @SendTo("/topic/room/{roomId}")
    public CodeMessage handleJoinMessage(@DestinationVariable String roomId, @Payload CodeMessage message) {
        message.setType(CodeMessage.MessageType.JOIN);
        message.setTimestamp(LocalDateTime.now().format(formatter));
        message.setContent(message.getSenderName() + " 加入了编辑室");
        
        return message;
    }
    
    @MessageMapping("/room/{roomId}/leave")
    @SendTo("/topic/room/{roomId}")
    public CodeMessage handleLeaveMessage(@DestinationVariable String roomId, @Payload CodeMessage message) {
        message.setType(CodeMessage.MessageType.LEAVE);
        message.setTimestamp(LocalDateTime.now().format(formatter));
        message.setContent(message.getSenderName() + " 离开了编辑室");
        
        return message;
    }
    
    @MessageMapping("/room/{roomId}/chat")
    @SendTo("/topic/room/{roomId}")
    public CodeMessage handleChatMessage(@DestinationVariable String roomId, @Payload CodeMessage message) {
        message.setType(CodeMessage.MessageType.CHAT);
        message.setTimestamp(LocalDateTime.now().format(formatter));
        
        return message;
    }
} 