package com.example.notby.data.remote;

import com.example.notby.data.model.User;
import com.example.notby.data.model.ApiResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface UserApi {
    @GET("user")
    Call<ApiResponse<List<User>>> getAll();

    @GET("user/id/{id}")
    Call<ApiResponse<User>> getById(@Path("id") String id);

    @GET("user/email/{email}")
    Call<ApiResponse<User>> getByEmail(@Path("email") String email);

    @GET("user/google-id/{googleId}")
    Call<ApiResponse<User>> getByGoogleId(@Path("googleId") String googleId);

    @GET("user/role/{role}")
    Call<ApiResponse<List<User>>> getByRole(@Path("role") String role);

    @POST("user")
    Call<ApiResponse<User>> create(@Body User user);

    @PATCH("user/{id}")
    Call<ApiResponse<User>> update(@Path("id") String id, @Body User user);

    @DELETE("user/{id}")
    Call<ApiResponse<Void>> delete(@Path("id") String id);
}
