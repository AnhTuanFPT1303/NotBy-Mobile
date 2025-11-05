package com.example.notby.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://notby-be-8q9y.onrender.com/";
    private static Retrofit retrofit = null;
    private static ForumPostApi forumPostApi = null;
    private static UserApi userApi = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ForumPostApi getForumPostApi() {
        if (forumPostApi == null) {
            forumPostApi = getClient().create(ForumPostApi.class);
        }
        return forumPostApi;
    }

    public static UserApi getUserApi() {
        if (userApi == null) {
            userApi = getClient().create(UserApi.class);
        }
        return userApi;
    }
}