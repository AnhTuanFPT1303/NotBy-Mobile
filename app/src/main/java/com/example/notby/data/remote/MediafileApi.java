package com.example.notby.data.remote;

import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.MediaFile;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MediafileApi {

    @GET("MediaFiles")
    Call<ApiResponse<List<MediaFile>>> getAll();

    @GET("MediaFiles/{id}")
    Call<ApiResponse<MediaFile>> getById(@Path("id") String id);

    @POST("MediaFiles")
    Call<ApiResponse<MediaFile>> create(@Body MediaFile mediaFile);

    @PATCH("MediaFiles/{id}")
    Call<ApiResponse<MediaFile>> update(@Path("id") String id, @Body MediaFile mediaFile);

    @DELETE("MediaFiles/{id}")
    Call<ApiResponse<Void>> delete(@Path("id") String id);

    @GET("MediaFiles/fileType/{fileType}")
    Call<ApiResponse<List<MediaFile>>> findByFileType(@Path("fileType") String fileType);

    @GET("MediaFiles/fileName/{fileName}")
    Call<ApiResponse<List<MediaFile>>> findByFileName(@Path("fileName") String fileName);
}
