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

    @SerializedName("ForumPostId")
    private String forumPostId;

    @SerializedName("FileId")
    private String fileId;

    @SerializedName("CreatedBy")
    private JsonElement createdBy;

    @SerializedName("Likes")
    private int likes;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("Post")
    private JsonElement post;

    @SerializedName("File")
    private JsonElement file;

    // Constructors
    public Comment() {}

    public Comment(String content, String forumPostId, String fileId, String createdBy, int likes) {
        this.content = content;
        this.forumPostId = forumPostId;
        this.fileId = fileId;
        this.createdBy = new JsonPrimitive(createdBy);
        this.likes = likes;
    }

    // Getters
    public String getId() { return id; }
    public String getContent() { return content; }
    public int getLikes() { return likes; }
    public String getForumPostId() { return forumPostId; }
    public String getFileId() { return fileId; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public JsonElement getCreatedBy() { return createdBy; }
    public JsonElement getPost() { return post; }
    public JsonElement getFile() { return file; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setContent(String content) { this.content = content; }
    public void setLikes(int likes) { this.likes = likes; }
    public void setForumPostId(String forumPostId) { this.forumPostId = forumPostId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public void setCreatedBy(JsonElement createdBy) { this.createdBy = createdBy; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedById() {
        if (createdBy == null || createdBy.isJsonNull()) return null;
        if (createdBy.isJsonPrimitive()) {
            try { return createdBy.getAsString(); } catch (Exception e) { return null; }
        }
        if (createdBy.isJsonObject()) {
            JsonObject obj = createdBy.getAsJsonObject();
            if (obj.has("_id") && !obj.get("_id").isJsonNull()) return obj.get("_id").getAsString();
            if (obj.has("id") && !obj.get("id").isJsonNull()) return obj.get("id").getAsString();
        }
        return null;
    }

    public User getCreatedByUser() {
        if (createdBy == null || createdBy.isJsonNull() || !createdBy.isJsonObject()) return null;
        try { return new Gson().fromJson(createdBy, User.class); } catch (Exception e) { return null; }
    }
}