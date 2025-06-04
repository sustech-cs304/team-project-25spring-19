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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    public boolean deleteRoom(Long roomId, Long userId) {
        CodeRoom room = getRoom(roomId);
        
        // 只有房间创建者可以删除房间
        if (!room.getOwner().getUserId().equals(userId)) {
            return false;
        }
        
        codeRoomRepository.delete(room);
        return true;
    }

    @Override
    public void sendMessage(CodeMessage message) {
        // 发送消息到指定的房间
        messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), message);
    }

    @Override
    public String runCode(String code, String language) {
        try {
            return executeCode(code, language);
        } catch (Exception e) {
            return "运行错误: " + e.getMessage();
        }
    }

    private String executeCode(String code, String language) throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir");
        String sessionId = UUID.randomUUID().toString();
        Path workDir = Paths.get(tempDir, "code_execution", sessionId);
        Files.createDirectories(workDir);

        try {
            String output;
            switch (language.toLowerCase()) {
                case "python":
                    output = executePython(code, workDir);
                    break;
                case "java":
                    output = executeJava(code, workDir);
                    break;
                case "javascript":
                    output = executeJavaScript(code, workDir);
                    break;
                case "c++":
                case "cpp":
                    output = executeCpp(code, workDir);
                    break;
                default:
                    output = "不支持的编程语言: " + language;
            }
            return output;
        } finally {
            // 清理临时文件
            deleteDirectory(workDir.toFile());
        }
    }

    private String executePython(String code, Path workDir) throws Exception {
        Path scriptFile = workDir.resolve("script.py");
        Files.write(scriptFile, code.getBytes());

        ProcessBuilder pb = new ProcessBuilder("python", scriptFile.toString());
        pb.directory(workDir.toFile());
        return runProcess(pb);
    }

    private String executeJava(String code, Path workDir) throws Exception {
        // 简单的Java代码执行，假设类名为Main
        String javaCode = code;
        if (!code.contains("class")) {
            javaCode = "public class Main {\n    public static void main(String[] args) {\n        " + code + "\n    }\n}";
        }

        Path javaFile = workDir.resolve("Main.java");
        Files.write(javaFile, javaCode.getBytes());

        // 编译
        ProcessBuilder compilePb = new ProcessBuilder("javac", "Main.java");
        compilePb.directory(workDir.toFile());
        String compileResult = runProcess(compilePb);
        
        if (!compileResult.isEmpty()) {
            return "编译错误:\n" + compileResult;
        }

        // 运行
        ProcessBuilder runPb = new ProcessBuilder("java", "Main");
        runPb.directory(workDir.toFile());
        return runProcess(runPb);
    }

    private String executeJavaScript(String code, Path workDir) throws Exception {
        Path scriptFile = workDir.resolve("script.js");
        Files.write(scriptFile, code.getBytes());

        ProcessBuilder pb = new ProcessBuilder("node", scriptFile.toString());
        pb.directory(workDir.toFile());
        return runProcess(pb);
    }

    private String executeCpp(String code, Path workDir) throws Exception {
        Path cppFile = workDir.resolve("main.cpp");
        Files.write(cppFile, code.getBytes());

        // 编译
        ProcessBuilder compilePb = new ProcessBuilder("g++", "-o", "main", "main.cpp");
        compilePb.directory(workDir.toFile());
        String compileResult = runProcess(compilePb);
        
        if (!compileResult.isEmpty()) {
            return "编译错误:\n" + compileResult;
        }

        // 运行
        ProcessBuilder runPb = new ProcessBuilder("./main");
        runPb.directory(workDir.toFile());
        return runProcess(runPb);
    }

    private String runProcess(ProcessBuilder pb) throws Exception {
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 设置超时时间为10秒
        boolean finished = process.waitFor(10, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return "代码执行超时";
        }

        // 读取输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        String result = output.toString().trim();
        return result.isEmpty() ? "程序执行完成，无输出" : result;
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
} 