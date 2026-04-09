package com.example.ddht.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String accessToken, String role, String fullName) {
        sharedPreferences.edit()
                .putString(Constants.KEY_ACCESS_TOKEN, accessToken)
                .putString(Constants.KEY_USER_ROLE, role)
                .putString(Constants.KEY_USER_NAME, fullName)
                .apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(Constants.KEY_ACCESS_TOKEN, null);
    }

    public String getUserRole() {
        return sharedPreferences.getString(Constants.KEY_USER_ROLE, null);
    }

    public String getUserName() {
        return sharedPreferences.getString(Constants.KEY_USER_NAME, null);
    }

    public boolean isLoggedIn() {
        String token = getAccessToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clearSession() {
        sharedPreferences.edit().clear().apply();
    }
}
