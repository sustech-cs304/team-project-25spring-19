package com.example.cs304project.service.impl;

import com.example.cs304project.entity.CodeRoom;
import com.example.cs304project.entity.User;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.model.CodeMessage;
import com.example.cs304project.repository.CodeRoomRepository;
import com.example.cs304project.repository.UserRepository;
import com.example.cs304project.service.CodeRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CodeRoomServiceImpl implements CodeRoomService {

    private final CodeRoomRepository codeRoomRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public CodeRoomServiceImpl(CodeRoomRepository codeRoomRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.codeRoomRepository = codeRoomRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public CodeRoom createRoom(String name, String language, Long ownerId) {
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + ownerId));
        
        CodeRoom room = new CodeRoom();
        room.setName(name);
        room.setLanguage(language);
        room.setOwner(owner);
        room.setCurrentCode("// 在这里编写代码");
        room.getMembers().add(owner);
        
        return codeRoomRepository.save(room);
    }

    @Override
    public CodeRoom getRoom(Long roomId) {
        return codeRoomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Code room not found with id: " + roomId));
    }

    @Override
    public List<CodeRoom> getUserRooms(Long userId) {
        return codeRoomRepository.findAllByUserId(userId);
    }

    @Override
    public List<CodeRoom> getAllRooms() {
        return codeRoomRepository.findAll();
    }

    @Override
    @Transactional
    public CodeRoom updateCode(Long roomId, String newCode) {
        CodeRoom room = getRoom(roomId);
        room.setCurrentCode(newCode);
        return codeRoomRepository.save(room);
    }

    @Override
    @Transactional
    public boolean addMember(Long roomId, Long userId) {
        CodeRoom room = getRoom(roomId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return room.getMembers().add(user);
    }

    @Override
    @Transactional
    public boolean removeMember(Long roomId, Long userId) {
        CodeRoom room = getRoom(roomId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return room.getMembers().remove(user);
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        CodeRoom room = getRoom(roomId);
        codeRoomRepository.delete(room);
    }

    @Override
    public void sendMessage(CodeMessage message) {
        // 发送消息到指定的房间
        messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), message);
    }
} 