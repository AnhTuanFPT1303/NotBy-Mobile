package com.example.notby.data.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class MediaFile {
    @SerializedName("_id")
    private String id;

    @SerializedName("fileName")
    private String fileName;

    @SerializedName("fileType")
    private String fileType;

    @SerializedName("Author")
    private JsonElement author; // can be id string or object

    @SerializedName("fileUrl")
    private String fileUrl;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public MediaFile() {}

    public String getId() { return id; }
    public String getFileName() { return fileName; }
    public String getFileType() { return fileType; }
    // Return author id (maintains previous signature)
    public String getAuthor() { return getAuthorId(); }
    public String getFileUrl() { return fileUrl; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Helper to extract author id whether it's a primitive or object
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

    public User getAuthorUser() {
        if (author == null || author.isJsonNull() || !author.isJsonObject()) return null;
        try { return new Gson().fromJson(author, User.class); } catch (Exception e) { return null; }
    }

    public void setId(String id) { this.id = id; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    // allow setting author by id when creating media
    public void setAuthor(String authorId) { if (authorId == null) this.author = null; else this.author = new JsonPrimitive(authorId); }
    // allow Gson to set the raw element
    public void setAuthorElement(JsonElement authorElement) { this.author = authorElement; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
