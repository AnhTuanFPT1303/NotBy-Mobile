package com.example.notby.data.model;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("status")
    private boolean status;

    @SerializedName("path")
    private String path;

    @SerializedName("message")
    private String message;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("data")
    private T data;

    @SerializedName("timestamp")
    private String timestamp;

    // Getters
    public boolean isStatus() { return status; }
    public String getPath() { return path; }
    public String getMessage() { return message; }
    public int getStatusCode() { return statusCode; }
    public T getData() { return data; }
    public String getTimestamp() { return timestamp; }
}
