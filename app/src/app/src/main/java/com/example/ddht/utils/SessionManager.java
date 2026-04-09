package com.example.ddht.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences sharedPreferences;
    private static final String KEY_USER_ID = "user_id";

    public SessionManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String accessToken, String role, String fullName) {
        saveSession(accessToken, role, fullName, null);
    }

    public void saveSession(String accessToken, String role, String fullName, String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit()
                .putString(Constants.KEY_ACCESS_TOKEN, accessToken)
                .putString(Constants.KEY_USER_ROLE, role)
                .putString(Constants.KEY_USER_NAME, fullName);
        if (userId != null) {
            editor.putString(KEY_USER_ID, userId);
        }
        editor.apply();
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

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public void saveName(String fullName) {
        sharedPreferences.edit().putString(Constants.KEY_USER_NAME, fullName).apply();
    }

    public boolean isLoggedIn() {
        String token = getAccessToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clearSession() {
        sharedPreferences.edit().clear().apply();
    }
}
