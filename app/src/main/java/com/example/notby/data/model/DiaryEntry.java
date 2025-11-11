package com.example.notby.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class DiaryEntry {

    @SerializedName("_id")
    private String id;
    private String title;
    private String content;
    private String category;
    private Object childId;
    private List<MediaFile> imageUrls = new ArrayList<>();

    @SerializedName("created_at")
    private String createdAt;

    // Getters ...
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public List<MediaFile> getImageUrls() { return imageUrls; }
    public String getCreatedAt() { return createdAt; }

    // Smart Getters
    public String getChildIdString() {
        if (childId == null) return null;
        if (childId instanceof String) {
            return (String) childId;
        }
        if (childId instanceof com.google.gson.internal.LinkedTreeMap) {
            try {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) childId;
                if (map.containsKey("_id")) {
                    return (String) map.get("_id");
                }
            } catch (Exception e) { return null; }
        }
        return null;
    }

    public String getChildName() {
        if (childId == null) return null;
        if (childId instanceof com.google.gson.internal.LinkedTreeMap) {
            try {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) childId;
                if (map.containsKey("firstName")) {
                    return (String) map.get("firstName");
                }
            } catch (Exception e) { return null; }
        }
        // Fallback to ID if it's just a string
        if (childId instanceof String) {
            return (String) childId;
        }
        return null;
    }

    // Setters ...
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setCategory(String category) { this.category = category; }
    public void setChildId(Object childId) { this.childId = childId; }
    public void setImageUrls(List<MediaFile> imageUrls) { this.imageUrls = imageUrls; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
