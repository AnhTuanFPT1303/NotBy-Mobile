package com.example.notby.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DiaryEntry {

    @SerializedName("_id")
    private String id;
    private String title;
    private String content;
    private String category;
    private String childId;
    private List<String> imageUrls;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public String getChildId() {
        return childId;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
