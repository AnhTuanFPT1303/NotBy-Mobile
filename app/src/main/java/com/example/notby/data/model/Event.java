package com.example.notby.data.model;

import com.google.gson.annotations.SerializedName;

public class Event {
    @SerializedName("_id")
    private String id;

    @SerializedName("childId")
    private Object childId; // Can be a String (when creating) or a Baby/Map object (when fetching)

    @SerializedName("title")
    private String title;

    @SerializedName("startAt")
    private String startAt;

    @SerializedName("endAt")
    private String endAt;

    @SerializedName("eventType")
    private String eventType;

    @SerializedName("notes")
    private String notes;

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getStartAt() { return startAt; }
    public String getEndAt() { return endAt; }
    public String getEventType() { return eventType; }
    public String getNotes() { return notes; }

    // Smart Getters for childId
    public Baby getChildAsBaby() {
        if (childId instanceof Baby) {
            return (Baby) childId;
        }
        return null;
    }

    public String getChildIdString() {
        if (childId == null) return null;
        if (childId instanceof String) {
            return (String) childId;
        } 
        if (childId instanceof Baby) {
            return ((Baby) childId).getId();
        }
        // Handle the case where Gson deserializes the object into a Map
        if (childId instanceof com.google.gson.internal.LinkedTreeMap) {
            try {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) childId;
                if (map.containsKey("_id")) {
                    return (String) map.get("_id");
                }
            } catch (Exception e) {
                // Could fail if the structure is not as expected
                return null;
            }
        }
        return null;
    }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setStartAt(String startAt) { this.startAt = startAt; }
    public void setEndAt(String endAt) { this.endAt = endAt; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setChildId(Object childId) { this.childId = childId; }
}
