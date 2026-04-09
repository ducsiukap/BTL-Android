package com.example.ddht.data.remote.dto;

public class UpdateUserRequest {
    private final String fullName;
    private final String email;

    public UpdateUserRequest(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
}
