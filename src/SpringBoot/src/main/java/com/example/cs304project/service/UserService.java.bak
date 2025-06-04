package com.example.cs304project.service;


import com.example.cs304project.dto.UserLogin;
import com.example.cs304project.dto.UserRegister;
import com.example.cs304project.entity.User;
import com.example.cs304project.exception.InvalidRequestException;
import com.example.cs304project.exception.ResourceNotFoundException;
import com.example.cs304project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    //注册新用户，设置初始信息
    public User createUser(UserRegister register) {

        if (userRepository.findUserByUserName(register.getUserName()) != null){
            throw new InvalidRequestException("用户名已存在");
        }
        if (userRepository.findUserByEmail(register.getEmail()) != null){
            throw new InvalidRequestException("邮箱已被注册");
        }

        User newUser = new User();
        newUser.setUserName(register.getUserName());
        newUser.setEmail(register.getEmail());
        newUser.setPassword(passwordEncoder.encode(register.getPassword()));
        newUser.setRole(register.getRole());
        newUser.setProfile(register.getProfile());

        return userRepository.save(newUser);
    }

    //登录认证，可使用邮箱或用户名+密码
    public User loginUser(UserLogin login){
        User user = userRepository.findUserByUserName(login.getIdentifier());

        if (user == null){
            user = userRepository.findUserByEmail(login.getIdentifier());
        }
        if (user == null) throw new InvalidRequestException("用户名或邮箱不存在");

        if (!passwordEncoder.matches(login.getPassword(),user.getPassword())){
            throw new InvalidRequestException("密码错误");
        }

        return user;
    }

    //自定义个人资料，更新用户信息
    public User updateUser(User user,Long userId){

        User isUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户" + user.getUserId() + "不存在"));
        isUser.setUserName(user.getUserName());
        isUser.setEmail(user.getEmail());
        isUser.setProfile(user.getProfile());
        return userRepository.save(isUser);
    }
    //修改密码
    public User changePassword(Long userId, String oldPassWord, String newPassWord ){

        User isUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户" + userId + "不存在"));

        if(!passwordEncoder.matches(oldPassWord, isUser.getPassword())){
            throw new InvalidRequestException("旧密码错误");
        }

        isUser.setPassword(passwordEncoder.encode(newPassWord));
        return userRepository.save(isUser);
    }

    //根据id查询用户
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户" + userId + "不存在"));
    }

    //列出所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    //注销账号
    public void deleteUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("要注销的用户不存在"));
        userRepository.deleteById(userId);
    }

}
