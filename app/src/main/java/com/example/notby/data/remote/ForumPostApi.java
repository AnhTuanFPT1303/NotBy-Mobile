package com.example.notby.data.remote;

import com.example.notby.data.model.ForumPost;
import com.example.notby.data.model.ApiResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ForumPostApi {
    @GET("Forumpost")
    Call<ApiResponse<List<ForumPost>>> getAll();

    @GET("Forumpost/{id}")
    Call<ApiResponse<ForumPost>> getById(@Path("id") String id);

    @GET("Forumpost/title/{title}")
    Call<ApiResponse<List<ForumPost>>> getByTitle(@Path("title") String title);

    @GET("Forumpost/author/{author}")
    Call<ApiResponse<List<ForumPost>>> getByAuthor(@Path("author") String author);

    @POST("Forumpost")
    Call<ApiResponse<ForumPost>> create(@Body ForumPost forumPost);

    @PATCH("Forumpost/{id}")
    Call<ApiResponse<ForumPost>> update(@Path("id") String id, @Body ForumPost forumPost);

    @DELETE("Forumpost/{id}")
    Call<ApiResponse<Void>> delete(@Path("id") String id);
}