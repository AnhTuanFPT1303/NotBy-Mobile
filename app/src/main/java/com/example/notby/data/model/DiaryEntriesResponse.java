package com.example.notby.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class DiaryEntriesResponse {

    @SerializedName("data")
    private List<DiaryEntry> diaryEntries = new ArrayList<>();

    public List<DiaryEntry> getDiaryEntries() {
        return diaryEntries;
    }
}
