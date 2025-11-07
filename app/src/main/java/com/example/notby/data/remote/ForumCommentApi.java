package com.example.notby.data.remote;

import com.example.notby.data.model.Comment;
import com.example.notby.data.model.ApiResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ForumCommentApi {
    @GET("ForumComment")
    Call<ApiResponse<List<Comment>>> findAll();

    @GET("ForumComment/{id}")
    Call<ApiResponse<Comment>> findById(@Path("id") String id);

    @GET("ForumComment/author/{authorId}")
    Call<ApiResponse<List<Comment>>> findByAuthor(@Path("authorId") String authorId);

    @GET("ForumComment/content/{content}")
    Call<ApiResponse<List<Comment>>> findByContent(@Path("content") String content);

    @GET("ForumComment/post/{postId}")
    Call<ApiResponse<List<Comment>>> findByPost(@Path("postId") String postId);

    @POST("ForumComment")
    Call<ApiResponse<Comment>> create(@Body Comment comment);

    @PATCH("ForumComment/{id}")
    Call<ApiResponse<Comment>> update(@Path("id") String id, @Body Comment comment);

    @DELETE("ForumComment/{id}")
    Call<ApiResponse<Void>> delete(@Path("id") String id);
}

