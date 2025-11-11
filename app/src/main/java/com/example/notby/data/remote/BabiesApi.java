package com.example.notby.data.remote;

import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.Baby;
import com.example.notby.data.model.BabiesResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BabiesApi {
    @GET("babies")
    Call<ApiResponse<BabiesResponse>> findAll(@Query("parentId") String parentId);

    @GET("babies")
    Call<Object> findAllRaw(@Query("parentId") String parentId);

    @GET("babies/{id}")
    Call<ApiResponse<Baby>> getById(@Path("id") String id);

    @POST("babies")
    Call<ApiResponse<Baby>> createBaby(@Body Baby baby);
    @DELETE("babies/{id}")
    Call<ApiResponse<Void>> deleteBaby(@Path("id") String id);
}
