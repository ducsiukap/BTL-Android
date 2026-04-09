package com.example.ddht.data.remote.dto;

public class LoginResponseData {
    private String id;
    private String fullName;
    private String role;
    private String accessToken;
    private String refreshToken;

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
