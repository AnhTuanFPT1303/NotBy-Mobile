package com.example.notby.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class EventsResponse {
    // This annotation now correctly points to the nested "data" array in the JSON response
    @SerializedName("data")
    private List<Event> events = new ArrayList<>();

    public List<Event> getEvents() {
        return events;
    }
}
