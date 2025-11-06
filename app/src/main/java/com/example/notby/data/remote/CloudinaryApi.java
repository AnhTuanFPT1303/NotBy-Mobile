package com.example.notby.data.remote;

import com.example.notby.data.model.ApiResponse;
import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface CloudinaryApi {
    @Multipart
    @POST("cloudinary/upload")
    Call<ApiResponse<Object>> uploadFile(@Part MultipartBody.Part file);

    @GET("cloudinary/images")
    Call<ApiResponse<List<Object>>> getAllImages();

    @GET("cloudinary/videos")
    Call<ApiResponse<List<Object>>> getAllVideos();

    @GET("cloudinary/files")
    Call<ApiResponse<List<Object>>> getAllFiles();
}
