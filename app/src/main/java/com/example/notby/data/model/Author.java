package com.example.notby.data.model;

public class Author {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String photo;
    private String role;

    public Author(String id, String firstName, String lastName, String email, String photo, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.photo = photo;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoto() {
        return photo;
    }

    public String getRole() {
        return role;
    }
}
