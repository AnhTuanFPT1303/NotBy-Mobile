package com.example.notby.data.model;

public class ForumPost {
    private String id;
    private String title;
    private String content;
    private int likes;
    private int views;
    private Author author;
    private String file;
    private String createdAt;
    private String updatedAt;

    public ForumPost(String id, String title, String content, int likes, int views, Author author, String file, String createdAt, String updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.likes = likes;
        this.views = views;
        this.author = author;
        this.file = file;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getLikes() {
        return likes;
    }

    public int getViews() {
        return views;
    }

    public Author getAuthor() {
        return author;
    }

    public String getFile() {
        return file;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
