package com.trelloclone.trello.auth;

public class UserResponse {
    private String email;
    private String userName;

    public UserResponse(String email, String userName) {
        this.email = email;
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }
}
