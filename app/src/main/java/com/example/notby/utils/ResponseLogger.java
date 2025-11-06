package com.example.notby.utils;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;

public class ResponseLogger {

    public static void logAllFields(String tag, JSONObject json) {
        if (json == null) {
            Log.d(tag, "JSON is null");
            return;
        }

        Log.d(tag, "=== JSON Response Structure ===");
        logRecursive(tag, json, "");
    }

    private static void logRecursive(String tag, JSONObject json, String prefix) {
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                Object value = json.get(key);
                if (value instanceof JSONObject) {
                    Log.d(tag, prefix + key + ": { ... }");
                    logRecursive(tag, (JSONObject) value, prefix + "  ");
                } else {
                    String valueStr = value.toString();
                    // Don't log the full token, just show it exists
                    if (key.toLowerCase().contains("token") && valueStr.length() > 20) {
                        Log.d(tag, prefix + key + ": " + valueStr.substring(0, 20) + "... (length: " + valueStr.length() + ")");
                    } else {
                        Log.d(tag, prefix + key + ": " + valueStr);
                    }
                }
            } catch (JSONException e) {
                Log.e(tag, "Error reading key: " + key);
            }
        }
    }
}

