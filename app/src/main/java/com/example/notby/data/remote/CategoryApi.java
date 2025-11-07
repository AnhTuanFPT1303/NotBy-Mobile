package com.example.notby.data.remote;

import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CategoryApi {
    @POST("category")
    Call<ApiResponse<Category>> createCategory(@Body Category category);

    @GET("category")
    Call<ApiResponse<List<Category>>> getAllCategories();

    @GET("category/id/{id}")
    Call<ApiResponse<Category>> getCategoryById(@Path("id") String id);

    @GET("category/title/{title}")
    Call<ApiResponse<Category>> getCategoryByTitle(@Path("title") String title);

    @PATCH("category/{id}")
    Call<ApiResponse<Category>> updateCategory(@Path("id") String id, @Body Category category);

    @DELETE("category/{id}")
    Call<ApiResponse<Category>> deleteCategory(@Path("id") String id);
}
