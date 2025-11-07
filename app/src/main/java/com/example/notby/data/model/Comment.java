package com.example.notby.data.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Comment {
    @SerializedName("_id")
    private String id;

    @SerializedName("Content")
    private String content;

    @SerializedName("Likes")
    private int likes;

    @SerializedName("CreatedBy")
    private JsonElement createdBy; // can be a String id or an object

    @SerializedName("Post")
    private String post;

    @SerializedName("File")
    private String file;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructors
    public Comment() {}

    public Comment(String content, String createdById, String postId) {
        this.content = content;
        this.post = postId;
        this.likes = 0;
        if (createdById != null && !createdById.isEmpty()) {
            this.createdBy = new JsonPrimitive(createdById);
        } else {
            this.createdBy = null;
        }
    }

    // Getters
    public String getId() { return id; }
    public String getContent() { return content; }
    public int getLikes() { return likes; }
    public String getPost() { return post; }
    public String getFile() { return file; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Helper to extract author id
    public String getCreatedById() {
        if (createdBy == null || createdBy.isJsonNull()) return null;
        if (createdBy.isJsonPrimitive()) {
            try {
                return createdBy.getAsString();
            } catch (Exception e) {
                return null;
            }
        }
        if (createdBy.isJsonObject()) {
            JsonObject obj = createdBy.getAsJsonObject();
            if (obj.has("_id") && !obj.get("_id").isJsonNull()) return obj.get("_id").getAsString();
            if (obj.has("id") && !obj.get("id").isJsonNull()) return obj.get("id").getAsString();
        }
        return null;
    }

    // Return User object if CreatedBy was an object; otherwise null
    public User getCreatedByUser() {
        if (createdBy == null || createdBy.isJsonNull() || !createdBy.isJsonObject()) return null;
        try {
            return new Gson().fromJson(createdBy, User.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setContent(String content) { this.content = content; }
    public void setLikes(int likes) { this.likes = likes; }
    public void setPost(String post) { this.post = post; }
    public void setFile(String file) { this.file = file; }

    public void setCreatedBy(String createdById) {
        if (createdById == null) this.createdBy = null;
        else this.createdBy = new JsonPrimitive(createdById);
    }

    public void setCreatedByElement(JsonElement createdByElement) { this.createdBy = createdByElement; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}

