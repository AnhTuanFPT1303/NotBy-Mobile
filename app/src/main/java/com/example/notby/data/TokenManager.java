package com.example.notby.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import org.json.JSONObject;

public class TokenManager {
    private static final String PREF_NAME = "NotByPrefs";
    private static final String KEY_JWT_TOKEN = "jwt_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_CHILD_ID = "child_id";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveToken(String token) {
        editor.putString(KEY_JWT_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_JWT_TOKEN, null);
    }

    public void saveUserData(String userId, String email, String name) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    /**
     * Extract user ID from JWT token
     * @return user ID from token or null if extraction fails
     */
    public String getUserIdFromToken() {
        String token = getToken();
        if (token == null || token.isEmpty()) {
            return null;
        }

        try {
            // JWT token has 3 parts separated by dots: header.payload.signature
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                return null;
            }

            // Decode the payload (second part)
            String payload = tokenParts[1];

            // Add padding if needed for Base64 decoding
            while (payload.length() % 4 != 0) {
                payload += "=";
            }

            // Decode Base64
            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String decodedPayload = new String(decodedBytes);

            // Parse JSON
            JSONObject payloadJson = new JSONObject(decodedPayload);

            // Try common user ID field names
            String userId = null;
            if (payloadJson.has("userId")) {
                userId = payloadJson.getString("userId");
            } else if (payloadJson.has("user_id")) {
                userId = payloadJson.getString("user_id");
            } else if (payloadJson.has("id")) {
                userId = payloadJson.getString("id");
            } else if (payloadJson.has("sub")) {
                userId = payloadJson.getString("sub");
            }

            return userId;

        } catch (Exception e) {
            return null;
        }
    }

    public void saveChildId(String childId) {
        editor.putString(KEY_CHILD_ID, childId);
        editor.apply();
    }

    public String getChildId() {
        return sharedPreferences.getString(KEY_CHILD_ID, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
