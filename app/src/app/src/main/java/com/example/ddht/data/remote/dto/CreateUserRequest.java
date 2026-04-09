package com.example.ddht.data.remote.dto;

public class CreateUserRequest {
    private final String fullName;
    private final String email;
    private final String role;

    public CreateUserRequest(String fullName, String email, String role) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
