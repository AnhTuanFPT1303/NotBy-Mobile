package com.example.notby.data.model;

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
    private String author;

    @SerializedName("File")
    private String file;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructors
    public ForumPost() {}

    public ForumPost(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.likes = 0;
        this.views = 0;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getLikes() { return likes; }
    public int getViews() { return views; }
    public String getAuthor() { return author; }
    public String getFile() { return file; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setLikes(int likes) { this.likes = likes; }
    public void setViews(int views) { this.views = views; }
    public void setAuthor(String author) { this.author = author; }
    public void setFile(String file) { this.file = file; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
