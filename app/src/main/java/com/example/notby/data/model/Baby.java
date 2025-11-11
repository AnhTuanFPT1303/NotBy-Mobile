package com.example.notby.data.model;

import com.google.gson.annotations.SerializedName;

public class Baby {
    @SerializedName("_id")
    private String id;

    @SerializedName("id")
    private String apiId; // Secondary id field from API response

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("dob")
    private String dob;

    @SerializedName("gender")
    private String gender;

    @SerializedName("parentId")
    private Object parentId; // Can be either String or User object

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("deletedAt")
    private String deletedAt;

    @SerializedName("deletedBy")
    private String deletedBy;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("updateBy")
    private String updateBy;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("__v")
    private Integer version;

    // Getters
    public String getId() {
        // Return _id first, fallback to id field if _id is null
        return id != null ? id : apiId;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDob() { return dob; }
    public String getGender() { return gender; }

    // Smart getter for parentId - returns the ID whether it's a string or User object
    public String getParentId() {
        if (parentId instanceof String) {
            return (String) parentId;
        } else if (parentId instanceof User) {
            return ((User) parentId).getId();
        }
        return null;
    }

    // Getter for full parent User object (if available)
    public User getParentUser() {
        if (parentId instanceof User) {
            return (User) parentId;
        }
        return null;
    }

    public Boolean getIsActive() { return isActive; }
    public String getDeletedAt() { return deletedAt; }
    public String getDeletedBy() { return deletedBy; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdateBy() { return updateBy; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public Integer getVersion() { return version; }

    // Setters (if needed)
    public void setId(String id) { this.id = id; }
    public void setApiId(String apiId) { this.apiId = apiId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setDob(String dob) { this.dob = dob; }
    public void setGender(String gender) { this.gender = gender; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public void setParentUser(User parentUser) { this.parentId = parentUser; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setDeletedAt(String deletedAt) { this.deletedAt = deletedAt; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setVersion(Integer version) { this.version = version; }
}
