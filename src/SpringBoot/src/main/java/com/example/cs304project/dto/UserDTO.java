package com.example.cs304project.dto;

public class UserDTO {

    private Long userId;
    
    private String userName;

    private String email;

    private String role;

    private String profile;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public UserDTO(Long userId, String userName, String email, String role, String profile) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.role = role;
        this.profile = profile;
    }

    public UserDTO() {
    }
}
