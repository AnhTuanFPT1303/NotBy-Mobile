package com.example.notby.data.model;

import com.google.gson.annotations.SerializedName;

public class Baby {
    @SerializedName("_id")
    private String id;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("dob")
    private String dob;

    @SerializedName("gender")
    private String gender;

    @SerializedName("parentId")
    private String parentId;

    // Getters
    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDob() { return dob; }
    public String getGender() { return gender; }
    public String getParentId() { return parentId; }

    // Setters (if needed)
    public void setId(String id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setDob(String dob) { this.dob = dob; }
    public void setGender(String gender) { this.gender = gender; }
    public void setParentId(String parentId) { this.parentId = parentId; }
}
