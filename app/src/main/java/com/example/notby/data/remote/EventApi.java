package com.example.notby.data.remote;

import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.Event;
import com.example.notby.data.model.EventsResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface EventApi {

    @GET("events")
    Call<ApiResponse<EventsResponse>> findAll(@Query("childId") String childId);

    @GET("events/{id}")
    Call<ApiResponse<Event>> findOne(@Path("id") String id);

    @POST("events")
    Call<ApiResponse<Event>> create(@Body Event event);

    @PATCH("events/{id}")
    Call<ApiResponse<Event>> update(@Path("id") String id, @Body Event event);

    @DELETE("events/{id}")
    Call<ApiResponse<Void>> delete(@Path("id") String id);
}
