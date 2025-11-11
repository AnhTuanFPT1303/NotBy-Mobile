package com.example.notby.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BabiesResponse {
    @SerializedName("total")
    private int total;

    @SerializedName("page")
    private int page;

    @SerializedName("limit")
    private int limit;

    @SerializedName("babies")
    private List<Baby> babies;

    // Getters
    public int getTotal() { return total; }
    public int getPage() { return page; }
    public int getLimit() { return limit; }
    public List<Baby> getBabies() { return babies; }

    // Setters
    public void setTotal(int total) { this.total = total; }
    public void setPage(int page) { this.page = page; }
    public void setLimit(int limit) { this.limit = limit; }
    public void setBabies(List<Baby> babies) { this.babies = babies; }
}
