package com.example.notby.data.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class ForumPost {
    @SerializedName("_id")
    private String id;

    @SerializedName("Title")
    private String title;

    @SerializedName("Content")
    private String content;

    @SerializedName("Likes")
    private int likes;

    @SerializedName("Views")
    private int views;

    @SerializedName("Author")
    private JsonElement author; // can be a String id or an object

    @SerializedName("File")
    private MediaFile file; // can be a String id or a MediaFile object

    @SerializedName("FileId")
    private String fileId; // Used when creating posts - send only the ID

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructors
    public ForumPost() {}

    // When creating a post we usually have author id (String)
    public ForumPost(String title, String content, String authorId) {
        this.title = title;
        this.content = content;
        this.likes = 0;
        this.views = 0;
        if (authorId != null && !authorId.isEmpty()) {
            this.author = new JsonPrimitive(authorId);
        } else {
            this.author = null;
        }
    }

    public MediaFile getFile() {
        return file;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getLikes() { return likes; }
    public int getViews() { return views; }

    // Returns author id whether the JSON contained a primitive id or an object
    public String getAuthor() {
        return getAuthorId();
    }

    // Helper to get fileId
    public String getFileId() {
        if (fileId != null) return fileId; // Return explicit fileId if set
        if (file == null) return null;
        return file.getId(); // Fallback to file's ID
    }

    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Helper to extract author id
    public String getAuthorId() {
        if (author == null || author.isJsonNull()) return null;
        if (author.isJsonPrimitive()) {
            try {
                return author.getAsString();
            } catch (Exception e) {
                return null;
            }
        }
        if (author.isJsonObject()) {
            JsonObject obj = author.getAsJsonObject();
            if (obj.has("_id") && !obj.get("_id").isJsonNull()) return obj.get("_id").getAsString();
            if (obj.has("id") && !obj.get("id").isJsonNull()) return obj.get("id").getAsString();
        }
        return null;
    }

    // Return User object if Author was an object; otherwise null
    public User getAuthorUser() {
        if (author == null || author.isJsonNull() || !author.isJsonObject()) return null;
        try {
            return new Gson().fromJson(author, User.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setLikes(int likes) { this.likes = likes; }
    public void setViews(int views) { this.views = views; }

    // Accept a String id when setting author programmatically
    public void setAuthor(String authorId) {
        if (authorId == null) this.author = null;
        else this.author = new JsonPrimitive(authorId);
    }

    // Accept a JsonElement (used by Gson during deserialization)
    public void setAuthorElement(JsonElement authorElement) { this.author = authorElement; }

    // Setter for file
    public void setFile(MediaFile file) { this.file = file; }

    // Setter for fileId (used when creating posts)
    public void setFileId(String fileId) { this.fileId = fileId; }

    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
