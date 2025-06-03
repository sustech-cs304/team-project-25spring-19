package com.example.cs304project.controller;

import com.example.cs304project.entity.CodeRoom;
import com.example.cs304project.entity.User;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.UserRepository;
import com.example.cs304project.service.CodeRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class CodeRoomController {

    private final CodeRoomService codeRoomService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(CodeRoomController.class);

    @Autowired
    public CodeRoomController(CodeRoomService codeRoomService, UserRepository userRepository) {
        this.codeRoomService = codeRoomService;
        this.userRepository = userRepository;
    }
    
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestParam String name, 
                                     @RequestParam String language,
                                     @RequestParam Long userId) {
        try {
            logger.info("Creating room with name: {}, language: {}, userId: {}", name, language, userId);
            
            // 验证用户存在
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                logger.error("创建房间失败: 用户ID {} 不存在", userId);
                Map<String, String> response = new HashMap<>();
                response.put("error", "用户不存在");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            CodeRoom room = codeRoomService.createRoom(name, language, userId);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            logger.error("Error creating room: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoom(@PathVariable Long roomId) {
        try {
            logger.info("Getting room with id: {}", roomId);
            CodeRoom room = codeRoomService.getRoom(roomId);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            logger.error("Error getting room: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserRooms(@PathVariable Long userId) {
        try {
            logger.info("Getting rooms for user: {}", userId);
            
            // 验证用户存在
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                logger.error("获取用户房间失败: 用户ID {} 不存在", userId);
                return ResponseEntity.ok(List.of()); // 返回空列表而不是错误
            }
            
            List<CodeRoom> rooms = codeRoomService.getUserRooms(userId);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            logger.error("Error getting user rooms: {}", e.getMessage(), e);
            // 出错时返回空列表，而不是错误
            return ResponseEntity.ok(List.of());
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllRooms() {
        try {
            logger.info("Getting all rooms");
            List<CodeRoom> rooms = codeRoomService.getAllRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            logger.error("Error getting all rooms: {}", e.getMessage(), e);
            // 出错时返回空列表，而不是错误
            return ResponseEntity.ok(List.of());
        }
    }
    
    @PutMapping("/{roomId}/code")
    public ResponseEntity<?> updateCode(@PathVariable Long roomId, @RequestBody Map<String, String> payload) {
        try {
            String newCode = payload.get("code");
            logger.info("Updating code for room: {}", roomId);
            CodeRoom room = codeRoomService.updateCode(roomId, newCode);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            logger.error("Error updating code: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/{roomId}/members/{userId}")
    public ResponseEntity<?> addMember(@PathVariable Long roomId, @PathVariable Long userId) {
        try {
            logger.info("Adding member {} to room: {}", userId, roomId);
            boolean added = codeRoomService.addMember(roomId, userId);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", added);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error adding member: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/{roomId}/members/{userId}")
    public ResponseEntity<?> removeMember(@PathVariable Long roomId, @PathVariable Long userId) {
        try {
            logger.info("Removing member {} from room: {}", userId, roomId);
            boolean removed = codeRoomService.removeMember(roomId, userId);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", removed);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error removing member: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId) {
        try {
            logger.info("Deleting room: {}", roomId);
            codeRoomService.deleteRoom(roomId);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("deleted", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting room: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 