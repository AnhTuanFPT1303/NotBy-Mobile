package com.example.notby.data.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Article {
    @SerializedName("_id")
    private String id;

    @SerializedName("CategoryId")
    private String categoryId;

    @SerializedName("Title")
    private String title;

    @SerializedName("Content")
    private String content;

    @SerializedName("FileId")
    private String fileId;

    @SerializedName("Author")
    private JsonElement author; // can be String, object, or null

    @SerializedName("Description")
    private String description;

    @SerializedName("File")
    private MediaFile file;

    @SerializedName("Category")
    private Category category;

    @SerializedName("Likes")
    private int likes;

    @SerializedName("Views")
    private int views;

    @SerializedName("Tags")
    private List<String> tags;

    @SerializedName("ReadTime")
    private int readTime;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Getters
    public String getId() { return id; }
    public String getCategoryId() { return categoryId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getFileId() { return fileId; }
    public String getAuthorId() {
        if (author == null || author.isJsonNull()) return null;
        if (author.isJsonPrimitive()) {
            try { return author.getAsString(); } catch (Exception e) { return null; }
        }
        if (author.isJsonObject()) {
            JsonObject obj = author.getAsJsonObject();
            if (obj.has("_id") && !obj.get("_id").isJsonNull()) return obj.get("_id").getAsString();
            if (obj.has("id") && !obj.get("id").isJsonNull()) return obj.get("id").getAsString();
        }
        return null;
    }
    public String getDescription() { return description; }
    public MediaFile getFile() { return file; }
    public Category getCategory() { return category; }
    public int getLikes() { return likes; }
    public int getViews() { return views; }
    public List<String> getTags() { return tags; }
    public int getReadTime() { return readTime; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public JsonElement getAuthor() { return author; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public void setAuthor(JsonElement author) { this.author = author; }
    public void setDescription(String description) { this.description = description; }
    public void setFile(MediaFile file) { this.file = file; }
    public void setCategory(Category category) { this.category = category; }
    public void setLikes(int likes) { this.likes = likes; }
    public void setViews(int views) { this.views = views; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setReadTime(int readTime) { this.readTime = readTime; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Get image URL helper method
    public String getImageUrl() {
        return file != null ? file.getFileUrl() : null;
    }
}
