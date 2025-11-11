package com.example.notby.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.notby.data.remote.BabiesApi;
import android.content.Context;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import com.example.notby.data.TokenManager;

public class ApiClient {
    private static final String BASE_URL = "https://notby-be-8q9y.onrender.com/";
    private static Retrofit retrofit = null;
    private static ForumPostApi forumPostApi = null;
    private static ForumCommentApi forumCommentApi = null;
    private static UserApi userApi = null;
    private static MediafileApi mediafileApi = null;
    private static CloudinaryApi cloudinaryApi = null;
    private static BabiesApi babiesApi = null;
    private static DiaryEntriesApi diaryEntriesApi = null;


    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Build a Retrofit client that attaches Authorization header from TokenManager (if token exists)
    public static Retrofit getClientWithAuth(Context context) {
        TokenManager tokenManager = new TokenManager(context.getApplicationContext());
        final String token = tokenManager.getToken();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder();
                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                            // Log a masked token and URL for debugging
                            String mask = token.length() > 8 ? token.substring(token.length()-8) : token;
                            android.util.Log.d("ApiClient", "Request: " + original.url() + " Authorization: ****" + mask);
                        }
                        Request request = builder.build();
                        return chain.proceed(request);
                    }
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // Convenience: get BabiesApi with auth (useful when endpoints require JWT)
    public static BabiesApi getBabiesApi(Context context) {
        return getClientWithAuth(context).create(BabiesApi.class);
    }
    public static BabiesApi getBabiesApi() {
        if (babiesApi == null) {
            babiesApi = getClient().create(BabiesApi.class);
        }
        return babiesApi;
    }

    public static DiaryEntriesApi getDiaryEntriesApi(Context context) {
        if (diaryEntriesApi == null) {
            diaryEntriesApi = getClientWithAuth(context).create(DiaryEntriesApi.class);
        }
        return diaryEntriesApi;
    }

    public static ForumPostApi getForumPostApi() {
        if (forumPostApi == null) {
            forumPostApi = getClient().create(ForumPostApi.class);
        }
        return forumPostApi;
    }
    public static ForumCommentApi getForumCommentApi() {
        if (forumCommentApi == null) {
            forumCommentApi = getClient().create(ForumCommentApi.class);
        }
        return forumCommentApi;
    }


    public static UserApi getUserApi() {
        if (userApi == null) {
            userApi = getClient().create(UserApi.class);
        }
        return userApi;
    }

    public static MediafileApi getMediafileApi() {
        if (mediafileApi == null) {
            mediafileApi = getClient().create(MediafileApi.class);
        }
        return mediafileApi;
    }

    public static CloudinaryApi getCloudinaryApi() {
        if (cloudinaryApi == null) {
            cloudinaryApi = getClient().create(CloudinaryApi.class);
        }
        return cloudinaryApi;
    }
}