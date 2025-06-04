package com.example.cs304project.service;

import com.example.cs304project.entity.CodeRoom;
import com.example.cs304project.model.CodeMessage;

import java.util.List;

public interface CodeRoomService {
    CodeRoom createRoom(String name, String language, Long ownerId);
    CodeRoom getRoom(Long roomId);
    List<CodeRoom> getUserRooms(Long userId);
    List<CodeRoom> getAllRooms();
    CodeRoom updateCode(Long roomId, String newCode);
    boolean addMember(Long roomId, Long userId);
    boolean removeMember(Long roomId, Long userId);
    void deleteRoom(Long roomId);
    void sendMessage(CodeMessage message);
} 