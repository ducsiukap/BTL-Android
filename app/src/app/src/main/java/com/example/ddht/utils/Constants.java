package com.example.ddht.utils;

public final class Constants {
    private Constants() {
    }

    // Android emulator can access host machine via 10.0.2.2.
    public static final String BASE_URL = "http://10.0.2.2:3333/api/v1/";
    public static final String PREFS_NAME = "ddht_session";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_USER_NAME = "user_name";
}
