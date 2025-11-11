package com.example.notby.data.remote;

import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.Article;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ArticleApi {
    @POST("article")
    Call<ApiResponse<Article>> createArticle(@Body Article article);

    @GET("article")
    Call<ApiResponse<List<Article>>> getAllArticles();

    @GET("article/id/{id}")
    Call<ApiResponse<Article>> getArticleById(@Path("id") String id);

    @GET("article/title/{title}")
    Call<ApiResponse<List<Article>>> getArticlesByTitle(@Path("title") String title);

    @PATCH("article/{id}")
    Call<ApiResponse<Article>> updateArticle(@Path("id") String id, @Body Article article);

    @DELETE("article/{id}")
    Call<ApiResponse<Article>> deleteArticle(@Path("id") String id);
}
