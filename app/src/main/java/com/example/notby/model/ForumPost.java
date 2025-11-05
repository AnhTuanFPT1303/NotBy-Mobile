package com.example.notby.model;

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
    private Author author;
    @SerializedName("created_at")
    private String createdAt;

    public static class Author {
        @SerializedName("_id")
        private String id;
        @SerializedName("firstName")
        private String firstName;
        @SerializedName("lastName")
        private String lastName;
        @SerializedName("photo")
        private String photo;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPhoto() { return photo; }
        public void setPhoto(String photo) { this.photo = photo; }
        public String getFullName() { return firstName + " " + lastName; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }
    public Author getAuthor() { return author; }
    public void setAuthor(Author author) { this.author = author; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
