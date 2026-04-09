package com.example.ddht.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateUserRequest {
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("email")
    private String email;

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
