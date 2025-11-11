package com.example.notby.data;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "NotByPrefs";
    private static final String KEY_JWT_TOKEN = "jwt_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_CHILD_ID = "child_id";
    private static final String KEY_PARENT_ID = "parent_id";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveChildId(String childId) {
        editor.putString(KEY_CHILD_ID, childId);
        editor.apply();
    }

    public String getChildId() {
        return sharedPreferences.getString(KEY_CHILD_ID, null);
    }

    public void saveToken(String token) {
        editor.putString(KEY_JWT_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_JWT_TOKEN, null);
    }

    public void saveUserData(String parentId, String email, String name) {
        editor.putString(KEY_PARENT_ID, parentId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_PARENT_ID, null);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
