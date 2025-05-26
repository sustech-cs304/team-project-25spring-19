package com.example.cs304project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {


    /**
     * 定义一个 PasswordEncoder Bean，使用 BCrypt 算法进行密码加密。
     * 这里可以根据需要传入加密强度参数，默认值为10。
     *
     * @return 一个 BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
/*
* 使用ChatGPT生成，版本：o3_mini_high
* 指令：如何在项目中配置 PasswordEncoder
* 方式：复制代码
* */