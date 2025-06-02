package com.example.cs304project.controller;


import com.example.cs304project.dto.UserDTO;
import com.example.cs304project.dto.UserLogin;
import com.example.cs304project.dto.UserPasswordDTO;
import com.example.cs304project.dto.UserRegister;
import com.example.cs304project.entity.User;
import com.example.cs304project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;
    //post /api/users/register 注册新用户
    @PostMapping("/register")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserRegister register) {

        User user = userService.createUser(register);
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setUserName(user.getUserName());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());
        userDTO.setProfile(user.getProfile());

        return ResponseEntity.ok(userDTO);
    }
    //post   /api/users/login           用户登录
    @PostMapping("/login")
    public ResponseEntity<UserDTO> loginUser(@RequestBody UserLogin login){
        User user = userService.loginUser(login);
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setUserName(user.getUserName());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());
        userDTO.setProfile(user.getProfile());

        return ResponseEntity.ok(userDTO);
    }

    //put /api/users/{userId}/update 更改用户信息
    @PutMapping("/{userId}/update")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId,
                                              @RequestBody UserDTO userDTO){
        User user = new User();
        user.setUserName(userDTO.getUserName());
        user.setEmail(userDTO.getEmail());
        user.setProfile(userDTO.getProfile());

        User newUser = userService.updateUser(user,userId);
        UserDTO newDTO = new UserDTO(newUser.getUserId(), newUser.getUserName(),newUser.getEmail(),newUser.getRole(),newUser.getProfile());

        return ResponseEntity.ok(newDTO);
    }


    //post /api/users/{userId}/password 更改用户密码
    @PostMapping("/{userId}/password")
    public ResponseEntity<UserDTO> changePassWord(@PathVariable Long userId, @RequestBody UserPasswordDTO userPassword) {

        String oldPassWord = userPassword.getOldPassword();
        String newPassword = userPassword.getNewPassword();

        UserDTO newDTO = new UserDTO();
        User user = userService.changePassword(userId,oldPassWord,newPassword);
        newDTO.setUserId(user.getUserId());
        newDTO.setUserName(user.getUserName());
        newDTO.setEmail(user.getEmail());
        newDTO.setRole(user.getRole());
        newDTO.setProfile(user.getProfile());
        return ResponseEntity.ok(newDTO);
    }

    //get /api/users 获取所有用户
    /*
    * 使用chatGPT o1模型
    * 指令：如何获取数据库中所有User信息并返回UserDTO
    * 方式：仿照代码
    * */
    @GetMapping("/getAllUsers")
    //get /api/users/getAllUsers
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(user -> new UserDTO(user.getUserId(), user.getUserName(), user.getEmail(), user.getRole(), user.getRole()))
                .collect(Collectors.toList());
    }

    //get /api/users/{userId} 根据id获取用户信息
    @GetMapping("/{userId}/getById")
    public UserDTO getUserById(@PathVariable("userId") Long id) {
        User user = userService.getUserById(id);
        UserDTO newDTO = new UserDTO();
        newDTO.setUserId(user.getUserId());
        newDTO.setUserName(user.getUserName());
        newDTO.setEmail(user.getEmail());
        newDTO.setRole(user.getRole());
        newDTO.setProfile(user.getProfile());
        return newDTO;
    }

    //delete /api/users/{userId} 注销账号
    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<String> deleteUser(@PathVariable("userId")Long userID){
        userService.deleteUser(userID);
        return ResponseEntity.ok("用户注销成功");
    }

}
