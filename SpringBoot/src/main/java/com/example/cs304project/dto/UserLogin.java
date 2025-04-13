package com.example.cs304project.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


public class UserLogin {

    private String identifier; // 用户名或邮箱
    private String password;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
