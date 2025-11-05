package com.example.notby.data.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("_id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("gender")
    private String gender;

    @SerializedName("photo")
    private String photo;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("role")
    private String role;

    @SerializedName("googleId")
    private String googleId;

    @SerializedName("dob")
    private String dob;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructors
    public User() {}

    public User(String email, String firstName, String lastName, String role) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Getters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getGender() { return gender; }
    public String getPhoto() { return photo; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRole() { return role; }
    public String getGoogleId() { return googleId; }
    public String getDob() { return dob; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setGender(String gender) { this.gender = gender; }
    public void setPhoto(String photo) { this.photo = photo; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setRole(String role) { this.role = role; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
    public void setDob(String dob) { this.dob = dob; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
